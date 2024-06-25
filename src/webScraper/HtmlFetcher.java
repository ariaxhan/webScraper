package webScraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.List;
import java.util.Map;

/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and
 * returns HTML content if possible.
 *
 * @see HttpsFetcher
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class HtmlFetcher {

	/**
	 * Checks if the provided URI uses a valid scheme (http or https).
	 *
	 * @param uri the URI to check
	 * @return {@code true} if the URI uses a valid scheme, {@code false} otherwise
	 */
	public static boolean isValidURL(URI uri) {
		// CITE: https://docs.oracle.com/javase/8/docs/api/java/net/URI.html
		String scheme = uri.getScheme();
		return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
	}

	/**
	 * Returns {@code true} if and only if there is a "content-type" header (assume
	 * lowercase) and the first value of that header starts with the value
	 * "text/html" (case-insensitive).
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return {@code true} if the headers indicate the content type is HTML
	 *
	 * @see HttpsFetcher#processHttpHeaders(BufferedReader)
	 */
	public static boolean isHtml(Map<String, List<String>> headers) {
		if (headers.containsKey("content-type")) {
			List<String> contentType = headers.get("content-type");
			return contentType.get(0).toLowerCase().startsWith("text/html");
		}
		return false;
	}

	/**
	 * Parses the HTTP status code from the provided HTTP headers, assuming the
	 * status line is stored under the {@code null} key.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the HTTP status code or -1 if unable to parse for any reasons
	 *
	 * @see HttpsFetcher#processHttpHeaders(BufferedReader)
	 */
	public static int getStatusCode(Map<String, List<String>> headers) {
		if (headers.containsKey(null)) {
			String statusLine = headers.get(null).get(0);
			String[] parts = statusLine.split(" ");
			if (parts.length >= 2) {
				try {
					return Integer.parseInt(parts[1]);
				} catch (NumberFormatException e) {
					return -1;
				}
			}
		}
		return -1;
	}

	/**
	 * If the HTTP status code is between 300 and 399 (inclusive) indicating a
	 * redirect, returns the first redirect location if it is provided. Otherwise
	 * returns {@code null}.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the first redirected location if the headers indicate a redirect
	 *
	 * @see HttpsFetcher#processHttpHeaders(BufferedReader)
	 */
	public static String getRedirect(Map<String, List<String>> headers) {
		if (getStatusCode(headers) > 299 && getStatusCode(headers) < 400) {
			if (headers.containsKey("location")) {
				return headers.get("location").get(0);
			} else {
				return null;
			}
		}
		return null;
	}

	/**
	 * Efficiently fetches HTML using HTTP/1.1 and sockets.
	 *
	 * <p>
	 * The HTTP body will only be fetched and processed if the status code is 200
	 * and the content-type is HTML. In that case, the HTML will be returned as a
	 * single joined String using the {@link System#lineSeparator}.
	 *
	 * <p>
	 * Otherwise, the HTTP body will not be fetched. However, if the status code is
	 * a redirect, then the location of the redirect will be recursively followed up
	 * to the specified number of times. Once the number of redirects falls to 0 or
	 * lower, then redirects will no longer be followed.
	 *
	 * <p>
	 * If valid HTML cannot be fetched within the specified number of redirects,
	 * then {@code null} is returned.
	 *
	 * @param uri       the URI to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the HTML or {@code null} if unable to fetch valid HTML
	 *
	 * @see HttpsFetcher#openConnection(URI)
	 * @see HttpsFetcher#printGetRequest(PrintWriter, URI)
	 * @see HttpsFetcher#processHttpHeaders(BufferedReader)
	 *
	 * @see String#join(CharSequence, CharSequence...)
	 * @see System#lineSeparator()
	 *
	 * @see #isHtml(Map)
	 * @see #getRedirect(Map)
	 */
	public static String fetch(URI uri, int redirects) {
		// Check if the URI has a valid scheme
		if (!isValidURL(uri)) {
			return null;
		}

		URI currentUri = uri;
		int remainingRedirects = redirects;

		String html = null;
		try {
			while (remainingRedirects >= 0) {
				try (// create socket and open connection using the uri
						Socket socket = HttpsFetcher.openConnection(currentUri);
						PrintWriter request = new PrintWriter(socket.getOutputStream());
						InputStreamReader input = new InputStreamReader(socket.getInputStream(), UTF_8);
						BufferedReader response = new BufferedReader(input);) {
					// Make HTTP GET request
					HttpsFetcher.printGetRequest(request, currentUri);

					// Process HTTP headers
					Map<String, List<String>> headers = HttpsFetcher.processHttpHeaders(response);

					
					 int statusCode = getStatusCode(headers);
	                    System.out.println("Status code: " + statusCode);
	                    
	                    
					// Check if the content is HTML
					if (isHtml(headers) && getStatusCode(headers) == 200) {
						StringBuilder sb = new StringBuilder();
						String line;
						while ((line = response.readLine()) != null) {
							sb.append(line).append(System.lineSeparator());
						}
						html = sb.toString();
						break; // Successfully fetched HTML
					} else if (getRedirect(headers) != null && remainingRedirects > 0) {
						currentUri = new URI(getRedirect(headers));
						remainingRedirects--;
						System.out.println("Redirecting to: " + currentUri);
					} else {
						break; // Non-HTML content or no more redirects
					}
				}
			}
		} catch (IOException | URISyntaxException e) {
			System.err.println("Error fetching URL: " + e.getMessage());
		}
		if (html == null) {
		    System.err.println("Failed to fetch valid HTML content from URL: " + uri);
		}

		return html;
	}

	/**
	 * Converts the {@link String} into a {@link URI} object and then calls
	 * {@link #fetch(URI, int)}.
	 *
	 * @param uri       the URI to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the HTML or {@code null} if unable to fetch valid HTML
	 *
	 * @see #fetch(URI, int)
	 */
	public static String fetch(String uri, int redirects) {
		try {
			// ensure redirects does not exceed 3
			int maxRedirects = 3;
			return fetch(new URI(uri), maxRedirects);
		} catch (NullPointerException | URISyntaxException e) {
			return null;
		}
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URI, int)} with 0 redirects.
	 *
	 * @param uri the URI to fetch
	 * @return the HTML or {@code null} if unable to fetch valid HTML
	 *
	 * @see #fetch(URI, int)
	 */
	public static String fetch(String uri) {
		return fetch(uri, 3);
	}

	/**
	 * Calls {@link #fetch(URI, int)} with 0 redirects.
	 *
	 * @param uri the URI to fetch
	 * @return the HTML or {@code null} if unable to fetch valid HTML
	 */
	public static String fetch(URI uri) {
		return fetch(uri, 3);
	}

	/**
	 * Demonstrates this class.
	 *
	 * @param args unused
	 * @throws IOException if unable to process uri
	 */
	public static void main(String[] args) throws IOException {
		String link = "https://usf-cs272-spring2024.github.io/project-web/input/birds/falcon.html";
		System.out.println(link);
		System.out.println(fetch(link));
	}
}
