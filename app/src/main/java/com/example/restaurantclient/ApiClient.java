package com.example.restaurantclient;

import com.example.restaurantclient.api.AuthApi;
import com.example.restaurantclient.api.ClientApi;
import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.SharedPreferences;

/**
 * КЛАСС API CLIENT - ЦЕНТРАЛИЗОВАННЫЙ КЛИЕНТ ДЛЯ СЕТЕВОГО ВЗАИМОДЕЙСТВИЯ
 *
 * Основное назначение:
 * Этот класс является единой точкой доступа для всех сетевых операций приложения.
 * Он реализует паттерн Singleton для управления Retrofit и OkHttpClient,
 * обеспечивая оптимальное использование ресурсов и согласованность настроек.
 */
public class ApiClient {

    /**
     * БАЗОВЫЙ URL СЕРВЕРА
     * 10.0.2.2 - специальный адрес для доступа к localhost из эмулятора Android
     * :8080 - порт сервера
     * /lab6_4kurs/ - базовый путь API (вероятно, проект для 4 курса, лабораторная 6)
     */
    private static final String BASE_URL = "http://10.0.2.2:8080/lab6_4kurs/";

    // Статические переменные для реализации паттерна Singleton
    private static Retrofit retrofit = null;        // Единственный экземпляр Retrofit
    private static OkHttpClient okHttpClient = null; // Единственный экземпляр OkHttpClient

    /**
     * ОСНОВНОЙ МЕТОД ПОЛУЧЕНИЯ RETROFIT КЛИЕНТА
     * Реализует ленивую инициализацию (Lazy Initialization) - объекты создаются только при первом вызове
     *
     * @param context Контекст приложения, необходим для доступа к SharedPreferences
     * @return Настроенный экземпляр Retrofit
     */
    public static Retrofit getClient(Context context) {
        // БЛОК ИНИЦИАЛИЗАЦИИ OKHTTP CLIENT
        // Создается только один раз благодаря проверке if (okHttpClient == null)
        if (okHttpClient == null) {
            // Получаем SharedPreferences для управления сессионными данными
            // "session" - имя файла preferences, где хранятся данные аутентификации
            SharedPreferences preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE);

            // НАСТРОЙКА ЛОГИРОВАНИЯ СЕТЕВЫХ ЗАПРОСОВ
            // HttpLoggingInterceptor перехватывает и логирует все HTTP-запросы и ответы
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // Уровень логирования BODY - показывает заголовки и тела запросов/ответов
            // Очень полезно для отладки, но в production следует уменьшить уровень
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // СОЗДАНИЕ OKHTTP CLIENT С КАСТОМНЫМИ ИНТЕРЦЕПТОРАМИ
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

    /**
     * ФАБРИЧНЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ API АУТЕНТИФИКАЦИИ
     * Создает реализацию AuthApi интерфейса через Retrofit
     *
     * @param context Контекст приложения
     * @return Готовый к использованию API клиент для операций аутентификации
     */
    public static AuthApi getAuthApi(Context context) {
        return getClient(context).create(AuthApi.class);
    }

    /**
     * ФАБРИЧНЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ КЛИЕНТСКОГО API
     * Создает реализацию ClientApi интерфейса через Retrofit
     *
     * @param context Контекст приложения
     * @return Готовый к использованию API клиент для основных операций приложения
     */
    public static ClientApi getClientApi(Context context) {
        return getClient(context).create(ClientApi.class);
    }
}