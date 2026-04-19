package com.example.douban.controller;

import java.util.Map;

final class RequestParams {
    private RequestParams() {
    }

    static int readBoundedInt(Map<String, String> data, String key, int defaultValue, int min, int max) {
        if (data == null || data.get(key) == null) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(data.get(key));
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
