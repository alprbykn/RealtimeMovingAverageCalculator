package com.example.realtimemovingavaragecalculator.runnable;

import com.example.realtimemovingavaragecalculator.model.Quote;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MovingAverageCalculator implements Runnable {
    private final long windowSize;
    private final Queue<Quote> quotes;
    private double sum;

    public MovingAverageCalculator(long windowSize) {
        this.windowSize = windowSize;
        this.quotes = new LinkedList<>();
        this.sum = 0;
    }

    public synchronized void addPrice(Quote quote) {
        quotes.add(quote);
        sum += quote.getPrice();

        // Remove outdated values
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - windowSize;
        while (!quotes.isEmpty() && quotes.peek().getTimestamp() <= startTime) {
            Quote removedQuote = quotes.poll();
            sum -= removedQuote.getPrice();
        }
    }

    public synchronized double getAverage() {
        if (quotes.isEmpty()) {
            return 0;
        }
        return sum / quotes.size();
    }

    public synchronized List<Double> getPrices() {
        List<Double> prices = new LinkedList<>();
        for (Quote quote : quotes) {
            prices.add(quote.getPrice());
        }
        return prices;
    }

    @Override
    public void run() {
    }
}
