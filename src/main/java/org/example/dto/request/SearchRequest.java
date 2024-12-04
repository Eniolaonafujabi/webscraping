package org.example.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchRequest {
    private String occupation;
    private String city;
    private String country;
    private String keyword;
}
