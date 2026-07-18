package com.tracelens.evidence.extraction;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.tracelens.evidence.config.EvidenceExtractionProperties;
import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.exception.EvidenceTextExtractionException;

@Component
public class PdfEvidenceTextExtractor
        implements EvidenceTextExtractor {

    private final EvidenceExtractionProperties properties;

    public PdfEvidenceTextExtractor(
            EvidenceExtractionProperties properties
    ) {
        this.properties = properties;
    }

    @Override
    public EvidenceFileType supportedFileType() {
        return EvidenceFileType.PDF;
    }

    @Override
    public String extract(
            Resource resource
    ) {

        byte[] pdfBytes = readPdfBytes(resource);

        try (
                PDDocument document =
                        Loader.loadPDF(pdfBytes)
        ) {

            validateDocument(document);

            String extractedText =
                    extractPages(document);

            TextExtractionSupport.requireNonBlank(
                    extractedText,
                    "The PDF does not contain extractable "
                    + "text. It may be an image-only or "
                    + "scanned PDF."
            );

            return TextExtractionSupport
                    .enforceCharacterLimit(
                            extractedText,
                            properties.getMaxCharacters()
                    );
        }
        catch (InvalidPasswordException exception) {
            throw new EvidenceTextExtractionException(
                    "The PDF is password-protected and "
                    + "cannot be processed",
                    exception
            );
        }
        catch (EvidenceTextExtractionException exception) {
            throw exception;
        }
        catch (IOException exception) {
            throw new EvidenceTextExtractionException(
                    "The PDF evidence is invalid, corrupted "
                    + "or could not be read",
                    exception
            );
        }
        catch (RuntimeException exception) {
            throw new EvidenceTextExtractionException(
                    "The PDF evidence could not be processed",
                    exception
            );
        }
    }

    private byte[] readPdfBytes(
            Resource resource
    ) {

        if (resource == null) {
            throw new EvidenceTextExtractionException(
                    "PDF evidence resource is unavailable"
            );
        }

        try (
                InputStream inputStream =
                        resource.getInputStream()
        ) {

            byte[] bytes = inputStream.readAllBytes();

            if (bytes.length == 0) {
                throw new EvidenceTextExtractionException(
                        "The PDF evidence is empty"
                );
            }

            return bytes;
        }
        catch (EvidenceTextExtractionException exception) {
            throw exception;
        }
        catch (IOException exception) {
            throw new EvidenceTextExtractionException(
                    "The PDF evidence could not be read",
                    exception
            );
        }
    }

    private void validateDocument(
            PDDocument document
    ) {

        int pageCount = document.getNumberOfPages();

        if (pageCount <= 0) {
            throw new EvidenceTextExtractionException(
                    "The PDF evidence does not contain "
                    + "any pages"
            );
        }

        if (pageCount > properties.getMaxPdfPages()) {
            throw new EvidenceTextExtractionException(
                    "PDF evidence exceeds the configured "
                    + "limit of "
                    + properties.getMaxPdfPages()
                    + " pages"
            );
        }

        if (
                document.isEncrypted()
                && !document
                        .getCurrentAccessPermission()
                        .canExtractContent()
        ) {
            throw new EvidenceTextExtractionException(
                    "Text extraction is not permitted "
                    + "for this encrypted PDF"
            );
        }
    }

    private String extractPages(
            PDDocument document
    ) throws IOException {

        PDFTextStripper textStripper =
                new PDFTextStripper();

        textStripper.setSortByPosition(true);

        StringBuilder output =
                new StringBuilder();

        int pageCount = document.getNumberOfPages();

        for (
                int pageNumber = 1;
                pageNumber <= pageCount;
                pageNumber++
        ) {

            textStripper.setStartPage(pageNumber);
            textStripper.setEndPage(pageNumber);

            String pageText =
                    TextExtractionSupport
                            .normalizeLineEndings(
                                    textStripper.getText(
                                            document
                                    )
                            )
                            .strip();

            if (pageText.isBlank()) {
                continue;
            }

            appendPage(
                    output,
                    pageNumber,
                    pageText
            );
        }

        return output.toString().strip();
    }

    private void appendPage(
            StringBuilder output,
            int pageNumber,
            String pageText
    ) {

        int maximumCharacters =
                properties.getMaxCharacters();

        if (!output.isEmpty()) {
            TextExtractionSupport.appendWithLimit(
                    output,
                    "\n\n",
                    maximumCharacters
            );
        }

        TextExtractionSupport.appendWithLimit(
                output,
                "--- Page "
                + pageNumber
                + " ---\n\n",
                maximumCharacters
        );

        TextExtractionSupport.appendWithLimit(
                output,
                pageText,
                maximumCharacters
        );
    }
}