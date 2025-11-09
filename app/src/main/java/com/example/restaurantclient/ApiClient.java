package com.example.restaurantclient;

import com.example.restaurantclient.api.AuthApi;
import com.example.restaurantclient.api.ClientApi;
import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.SharedPreferences;

 // реализует паттерн Singleton для управления Retrofit и OkHttpClient,

public class ApiClient {

    // 10.0.2.2 - специальный адрес для доступа к localhost из эмулятора Android
    private static final String BASE_URL = "http://10.0.2.2:8080/lab6_4kurs/";

    private static Retrofit retrofit = null;
    private static OkHttpClient okHttpClient = null;

    public static Retrofit getClient(Context context) {
        // БЛОК ИНИЦИАЛИЗАЦИИ OKHTTP CLIENT
        // Создается только один раз благодаря проверке if (okHttpClient == null)
        if (okHttpClient == null) {
            // "session" - имя файла preferences, где хранятся данные аутентификации
            SharedPreferences preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE);
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // СОЗДАНИЕ OKHTTP CLIENT
            okHttpClient = new OkHttpClient.Builder()
                    // SessionInterceptor - кастомный интерцептор для автоматической
                    // обработки аутентификации (добавление токенов в заголовки)
                    .addInterceptor(new SessionInterceptor(preferences))
                    // Интерцептор логирования - для отладки сетевых запросов
                    .addInterceptor(logging)
                    .build();
        }

        // БЛОК ИНИЦИАЛИЗАЦИИ RETROFIT
        // Создается только один раз благодаря проверке if (retrofit == null)
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)                    // Установка базового URL
                    .client(okHttpClient)                 // Использование настроенного OkHttpClient
                    .addConverterFactory(GsonConverterFactory.create()) // Конвертер для преобразования
                    // JSON в Java-объекты и обратно
                    .build();
        }
        return retrofit;
    }

    public static AuthApi getAuthApi(Context context) {
        return getClient(context).create(AuthApi.class);
    }

    public static ClientApi getClientApi(Context context) {
        return getClient(context).create(ClientApi.class);
    }
}