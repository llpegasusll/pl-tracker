package com.pltracker.producer;

import com.pltracker.config.ActiveMQConfig;
import com.pltracker.model.MatchEvent;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class MatchEventProducer {

    private final JmsTemplate jmsTemplate;

    // Constructor injection
    public MatchEventProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    // Send MATCH_STARTED event to AMQ queue
    public void sendMatchStarted(MatchEvent event) {
        event.setEventType(MatchEvent.EventType.MATCH_STARTED);
        jmsTemplate.convertAndSend(
            ActiveMQConfig.MATCH_STARTED_QUEUE, event
        );
        System.out.println("AMQ → match.started: "
            + event.getHomeTeam() + " vs " + event.getAwayTeam());
    }

    // Send MATCH_FINISHED event to AMQ queue
    public void sendMatchFinished(MatchEvent event) {
        event.setEventType(MatchEvent.EventType.MATCH_FINISHED);
        jmsTemplate.convertAndSend(
            ActiveMQConfig.MATCH_FINISHED_QUEUE, event
        );
        System.out.println("AMQ → match.finished: "
            + event.getHomeTeam() + " vs " + event.getAwayTeam()
            + " (" + event.getHomeScore() + "-" + event.getAwayScore() + ")");
    }
}