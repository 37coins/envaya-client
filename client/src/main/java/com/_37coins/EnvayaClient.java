package com._37coins;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.pojo.EnvayaRequest;
import com._37coins.pojo.EnvayaRequest.Action;
import com._37coins.pojo.EnvayaRequest.MessageType;
import com._37coins.pojo.EnvayaResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;



public class EnvayaClient {
    private static final Logger log = LoggerFactory.getLogger(EnvayaClient.class);
    private final OkHttpClient httpClient;
    private String uri;
    private String digestToken;
    private String mobile;

    public EnvayaClient(String uri, String digestToken, String mobile) {
        this(uri, digestToken, mobile, new OkHttpClient());
    }

    public EnvayaClient(String uri, String digestToken, String mobile, OkHttpClient httpClient) {
        this.uri = uri;
        this.mobile = mobile;
        this.digestToken = digestToken;
        this.httpClient = httpClient;
    }

    protected <K> K parsePayload(Response response, Class<K> entityClass)
            throws EnvayaClientException {
        try {
            return new ObjectMapper().readValue(response.body().byteStream(), entityClass);
        } catch (IOException e) {
            log.error("envaya client parsePayload error:", e);
            throw new EnvayaClientException(
                    EnvayaClientException.Reason.ERROR_PARSING, e);
        }
    }

    protected <K> K getPayload(Request request, Class<K> entityClass)
            throws EnvayaClientException {
        Response response;
        log.debug("Request URL->:"+request.urlString());
        log.debug("Request Method->:"+request.method());
        
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            log.error("envaya client httpClient.execute error", e);
            throw new EnvayaClientException(
                    EnvayaClientException.Reason.ERROR_GETTING_RESOURCE, e);
        }

        log.debug("Response Status code:"+response.code());
        if (isSucceed(response) && request.method().equals("DELETE")) {
          return parsePayload(response, entityClass);
        } else if (isSucceed(response)) {
           return null;
        } else {
            throw new EnvayaClientException(
                    EnvayaClientException.Reason.AUTHENTICATION_FAILED);
        }
    }
    
    protected <K> K getPayload(EnvayaRequest request, Class<K> entityClass)
            throws EnvayaClientException {
        request
            .setPhoneNumber(mobile)
            .setNow(System.currentTimeMillis());
        
        String reqSig = null;
        try {
            reqSig = EnvayaUtil.calculateSignature(uri, request.toMap(), digestToken);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new EnvayaClientException(
                    EnvayaClientException.Reason.ERROR_PARSING);
        }
        
        FormEncodingBuilder formBuilder = new FormEncodingBuilder();
        for (Map<String,String> nvp: request.toMap())
            for (Entry<String,String> es: nvp.entrySet())
                formBuilder.addEncoded(es.getKey(),es.getValue());

        Request req = new Request.Builder()
            .url(uri)
            .addHeader(EnvayaUtil.AUTH_HEADER, reqSig)
            .post(formBuilder.build()).build();
        
        return getPayload(req, entityClass);        
    }

    public EnvayaResponse test() throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.TEST), EnvayaResponse.class);
    }

    public EnvayaResponse sendStatus(EnvayaRequest.Status status, String id, String error) throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.SEND_STATUS)
            .setId(id)
            .setStatus(status)
            .setError(error), EnvayaResponse.class);
    }
    
    public EnvayaResponse deviceStatus(EnvayaRequest.Status status) throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.DEVICE_STATUS)
            .setStatus(status), EnvayaResponse.class);
    }
    
    public EnvayaResponse incoming(String from, MessageType messageType, String message, Long timestamp) throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.INCOMING)
            .setFrom(from)
            .setMessageType(messageType)
            .setMessage(message)
            .setTimestamp(timestamp), EnvayaResponse.class);
    }
    
    public EnvayaResponse outgoing() throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.OUTGOING), EnvayaResponse.class);
    }
    
    public EnvayaResponse amqpStarted(String consumerTag) throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.AMQP_STARTED)
            .setConsumerTag(consumerTag), EnvayaResponse.class);
    }
    
    public static boolean isSucceed(Response response) {
        return response.code() >= 200
                && response.code() < 300;
    }

    public static String toLowerCase(String str) {
        return !str.contains("%") ? str.toLowerCase() : str;
    }

}
