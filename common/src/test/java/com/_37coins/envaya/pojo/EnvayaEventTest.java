package com._37coins.envaya.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class EnvayaEventTest extends Assert {
  ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testSignRequest() throws Exception {
    String rawString = "{\"event\":\"sign_request\",\"sign_tx_request\":\"another_serialized_json\"}";

    EnvayaEvent event = mapper.readValue(rawString, EnvayaEvent.class);
    assertEquals(EnvayaEvent.Event.SIGN_REQUEST, event.getEvent());
    assertEquals("another_serialized_json", event.getSignTxRequest());
  }

  @Test
  public void testCreateRequest() throws Exception {
    String rawString = "{\"event\":\"create_request\",\"new_account_request\":\"another_serialized_json\"}";

    EnvayaEvent event = mapper.readValue(rawString, EnvayaEvent.class);
    assertEquals(EnvayaEvent.Event.CREATE_REQUEST, event.getEvent());
    assertEquals("another_serialized_json", event.getNewAccountRequest());
  }
}