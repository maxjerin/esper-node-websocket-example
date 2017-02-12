package com.gravlle.finance.core;

import com.espertech.esper.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ClientEndpoint
public class EsperStart {
    public EPRuntime epRuntime;
    final static CountDownLatch messageLatch = new CountDownLatch(1);
    public ObjectMapper mapper = new ObjectMapper();

    public static void main(String args[]) {
        new EsperStart().init();
    }

    public void init() {
        Configuration config = new Configuration();
        config.addEventType("MarketData", MarketData.class.getName());
        EPServiceProvider epEngine = EPServiceProviderManager.getProvider("MarketData", config);
        EPAdministrator epAdministrator = epEngine.getEPAdministrator();
        epRuntime = epEngine.getEPRuntime();

        EPStatement marketDataEPL = epAdministrator.createEPL("select * from MarketData.win:time(60 sec) " +
                "where price > 5");
        marketDataEPL.setSubscriber(new MarketDataSubscriber());

        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String uri = "ws://localhost:8080/";
            System.out.println("Connecting to " + uri);
            container.connectToServer(EsperStart.class, URI.create(uri));
            messageLatch.await(100, TimeUnit.SECONDS);

        } catch (DeploymentException | IOException ex) {
            Logger.getLogger(EsperStart.class.getName()).log(null, ex);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to endpoint: " + session.getBasicRemote());
        try {
            String name = "start";
            System.out.println("Sending message to endpoint: " + name);
            session.getBasicRemote().sendText(name);
        } catch (IOException ex) {
            Logger.getLogger(EsperStart.class.getName()).log(null, ex);
        }
    }

    @OnMessage
    public void processMessage(String message) {
        System.out.println("Received message in client: " + message);
        try {
            epRuntime.sendEvent(mapper.readValue(message, MarketData.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        EsperStart.messageLatch.countDown();
    }

    @OnError
    public void processError(Throwable t) {
        t.printStackTrace();
    }
}
