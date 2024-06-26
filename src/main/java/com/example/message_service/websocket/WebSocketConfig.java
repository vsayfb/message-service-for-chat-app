package com.example.message_service.websocket;

import com.example.message_service.websocket.interceptor.AuthenticationInterceptor;
import com.example.message_service.websocket.interceptor.SubscriptionInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final SubscriptionInterceptor subscriptionInterceptor;
    private final AuthenticationInterceptor authenticationInterceptor;

    @Value("${stomp.broker.host:localhost}")
    private String host;

    public WebSocketConfig(
            SubscriptionInterceptor subscriptionInterceptor,
            AuthenticationInterceptor authenticationInterceptor) {
        this.subscriptionInterceptor = subscriptionInterceptor;
        this.authenticationInterceptor = authenticationInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("rooms").setAllowedOrigins("*");

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authenticationInterceptor, subscriptionInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.setApplicationDestinationPrefixes("/messages")
                .enableStompBrokerRelay("/topic")
                .setRelayHost(host)
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest");
    }
}
