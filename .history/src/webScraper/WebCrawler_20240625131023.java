package webScraper;

package edu.usfca.cs272;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.TreeSet;

public class WebCrawler {

	/**
	 * Web pages must be requested using **sockets and HTTP/S** from the web server
	 * as follows:
	 * 
	 * - If the HTTP headers returned by the web server includes a `200 OK` HTTP/S
	 * response status code and HTML content type, then download, process, and add
	 * the HTML content to the inverted index. - If the HTTP headers returned by the
	 * web server includes a **redirect** HTTP/S response status code, follow up to
	 * 3 redirects maximum until a `200 OK` is returned. Associate the final
	 * response with the original cleaned URI and process. For example, the URI
	 * [~cs212/redirect/one](https://www.cs.usfca.edu/~cs212/redirect/one)
	 * eventually redirects to
	 * [~cs212/simple/hello.html](https://www.cs.usfca.edu/~cs212/simple/hello.html).
	 * The web crawler will associate the HTTPS response of
	 * [~cs212/simple/hello.html](https://www.cs.usfca.edu/~cs212/simple/hello.html)
	 * with the original URI
	 * [~cs212/redirect/one](https://www.cs.usfca.edu/~cs212/redirect/one) when
	 * processing.
	 * 
	 * For efficiency (and to avoid being blocked or rate-limited by the web
	 * server), do not download unnecessary content and only download necessary
	 * content exactly once from the web server. Specifically:
	 * 
	 * - Do not fetch the web page content if it is not HTML and a `200 OK` status
	 * code. For example, *only* the headers (not the content) will be downloaded
	 * for large text file without the `text/html` content-type, or for a `404`
	 * status web page. - Do not fetch the web page content more than once. For
	 * example, do not fetch the entire web page content once to check the headers
	 * and again to process the HTML.
	 * 
	 * The downloaded HTML must be processed before being stored in the inverted
	 * index as follows:
	 * 
	 * 1. Remove HTML comments and the `head`, `style`, `script`, `noscript`, and
	 * `svg` block elements. 2. Remove all of the remaining HTML tags. 3. Convert
	 * any HTML 4 entities to their Unicode symbol and remove any other HTML
	 * entities found that could not be converted. 4. Clean, parse, and stem the
	 * resulting text. 5. Efficiently add the stems to the inverted index with the
	 * URL as the location.
	 * 
	 * Eventually, your web crawler must be multithreaded. It is up to you whether
	 * you want to start with a single threaded or multithreaded implementation for
	 * this release.
	 */

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
		// Clean and parse
		ArrayList<String> uniqueStems = new ArrayList<>();
		try {
			uniqueStems = FileStemmer.listStems(cleanHtml);
		} catch (Exception e) {
			System.out.println("Error stemming HTML file");
		}
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
