package com.pltracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchEvent implements Serializable {

    public enum EventType {
        MATCH_STARTED,
        MATCH_FINISHED
    }

    private int matchId;
    private String homeTeam;
    private String awayTeam;
    private int homeScore;
    private int awayScore;
    private int matchday;
    private EventType eventType;
}