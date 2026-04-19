package com.pltracker.controller;

import com.pltracker.model.TeamStats;
import com.pltracker.service.StandingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/league")
@CrossOrigin(origins = "*")
public class LeagueController {

    private final StandingsService standingsService;

    // Constructor injection
    public LeagueController(StandingsService standingsService) {
        this.standingsService = standingsService;
    }

    // GET /api/league/table → full PL table
    @GetMapping("/table")
    public ResponseEntity<List<TeamStats>> getTable() {
        List<TeamStats> standings = standingsService.getLatestStandings();
        if (standings.isEmpty()) {
            return ResponseEntity.status(503).build();
        }
        return ResponseEntity.ok(standings);
    }

    // GET /api/league/table/{position} → single team
    @GetMapping("/table/{position}")
    public ResponseEntity<TeamStats> getTeamByPosition(
            @PathVariable int position) {
        return standingsService.getLatestStandings().stream()
                .filter(t -> t.getPosition() == position)
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/league/table/relegation → only bottom 3
    @GetMapping("/table/relegation")
    public ResponseEntity<List<TeamStats>> getRelegationZone() {
        List<TeamStats> zone = standingsService.getLatestStandings()
                .stream()
                .filter(t -> t.getPosition() >= 18)
                .toList();
        return ResponseEntity.ok(zone);
    }

    // POST /api/league/refresh → manually trigger recalculation
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh() {
        standingsService.recalculateStandings();
        return ResponseEntity.ok("Standings refreshed successfully!");
    }
}