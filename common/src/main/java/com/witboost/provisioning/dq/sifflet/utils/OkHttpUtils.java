package com.witboost.provisioning.dq.sifflet.utils;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

public class OkHttpUtils {

    @NotNull
    public static Request buildPostRequest(String url, String jsonBody, String token) {

        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.get("application/json"));

        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", token)
                .build();
    }

    @NotNull
    public static Request buildGetRequest(String url, String token) {
        return new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("authorization", token)
                .build();
    }
}
