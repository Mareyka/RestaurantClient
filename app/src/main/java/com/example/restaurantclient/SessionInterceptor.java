package com.example.restaurantclient;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import java.util.List;

public class SessionInterceptor implements Interceptor {
    private SharedPreferences preferences;

    public SessionInterceptor(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        String sessionId = preferences.getString("session_id", null);
        Request.Builder requestBuilder = originalRequest.newBuilder();

        if (sessionId != null) {
            requestBuilder.addHeader("Cookie", "JSESSIONID=" + sessionId);
        }

        Response response = chain.proceed(requestBuilder.build());

        List<String> cookies = response.headers("Set-Cookie");

        for (String cookie : cookies) {
            if (cookie.contains("JSESSIONID")) {

                String[] cookieParts = cookie.split(";")[0].split("=");

                if (cookieParts.length > 1) {
                    String newSessionId = cookieParts[1];

                    preferences.edit()
                            .putString("session_id", newSessionId)
                            .apply();
                    break;
                }
            }
        }

        return response;
    }
}