package com.pltracker.service;

import com.pltracker.model.TeamStats;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StandingsService {

    private final FootballApiService footballApiService;
    private List<TeamStats> latestStandings = new ArrayList<>();

    private static final int TOTAL_GAMES     = 38;
    private static final int RELEGATION_ZONE = 18;
    private static final int TOP_4_ZONE      = 4;

    public StandingsService(FootballApiService footballApiService) {
        this.footballApiService = footballApiService;
    }

    public void recalculateStandings() {
        List<Map<String, Object>> table = footballApiService.fetchStandings();
        if (table == null || table.isEmpty()) return;

        List<TeamStats> updated = new ArrayList<>();

        int leaderPoints = getPoints(table.get(0));
        int safetyPoints = getPoints(table.get(RELEGATION_ZONE - 1));
        int top4Points   = getPoints(table.get(TOP_4_ZONE - 1));

        // Total games remaining for leader (used for title elimination check)
        int leaderPlayed   = (int) table.get(0).get("playedGames");
        int leaderGamesLeft = TOTAL_GAMES - leaderPlayed;

        for (Map<String, Object> row : table) {
            int position = (int) row.get("position");
            String team  = ((Map<String, Object>) row.get("team"))
                            .get("name").toString();
            int played   = (int) row.get("playedGames");
            int won      = (int) row.get("won");
            int drawn    = (int) row.get("draw");
            int lost     = (int) row.get("lost");
            int points   = (int) row.get("points");

            int gamesLeft = TOTAL_GAMES - played;
            int maxPoints = points + (gamesLeft * 3);

            double titleChance = calculateTitleChance(
                points, leaderPoints, leaderGamesLeft,
                maxPoints, position, gamesLeft
            );

            double top4Chance = calculateTop4Chance(
                points, top4Points, maxPoints, position, gamesLeft
            );

            double relegationRisk = calculateRelegationRisk(
                points, safetyPoints, gamesLeft, position
            );

            String status = calculateStatus(
                points, safetyPoints, maxPoints,
                position, gamesLeft
            );

            updated.add(new TeamStats(
                position, team, played, won, drawn, lost,
                points, titleChance, top4Chance,
                relegationRisk, status
            ));
        }

        this.latestStandings = updated;
        System.out.println("Standings recalculated — "
            + updated.size() + " teams.");
    }

    // Just returns cached data — no API call
    public List<TeamStats> getLatestStandings() {
        if (latestStandings.isEmpty()) {
            recalculateStandings();
        }
        return latestStandings;
    }

    // ── Title chance ──────────────────────────────────────────
    private double calculateTitleChance(
            int points, int leaderPoints, int leaderGamesLeft,
            int maxPoints, int position, int gamesLeft) {

        // Mathematically eliminated —
        // can't reach leader's minimum possible points
        int leaderMinPoints = leaderPoints; // leader can draw/lose remaining
        if (maxPoints < leaderPoints) return 0.0;

        // Leader — how safe is their lead?
        if (position == 1) {
            // Check if they can be caught
            // 2nd place max points
            return 90.0;
        }

        int gap = leaderPoints - points;

        // Gap too large even with games remaining
        if (gap > gamesLeft * 3) return 0.0;

        // Tight title race
        if (gap == 0)  return 45.0;
        if (gap <= 3)  return 30.0;
        if (gap <= 6)  return 15.0;
        if (gap <= 9)  return 5.0;
        if (gap <= 12) return 1.0;
        return 0.0;
    }

    // ── Top 4 chance ──────────────────────────────────────────
    private double calculateTop4Chance(
            int points, int top4Points,
            int maxPoints, int position, int gamesLeft) {

        // Mathematically cannot reach top 4
        if (position > 4 && maxPoints < top4Points) return 0.0;

        // Already in top 4 — how secure?
        if (position <= 4) {
            int gap = points - top4Points;
            // Already 4th — tight
            if (position == 4 && gap == 0) return 55.0;
            if (position == 4 && gap <= 3) return 65.0;
            if (position == 4)             return 80.0;
            // Position 1-3 — more secure
            if (position == 3) return 85.0;
            if (position == 2) return 90.0;
            if (position == 1) return 95.0;
        }

        // Outside top 4 — gap based
        int gap = top4Points - points;
        if (gap <= gamesLeft * 3) {
            if (gap <= 3)  return 40.0;
            if (gap <= 6)  return 20.0;
            if (gap <= 9)  return 8.0;
            if (gap <= 12) return 2.0;
        }
        return 0.0;
    }

    // ── Relegation risk ───────────────────────────────────────
    private double calculateRelegationRisk(
            int points, int safetyPoints,
            int gamesLeft, int position) {

        int maxPoints = points + (gamesLeft * 3);

        // Mathematically safe — even safety line can't catch them
        if (points > safetyPoints + (gamesLeft * 3)) return 0.0;

        // Mathematically relegated — can't reach safety line
        if (maxPoints < safetyPoints) return 100.0;

        // Safe positions — very low risk
        if (position <= 13) return 0.0;
        if (position == 14) return 0.5;
        if (position == 15) return 5.0;
        if (position == 16) return 15.0;
        if (position == 17) return 35.0;

        // In relegation zone (18-20) — gap based
        int gap = safetyPoints - points;

        if (position == 18) {
            // Right on the line
            if (gap == 0)  return 55.0;
            if (gap <= 3)  return 65.0;
            if (gap <= 6)  return 75.0;
            if (gap <= 9)  return 85.0;
            return 92.0;
        }

        if (position == 19) {
            if (gap <= 3)  return 75.0;
            if (gap <= 6)  return 83.0;
            if (gap <= 9)  return 90.0;
            return 95.0;
        }

        if (position == 20) {
            if (gap <= 3)  return 85.0;
            if (gap <= 6)  return 90.0;
            if (gap <= 9)  return 95.0;
            return 98.0;
        }

        return 95.0;
    }

    // ── Status label ──────────────────────────────────────────
    private String calculateStatus(
            int points, int safetyPoints,
            int maxPoints, int position, int gamesLeft) {

        // Mathematically relegated
        if (maxPoints < safetyPoints) return "RELEGATED";

        // Mathematically safe
        if (points > safetyPoints + (gamesLeft * 3)) return "SAFE";

        // Champions (end of season only)
        if (gamesLeft == 0 && position == 1) return "CHAMPIONS";

        if (position <= 4)  return "CHAMPIONS LEAGUE";
        if (position >= 18) return "RELEGATION ZONE";
        if (position >= 16) return "DANGER ZONE";
        return "MID TABLE";
    }

    private int getPoints(Map<String, Object> row) {
        return (int) row.get("points");
    }
}