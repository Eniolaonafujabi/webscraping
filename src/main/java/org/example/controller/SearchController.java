package org.example.controller;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.example.dto.request.SearchRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@CrossOrigin("*")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.api.cx}")
    private String cx;

    @PostMapping("/search")
    public List<Map<String, Object>> search(@RequestBody SearchRequest searchRequest) {

        log.info("search request dto: {}", searchRequest);
        System.out.println(searchRequest.toString());

        StringBuilder queryBuilder = new StringBuilder(searchRequest.getOccupation());
        if (searchRequest.getCity() != null) queryBuilder.append(" in ").append(searchRequest.getCity());
        if (searchRequest.getCountry() != null) queryBuilder.append(" ").append(searchRequest.getCountry());
        if (searchRequest.getKeyword() != null) queryBuilder.append(" ").append(searchRequest.getKeyword());

        String query = queryBuilder.toString();

        List<Map<String, Object>> results = new ArrayList<>();
        String googleSearchUrl = "https://www.googleapis.com/customsearch/v1";

        HttpResponse<JsonNode> response = Unirest.get(googleSearchUrl)
                .queryString("key", apiKey)
                .queryString("cx", cx)
                .queryString("q", query)
                .asJson();

        if (response.getStatus() == 200) {
            var items = response.getBody().getObject().getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                var item = items.getJSONObject(i);
                String title = item.getString("title");
                String link = item.getString("link");

                Set<String> emails = scrapeEmails(link);

                Map<String, Object> result = new HashMap<>();
                result.put("title", title);
                result.put("url", link);
                result.put("emails", new ArrayList<>(emails)); // Convert Set to List

                results.add(result);
            }
        }

        return results;
    }

    private Set<String> scrapeEmails(String url) {
        Set<String> emails = new HashSet<>();
        try {
            Document document = Jsoup.connect(url).get();
            String text = document.text();

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
