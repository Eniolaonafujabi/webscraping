package org.example.controller;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.example.dto.response.SearchResult;
import org.example.dto.request.SearchRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class SearchController {

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.api.cx}")
    private String cx;

    @GetMapping("/search")
    public List<SearchResult> search(@RequestBody SearchRequest searchRequest) {

        // Build search query
        StringBuilder queryBuilder = new StringBuilder(searchRequest.getOccupation());
        if (searchRequest.getCity() != null) queryBuilder.append(" in ").append(searchRequest.getCity());
        if (searchRequest.getCountry() != null) queryBuilder.append(" ").append(searchRequest.getCountry());
        if (searchRequest.getFileType() != null) {
            queryBuilder.append(" filetype:").append(searchRequest.getFileType());
        }
        String query = queryBuilder.toString();
        List<SearchResult> results = new ArrayList<>();

        String googleSearchUrl = "https://www.googleapis.com/customsearch/v1";

        // Perform Google search using API
        HttpResponse<JsonNode> response = Unirest.get(googleSearchUrl)
                .queryString("key", apiKey)
                .queryString("cx", cx)
                .queryString("q", query)
                .queryString("fileType", searchRequest.getFileType())
                .asJson();

        if (response.getStatus() == 200) {
            var items = response.getBody().getObject().getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                var item = items.getJSONObject(i);
                String title = item.getString("title");
                String link = item.getString("link");

                // Scrape emails from the link
                Set<String> emails = scrapeEmails(link);

                // Add result to the list
//                results.add(new SearchResult(title, link));
            }
        }

        return results;
    }

    private Set<String> scrapeEmails(String url) {
        Set<String> emails = new HashSet<>();
        try {
            Document document = Jsoup.connect(url).get();
            String text = document.text();

            // Regex for email extraction
            Pattern emailPattern = Pattern.compile(
                    "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
            Matcher matcher = emailPattern.matcher(text);

            while (matcher.find()) {
                emails.add(matcher.group());
            }
        } catch (IOException e) {
            System.err.println("Error scraping URL: " + url);
        }
        return emails;
    }
}
