package com.apihelper;


import com.apihelper.parsers.JsonParser;
import com.apihelper.parsers.Parser;
import com.apihelper.utils.L;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okio.Buffer;
import okio.BufferedSource;

public abstract class WebSocketDecorator implements WebSocketListener {
    public static final String CLOSED_BY_USER_REASON = "closed_by_user_reason";
    private static final String TYPE_KEY = "type";
    private static final String DESTINATION_KEY = "destination";
    private static final String DATA_KEY = "data";
    private static final String PING_STRING = "{\"" + DESTINATION_KEY + "\": \"user.ping\", \"" + DATA_KEY + "\": null}";
    public static final String USER_SEND_MESSAGE = "user.send_message";
    public static final String USER_RECEIVE_MESSAGE = "recive_message";
    private com.squareup.okhttp.OkHttpClient client;
    private com.squareup.okhttp.Request.Builder requestBuilder;
    private BehaviorMediator mediator;
    protected WebSocket instance;
    private boolean isClosed = true;
    private Map<String, String> keys = new HashMap<>();

    private Parser<Message> messageParser = new TextParser();

    public WebSocketDecorator(String endpoint, BehaviorMediator mediator) {
        this.client = new com.squareup.okhttp.OkHttpClient();
        this.client.setConnectTimeout(40000, TimeUnit.MILLISECONDS);
        this.client.setReadTimeout(5, TimeUnit.MINUTES);
        this.client.setWriteTimeout(30, TimeUnit.SECONDS);
        this.mediator = mediator;
        // Create request for remote resource.
        this.requestBuilder = new com.squareup.okhttp.Request.Builder().url(endpoint);
    }

    public WebSocketDecorator addKey(String key, String value) {
        return  this;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        L.log("onOpen", "call");
        instance = webSocket;
        isClosed = false;
    }

    @Override
    public void onPong(Buffer payload) {
        try {
            String data = "call";
            if (payload != null) {
                data = payload.readUtf8();
            }
            L.log("onPong", data);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onMessage(BufferedSource payload, WebSocket.PayloadType type) {
        L.log("onMessage type", type);
        switch (type) {
            case TEXT:
                try {
                    byte[] data = payload.readByteArray();
                    payload.close();
                    Message message = messageParser.parse(data);
                    onMessage(message);
                    L.log("onMessage data", new String(data));
                } catch (IOException e) {
                    e.printStackTrace();
                    onFailure(e, null);
                }
                break;
        }
    }

    public abstract void onMessage(Message message);

    @Override
    public void onFailure(IOException e, Response response) {
        e.printStackTrace();
        if (response != null && response.body() != null) {
            try {
                L.log("onFailure response", new String(response.body().bytes()));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onClose(int code, String reason) {
        L.log("onClose", reason);
        isClosed = true;
        if (!CLOSED_BY_USER_REASON.equals(reason)) {
            connect();
        }
    }

    public void connect() {
        // Execute the request and retrieve the response.
        WebSocketCall call = WebSocketCall.create(client,
                requestBuilder.headers(Headers.of(mediator.getHeaders())).build());
        call.enqueue(this);
    }

    public void sendMessage(JsonNode data) {
        if (instance != null) {
            ping();
            try {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                node.put(DESTINATION_KEY, USER_SEND_MESSAGE);
                node.put(DATA_KEY, data);
                instance.sendMessage(WebSocket.PayloadType.TEXT,
                        new Buffer().writeUtf8(node.toString()));
                L.log("sendMessage", data.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendData(ObjectNode data) {
        if (instance != null) {
            ping();
            try {
                instance.sendMessage(WebSocket.PayloadType.TEXT,
                        new Buffer().writeUtf8(data.toString()));
                L.log("sendMessage", data.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void ping() {
        if (instance != null && !isClosed) {
            try {
                instance.sendPing(new Buffer().writeUtf8(PING_STRING));
                return;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        connect();
    }

    public void close() {
        L.log("close", "call");
        if (instance != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        instance.close(1000, CLOSED_BY_USER_REASON);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private class TextParser extends JsonParser<Message> {

        @Override
        public Message parse(JsonNode jsonNode) {
            Message message = new Message();
            message.destination = jsonNode.path(DESTINATION_KEY).asText();
            message.data = jsonNode.path(DATA_KEY);
            return message;
        }
    }

    public class Message {
        private String destination = "";
        private JsonNode data;

        public String getDestination() {
            return destination;
        }

        public JsonNode getData() {
            return data;
        }
    }
}