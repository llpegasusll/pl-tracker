package com.pltracker.service;

import com.pltracker.config.ActiveMQConfig;
import com.pltracker.model.MatchEvent;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final StandingsService standingsService;

    // Constructor injection
    public NotificationService(StandingsService standingsService) {
        this.standingsService = standingsService;
    }

    // Listens to "match.started" queue
    @JmsListener(destination = ActiveMQConfig.MATCH_STARTED_QUEUE)
    public void onMatchStarted(MatchEvent event) {
        System.out.println("=================================");
        System.out.println("MATCH STARTED!");
        System.out.println(event.getHomeTeam() 
            + " vs " + event.getAwayTeam());
        System.out.println("Matchday: " + event.getMatchday());
        System.out.println("=================================");
    }

    // Listens to "match.finished" queue
    @JmsListener(destination = ActiveMQConfig.MATCH_FINISHED_QUEUE)
    public void onMatchFinished(MatchEvent event) {
        System.out.println("=================================");
        System.out.println("MATCH FINISHED!");
        System.out.println(event.getHomeTeam() 
            + " vs "   + event.getAwayTeam());
        System.out.println("Score: " 
            + event.getHomeScore() 
            + " - "    + event.getAwayScore());
        System.out.println("=================================");

        // Recalculate standings after match finishes
        System.out.println("Recalculating standings...");
        standingsService.recalculateStandings();
        System.out.println("Standings updated!");
    }
}