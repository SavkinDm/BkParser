package com.sdm.BkParser.SupportClasses.Rest;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class NewRestTemplate extends org.springframework.web.client.RestTemplate {



    public JSONObject getForJSON(String URL){
        ResponseEntity<String> response = getForEntity(URL, String.class);
        return  new JSONObject(response.getBody());
    }



    public StringBuilder getForHTMLString(String URL) {
        ResponseEntity<String> response = getForEntity(URL, String.class);
        return new StringBuilder(response.getBody());

    }

    public JSONObject postForJSON(String URL, HttpHeaders headers, String params) {
        HttpEntity<String> entity = new HttpEntity<>(params, headers);
        String response = postForObject(URL, entity, String.class);
        return new JSONObject(response);
    }


    public JSONObject requestForLigaStavok(String URL, String params){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentLength(params.length());
        HttpEntity<String> entity = new HttpEntity<>(params, headers);

        String response = postForObject(URL, entity, String.class);
        return  new JSONObject(response);

    }
}
