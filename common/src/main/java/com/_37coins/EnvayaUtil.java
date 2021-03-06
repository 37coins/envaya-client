package com._37coins;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;

public class EnvayaUtil {
    public final static String AUTH_HEADER = "X-Request-Signature";
    
    public static String calculateSignature(String uri, List<NameValuePair> nvps, String pw) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Map<String,String> paramMap = new HashMap<>(nvps.size());
        for (NameValuePair nvp: nvps){
            paramMap.put(nvp.getName(), nvp.getValue());
        }
        return calculateSignature(uri, paramMap, pw);
    }
    
    
    public static String calculateSignature(String url, Map<String,String> paramMap, String pw) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        if (null==url||null==paramMap||null==pw){
            return null;
        }
        List<String> params = new ArrayList<>();
        for (Entry<String,String> m :paramMap.entrySet()){
            if (null != m.getValue()){
                params.add(m.getKey()+"="+m.getValue());
            }
        }
        Collections.sort(params);
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        for (String s : params){
            sb.append(",");
            sb.append(s);
        }
        sb.append(",");
        sb.append(pw);
        String value = sb.toString();
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(value.getBytes("utf-8"));

        return new String(Base64.encodeBase64(md.digest()));     
    }
}
