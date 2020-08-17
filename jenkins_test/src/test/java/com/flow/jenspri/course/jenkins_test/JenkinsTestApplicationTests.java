package com.flow.jenspri.course.jenkins_test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(classes = {JenkinsTestApplication.class},webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EnableAutoConfiguration
class JenkinsTestApplicationTests {

    private static Log logger = LogFactory.getLog(JenkinsTestApplicationTests.class);

    @LocalServerPort
    private int port = 1234;

    @Test
    public void chatEndpoint() throws Exception {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(
                ClientConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
                .properties(
                        "websocket.uri:ws://localhost:" + this.port + "/chat/websocket")
                .run("--spring.main.web_environment=false");
        long count = context.getBean(ClientConfiguration.class).latch.getCount();
        AtomicReference<String> messagePayloadReference = context
                .getBean(ClientConfiguration.class).messagePayload;
        context.close();
        assertThat(count, equalTo(0L));
        assertThat(messagePayloadReference.get(),
                containsString("{\"message\":\"test\",\"author\":\"test\",\"time\":"));
    }

    @EnableAutoConfiguration
    static class ClientConfiguration implements CommandLineRunner {

        @Value("${websocket.uri}")
        private String webSocketUri;

        private final CountDownLatch latch = new CountDownLatch(1);

        private final AtomicReference<String> messagePayload = new AtomicReference<String>();

        @Override
        public void run(String... args) throws Exception {
            logger.info("Waiting for response: latch=" + this.latch.getCount());
            if (this.latch.await(10, TimeUnit.SECONDS)) {
                logger.info("Got response: " + this.messagePayload.get());
            }
            else {
                logger.info("Response not received: latch=" + this.latch.getCount());
            }
        }

        @Bean
        public WebSocketConnectionManager wsConnectionManager() {
            WebSocketConnectionManager manager = new WebSocketConnectionManager(client(),
                    handler(), this.webSocketUri);
            manager.setAutoStartup(true);
            return manager;
        }

        @Bean
        public StandardWebSocketClient client() {
            return new StandardWebSocketClient();
        }

        @Bean
        public TextWebSocketHandler handler() {
            return new TextWebSocketHandler() {

                @Override
                public void afterConnectionEstablished(WebSocketSession session)
                        throws Exception {
                    session.sendMessage(new TextMessage(
                            "{\"author\":\"test\",\"message\":\"test\"}"));
                }

                @Override
                protected void handleTextMessage(WebSocketSession session,
                                                 TextMessage message) throws Exception {
                    logger.info("Received: " + message + " ("
                            + ClientConfiguration.this.latch.getCount() + ")");
                    session.close();
                    ClientConfiguration.this.messagePayload.set(message.getPayload());
                    ClientConfiguration.this.latch.countDown();
                }
            };
        }
    }

//    @Test
//    void contextLoads() {
//    }

}
