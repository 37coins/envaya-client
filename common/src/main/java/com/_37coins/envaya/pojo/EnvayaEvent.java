package com._37coins.envaya.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class EnvayaEvent {

    public enum Event {
        SEND("send"), 
        CANCEL("cancel"),
        CANCEL_ALL("cancel_all"),
        LOG("log"),
        SETTINGS("settings"),
        SIGN_REQUEST("sign_request"),
        CREATE_REQUEST("create_request");

        private String text;

        Event(String text) {
            this.text = text;
        }

        @JsonValue
        final String value() {
            return this.text;
        }

        public String getText() {
            return this.text;
        }

        @JsonCreator
        public static Event fromString(String text) {
            if (text != null) {
                for (Event b : Event.values()) {
                    if (text.equalsIgnoreCase(b.text)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }
    
    private Event event;
    private List<EnvayaMessage> messages;
    private String id;
    private String message;
    private EnvayaSettings settings;

    /**
     * Serialized JSON
     * @see <a href="https://github.com/37coins/rambutan-oracle/blob/develop/common/src/main/java/com/_37coins/rambutan/pojo/SignTransactionRequest.java">SignTransactionRequest</a>
     */
    @JsonProperty("sign_tx_request")
    private String signTxRequest;

    /**
     * Serialized JSON
     * @see <a href="https://github.com/37coins/rambutan-oracle/blob/develop/common/src/main/java/com/_37coins/rambutan/pojo/NewAccountRequest.java">NewAccountRequest</a>
     */
    @JsonProperty("new_account_request")
    private String newAccountRequest;
    
    public Event getEvent() {
        return event;
    }
    public EnvayaEvent setEvent(Event event) {
        this.event = event;
        return this;
    }
    public List<EnvayaMessage> getMessages() {
        return messages;
    }
    public EnvayaEvent setMessages(List<EnvayaMessage> messages) {
        this.messages = messages;
        return this;
    }
    public String getId() {
        return id;
    }
    public EnvayaEvent setId(String id) {
        this.id = id;
        return this;
    }
    public String getMessage() {
        return message;
    }
    public EnvayaEvent setMessage(String message) {
        this.message = message;
        return this;
    }
    public EnvayaSettings getSettings() {
        return settings;
    }
    public EnvayaEvent setSettings(EnvayaSettings settings) {
        this.settings = settings;
        return this;
    }

    public String getSignTxRequest() {
        return signTxRequest;
    }

    public EnvayaEvent setSignTxRequest(String signTxRequest) {
        this.signTxRequest = signTxRequest;
        return this;
    }

    public String getNewAccountRequest() {
        return newAccountRequest;
    }

    public EnvayaEvent setNewAccountRequest(String newAccountRequest) {
        this.newAccountRequest = newAccountRequest;
        return this;
    }
}
