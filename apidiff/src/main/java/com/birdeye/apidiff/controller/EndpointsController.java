package com.birdeye.apidiff.controller;

import java.util.Iterator;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Sugandha Aggarwal
 * @created 2024-08-07
 **/

@RestController
@RequestMapping("/api")
public class EndpointsController {
	
	@GetMapping("/hello")
	public String hello() {
		return "Service is UP!";
	}

    @PostMapping("/compare")
    public String compareEndpoints(
    	@RequestHeader String userid,
        @RequestBody Map<String, Object> request) {
    	if (userid.isBlank() || userid.isEmpty()) {
            return "user-id header is missing";
        }

        if (request == null || !request.containsKey("endpoint1") || !request.containsKey("endpoint2")  ) {
            return "Invalid request payload";
        }
    
    	String endpoint1 = "";
    	String endpoint2 = "";
    	System.out.println("received request..");
    	Map<String, Object> apiPayload = null;
    	if(request!=null) {
    		endpoint1 = (String) request.get("endpoint1");
    		endpoint2 = (String) request.get("endpoint2");
    		
    		apiPayload = (Map<String, Object>) request.get("apiPayload");
    		if (endpoint1.isBlank() || endpoint2.isBlank() || apiPayload.isEmpty()) {
                return "Make sure the endpoints/ apipayload is not null or empty";
            }
    		
        	System.out.println("request read..");

    	}

    	String contentType = "application/json";
    	
    	System.out.println("calling restassured endpoint1...");

        // Send requests to both endpoints with the payload
        Response response1 = RestAssured.given().header("Content-Type", contentType)
        								.header("user-id",userid)
                                        .body(apiPayload)
                                        .post(endpoint1);
        
        System.out.println("response received from endpoint1..."+response1.asString());
        
    	System.out.println("calling restassured endpoint2...");

        Response response2 = RestAssured.given().header("Content-Type", contentType)
        								.header("user-id",userid)
                                        .body(apiPayload)
                                        .post(endpoint2);
        
        System.out.println("response received from endpoint2..."+response2.asString());
    	System.out.println("comparing responses...");

        // Compare the responses
        String response = compareResponses(response1.getBody().asString(), response2.getBody().asString());
        System.out.println("processing complete");

        if("".equals(response)) {
        	response = "No mismatches, both APIs gave same response!";
        }
        return response;
    }

    private String compareResponses(String responseBody1, String responseBody2) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode json1 = mapper.readTree(responseBody1);
            JsonNode json2 = mapper.readTree(responseBody2);

            StringBuilder comparisonResult = new StringBuilder();

            // Compare JSON nodes
            compareJsonNodes(json1, json2, comparisonResult);

            return comparisonResult.toString();
        } catch (Exception e) {
            return "Error comparing responses: " + e.getMessage();
        }
    }

    private void compareJsonNodes(JsonNode json1, JsonNode json2, StringBuilder result) {
        Iterator<Map.Entry<String, JsonNode>> fields1 = json1.fields();
        Iterator<Map.Entry<String, JsonNode>> fields2 = json2.fields();

        while (fields1.hasNext()) {
            Map.Entry<String, JsonNode> field1 = fields1.next();
            Map.Entry<String, JsonNode> field2 = fields2.next();

            String key = field1.getKey();
            JsonNode value1 = field1.getValue();
            JsonNode value2 = field2.getValue();

           
            if (field1 == null || field2 == null || !value1.equals(value2)) {
                result.append("Difference in field '").append(key != null ? key : "<unknown>").append("':\n");
                if (value1 != null) {
                    result.append("  Endpoint 1: ").append(value1).append("\n");
                }
                if (value2 != null) {
                    result.append("  Endpoint 2: ").append(value2).append("\n");
                }
            } 
        }
    }
}


