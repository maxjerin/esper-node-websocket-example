package com.gravlle.finance.core;

public class MarketDataSubscriber {

    public void update(MarketData marketData) {
        System.out.println("Subscriber was called");
    }
}
