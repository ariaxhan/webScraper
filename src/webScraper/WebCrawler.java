package webScraper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebCrawler {
    private static final Set<URI> visitedUris = new HashSet<>();
    private static int pagesCrawled = 0;

    public static void crawl(URI uri, InvertedIndex index, int totalPages) {
        if (pagesCrawled >= totalPages) {
            return;
        }

        if (!HtmlFetcher.isValidURL(uri) || visitedUris.contains(uri)) {
            return;
        }

        visitedUris.add(uri);
        String html = HtmlFetcher.fetch(uri);

        if (html == null || html.startsWith("Error") || html.startsWith("Non-HTML") || html.startsWith("Invalid")) {
            return;
        }

        index.indexPage(html, uri);
        pagesCrawled++;

        if (pagesCrawled >= totalPages) {
            return;
        }

        List<String> hyperlinks = HtmlCleaner.extractHyperlinks(html);

        for (String link : hyperlinks) {
            if (link.contains("/recipe")) { // Check if the link contains "/recipe"
                try {
                    URI linkUri = new URI(link);
                    if (!linkUri.isAbsolute()) {
                        linkUri = uri.resolve(linkUri);
                    }
                    crawl(linkUri, index, totalPages);
                } catch (URISyntaxException e) {
                    System.out.println("Invalid URI syntax: " + link);
                }
            }
        }
    }
}