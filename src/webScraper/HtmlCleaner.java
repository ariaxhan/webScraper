package webScraper;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Cleans simple, validating HTML 4/5 into plain text. For simplicity, this
 * class cleans already validating HTML, it does not validate the HTML itself.
 * For example, the {@link #stripEntities(String)} method removes HTML entities
 * but does not check that the removed entity was valid.
 *
 * <p>
 * Look at the "See Also" section for useful classes and methods for
 * implementing this class.
 *
 * @see String#replaceAll(String, String)
 * @see Pattern#DOTALL
 * @see Pattern#CASE_INSENSITIVE
 * @see StringEscapeUtils#unescapeHtml4(String)
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class HtmlCleaner {

	/**
	 * Replaces all HTML tags with an empty string. For example, the html
	 * {@code A<b>B</b>C} will become {@code ABC}.
	 *
	 * <p>
	 * <em>(View this comment as HTML in the Javadoc view.)</em>
	 *
	 * @param html valid HTML 4 text
	 * @return text without any HTML tags
	 *
	 * @see String#replaceAll(String, String)
	 */
	public static String stripTags(String html) {
		// find all HTML tags and replace them with an empty string
		String regex = "<[^<>]+>";
		return html.replaceAll(regex, "");
	}

	/**
	 * Replaces all HTML 4 entities with their Unicode character equivalent or, if
	 * unrecognized, replaces the entity code with an empty string. Should also work
	 * for entities that use decimal syntax like {@code &#8211;} for the &#8211;
	 * symbol or {@code &#x2013;} for the &#x2013; symbol.
	 *
	 * <p>
	 * For example, {@code 2010&ndash;2012} will become {@code 2010–2012} and
	 * {@code &gt;&dash;x} will become {@code >x} with the unrecognized
	 * {@code &dash;} entity getting removed. (The {@code &dash;} entity is valid
	 * HTML 5, but not valid HTML 4.)
	 *
	 * <p>
	 * <em>(View this comment as HTML in the Javadoc view.)</em>
	 *
	 * @see StringEscapeUtils#unescapeHtml4(String)
	 * @see String#replaceAll(String, String)
	 *
	 * @param html valid HTML 4 text
	 * @return text with all HTML entities converted or removed
	 */
   

    public static String stripEntities(String html) {
        // CITE:
        // https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/StringEscapeUtils.html
        // handle known HTML 4 entities using StringEscapeUtils
        String unescapedHtml = StringEscapeUtils.unescapeHtml4(html);
        // remove other html entities
        // CITE: https://rows.com/tools/regex-generator
        String regex = "&[^;\\s]+;";
        unescapedHtml = unescapedHtml.replaceAll(regex, "");
        return unescapedHtml;
    }

	/**
	 * Replaces all HTML comments with an empty string. For example:
	 *
	 * <pre>
	 * A&lt;!-- B --&gt;C
	 * </pre>
	 *
	 * ...and this HTML:
	 *
	 * <pre>
	 * A&lt;!--
	 * B --&gt;C
	 * </pre>
	 *
	 * ...will both become "AC" after stripping comments.
	 *
	 * <p>
	 * <em>(View this comment as HTML in the Javadoc view.)</em>
	 *
	 * @param html valid HTML 4 text
	 * @return text without any HTML comments
	 *
	 * @see String#replaceAll(String, String)
	 */
	public static String stripComments(String html) {
		// find all HTML comments and replace them with an empty string
		// CITE:
		// https://www.tutorialspoint.com/pattern-dotall-field-in-java-with-examples
		// use DOTALL flag to handle multiline comments
		Pattern pattern = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(html);

		// return the result with leading spaces preserved
		return matcher.replaceAll("");
	}

	/**
	 * Replaces everything between the element tags and the element tags themselves
	 * with an empty string. For example, consider the html code:
	 *
	 * <pre>
	 * &lt;style type="text/css"&gt;
	 *   body { font-size: 10pt; }
	 * &lt;/style&gt;
	 * </pre>
	 *
	 * If removing the "style" element, all of the above code will be removed, and
	 * replaced with an empty string.
	 *
	 * <p>
	 * <em>(View this comment as HTML in the Javadoc view.)</em>
	 *
	 * @param html valid HTML 4 text
	 * @param name name of the HTML element (like "style" or "script")
	 * @return text without that HTML element
	 *
	 * @see String#formatted(Object...)
	 * @see String#format(String, Object...)
	 * @see String#replaceAll(String, String)
	 */
	public static String stripElement(String html, String name) {
		// CITE:
		// https://stackoverflow.com/questions/240546/remove-html-tags-from-a-string
		// CITE: https://regex101.com/r/rO8jN8/1
		String regex = "(?i)<\\s*" + name + "\\b[^>]*>.*?</\\s*" + name + "\\s*>";
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(html);

		// Remove the matched elements
		return matcher.replaceAll("");
	}
	
	/**
     * Extracts all hyperlinks using the a anchor tag and href property within the HTML content.
     *
     * @param html valid HTML 4 text
     * @return list of hyperlinks in the order they are provided on the page
     */
	 public static List<String> extractHyperlinks(String html) {
	        List<String> hyperlinks = new ArrayList<>();

	        // CITE: https://regex-generator.olafneumann.org/
	        String regex = "<a\\s+[^>]*href\\s*=\\s*\"([^\"]*)\"[^>]*>";

	        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	        Matcher matcher = pattern.matcher(html);

	        while (matcher.find()) {
	            hyperlinks.add(matcher.group(1));
	        }

	        return hyperlinks;
	    }


	/**
	 * A simple (but less efficient) approach for removing comments and certain
	 * block elements from the provided html. The block elements removed include:
	 * head, style, script, noscript, iframe, and svg.
	 *
	 * @param html valid HTML 4 text
	 * @return text clean of any comments and certain HTML block elements
	 */
	public static String stripBlockElements(String html) {
		html = stripComments(html);
		html = stripElement(html, "head");
		html = stripElement(html, "style");
		html = stripElement(html, "script");
		html = stripElement(html, "noscript");
		html = stripElement(html, "iframe");
		html = stripElement(html, "svg");
		return html;
	}

	/**
	 * Removes all HTML tags and certain block elements from the provided text.
	 *
	 * @see #stripBlockElements(String)
	 * @see #stripTags(String)
	 *
	 * @param html valid HTML 4 text
	 * @return text clean of any HTML tags and certain block elements
	 */
	public static String stripHtml(String html) {
		
		html = stripBlockElements(html);
		html = stripTags(html);
		html = stripEntities(html);
		return html;
	}
	
	/**
	 * Finds all recipes with the tag <div id="wprm-recipe-container-9068" ... <span class="cp-load-after-post"></span>
	 * Recipes should be between the tags. 

	 */
	
	public static String extractRecipe(String html) {
		// The regex to capture the content between <div id="wprm-recipe-container-9068"> and <span class="cp-load-after-post"></span>
		String regex = "(?s)<div\\s+id=\"wprm-recipe-container-9068\".*?>(.*?)<span\\s+class=\"cp-load-after-post\"></span>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(html);
		
		StringBuilder result = new StringBuilder();
		
		while (matcher.find()) {
			result.append(matcher.group(1));
		}
		
		return result.toString();
	}
	
	/**
	 * Extracts the title from a div tag with class "view3_top_tit".
	 *
	 * @param html valid HTML text
	 * @return the title text if found, otherwise an empty string
	 */
	public static String extractTitle(String html) {
		String regex = "<div\\s+class=\"view3_top_tit\">(.*?)</div>";
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(html);

		if (matcher.find()) {
			return matcher.group(1).trim();
		}

		return "";
	}
	
	  /**
     * Extracts the text sections with numbers from a div tag with class "view3_top_info".
     *
     * @param html valid HTML text
     * @return the extracted text if found, otherwise an empty string
     */
    public static String extractInfo(String html) {
        String regex = "<div\\s+class=\"view3_top_info\">(.*?)</div>";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        if (matcher.find()) {
            // Extract the content inside the div
            String content = matcher.group(1);

            // Remove the img tags and span tags, keeping only the text content
            content = content.replaceAll("<img[^>]*>", ""); // Remove img tags
            content = content.replaceAll("<span[^>]*>", "").replaceAll("</span>", ""); // Remove span tags

            // Extract text sections with numbers
            Pattern numberPattern = Pattern.compile("\\d+[^\\s]*");
            Matcher numberMatcher = numberPattern.matcher(content);

            StringBuilder result = new StringBuilder();

            while (numberMatcher.find()) {
                result.append(numberMatcher.group()).append(" ");
            }

            return result.toString().trim();
        }

        return "";
    }
    
    /**
     * Extracts ingredient names and their quantities from the given HTML.
     *
     * @param html valid HTML text
     * @return a string containing the extracted ingredients and quantities
     */
    public static String extractIngredients(String html) {
        StringBuilder result = new StringBuilder();
        String regex = "<div class=\"ingre_list_name\">\\s*<a [^>]+>([^<]+)</a>\\s*</div>\\s*<span class=\"ingre_list_ea\">([^<]+)</span>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            String ingredient = matcher.group(1).trim();
            String quantity = matcher.group(2).trim();
            result.append(ingredient).append(" ").append(quantity).append("\n");
        }

        return result.toString().trim();
    }


    /**
     * Extracts the steps and images from the given HTML.
     *
     * @param html valid HTML text
     * @return a string containing the extracted steps and image URLs
     */
    public static String extractSteps(String html) {
        int startIndex = html.indexOf("class=\"step_list st_thumb\"");
        int endIndex = html.indexOf("class=\"reply_mn_tab\"");
        if (startIndex != -1 && endIndex != -1) {
            html = html.substring(startIndex, endIndex);
        }

        StringBuilder result = new StringBuilder();
        String regexStep = "<li[^>]*>\\s*<div class=\"step_list_num\">.*?</div>\\s*<div class=\"step_list_txt\">\\s*<div class=\"step_list_txt_cont\">(.*?)</div>";

        Pattern patternStep = Pattern.compile(regexStep, Pattern.DOTALL);
        Matcher matcherStep = patternStep.matcher(html);

        int stepNumber = 1;
        List<String> steps = new ArrayList<>();

        // Extract steps
        while (matcherStep.find()) {
            String stepText = matcherStep.group(1).trim();
            stepText = stepText.replaceAll("<br\\s*/?>", "\n"); // Replace <br> tags with newlines

            StringBuilder stepBuilder = new StringBuilder();
            stepBuilder.append("Step ").append(stepNumber).append(":\n").append(stepText);

            String stepWithLinks = stepBuilder.toString();
            stepWithLinks = stripHtml(stepWithLinks); // Strip remaining HTML tags

            steps.add(stepWithLinks); // Store each step
            stepNumber++;
        }

        // Extract images
        List<String> imageUrls = findImages(html);
        for (int i = 0; i < steps.size(); i++) {
            result.append(steps.get(i));
            if (i < imageUrls.size()) {
                result.append("\nLink: ").append(imageUrls.get(i));
            }
            result.append("\n\n"); // Add extra newline for step separation
        }

        System.out.println("Extracted steps:\n" + result.toString());

        return result.toString().trim();
    }

    

    /**
     * Finds all image URLs on the given HTML page.
     *
     * @param html valid HTML text
     * @return a list of image URLs found in the HTML
     */
    public static List<String> findImages(String html) {
        List<String> imageUrls = new ArrayList<>();
        String regexImage = "<img[^>]*src=[\"']([^\"']*)[\"'][^>]*>";

        Pattern patternImage = Pattern.compile(regexImage, Pattern.DOTALL);
        Matcher matcherImage = patternImage.matcher(html);

        while (matcherImage.find()) {
            String imageUrl = matcherImage.group(1).trim();
            imageUrls.add(imageUrl);
            System.out.println("Found image: " + imageUrl); // Debug: Print found image URL
        }

        return imageUrls;
    }

}
