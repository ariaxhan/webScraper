package webScraper;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class to handle creation and modification of inverted index and word counts
 */
public class InvertedIndex {

	/**
	 * map to store word counts for each file in index
	 */
	protected final Map<String, Integer> wordCounts;
	/**
	 * map to store the inverted index
	 * first String is word, second is location, third is positions
	 */

	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex;

	/**
	 * constructor to initialise the inverted index and word count data structures.
	 */
	public InvertedIndex() {
		this.wordCounts = new TreeMap<String, Integer>();
		this.invertedIndex = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
	}

	/**
	 * Adds a word with its location and position to the index.
	 *
	 * @param word     the word to add
	 * @param location the file in which the word is located
	 * @param position the position of the word in the file
	 */
	public void addWord(String word, String location, int position) {
		// Retrieve or create the map of locations for the word
		TreeMap<String, TreeSet<Integer>> locationMap = invertedIndex.computeIfAbsent(word, k -> new TreeMap<>());

		// Retrieve or create the set of positions for the location
		TreeSet<Integer> positionSet = locationMap.computeIfAbsent(location, k -> new TreeSet<>());

		// Add the new position to the set
		positionSet.add(position);

		// Always update the word count to reflect the maximum position found so far for
		// any word in this location
		// This ensures that even if the new position is not the maximum, the max is
		// correctly maintained
		if (!positionSet.isEmpty()) {
			wordCounts.put(location, positionSet.last());
		}
	}

	/**
	 * adds multiple words starting at a default position.
	 *
	 * @param words    the words to add
	 * @param location the location of the words
	 */
	public void addWords(List<String> words, String location) {
		addWords(words, location, 0);
	}

	/**
	 * adds multiple words starting at a specific position.
	 *
	 * @param words    the words to add
	 * @param location the location of the words
	 * @param position the starting position
	 */
	public void addWords(List<String> words, String location, int position) {
		for (String word : words) {
			addWord(word, location, position++);
		}
	}

	/**
	 * Merges an inverted index into the current index
	 * 
	 * @param outputPath
	 * @throws IOException
	 */
	public void merge(InvertedIndex tempIndex) {
		// loop through the temp index
		for (var entry : tempIndex.invertedIndex.entrySet()) {
			// check if the entry already exists
			if (!hasWord(entry.getKey())) {
				this.invertedIndex.put(entry.getKey(), entry.getValue());
			} else {
				// if it does, do the merge
				for (var location : entry.getValue().entrySet()) {
					var currentLocation = invertedIndex.get(entry.getKey()).computeIfAbsent(location.getKey(),
							k -> new TreeSet<>());
					// add all locations to current
					currentLocation.addAll(location.getValue());
				}
			}
		}
		// go through each of the entries
		for (var file : tempIndex.wordCounts.entrySet()) {
			// check if the file exists in the wordCounts for the current index
			if (!hasCount(file.getKey())) {
				// if it doesn't, add it
				this.wordCounts.put(file.getKey(), file.getValue());
			} else {
				// if it does, update the count
				this.wordCounts.put(file.getKey(), this.wordCounts.get(file.getKey()) + file.getValue());
			}

		}
	}

	/**
	 * writes the inverted index to a file.
	 *
	 * @param outputPath the file to write to
	 * @throws IOException if an I/O error occurs
	 */
	public void writeIndex(Path outputPath) throws IOException {
		// use method in jsonwriter to write inverted index
		JsonWriter.writeInverted(this.invertedIndex, outputPath);
	}

	/**
	 * writes word counts to a file.
	 *
	 * @param outputPath the file to write to
	 * @throws IOException if an I/O error occurs
	 */
	public void writeCounts(Path outputPath) throws IOException {
		// use method in jsonwriter to write word counts
		JsonWriter.writeObject(this.wordCounts, outputPath);
	}

	/**
	 * get, num, and has methods.
	 */

	/**
	 * gets an unmodifiable view of word counts.
	 *
	 * @return the word counts
	 */
	public Map<String, Integer> getCounts() {
		return Collections.unmodifiableMap(this.wordCounts);
	}

	/**
	 * gets the word count for a specific location.
	 *
	 * @param location the location
	 * @return the word count
	 */
	public int getCount(String location) {
		return wordCounts.getOrDefault(location, 0);
	}

	/**
	 * determines if a specific location has a word count.
	 *
	 * @param location the location to lookup
	 * @return true if there is a count for the specified location
	 */
	public boolean hasCount(String location) {
		return wordCounts.containsKey(location);
	}

	/**
	 * gets an unmodifiable set of words in the index.
	 *
	 * @return the words
	 */
	public Set<String> getWords() {
		return Collections.unmodifiableSet(invertedIndex.keySet());
	}

	/**
	 * gets number of words in the index
	 *
	 * @return the size
	 */
	public int numWords() {
		return this.getWords().size();
	}

	/**
	 * determines if a specific word is in the index.
	 *
	 * @param word the word to lookup
	 * @return true if the word is in the index
	 */
	public boolean hasWord(String word) {
		return this.getWords().contains(word);
	}

	/**
	 * gets an unmodifiable set of locations for a specific word.
	 *
	 * @param word the word to find locations for
	 * @return the locations
	 */
	public Set<String> getLocations(String word) {
		// get the map inside the index for the specific word
		Map<String, TreeSet<Integer>> locationMap = invertedIndex.get(word);
		// return the locations for the word
		return locationMap != null ? Collections.unmodifiableSet(locationMap.keySet()) : Collections.emptySet();
	}

	/**
	 * gets an unmodifiable set of all locations in the index
	 * 
	 * @return set of locations
	 */
	protected Set<String> getLocations() {
		// get all of the locations for all of the words using the word counts
		return this.wordCounts.keySet();
	}

	/**
	 * returns number of locations for a word
	 *
	 * @param word the word to find location size for
	 * @return the locations
	 */
	public int numLocations(String word) {
		// get number of locations for a word using the size of its set
		return this.getLocations(word).size();
	}

	/**
	 * returns number of locations
	 *
	 * @return the locations
	 */
	public int numLocations() {
		// get total number of locations
		return this.getLocations().size();
	}

	/**
	 * checks if a location exists in the index
	 * 
	 * @param location to look for
	 * @return true if it does
	 */
	public boolean hasLocation(String location) {
		// check for the location in the set of locations
		return this.getLocations().contains(location);
	}

	/**
	 * determines if a specific location is in the index for a word.
	 *
	 * @param word     the location to lookup
	 * @param location to check for
	 * @return true if the location is in the index for the word
	 */
	public boolean hasLocation(String word, String location) {
		// check for the location in the set for a specific word
		return this.getLocations(word).contains(location);
	}

	/**
	 * gets an unmodifiable set of positions for a word in a specific location.
	 *
	 * @param word     the word
	 * @param location the location
	 * @return the positions
	 */
	public Set<Integer> getPositions(String word, String location) {
		// get map of all locations with the corresponding positions for the word
		// check if location map is null
		var locations = invertedIndex.get(word);
		if (locations != null) {
			// if the positions are not null, return them
			Set<Integer> positions = locations.get(location);
			if (positions != null) {
				return Collections.unmodifiableSet(positions);
			}
		}
		// otherwise, return an empty set
		return Collections.emptySet();
	}

	/**
	 * gets the number of positions stored for a specific word in the index.
	 *
	 * @param word     the word to lookup
	 * @param location the location to check
	 * @return the number of positions stored for the word, or 0 if the word is not
	 *         in the index
	 */
	public int numPositions(String word, String location) {
		// get number of positions for a specific word and location
		return getPositions(word, location).size();
	}

	/**
	 * returns true if the given position exists for the word and location
	 *
	 * @param word     the word to lookup
	 * @param location to check
	 * @param position to look up
	 * @return true if the position exists
	 */
	public boolean hasPosition(String word, String location, Integer position) {
		return getPositions(word, location).contains(position);
	}

	/**
	 * to string methods
	 */

	/**
	 * provides a string representation of the index.
	 *
	 * @return the index as a string
	 */
	public String indexToString() {
		return this.invertedIndex.toString();
	}

	/**
	 * provides a string representation of the word counts.
	 *
	 * @return the word counts as a string
	 */
	public String countToString() {
		return this.wordCounts.toString();
	}

	/**
	 * overrides the default toString method.
	 *
	 * @return a string containing both the index and the word counts
	 */
	@Override
	public String toString() {
		return this.indexToString() + "\n" + this.countToString();
	}

	/**
	 * Non static inner class to store search results and compare them.
	 */
	public class SearchResult implements Comparable<SearchResult> {
		/**
		 * File location of the search result
		 */
		private final String location;
		/**
		 * Number of matches
		 */
		private int matches;
		/**
		 * Word count for match
		 */
		private final int totalWordCount;
		/**
		 * Calculated score
		 */
		private double score;

		/**
		 * Constructs a SearchResult object with the specified location, number of
		 * matches, and total word count.
		 *
		 * @param location the file location of the search result
		 */
		public SearchResult(String location) {
			this.location = location;
			this.matches = 0;
			this.totalWordCount = wordCounts.get(location);
			this.score = 0;
		}

		/**
		 * Returns the number of matches for this search result.
		 *
		 * @return the number of matches
		 */
		public int getMatches() {
			return matches;
		}

		/**
		 * Updates the search result by incrementing the match count and recalculating
		 * the score.
		 *
		 * @param additionalMatches to add
		 */
		public void update(int additionalMatches) {
			this.matches += additionalMatches; // Accumulate the number of matches
			updateScore();
		}

		/**
		 * Recalculates the score for this search result. The score is computed as the
		 * number of matches
		 * divided by the total word count.
		 */
		private void updateScore() {
			this.score = (double) this.matches / this.totalWordCount;
		}

		/**
		 * Returns the score of the search result as a string.
		 *
		 * @return the score as a string
		 */
		public String getScore() {
			return Double.valueOf(this.score).toString();
		}

		/**
		 * Compares this search result with another search result for ordering. Results
		 * are ordered primarily by
		 * score in descending order, then by total word count in descending order, and
		 * lastly by location in
		 * ascending alphabetical order.
		 *
		 * @param result the search result to be compared
		 * @return a negative integer, zero, or a positive integer as this object is
		 *         less than, equal to, or greater
		 *         than the specified object
		 */
		@Override
		public int compareTo(SearchResult result) {
			// Check if either URI starts with "http" or "https" for special handling
			boolean thisHttp = this.location.toLowerCase().startsWith("http");
			boolean thisHttps = this.location.toLowerCase().startsWith("https");
			boolean resultHttp = result.location.toLowerCase().startsWith("http");
			boolean resultHttps = result.location.toLowerCase().startsWith("https");

			// Ensure "https" URIs come before "http" URIs
			if (thisHttps && !resultHttps) {
				return -1; // This URI (https) comes before result URI (http)
			} else if (!thisHttps && resultHttps) {
				return 1; // Result URI (https) comes before this URI (http)
			}

			// Ensure "http" URIs come before non-"http" URIs
			if (thisHttp && !resultHttp && !resultHttps) {
				return -1; // This URI (http) comes before result URI (not http or https)
			} else if (!thisHttp && resultHttp && !thisHttps) {
				return 1; // Result URI (http) comes before this URI (not http or https)
			}

			// Compare scores in descending order
			int scoreComparison = Double.compare(result.score, this.score);
			if (scoreComparison != 0) {
				return scoreComparison;
			}

			// Compare total word counts in descending order
			int wordCountComparison = Integer.compare(result.totalWordCount, this.totalWordCount);
			if (wordCountComparison != 0) {
				return wordCountComparison;
			}

			// Compare locations in ascending order (case-insensitive)
			return this.location.compareToIgnoreCase(result.location);
		}

		/**
		 * Returns the location of this search result.
		 *
		 * @return the location
		 */
		public String getLocation() {
			return this.location;
		}

		/**
		 * Returns a string representation of the search result in a JSON format.
		 *
		 * @return a string in JSON format representing the search result
		 */
		@Override
		public String toString() {
			return "{\n" +
					"  \"count\": " + Math.round(matches) + ",\n" +
					"  \"score\": " + String.format("%.8f", score) + ",\n" +
					"  \"where\": \"" + location + "\"\n" +
					"}";
		}
	}

	/**
	 * Convenience method to decide what kind of search to perform
	 *
	 * @param words           to search for
	 * @param partialProvided to determine if it should be an exact or partial
	 *                        search
	 * @return TreeMap of search results
	 */
	public List<SearchResult> search(Set<String> words, boolean partialProvided) {
		// create a new treeset of the result list and return it
		return partialProvided ? partialSearch(words) : (exactSearch(words));
	}

	/**
	 * function to conduct an exact search to the inverted index data structure with
	 * already parsed words from a single line of the query file,
	 *
	 * @param words the words to search for
	 * @return a sorted list of search results.
	 */
	public List<SearchResult> exactSearch(Set<String> words) {
		HashMap<String, SearchResult> resultMap = new HashMap<>();
		for (String word : words) {
			TreeMap<String, TreeSet<Integer>> locations = invertedIndex.get(word);
			if (locations != null) {
				updateResults(locations, resultMap);
			}
		}
		return sortAndCollectResults(resultMap);
	}

	/**
	 * conducts a partial search of the inverted index
	 *
	 * @param words to search for
	 * @return List of search results
	 */
	public List<SearchResult> partialSearch(Set<String> words) {
		HashMap<String, SearchResult> resultMap = new HashMap<>();
		for (String word : words) {
			NavigableMap<String, TreeMap<String, TreeSet<Integer>>> tailMap = invertedIndex.tailMap(word, true);
			for (Map.Entry<String, TreeMap<String, TreeSet<Integer>>> entry : tailMap.entrySet()) {
				if (!entry.getKey().startsWith(word)) {
					break;
				}
				updateResults(entry.getValue(), resultMap);
			}
		}
		return sortAndCollectResults(resultMap);
	}

	/**
	 * Private helper method to sort and collect the results
	 *
	 * @param resultMap to sort and collect
	 * @return list of search results
	 */
	private static List<SearchResult> sortAndCollectResults(HashMap<String, SearchResult> resultMap) {
		List<SearchResult> results = new ArrayList<>(resultMap.values());
		Collections.sort(results);
		return results;
	}

	/**
	 * Private helper method to loop through the words and locations
	 *
	 * @param locations to loop through
	 * @param resultMap to update
	 */
	private void updateResults(Map<String, TreeSet<Integer>> locations, HashMap<String, SearchResult> resultMap) {
		for (Map.Entry<String, TreeSet<Integer>> entry : locations.entrySet()) {
			String location = entry.getKey();
			int occurrences = entry.getValue().size(); // Correct number of occurrences of the word at the location

			SearchResult result = resultMap.computeIfAbsent(location, k -> new SearchResult(k));
			result.update(occurrences); // Make sure occurrences are being accumulated properly
		}
	}

	/**
	 * Processes the HTML content of a page by stemming words and adding them to the
	 * index.
	 * 
	 * @param html the HTML content to process
	 * @param uri  the URI of the page
	 */
	void indexPage(String html, URI uri) {
		// Clean HTML and extract unique stems
		String cleanHtml = HtmlCleaner.stripHtml(html);
		ArrayList<String> uniqueStems;
		try {
			uniqueStems = FileStemmer.listStems(cleanHtml);
		} catch (Exception e) {
			System.err.println("Error stemming HTML content from URI: " + uri);
			return;
		}

		// Add stemmed words to the index
		SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
		int position = 0;
		for (String word : uniqueStems) {
			position++;
			this.addWord(stemmer.stem(word).toString(), uri.toString(), position);
		}
	}

}