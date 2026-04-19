package com.pltracker.service;

import com.pltracker.model.MatchEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FootballApiService {

    @Value("${football.api.key}")
    private String apiKey;

    @Value("${football.api.url}")
    private String apiUrl;

    // Premier League competition code on football-data.org
    private static final String PL_CODE = "PL";

    private final RestTemplate restTemplate = new RestTemplate();

    // Fetch current standings from API
    public List<Map<String, Object>> fetchStandings() {
        String url = apiUrl + "/competitions/" + PL_CODE + "/standings";
        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class
            );

            Map body = response.getBody();
            if (body == null) return new ArrayList<>();

            // Navigate JSON: standings[0].table
            List<Map> standings = (List<Map>) body.get("standings");
            Map firstStanding = standings.get(0);
            return (List<Map<String, Object>>) firstStanding.get("table");

        } catch (Exception e) {
            System.err.println("API call failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Fetch matches for current matchday
    public List<Map<String, Object>> fetchCurrentMatchday() {
        String url = apiUrl + "/competitions/" + PL_CODE + "/matches?status=IN_PLAY,FINISHED";
        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class
            );

            Map body = response.getBody();
            if (body == null) return new ArrayList<>();

            return (List<Map<String, Object>>) body.get("matches");

        } catch (Exception e) {
            System.err.println("Matchday fetch failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Build headers with API key
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);
        return headers;
    }
}