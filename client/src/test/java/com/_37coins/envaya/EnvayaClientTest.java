package com._37coins.envaya;

import com._37coins.envaya.pojo.EnvayaEvent;
import com._37coins.envaya.pojo.EnvayaMessage;
import com._37coins.envaya.pojo.EnvayaRequest;
import com._37coins.envaya.pojo.EnvayaResponse;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class EnvayaClientTest extends Assert {
  static MockWebServer mockWebServer;
  static URL url;

  EnvayaClient envayaClient;

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

  @Before
  public void setUp() throws Exception {
    envayaClient = new EnvayaClient(url.toString(), "123abc", "+79270933085");
  }

  @Test
  public void testCreateResponse() throws Exception {
    mockWebServer.enqueue(
        new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("{\"events\": [{\"event\": \"create_request\", \"new_account_request\": \"some_json\"}]}"));

    EnvayaResponse response = envayaClient.createResponse("+31258569812", "NewAccountResponse_JSON_string",
        System.currentTimeMillis());

    EnvayaEvent responseEvent = response.getEvents().get(0);
    assertEquals(responseEvent.getEvent(), EnvayaEvent.Event.CREATE_REQUEST);
    assertEquals("some_json", responseEvent.getNewAccountRequest());

    EnvayaRequest envayaRequest = readRequest();
    assertEquals(EnvayaRequest.Action.CREATE_RESPONSE, envayaRequest.getAction());
    assertEquals(200, envayaRequest.getVersion().intValue());
    assertEquals("NewAccountResponse_JSON_string", envayaRequest.getNewAccountResponse());
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

    EnvayaRequest envayaRequest = readRequest();
    assertEquals(EnvayaRequest.Action.SIGN_RESPONSE, envayaRequest.getAction());
    assertEquals(200, envayaRequest.getVersion().intValue());
    assertEquals("sign_request_JSON_string", envayaRequest.getSignResponse());
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

    EnvayaResponse response = envayaClient.outgoing();
    EnvayaEvent responseEvent = response.getEvents().get(0);
    assertEquals(responseEvent.getEvent(), EnvayaEvent.Event.SEND);

    EnvayaMessage envayaMessage = responseEvent.getMessages().get(0);
    assertEquals("1", envayaMessage.getId());
    assertEquals("sms text", envayaMessage.getMessage());
    assertEquals("+19631238574", envayaMessage.getTo());

    EnvayaRequest envayaRequest = readRequest();
    assertEquals(EnvayaRequest.Action.OUTGOING, envayaRequest.getAction());
  }

  @Test
  public void testSendStatus() throws Exception {
    mockWebServer.enqueue(emptyResponse());

    EnvayaResponse response = envayaClient.sendStatus(EnvayaRequest.Status.SENT, "id", null);
    assertEquals(0, response.getEvents().size());

    EnvayaRequest envayaRequest = readRequest();
    assertEquals(EnvayaRequest.Action.SEND_STATUS, envayaRequest.getAction());
    assertEquals(EnvayaRequest.Status.SENT, envayaRequest.getStatus());
  }

  @Test
  public void testDeviceStatus() throws Exception {
    mockWebServer.enqueue(emptyResponse());
    EnvayaResponse envayaResponse = envayaClient.deviceStatus(EnvayaRequest.Status.BATTERY_LOW);
    assertEquals(0, envayaResponse.getEvents().size());

    EnvayaRequest envayaRequest = readRequest();
    assertEquals(EnvayaRequest.Action.DEVICE_STATUS, envayaRequest.getAction());
    assertEquals(EnvayaRequest.Status.BATTERY_LOW, envayaRequest.getStatus());
  }

  @Test
  public void testIncoming() throws Exception {
    mockWebServer.enqueue(emptyResponse());
    EnvayaResponse envayaResponse = envayaClient.incoming("+79263258465", EnvayaRequest.MessageType.SMS, "sms text",
        System.currentTimeMillis());
    assertEquals(0, envayaResponse.getEvents().size());

    EnvayaRequest envayaRequest = readRequest();
    assertEquals(EnvayaRequest.Action.INCOMING, envayaRequest.getAction());
    assertEquals("+79263258465", envayaRequest.getFrom());
    assertEquals(EnvayaRequest.MessageType.SMS, envayaRequest.getMessageType());
    assertEquals("sms text", envayaRequest.getMessage());
  }

  @Test
  public void testParsed() throws Exception {
    mockWebServer.enqueue(emptyResponse());
    EnvayaResponse envayaResponse = envayaClient.parsed("+79263258465", "sms text", "CommandDataSet_JSON_value",
        System.currentTimeMillis());
    assertEquals(0, envayaResponse.getEvents().size());

    EnvayaRequest envayaRequest = readRequest();
    assertEquals(EnvayaRequest.Action.INCOMING, envayaRequest.getAction());
    assertEquals("+79263258465", envayaRequest.getFrom());
    assertEquals(EnvayaRequest.MessageType.SMS_PARSED, envayaRequest.getMessageType());
    assertEquals("sms text", envayaRequest.getMessage());
    assertEquals(200, envayaRequest.getVersion().intValue());
  }

  @Test
  public void testAmqpStarted() throws Exception {
    mockWebServer.enqueue(emptyResponse());
    EnvayaResponse envayaResponse = envayaClient.amqpStarted("TAG");
    assertEquals(0, envayaResponse.getEvents().size());

    EnvayaRequest envayaRequest = readRequest();
    assertEquals(EnvayaRequest.Action.AMQP_STARTED, envayaRequest.getAction());
    assertEquals("TAG", envayaRequest.getConsumerTag());
  }

  @Test
  public void testTest() throws Exception {
    mockWebServer.enqueue(emptyResponse());
    EnvayaResponse envayaResponse = envayaClient.test();
    assertEquals(0, envayaResponse.getEvents().size());

    EnvayaRequest envayaRequest = readRequest();
    assertEquals(EnvayaRequest.Action.TEST, envayaRequest.getAction());
  }

  private MockResponse emptyResponse() {
    return new MockResponse()
        .setHeader("Content-Type", "application/json")
        .setBody("{\"events\": []}");
  }

  private EnvayaRequest readRequest() throws InterruptedException, IOException {
    ByteArrayOutputStream requestOs = new ByteArrayOutputStream();
    mockWebServer.takeRequest().getBody().writeTo(requestOs);
    byte[] requestBytes = requestOs.toByteArray();
    return EnvayaRequest.fromBody(new ByteArrayInputStream(requestBytes));
  }
}
