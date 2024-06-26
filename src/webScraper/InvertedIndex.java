package webScraper;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;

/**
 * Class to handle creation and modification of inverted index and HTML content
 */
public class InvertedIndex {

    private final TreeMap<String, Map<String, Object>> invertedIndex;

    public InvertedIndex() {
        this.invertedIndex = new TreeMap<>();
    }

    public Map<String, Map<String, Object>> getHtmlContentMap() {
        return Collections.unmodifiableMap(this.invertedIndex);
    }

    public boolean hasLocation(String location) {
        return this.invertedIndex.containsKey(location);
    }

    public Map<String, Object> getHtmlContent(String location) {
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
        String title = HtmlCleaner.extractTitle(html);
        String info = HtmlCleaner.extractInfo(html); // assuming it includes serving size and time
        String ingredients = HtmlCleaner.extractIngredients(html);
        String steps = HtmlCleaner.extractSteps(html);

        Map<String, Object> recipeJson = new HashMap<>();
        recipeJson.put("title", title);

        // Assuming info contains serving size and time in a specific format
        String[] infoParts = info.split(" ");
        if (infoParts.length >= 2) {
            recipeJson.put("serving_size", infoParts[0]);
            recipeJson.put("time", infoParts[1]);
        } else {
            recipeJson.put("serving_size", "");
            recipeJson.put("time", "");
        }

        // Add ingredients as a list
        List<String> ingredientsList = new ArrayList<>();
        for (String ingredient : ingredients.split("\n")) {
            ingredientsList.add(ingredient);
        }
        recipeJson.put("ingredients", ingredientsList);

        // Add steps as a list of maps
        List<Map<String, String>> stepsList = new ArrayList<>();
        String[] stepsArray = steps.split("Step ");
        for (int i = 1; i < stepsArray.length; i++) {
            String step = stepsArray[i];
            String[] parts = step.split("\n");
            if (parts.length >= 2 && !parts[1].trim().isEmpty()) {
                Map<String, String> stepMap = new HashMap<>();
                stepMap.put("step", "Step " + parts[0].trim());
                stepMap.put("description", parts[1].trim());

                if (parts.length >= 3 && parts[2].contains("Link:")) {
                    stepMap.put("link", parts[2].split("Link:")[1].trim());
                } else {
                    stepMap.put("link", "");
                }

                stepsList.add(stepMap);
            }
        }
        recipeJson.put("steps", stepsList);

        invertedIndex.put(uri.toString(), recipeJson);
    }
}