package com.pltracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamStats {
    private int position;
    private String team;
    private int played;
    private int won;
    private int drawn;
    private int lost;
    private int points;
    private double titleChance;
    private double top4Chance;
    private double relegationRisk;
    private String status;
}