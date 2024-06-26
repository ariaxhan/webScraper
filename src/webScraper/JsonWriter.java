package webScraper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @version Spring 2024
 */
public class JsonWriter {

    /**
     * Indents the writer by the specified number of times. Does nothing if the
     * indentation level is 0 or less.
     *
     * @param writer the writer to use
     * @param indent the number of times to indent
     * @throws IOException if an IO error occurs
     */
    public static void writeIndent(Writer writer, int indent) throws IOException {
        while (indent-- > 0) {
            writer.write("  ");
        }
    }

    /**
     * Indents and then writes the String element.
     *
     * @param element the element to write
     * @param writer  the writer to use
     * @param indent  the number of times to indent
     * @throws IOException if an IO error occurs
     */
    public static void writeIndent(String element, Writer writer, int indent) throws IOException {
        writeIndent(writer, indent);
        writer.write(element);
    }

    /**
     * Indents and then writes the text element surrounded by {@code " "} quotation
     * marks.
     *
     * @param element the element to write
     * @param writer  the writer to use
     * @param indent  the number of times to indent
     * @throws IOException if an IO error occurs
     */
    public static void writeQuote(String element, Writer writer, int indent) throws IOException {
        writeIndent(writer, indent);
        writer.write('"');
        writer.write(element);
        writer.write('"');
    }

    /**
     * Writes the provided map as a pretty JSON object.
     * The method uses generic notation, allowing it to work with any map structure.
     *
     * @param map    the map to write
     * @param writer the writer to use
     * @param indent the initial indent level; the first bracket is not indented,
     *               inner elements are indented by one, and the last bracket is
     *               indented at the initial indentation level
     * @throws IOException if an IO error occurs
     */
    public static void writeObject(Map<?, ?> map, Writer writer, int indent) throws IOException {
        writer.write("{");
        var iterator = map.entrySet().iterator();
        if (iterator.hasNext()) {
            writer.write("\n");
            var firstEntry = iterator.next();
            writeQuote(firstEntry.getKey().toString(), writer, indent);
            writer.write(": ");
            writeValue(firstEntry.getValue(), writer, indent);
        }
        while (iterator.hasNext()) {
            writer.write(",\n");
            var nextEntry = iterator.next();
            writeQuote(nextEntry.getKey().toString(), writer, indent);
            writer.write(": ");
            writeValue(nextEntry.getValue(), writer, indent);
        }
        writer.write("\n");
        writeIndent(writer, indent - 1);
        writer.write("}");
    }

    /**
     * Helper method to write a value based on its type.
     *
     * @param value  the value to write
     * @param writer the writer to use
     * @param indent the current indent level
     * @throws IOException if an IO error occurs
     */
    private static void writeValue(Object value, Writer writer, int indent) throws IOException {
        if (value instanceof String) {
            writer.write("\"" + value.toString() + "\"");
        } else if (value instanceof Map) {
            writeObject((Map<?, ?>) value, writer, indent + 1);
        } else if (value instanceof List) {
            writeArray((List<?>) value, writer, indent + 1);
        } else {
            writer.write(value.toString());
        }
    }

    /**
     * Writes a list as a pretty JSON array.
     *
     * @param list   the list to write
     * @param writer the writer to use
     * @param indent the initial indent level; the first bracket is not indented,
     *               inner elements are indented by one, and the last bracket is
     *               indented at the initial indentation level
     * @throws IOException if an IO error occurs
     */
    private static void writeArray(List<?> list, Writer writer, int indent) throws IOException {
        writer.write("[");
        var iterator = list.iterator();
        if (iterator.hasNext()) {
            writer.write("\n");
            writeIndent(writer, indent + 1);
            writeValue(iterator.next(), writer, indent + 1);
        }
        while (iterator.hasNext()) {
            writer.write(",\n");
            writeIndent(writer, indent + 1);
            writeValue(iterator.next(), writer, indent + 1);
        }
        writer.write("\n");
        writeIndent(writer, indent);
        writer.write("]");
    }

    /**
     * Writes the provided map to a file in JSON format.
     *
     * @param map        the map to write
     * @param outputPath the path of the file to write to
     * @throws IOException if an IO error occurs
     */
    public static void writeObject(Map<?, ?> map, Path outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writeObject(map, writer, 1);
        }
    }

    /**
     * Returns the provided map as a pretty JSON object string.
     *
     * @param map the map to use
     * @return a {@link String} containing the map in pretty JSON format
     */
    public static String writeObject(Map<?, ?> map) {
        try {
            StringWriter writer = new StringWriter();
            writeObject(map, writer, 0);
            return writer.toString();
        } catch (IOException e) {
            return null;
        }
    }
}