package com.nexsplit.model;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
    public static Map<String, Object> errorResponse(String message, int status) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", message);
        response.put("status", status);
        return response;
    }

    public static Map<String, Object> successResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", false);
        response.put("data", data);
        return response;
    }
}