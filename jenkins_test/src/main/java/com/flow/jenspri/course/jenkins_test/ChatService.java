package com.flow.jenspri.course.jenkins_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@ManagedService(path = "/chat")
public class ChatService {

    private final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Ready
    public void onReady(final AtmosphereResource resource) {
        this.logger.info("connected : {}", resource.uuid());
    }

    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        this.logger.info("Client {} disconnected [{}]", event.getResource().uuid(),
                (event.isCancelled() ? "cancelled" : "closed"));
    }

    @org.atmosphere.config.service.Message(encoders = JacksonEncoderDecoder.class, decoders = JacksonEncoderDecoder.class)
    public Message onMessage(Message message) throws IOException {
        this.logger.info("Author {} sent message {}", message.getAuthor(), message.getMessage());
        return message;
    }

    public static class JacksonEncoderDecoder
            implements Encoder<Message, String>, Decoder<String, Message> {

        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String encode(Message m) {
            try {
                return this.mapper.writeValueAsString(m);
            }
            catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public Message decode(String s) {
            try {
                return this.mapper.readValue(s, Message.class);
            }
            catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

    }

}
