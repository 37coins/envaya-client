package com._37coins.envaya;

import com._37coins.envaya.pojo.EnvayaEvent;
import com._37coins.envaya.pojo.EnvayaMessage;
import com._37coins.envaya.pojo.EnvayaResponse;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class EnvayaClientTest extends Assert {
  static MockWebServer mockWebServer;
  static URL url;

  @BeforeClass
  public static void beforeClass() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    url = mockWebServer.getUrl("/some_url");
  }

  @AfterClass
  public static void afterClass() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  public void testCreateResponse() throws Exception {
    mockWebServer.enqueue(
        new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("{\"events\": [{\"event\": \"create_request\", \"new_account_request\": \"some_json\"}]}"));

    EnvayaClient envayaClient = new EnvayaClient(url.toString(), "123abc", "+79270933085");

    EnvayaResponse response = envayaClient.createResponse("+31258569812", "NewAccountResponse_JSON_string",
        System.currentTimeMillis());

    EnvayaEvent responseEvent = response.getEvents().get(0);
    assertEquals(responseEvent.getEvent(), EnvayaEvent.Event.CREATE_REQUEST);
    assertEquals("some_json", responseEvent.getNewAccountRequest());
  }

  @Test
  public void testSignResponse() throws Exception {
    mockWebServer.enqueue(
        new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("{\"events\": [{\"event\": \"sign_request\", \"sign_tx_request\": \"some_json\"}]}"));

    EnvayaClient envayaClient = new EnvayaClient(url.toString(), "123abc", "+79270933085");

    EnvayaResponse response = envayaClient.signResponse("+31258569812", "sign_request_JSON_string",
        System.currentTimeMillis());

    EnvayaEvent responseEvent = response.getEvents().get(0);
    assertEquals(responseEvent.getEvent(), EnvayaEvent.Event.SIGN_REQUEST);
    assertEquals("some_json", responseEvent.getSignTxRequest());
  }

  @Test
  public void testOutgoing() throws Exception {
    mockWebServer.enqueue(
        new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("{\"events\": [{" +
                "  \"event\": \"send\"," +
                "  \"messages\": [{" +
                "    \"id\": 1," +
                "    \"message\": \"sms text\"," +
                "    \"to\": \"+19631238574\"" +
                "    }]" +
                "  }]" +
                "}"));

    EnvayaClient envayaClient = new EnvayaClient(url.toString(), "123abc", "+79270933085");

    EnvayaResponse response = envayaClient.outgoing();

    EnvayaEvent responseEvent = response.getEvents().get(0);
    assertEquals(responseEvent.getEvent(), EnvayaEvent.Event.SEND);

    EnvayaMessage envayaMessage = responseEvent.getMessages().get(0);
    assertEquals("1", envayaMessage.getId());
    assertEquals("sms text", envayaMessage.getMessage());
    assertEquals("+19631238574", envayaMessage.getTo());
  }
}