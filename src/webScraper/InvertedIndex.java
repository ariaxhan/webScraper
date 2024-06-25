package webScraper;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class to handle creation and modification of inverted index and HTML content
 */
public class InvertedIndex {

    private final TreeMap<String, String> invertedIndex;

    public InvertedIndex() {
        this.invertedIndex = new TreeMap<>();
    }

    public void writeInverted(Path outputPath) throws IOException {
        JsonWriter.writeInverted(this.invertedIndex, outputPath);
    }

    public Map<String, String> getHtmlContentMap() {
        return Collections.unmodifiableMap(this.invertedIndex);
    }

    public boolean hasLocation(String location) {
        return this.invertedIndex.containsKey(location);
    }

    public String getHtmlContent(String location) {
        return this.invertedIndex.get(location);
    }

    @Override
    public String toString() {
        return "HTML Content:\n" + this.invertedIndex.toString();
    }

    /**
     * Processes the HTML content of a page by cleaning it and adding it to the
     * index.
     * 
     * @param html the HTML content to process
     * @param uri  the URI of the page
     */
    public void indexPage(String html, URI uri) {
        // Clean HTML content
        String cleanHtml = HtmlCleaner.stripHtml(html);

        // Store the cleaned HTML content for the URL
        invertedIndex.put(uri.toString(), cleanHtml);
    }
}