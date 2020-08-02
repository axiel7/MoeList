package com.axiel7.moelist.rest;

import java.util.Collections;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    public static final String API_BASE_URL = "https://myanimelist.net";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null);
    }

    public static <S> S createService(
            Class<S> serviceClass, String clientId, String clientSecret) {
        if (!clientId.isEmpty() && !clientSecret.isEmpty()) {
            String authToken = Credentials.basic(clientId, clientSecret);
            return createService(serviceClass, authToken);
        }

        return createService(serviceClass, clientId, null);
    }

    public static <S> S createService(
            Class<S> serviceClass, final String authToken) {
        Retrofit retrofit;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (authToken!=null) {
            AuthenticationInterceptor authInterceptor =
                    new AuthenticationInterceptor(authToken);

            if (!httpClient.interceptors().contains(authInterceptor)) {
                httpClient.addInterceptor(authInterceptor);
            }
        }
        httpClient.addInterceptor(logging);
        httpClient.protocols(Collections.singletonList(Protocol.HTTP_1_1));

        builder.client(httpClient.build());
        retrofit = builder.build();

        return retrofit.create(serviceClass);
    }
}
