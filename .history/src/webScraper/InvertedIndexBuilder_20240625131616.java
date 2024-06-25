package webScraper;

import java.io.BufferedReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * Builder for directory traversing and file stemming
 *
 */
public class InvertedIndexBuilder {

	/**
	 * object to use invertedIndex
	 */
	private final InvertedIndex invertedIndex;

	/**
	 * constructor
	 *
	 * @param invertedIndex object to use within the Builder class
	 */
	public InvertedIndexBuilder(InvertedIndex invertedIndex) {
		this.invertedIndex = invertedIndex;
	}

	/**
	 * function to traverse through a directory and process each file
	 *
	 * @param directory path to directory
	 * @throws Exception to be handled elsewhere
	 */
	public void processDirectory(Path directory) throws Exception {
		// loop through files using try with resources
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
			for (Path path : listing) {
				// check for directory, process recursively if so
				if (Files.isDirectory(path)) {
					processDirectory(path);
					// otherwise, process the word counts and inverted index
				} else if (containsTextfiles(path)) {
					processFile(path);
				}
			}
		}
	}

	/**
	 * Function to process a single file, populating the inverted index
	 *
	 * @param filename to be processed
	 * @throws Exception to be handled elsewhere
	 */
	public void processFile(Path filename) throws Exception {
		String stringFilename = filename.toString();
		String line;
		SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);
		int position = 1; // Initialize a position counter
		// try to make reader with try with resources
		try (BufferedReader newBuffer = Files.newBufferedReader(filename)) {
			// read each line and parse
			while ((line = newBuffer.readLine()) != null) {
				// clean and stem
				String[] cleanLine = FileStemmer.parse(line);
				for (String word : cleanLine) {
					// add directly to the inverted index
					this.invertedIndex.addWord(stemmer.stem(word).toString(), stringFilename, position++);

				}
			}
		}

	}

	/**
	 * Function to process a path based on if the path points to a directory or a
	 * file
	 *
	 * @param path to process
	 * @throws Exception to be handled elsewhere
	 */
	public void processPath(Path path) throws Exception {
		if (Files.isDirectory(path)) {
			processDirectory(path);
		} else {
			processFile(path);
		}
	}

	/**
	 * helper function to check a file for text files
	 *
	 * @param path file to check for txt/text
	 * @return if directory contains text files
	 */
	public static boolean containsTextfiles(Path path) {
		if (Files.isRegularFile(path)) {
			String pathString = path.toString().toLowerCase();
			return (pathString.endsWith(".txt") || pathString.endsWith(".text"));
		}
		return false;
	}
}
