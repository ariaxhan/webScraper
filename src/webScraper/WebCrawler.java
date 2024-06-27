package webScraper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebCrawler {
    private static final Set<URI> visitedUris = new HashSet<>();
    private static int pagesCrawled = 0;

    public static void crawl(URI uri, InvertedIndex index, int totalPages) {
        System.out.println("Crawling URI: " + uri);
        if (pagesCrawled >= totalPages) {
            System.out.println("Reached the maximum number of pages to crawl: " + totalPages);
            return;
        }

        // Check if the link is a YouTube link
        if (uri.getHost() != null && uri.getHost().contains("youtube.com")) {
            System.out.println("Found YouTube link: " + uri);
            try {
                YouTubeDownloader.downloadVideo(uri.toString(), "downloads"); // Save to downloads directory
                System.out.println("Downloaded YouTube video: " + uri);
                return;
            } catch (Exception e) {
                System.err.println("Failed to download YouTube video: " + uri);
                e.printStackTrace();
                return;
            }
        }

        if (!HtmlFetcher.isValidURL(uri) || visitedUris.contains(uri)) {
            System.out.println("Invalid or already visited URI: " + uri);
            return;
        }

        visitedUris.add(uri);
        String html = HtmlFetcher.fetch(uri);

        if (html == null || html.startsWith("Error") || html.startsWith("Non-HTML") || html.startsWith("Invalid")) {
            System.out.println("Failed to fetch HTML content from URI: " + uri);
            return;
        }

        System.out.println("Indexing content from URI: " + uri);
        index.indexPage(html, uri);
        pagesCrawled++;
        System.out.println("Pages crawled: " + pagesCrawled);

        if (pagesCrawled >= totalPages) {
            System.out.println("Reached the maximum number of pages to crawl after indexing: " + totalPages);
            return;
        }

        List<String> hyperlinks = HtmlCleaner.extractHyperlinks(html);
        System.out.println("Found " + hyperlinks.size() + " hyperlinks on page.");

        for (String link : hyperlinks) {
            try {
                URI linkUri = new URI(link);
                if (!linkUri.isAbsolute()) {
                    linkUri = uri.resolve(linkUri);
                }

                System.out.println("Recursively crawling link: " + linkUri);
                crawl(linkUri, index, totalPages);
            } catch (URISyntaxException e) {
                System.out.println("Invalid URI syntax: " + link);
                e.printStackTrace();
            }
        }
    }
}