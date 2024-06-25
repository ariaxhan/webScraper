package webScraper;
package edu.usfca.cs272;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Scraper {
	private static final Logger log = LogManager.getLogger(Scraper.class);
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
		log.debug("Building index with single-threading from path: {}", textPath);
		try {
			builder.processPath(textPath);
		} catch (IOException e) {
			log.error("Unable to process text files at: {}", textPath, e);
		} catch (Exception e) {
			log.error("Unable to process text files at: {}", textPath, e);
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
		log.debug("Starting web crawling from seed URL: {}", seedUrl);
		try {
			SingleThreadedWebCrawler crawler = new SingleThreadedWebCrawler(index, totalPages);
			crawler.initialBuild(seedUrl);
		} catch (URISyntaxException e) {
			log.error("Error with seed URL: {}", seedUrl, e);
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
			log.info("Successfully wrote counts to {}", countsPath);
		} catch (IOException e) {
			log.error("Unable to write counts to file: {}", countsPath, e);
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
			log.info("Successfully wrote index to {}", indexPath);
		} catch (IOException e) {
			log.error("Unable to write inverted index to file: {}", indexPath, e);
		}
	}
}