package com.example.message_service.websocket;

import com.example.message_service.websocket.interceptor.AuthorizationInterceptor;
import com.example.message_service.websocket.interceptor.SubscriptionInterceptor;
import com.example.message_service.websocket.interceptor.UnsubscribingInterceptor;

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
    private final AuthorizationInterceptor authorizationInterceptor;
    private final UnsubscribingInterceptor unsubscribingInterceptor;

    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUser;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;

    public WebSocketConfig(
            SubscriptionInterceptor subscriptionInterceptor,
            AuthorizationInterceptor authorizationInterceptor,
            UnsubscribingInterceptor unsubscribingInterceptor) {
        this.subscriptionInterceptor = subscriptionInterceptor;
        this.authorizationInterceptor = authorizationInterceptor;
        this.unsubscribingInterceptor = unsubscribingInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("rooms").setAllowedOrigins("*");

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authorizationInterceptor, subscriptionInterceptor, unsubscribingInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.setApplicationDestinationPrefixes("/messages")
                .enableStompBrokerRelay("/topic")
                .setRelayHost(rabbitHost)
                .setSystemLogin(rabbitUser)
                .setSystemPasscode(rabbitPassword)
                .setClientLogin(rabbitUser)
                .setClientPasscode(rabbitPassword);
    }
}
