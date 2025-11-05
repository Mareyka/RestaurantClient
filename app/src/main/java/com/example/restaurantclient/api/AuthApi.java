package com.example.restaurantclient.api;

import com.example.restaurantclient.models.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * ИНТЕРФЕЙС: AuthApi (API для аутентификации)
 * НАЗНАЧЕНИЕ: Определяет контракт HTTP запросов для операций авторизации и управления сессиями
 * ТЕХНОЛОГИЯ: Retrofit - декларативное определение REST API через аннотации
 * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 6 - клиентская часть для взаимодействия с сервером
 */
public interface AuthApi {

    /**
     * МЕТОД: register - регистрация нового пользователя
     * HTTP: POST /auth/register
     * ФОРМАТ ДАННЫХ: application/x-www-form-urlencoded (соответствует требованию лабораторной)
     *
     * @param login - уникальный логин пользователя
     * @param password - пароль (передается в теле, что позволяет шифрование при HTTPS)
     * @param fullName - полное имя пользователя
     * @param email - email адрес
     * @return Call<AuthResponse> - отложенный запрос с ответом в формате AuthResponse
     *
     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 5 - "Логин и пароль должны передаваться в теле запроса"
     */
    @POST("auth/register")                    // HTTP POST метод к endpoint /auth/register
    @FormUrlEncoded                           // Указывает, что данные передаются в теле как form-encoded
    Call<AuthResponse> register(
            @Field("login") String login,     // Параметр "login" в теле запроса
            @Field("password") String password,
            @Field("fullName") String fullName,
            @Field("email") String email
    );

    /**
     * МЕТОД: login - аутентификация пользователя
     * HTTP: POST /auth/login
     * ФОРМАТ ДАННЫХ: application/x-www-form-urlencoded
     * СЕССИЯ: Сервер устанавливает JSESSIONID cookie при успешной авторизации
     *
     * @param login - логин пользователя
     * @param password - пароль пользователя
     * @return Call<AuthResponse> - содержит информацию об успехе авторизации и данные пользователя
     *
     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 6 - "Поддерживать сессию со стороны клиента"
     */
    @POST("auth/login")                       // HTTP POST метод к endpoint /auth/login
    @FormUrlEncoded                           // Данные передаются в теле запроса
    Call<AuthResponse> login(
            @Field("login") String login,
            @Field("password") String password
    );

    /**
     * МЕТОД: checkAuth - проверка текущего статуса аутентификации
     * HTTP: GET /auth/check
     * АВТОРИЗАЦИЯ: Использует JSESSIONID cookie для идентификации сессии
     * ВОЗВРАЩАЕТ: Информацию о текущем авторизованном пользователе или authenticated: false
     *
     * @return Call<AuthResponse> - содержит статус аутентификации и данные пользователя
     *
     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 6 - проверка активности сессии
     */
    @GET("auth/check")                        // HTTP GET метод к endpoint /auth/check
    Call<AuthResponse> checkAuth();           // Не требует параметров - использует cookie сессии

    /**
     * МЕТОД: logout - завершение сессии пользователя
     * HTTP: GET /auth/logout
     * ДЕЙСТВИЕ: Сервер инвалидирует сессию и очищает JSESSIONID cookie
     *
     * @return Call<AuthResponse> - подтверждение успешного выхода
     *
     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 6 - управление сессиями
     */
    @GET("auth/logout")                       // HTTP GET метод к endpoint /auth/logout
    Call<AuthResponse> logout();              // Не требует параметров - использует текущую сессию
}