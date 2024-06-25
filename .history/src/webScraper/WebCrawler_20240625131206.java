package webScraper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class WebCrawler {
	

	public static void crawl(URI uri, InvertedIndex index) {
		URI originalUri = uri;
		try {
			originalUri = removeHash(uri);
		} catch (URISyntaxException e) {
			System.out.println("Error removing hash");
		}
		if (!HtmlFetcher.isValidURL(originalUri)) {
			System.out.println("Invalid URL scheme: " + originalUri);
			return;
		}
		// get the html
		String html = HtmlFetcher.fetch(originalUri);
		if (html == null || html.startsWith("Error") || html.startsWith("Non-HTML") || html.startsWith("Invalid")) {
			System.out.println("Fetch failed for URI: " + originalUri + " with message: " + html);
			return;
		}
		// get the hyperlinks
		List<String> hyperlinks = HtmlCleaner.extractHyperlinks(html);

		// Process the current page
		processPage(html, originalUri, index);

		// Process each hyperlink
		for (String link : hyperlinks) {
			try {
				URI linkUri = new URI(link);
				// Ensure the link is absolute
				if (!linkUri.isAbsolute()) {
					linkUri = originalUri.resolve(linkUri);
				}
				// Recursively crawl the hyperlink
				crawl(linkUri, index);
			} catch (URISyntaxException e) {
				System.out.println("Invalid URI syntax: " + link);
			}
		}
	}

	public static void processPage(String html, URI originalUri, InvertedIndex index) {
		// Clean the HTML
		String cleanHtml = HtmlCleaner.stripHtml(html);
		
		int position = 0;
		// If it is not empty, add to the inverted index
		if (cleanHtml != null) {
			for (String word : uniqueStems) {
				position++;
				// Check for the word
				index.addWord(word, originalUri.toString(), position);
			}
		}
	}

	/**
	 * Helper function to remove the fragment (the part after the #) from the URI if
	 * it exists.
	 *
	 * @param uri the URI to process
	 * @return the URI without the fragment
	 * @throws URISyntaxException if the URI syntax is incorrect
	 */
	public static URI removeHash(URI uri) throws URISyntaxException {
		// check for null uri
		if (uri == null) {
			return null;
		}
		// convert uri to string
		String urlString = uri.toString();
		// find index of #
		int hashIndex = urlString.indexOf('#');
		// if it is not at the end, remove it
		if (hashIndex != -1) {
			urlString = urlString.substring(0, hashIndex);
		}
		// return the new uri
		return new URI(urlString);
	}
}
