package webScraper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class Scraper {
    private final InvertedIndex index;

    public Scraper(InvertedIndex index) {
        this.index = index;
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

        WebCrawler.crawl(seedUrl, index, totalPages);

        System.out.println(index.toString());
    }

    /**
     * Writes the index to the specified path.
     *
     * @param indexPath the path to write the index
     */
    public void writeIndex(Path indexPath) {
        try {
            JsonWriter.writeObject(index.getHtmlContentMap(), indexPath);
        } catch (IOException e) {
            System.err.println("Unable to write index to file: " + indexPath);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java webScraper.Scraper <URI> <totalPages>");
            System.exit(1);
        }

        try {
            URI seedUrl = new URI(args[0]);
            int totalPages = Integer.parseInt(args[1]);

            InvertedIndex index = new InvertedIndex();
            Scraper scraper = new Scraper(index);

            scraper.buildWebCrawl(seedUrl, totalPages);

            // Write the index to a file
            Path indexPath = Path.of("index.json");
            scraper.writeIndex(indexPath);

        } catch (URISyntaxException e) {
            System.err.println("Invalid URI: " + args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number for totalPages: " + args[1]);
        }
    }
}