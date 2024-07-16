# Message Service For Chat Application

This microservice is part of the _[real-time chat application](https://github.com/vsayfb/real-time-chat-application)._

### Description

This microservice provides a WebSocket endpoint for users to join chat rooms and send messages.

The WebSocket connection endpoint exposed at _ws://api-gateway/websocket/_ .

### Subscribing To Topics

For simplicity, a WebSocket session can only be associated with only one subscription.

To subscribe to a topic, a **SUBSCRIBE** frame must be sent that contains a valid destination (that is a room id). For example _/topic/room-id_. IIf a room with the specified room ID does not exist, the subscription will be discarded.

If the subscription frame **contains a valid JWT** (signed by [authentication server](https://github.com/vsayfb/authentication-service-for-chat-app)) in **Authorization** header, then this subsription will be registered as **MEMBER** and the user **will be able to send and receive messages** in that subscription. If it does not contain a valid JWT, the subscription will be registered as **GUEST**, allowing the user to **read messages but not send them**.

After successfull subscription, this subscription handles three type of messages. These are;

- STANDARD : Simple plain text messages from chat members.
- JOIN : Published after a user joins the room.
- LEAVE: Published after a user leaves the room.

For more information, see [RoomMessage](<[https://](https://github.com/vsayfb/message-service-for-chat-app/blob/master/src/main/java/com/example/message_service/dto/RoomMessage.java)>).

### Unsubscribing from Topics

To unsubscribe from a topic, an **UNSUBSCRIBE** frame must be sent. Once the server receives the frame, the member will be removed from the chat room.

If the server does not receive the **UNSUBSCRIBE** frame, the Spring Boot [SessionDisconnectHandler](<[https://](https://github.com/vsayfb/message-service-for-chat-app/blob/master/src/main/java/com/example/message_service/listener/WebSocketListener.java)>) can detect the disconnection and delete the member from the chat room.

If the event listener fails to handle WebSocket disconnections, members may remain in the chat room indefinitely. Implementing a heartbeat mechanism could solve this problem, but currently, such a feature is not available.

### Sending Messages To Topics

To send messages to topics a **SEND** frame must be sent that contains a valid destination (that is a room id). For example _/messages/room-id_.

To recall, messages from non-member users will be discarded.

## Running the application

#### Development

`docker compose up -d && docker compose logs -f`

#### Testing

`BUILD_TARGET=test docker compose up -d && docker compose logs -f`

#### Production

`docker build -t message-ms . && kubectl apply -f deployment.yml`
