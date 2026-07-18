package com.tracelens.evidence.extraction;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.tracelens.evidence.config.EvidenceExtractionProperties;
import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.exception.EvidenceTextExtractionException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class JsonEvidenceTextExtractor
        implements EvidenceTextExtractor {

    private final EvidenceExtractionProperties properties;
    private final JsonMapper jsonMapper;

    public JsonEvidenceTextExtractor(
            EvidenceExtractionProperties properties,
            JsonMapper jsonMapper
    ) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public EvidenceFileType supportedFileType() {
        return EvidenceFileType.JSON;
    }

    @Override
    public String extract(
            Resource resource
    ) {

        String jsonText =
                TextExtractionSupport.readStrictUtf8(
                        resource
                );

        TextExtractionSupport.requireNonBlank(
                jsonText,
                "The JSON evidence is empty"
        );

        JsonNode rootNode;

        try {
            rootNode = jsonMapper.readTree(jsonText);
        }
        catch (RuntimeException exception) {
            throw new EvidenceTextExtractionException(
                    "The JSON evidence contains "
                    + "malformed content",
                    exception
            );
        }

        if (rootNode == null) {
            throw new EvidenceTextExtractionException(
                    "The JSON evidence does not contain "
                    + "a valid JSON value"
            );
        }

        StringBuilder output =
                new StringBuilder();

        flattenNode(
                rootNode,
                "$",
                1,
                output
        );

        String extractedText =
                output.toString().strip();

        TextExtractionSupport.requireNonBlank(
                extractedText,
                "The JSON evidence does not contain "
                + "extractable content"
        );

        return TextExtractionSupport
                .enforceCharacterLimit(
                        extractedText,
                        properties.getMaxCharacters()
                );
    }

    private void flattenNode(
            JsonNode node,
            String path,
            int depth,
            StringBuilder output
    ) {

        if (depth > properties.getMaxJsonDepth()) {
            throw new EvidenceTextExtractionException(
                    "JSON evidence exceeds the configured "
                    + "maximum depth of "
                    + properties.getMaxJsonDepth()
            );
        }

        if (node.isObject()) {
            flattenObject(
                    node,
                    path,
                    depth,
                    output
            );

            return;
        }

        if (node.isArray()) {
            flattenArray(
                    node,
                    path,
                    depth,
                    output
            );

            return;
        }

        appendScalar(
                path,
                node,
                output
        );
    }

    private void flattenObject(
            JsonNode objectNode,
            String path,
            int depth,
            StringBuilder output
    ) {

        if (objectNode.isEmpty()) {
            appendLine(
                    output,
                    path + ": {}"
            );

            return;
        }

        for (
                String propertyName
                : objectNode.propertyNames()
        ) {

            JsonNode childNode =
                    objectNode.get(propertyName);

            String childPath =
                    "$".equals(path)
                            ? propertyName
                            : path
                            + "."
                            + propertyName;

            flattenNode(
                    childNode,
                    childPath,
                    depth + 1,
                    output
            );
        }
    }

    private void flattenArray(
            JsonNode arrayNode,
            String path,
            int depth,
            StringBuilder output
    ) {

        if (arrayNode.isEmpty()) {
            appendLine(
                    output,
                    path + ": []"
            );

            return;
        }

        for (
                int index = 0;
                index < arrayNode.size();
                index++
        ) {

            flattenNode(
                    arrayNode.get(index),
                    path
                    + "["
                    + index
                    + "]",
                    depth + 1,
                    output
            );
        }
    }

    private void appendScalar(
            String path,
            JsonNode node,
            StringBuilder output
    ) {

        String value;

        if (node == null || node.isNull()) {
            value = "null";
        }
        else {
            value =
                    TextExtractionSupport
                            .normalizeInlineValue(
                                    node.asString()
                            );
        }

        appendLine(
                output,
                path + ": " + value
        );
    }

    private void appendLine(
            StringBuilder output,
            String line
    ) {

        if (!output.isEmpty()) {
            TextExtractionSupport.appendWithLimit(
                    output,
                    "\n",
                    properties.getMaxCharacters()
            );
        }

        TextExtractionSupport.appendWithLimit(
                output,
                line,
                properties.getMaxCharacters()
        );
    }
}