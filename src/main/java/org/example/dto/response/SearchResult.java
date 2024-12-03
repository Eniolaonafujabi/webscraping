package org.example.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class SearchResult {
    private String name;
    private String email;
    private String company;
    private String location;

    public SearchResult(String name, String email, String company, String location) {
        this.name = name != null ? name : "N/A";
        this.email = email != null ? email : "N/A";
        this.company = company != null ? company : "N/A";
        this.location = location != null ? location : "N/A";
    }
}
