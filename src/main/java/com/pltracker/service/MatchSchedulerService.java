package com.pltracker.service;

import com.pltracker.model.MatchEvent;
import com.pltracker.producer.MatchEventProducer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@EnableScheduling
public class MatchSchedulerService {

    private final FootballApiService footballApiService;
    private final MatchEventProducer matchEventProducer;

    // Track already-notified matches to avoid duplicate events
    private final Set<Integer> notifiedStarted  = new HashSet<>();
    private final Set<Integer> notifiedFinished = new HashSet<>();

    // Constructor injection
    public MatchSchedulerService(
            FootballApiService footballApiService,
            MatchEventProducer matchEventProducer) {
        this.footballApiService = footballApiService;
        this.matchEventProducer = matchEventProducer;
    }

    // Runs every 60 seconds
    @Scheduled(fixedRate = 300000)
    public void pollMatchday() {
        System.out.println("Polling matchday...");
        List<Map<String, Object>> matches =
            footballApiService.fetchCurrentMatchday();

        if (matches == null || matches.isEmpty()) {
            System.out.println("No matches found.");
            return;
        }

        for (Map<String, Object> match : matches) {
            int matchId  = (int) match.get("id");
            String status = match.get("status").toString();

            // Extract team names
            Map<String, Object> homeTeamMap =
                (Map<String, Object>) match.get("homeTeam");
            Map<String, Object> awayTeamMap =
                (Map<String, Object>) match.get("awayTeam");
            String homeTeam = homeTeamMap.get("name").toString();
            String awayTeam = awayTeamMap.get("name").toString();

            // Extract matchday
            int matchday = match.get("matchday") != null
                ? (int) match.get("matchday") : 0;

            // Extract scores
            Map<String, Object> score =
                (Map<String, Object>) match.get("score");
            Map<String, Object> fullTime =
                (Map<String, Object>) score.get("fullTime");
            int homeScore = fullTime.get("home") != null
                ? (int) fullTime.get("home") : 0;
            int awayScore = fullTime.get("away") != null
                ? (int) fullTime.get("away") : 0;

            // Build event object
            MatchEvent event = new MatchEvent(
                matchId, homeTeam, awayTeam,
                homeScore, awayScore, matchday, null
            );

            // Detect match IN_PLAY → fire match.started
            if (status.equals("IN_PLAY")
                    && !notifiedStarted.contains(matchId)) {
                matchEventProducer.sendMatchStarted(event);
                notifiedStarted.add(matchId);
            }

            // Detect match FINISHED → fire match.finished
            if (status.equals("FINISHED")
                    && !notifiedFinished.contains(matchId)) {
                matchEventProducer.sendMatchFinished(event);
                notifiedFinished.add(matchId);
            }
        }
    }
}