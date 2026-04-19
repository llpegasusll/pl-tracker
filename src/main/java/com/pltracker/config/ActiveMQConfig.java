package com.pltracker.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
public class ActiveMQConfig {

    // Reads from application.properties
    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.password}")
    private String password;

    // Queue names — used by producer and consumer
    public static final String MATCH_STARTED_QUEUE  = "match.started";
    public static final String MATCH_FINISHED_QUEUE = "match.finished";

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory =
            new ActiveMQConnectionFactory(username, password, brokerUrl);

        // Trust our MatchEvent class for serialization
        factory.setTrustedPackages(java.util.List.of("com.pltracker.model"));
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate template = new JmsTemplate(connectionFactory());
        return template;
    }
}