package com.tracelens.evidence.extraction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.tracelens.evidence.config.EvidenceExtractionProperties;
import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.exception.EvidenceTextExtractionException;

@Component
public class CsvEvidenceTextExtractor
        implements EvidenceTextExtractor {

    private final EvidenceExtractionProperties properties;

    public CsvEvidenceTextExtractor(
            EvidenceExtractionProperties properties
    ) {
        this.properties = properties;
    }

    @Override
    public EvidenceFileType supportedFileType() {
        return EvidenceFileType.CSV;
    }

    @Override
    public String extract(
            Resource resource
    ) {

        String csvText =
                TextExtractionSupport
                        .normalizeLineEndings(
                                TextExtractionSupport
                                        .readStrictUtf8(
                                                resource
                                        )
                        );

        TextExtractionSupport.requireNonBlank(
                csvText,
                "The CSV evidence is empty"
        );

        List<List<String>> rows =
                parseCsv(csvText);

        if (rows.isEmpty()) {
            throw new EvidenceTextExtractionException(
                    "The CSV evidence does not contain "
                    + "any records"
            );
        }

        List<String> headers =
                normalizeAndValidateHeaders(
                        rows.get(0)
                );

        int dataRowCount =
                rows.size() - 1;

        if (
                dataRowCount
                > properties.getMaxCsvRows()
        ) {
            throw new EvidenceTextExtractionException(
                    "CSV evidence exceeds the configured "
                    + "limit of "
                    + properties.getMaxCsvRows()
                    + " data rows"
            );
        }

        StringBuilder output =
                new StringBuilder();

        append(
                output,
                "CSV Headers:\n",
                properties.getMaxCharacters()
        );

        for (String header : headers) {
            append(
                    output,
                    "- " + header + "\n",
                    properties.getMaxCharacters()
            );
        }

        if (dataRowCount == 0) {
            append(
                    output,
                    "\nThe CSV contains headers but "
                    + "no data rows.",
                    properties.getMaxCharacters()
            );

            return output.toString().strip();
        }

        for (
                int rowIndex = 1;
                rowIndex < rows.size();
                rowIndex++
        ) {

            List<String> row =
                    rows.get(rowIndex);

            if (row.size() != headers.size()) {
                throw new EvidenceTextExtractionException(
                        "CSV row "
                        + rowIndex
                        + " contains "
                        + row.size()
                        + " columns, but the header "
                        + "contains "
                        + headers.size()
                        + " columns"
                );
            }

            append(
                    output,
                    "\nRow "
                    + rowIndex
                    + ":\n",
                    properties.getMaxCharacters()
            );

            for (
                    int columnIndex = 0;
                    columnIndex < headers.size();
                    columnIndex++
            ) {

                String value =
                        TextExtractionSupport
                                .normalizeInlineValue(
                                        row.get(
                                                columnIndex
                                        )
                                );

                append(
                        output,
                        headers.get(columnIndex)
                        + ": "
                        + value
                        + "\n",
                        properties.getMaxCharacters()
                );
            }
        }

        return output.toString().strip();
    }

    private List<List<String>> parseCsv(
            String csvText
    ) {

        List<List<String>> rows =
                new ArrayList<>();

        List<String> currentRow =
                new ArrayList<>();

        StringBuilder currentField =
                new StringBuilder();

        boolean insideQuotedField = false;
        boolean quotedFieldClosed = false;

        for (
                int index = 0;
                index < csvText.length();
                index++
        ) {

            char character =
                    csvText.charAt(index);

            if (insideQuotedField) {

                if (character == '"') {

                    boolean escapedQuote =
                            index + 1
                            < csvText.length()
                            && csvText.charAt(
                                    index + 1
                            ) == '"';

                    if (escapedQuote) {
                        currentField.append('"');
                        index++;
                    }
                    else {
                        insideQuotedField = false;
                        quotedFieldClosed = true;
                    }
                }
                else {
                    currentField.append(character);
                }

                continue;
            }

            if (quotedFieldClosed) {

                if (character == ',') {
                    addField(
                            currentRow,
                            currentField
                    );

                    quotedFieldClosed = false;
                }
                else if (character == '\n') {
                    addField(
                            currentRow,
                            currentField
                    );

                    addRowIfNotBlank(
                            rows,
                            currentRow
                    );

                    currentRow =
                            new ArrayList<>();

                    quotedFieldClosed = false;

                    enforceParsedRowLimit(rows);
                }
                else if (
                        character == ' '
                        || character == '\t'
                ) {
                    // Allow whitespace after closing quote.
                }
                else {
                    throw new EvidenceTextExtractionException(
                            "CSV contains unexpected content "
                            + "after a closing quotation mark"
                    );
                }

                continue;
            }

            if (character == '"') {

                if (currentField.length() != 0) {
                    throw new EvidenceTextExtractionException(
                            "CSV quotation marks must begin "
                            + "at the start of a field"
                    );
                }

                insideQuotedField = true;
            }
            else if (character == ',') {
                addField(
                        currentRow,
                        currentField
                );
            }
            else if (character == '\n') {
                addField(
                        currentRow,
                        currentField
                );

                addRowIfNotBlank(
                        rows,
                        currentRow
                );

                currentRow =
                        new ArrayList<>();

                enforceParsedRowLimit(rows);
            }
            else {
                currentField.append(character);
            }
        }

        if (insideQuotedField) {
            throw new EvidenceTextExtractionException(
                    "CSV contains an unclosed quoted field"
            );
        }

        if (
                quotedFieldClosed
                || currentField.length() > 0
                || !currentRow.isEmpty()
        ) {
            addField(
                    currentRow,
                    currentField
            );

            addRowIfNotBlank(
                    rows,
                    currentRow
            );
        }

        enforceParsedRowLimit(rows);

        return rows;
    }

    private void addField(
            List<String> row,
            StringBuilder field
    ) {

        row.add(field.toString().strip());
        field.setLength(0);
    }

    private void addRowIfNotBlank(
            List<List<String>> rows,
            List<String> row
    ) {

        boolean containsContent =
                row.stream()
                        .anyMatch(
                                value ->
                                        value != null
                                        && !value.isBlank()
                        );

        if (containsContent) {
            rows.add(
                    List.copyOf(row)
            );
        }
    }

    private void enforceParsedRowLimit(
            List<List<String>> rows
    ) {

        int maximumRowsIncludingHeader =
                properties.getMaxCsvRows() + 1;

        if (
                rows.size()
                > maximumRowsIncludingHeader
        ) {
            throw new EvidenceTextExtractionException(
                    "CSV evidence exceeds the configured "
                    + "limit of "
                    + properties.getMaxCsvRows()
                    + " data rows"
            );
        }
    }

    private List<String> normalizeAndValidateHeaders(
            List<String> rawHeaders
    ) {

        if (
                rawHeaders == null
                || rawHeaders.isEmpty()
        ) {
            throw new EvidenceTextExtractionException(
                    "CSV evidence does not contain "
                    + "a header row"
            );
        }

        List<String> normalizedHeaders =
                new ArrayList<>();

        Set<String> uniqueHeaders =
                new HashSet<>();

        for (
                int index = 0;
                index < rawHeaders.size();
                index++
        ) {

            String header =
                    rawHeaders.get(index) == null
                            ? ""
                            : rawHeaders
                                    .get(index)
                                    .strip();

            if (header.isBlank()) {
                throw new EvidenceTextExtractionException(
                        "CSV header column "
                        + (index + 1)
                        + " is blank"
                );
            }

            String normalizedKey =
                    header.toLowerCase(
                            Locale.ROOT
                    );

            if (!uniqueHeaders.add(normalizedKey)) {
                throw new EvidenceTextExtractionException(
                        "CSV contains a duplicate header: "
                        + header
                );
            }

            normalizedHeaders.add(header);
        }

        return List.copyOf(normalizedHeaders);
    }

    private void append(
            StringBuilder output,
            String value,
            int maximumCharacters
    ) {

        TextExtractionSupport.appendWithLimit(
                output,
                value,
                maximumCharacters
        );
    }
}