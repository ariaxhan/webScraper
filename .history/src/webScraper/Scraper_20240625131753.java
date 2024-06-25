package webScraper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Scraper {
	private final InvertedIndex index;

	public Scraper(InvertedIndex index) {
		this.index = index;
	}

	/**
	 * Builds the index using a single-threaded approach from the given text path.
	 *
	 * @param textPath the path to the text files to index
	 */
	public void buildIndex(Path textPath) {
		InvertedIndexBuilder builder = new InvertedIndexBuilder(index);
	
		try {
			builder.processPath(textPath);
		} catch (IOException e) {
			System.err.println("Unable to process text files at: " + textPath);
		} catch (Exception e) {
			System.err.println("Unable to build index from text files at: " + textPath);
		}
	}

	/**
	 * Builds the index using a single-threaded approach for web crawling from the
	 * given seed URL.
	 *
	 * @param seedUrl    the seed URL to start web crawling from
	 * @param totalPages the total number of pages to crawl
	 */
	public void buildWebCrawl(URI seedUrl, int totalPages) {
		System.out.println("Starting web crawling from seed URL: " + seedUrl);
		try {
			WebCrawler crawler = new WebCrawler();
			crawler.initialBuild(seedUrl);
		} catch (URISyntaxException e) {
			System.out.println("Invalid URI syntax: " + seedUrl);
		}
	}

	/**
	 * Writes the word counts to the specified path.
	 *
	 * @param countsPath the path to write the word counts
	 */
	public void writeCounts(Path countsPath) {
		try {
			index.writeCounts(countsPath);
			
		} catch (IOException e) {
			
		}
	}

	/**
	 * Writes the index to the specified path.
	 *
	 * @param indexPath the path to write the index
	 */
	public void writeIndex(Path indexPath) {
		try {
			index.writeIndex(indexPath);
			
		} catch (IOException e) {
			
		}
	}
}