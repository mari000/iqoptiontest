package iqoptiontest.helper;

import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import groovy.json.JsonSlurper;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class WsHelper {

    private WebSocket ws;
    private JsonSlurper jsonSlurper = new JsonSlurper();
    private final WebSocketAdapter profileListener;
    private ReadContext profileCtx;

    public WsHelper(URI wsUsri, int connectionTimeout) throws IOException {
        this.profileListener = new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket websocket, String text) {
                ReadContext ctx = JsonPath.parse(text);
                if (ctx.read("$.name").equals("profile")) {
                    profileCtx = ctx;
                    synchronized (this) {
                        this.notify();
                    }
                }
            }
        };
        this.ws = new WebSocketFactory()
                .setConnectionTimeout(connectionTimeout)
                .createSocket(wsUsri)
                .addListener(this.profileListener);
    }

    public void connect() throws WebSocketException, IOException {
        this.ws = ws.recreate().connect();
    }

    public void disconnect() {
        ws.disconnect();
    }

    public ReadContext authorize(String ssid, String requestId, int msTimeout) throws InterruptedException {
        this.profileCtx = null;
        final Map<String, Object> messageMap = ImmutableMap.<String, Object>builder()
                .put("name", "ssid")
                .put("msg", ssid)
                .put("request_id", requestId)
                .build();
        ws.sendText(new JSONObject().toJSONString(messageMap));
        synchronized (profileListener) {
            profileListener.wait(msTimeout);
        }
        return this.profileCtx;
    }

}
