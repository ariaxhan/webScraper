package webScraper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class JsonWriter {
	/**
	 * static decimal format
	 */
	final static DecimalFormat FORMATTER = new DecimalFormat("0.00000000"); // Formatter for numeric output

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
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the
	 *                 initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */

	public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {
		writer.write("[");
		var iterator = elements.iterator();
		if (iterator.hasNext()) {
			writer.write("\n");
			writeIndent(writer, indent + 1);
			writer.write(iterator.next().toString());
		}
		while (iterator.hasNext()) {
			writer.write(",");
			writer.write("\n");
			writeIndent(writer, indent + 1);
			writer.write(iterator.next().toString());
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the
	 *                 initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent)
			throws IOException {
		writer.write("{");
		// create iterator
		Iterator<? extends Map.Entry<String, ? extends Number>> iterator = elements.entrySet().iterator();
		// handle the first element outside the loop to avoid comma prefix
		if (iterator.hasNext()) {
			writer.write("\n");
			Map.Entry<String, ? extends Number> firstEntry = iterator.next();
			writeQuote(firstEntry.getKey(), writer, indent + 1);
			writer.write(": " + (firstEntry.getValue().toString()));
		}
		while (iterator.hasNext()) {
			writer.write(",\n");
			Map.Entry<String, ? extends Number> entry = iterator.next();
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": " + (entry.getValue().toString()));
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
		writer.flush();
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 * @throws IOException if an IO error occurs
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) throws IOException {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the
	 *                 initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("{");
		var iterator = elements.entrySet().iterator();

		if (iterator.hasNext()) {
			Entry<String, ? extends Collection<? extends Number>> firstEntry = iterator.next();
			writer.write("\n");
			writeQuote(firstEntry.getKey(), writer, indent + 1);
			writer.write(": ");
			writeArray(firstEntry.getValue(), writer, indent + 1);
		}

		while (iterator.hasNext()) {
			writer.write(",\n");
			Entry<String, ? extends Collection<? extends Number>> entry = iterator.next();
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": ");
			writeArray(entry.getValue(), writer, indent + 1);
		}

		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the
	 *                 initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		var iterator = elements.iterator();
		writer.write("[");

		if (iterator.hasNext()) {
			writer.write("\n");
			Map<String, ? extends Number> firstEntry = iterator.next();
			writeIndent(writer, indent + 1);
			writeObject(firstEntry, writer, indent + 1);
		}

		while (iterator.hasNext()) {
			writer.write(",\n");
			Map<String, ? extends Number> entry = iterator.next();
			writeIndent(writer, indent + 1);
			writeObject(entry, writer, indent + 1);
		}

		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/** Prevent instantiating this class of static methods. */
	private JsonWriter() {
	}

	/**
	 * Writes the inverted index as a pretty JSON object.
	 * The method uses generic notation, allowing it to work with any map of maps
	 * structure.
	 *
	 * @param invertedIndex the inverted index to write
	 * @param writer        the writer to use
	 * @param indent        the initial indent level; the first bracket is not
	 *                      indented,
	 *                      inner elements are indented by one, and the last bracket
	 *                      is
	 *                      indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeInverted(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> invertedIndex, Writer writer,
			int indent)
			throws IOException {
		// create iterator
		var iterator = invertedIndex.entrySet().iterator();
		// write beginning curly brace
		writer.write("{");
		if (iterator.hasNext()) {
			writer.write("\n");
			// store the entry
			var firstEntry = iterator.next();
			// write in the keys with quotes
			writeQuote(firstEntry.getKey(), writer, indent);
			writer.write(": ");
			// write in the array of values
			writeObjectArrays(firstEntry.getValue(), writer, indent);
		}
		// use the iterator to go through each entry in the elements collection and
		// write them
		while (iterator.hasNext()) {
			writer.write(",\n");
			// store the entry
			var nextEntry = iterator.next();
			// write in the keys with quotes
			writeQuote(nextEntry.getKey(), writer, indent);
			writer.write(": ");
			// write in the array of values
			writeObjectArrays(nextEntry.getValue(), writer, indent);
		}
		writeIndent(writer, indent);
		// closing curly brace
		writer.write("\n}");
	}

	/**
	 * Writes the provided inverted index to a file in JSON format.
	 *
	 * @param invertedIndex the inverted index to write
	 * @param outputPath    the path of the file to write to
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeInverted(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> invertedIndex, Path outputPath)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
			// use the writeInverted method to write the inverted index
			writeInverted(invertedIndex, writer, 1);
		}
	}

	/**
	 * Writes the search results to a file in JSON format.
	 *
	 * @param queryMap the map containing the query keys and their corresponding
	 *                 search results
	 * @param path     the path to the output file
	 * @param writer   the writer used for output
	 * @param indent   the indentation level for the current context
	 * @throws IOException if an I/O error occurs during writing
	 */
	public static void writeSearchResults(Map<String, ? extends Collection<InvertedIndex.SearchResult>> queryMap,
			Path path, Writer writer, int indent)
			throws IOException {
		writer.write("{");
		// Create an iterator for the queryMap
		var iterator = queryMap.entrySet().iterator();
		// write the first key-value pair
		if (iterator.hasNext()) {
			writer.write("\n");
			Entry<String, ? extends Collection<InvertedIndex.SearchResult>> firstEntry = iterator.next();
			writeQuote(firstEntry.getKey(), writer, 1);
			writer.write(": ");
			writeSearchResultArray(firstEntry.getValue(), writer, 1);
		}
		// Loop through the queryMap
		while (iterator.hasNext()) {
			writer.write(",\n");
			// Get the next entry
			Entry<String, ? extends Collection<InvertedIndex.SearchResult>> entry = iterator.next();
			// Write the key and value
			writeQuote(entry.getKey(), writer, 1);
			writer.write(": ");
			writeSearchResultArray(entry.getValue(), writer, 1);
		}
		writer.write("\n}"); // End of the JSON object
	}

	/**
	 * Convenience method to write to file
	 *
	 * @param queryMap the map containing the query keys and their corresponding
	 *                 search results
	 * @param path     the path to the output file
	 */
	public static void writeSearchResults(Map<String, ? extends Collection<InvertedIndex.SearchResult>> queryMap,
			Path path) {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writeSearchResults(queryMap, path, writer, 1);
		} catch (IOException e) {
			System.out.println("Error writing search results to file: " + path);
		}
	}

	/**
	 * Writes an array of search results in JSON format.
	 *
	 * @param results the TreeSet of SearchResult objects to be written
	 * @param writer  the writer used for output
	 * @param indent  the indentation level for the current context
	 * @throws IOException if an I/O error occurs during writing
	 */
	public static void writeSearchResultArray(Collection<InvertedIndex.SearchResult> results, Writer writer, int indent)
			throws IOException {
		writer.write("[");
		// Create an iterator for the results
		var iterator = results.iterator();
		if (iterator.hasNext()) {
			writer.write("\n");
			writeSearchResult(iterator.next(), writer, indent + 1);
		}
		// Loop through the results
		while (iterator.hasNext()) {
			writer.write(",\n");
			// Write the SearchResult object
			writeSearchResult(iterator.next(), writer, indent + 1);
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]"); // Close the array
	}

	/**
	 * Writes a single search result in JSON format.
	 *
	 * @param result the SearchResult object to be written
	 * @param writer the writer used for output
	 * @param indent the indentation level for the current context
	 * @throws IOException if an I/O error occurs during writing
	 */
	private static void writeSearchResult(InvertedIndex.SearchResult result, Writer writer, int indent)
			throws IOException {
		writeIndent(writer, indent);
		writer.write("{\n");

		// write count key-value pair
		writeIndent(writer, indent + 1);
		writer.write(String.format("\"count\": %d,\n", result.getMatches()));

		// write score key-value pair
		String formattedScore = FORMATTER.format(Double.parseDouble(result.getScore()));
		writeIndent(writer, indent + 1);
		writer.write(String.format("\"score\": %s,\n", formattedScore));

		// write where key-value pair (last, no comma)
		writeIndent(writer, indent + 1);
		writer.write(String.format("\"where\": \"%s\"\n", result.getLocation()));

		// close JSON object
		writeIndent(writer, indent);
		writer.write("}");
	}

}