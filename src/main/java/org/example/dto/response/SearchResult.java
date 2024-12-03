package org.example.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class SearchResult {
    private String title;
    private String url;
    private Set<String> emails;

    public SearchResult(String title, String url, Set<String> emails) {
        this.title = title;
        this.url = url;
        this.emails = emails;
    }
}
