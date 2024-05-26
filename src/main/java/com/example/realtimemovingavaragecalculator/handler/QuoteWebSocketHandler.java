package com.example.realtimemovingavaragecalculator.handler;

import com.example.realtimemovingavaragecalculator.config.AppConfig;
import com.example.realtimemovingavaragecalculator.model.Quote;
import com.example.realtimemovingavaragecalculator.runnable.MovingAverageCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class QuoteWebSocketHandler extends TextWebSocketHandler {

    private final Integer windowSize;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MovingAverageCalculator calculator;
    private ExecutorService executorService;

    @Autowired
    public QuoteWebSocketHandler(AppConfig appConfig) {
        this.windowSize = appConfig.getWindowSize();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        calculator = new MovingAverageCalculator(windowSize);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(calculator);
        System.out.println("Connection established: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        System.out.println("Connection closed: " + session.getId());
        super.afterConnectionClosed(session, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Quote quote = new Quote(objectMapper.readValue(message.getPayload(), Quote.class).getPrice(),System.currentTimeMillis());
            calculator.addPrice(quote);
            double average = calculator.getAverage();
            List<Double> prices = calculator.getPrices();

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("average_price", average);
            responseMap.put("prices", prices);

            String response = objectMapper.writeValueAsString(responseMap);
            session.sendMessage(new TextMessage(response));
        } catch (Exception e) {
            session.sendMessage(new TextMessage("{\"error\": \"Invalid message format\"}"));
        }
    }
}
