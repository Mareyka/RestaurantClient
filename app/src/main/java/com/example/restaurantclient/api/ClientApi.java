package com.example.restaurantclient.api;

import com.example.restaurantclient.models.Client;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import java.util.List;

/**
 * ИНТЕРФЕЙС: ClientApi (API для работы с клиентами)
 * НАЗНАЧЕНИЕ: Определяет контракт HTTP запросов для CRUD операций с клиентами
 * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 2,3 - использование Retrofit для взаимодействия с REST API
 * RESTFUL DESIGN: Следует принципам REST для операций с ресурсами
 */
public interface ClientApi {

    /**
     * МЕТОД: getAllClients - получение списка всех клиентов
     * HTTP: GET /clients
     * НАЗНАЧЕНИЕ: Чтение данных (R в CRUD)
     * ДОСТУП: Доступен неавторизованным пользователям (требование лабораторной)
     *
     * @return Call<List<Client>> - список клиентов в формате JSON
     *
     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ:
     * - Пункт 2: "Получить всю таблицу из БД списком"
     * - Пункт 6: "Неавторизованному пользователю разрешить выполнение только GET-запросов"
     */
    @GET("clients")                          // HTTP GET метод к endpoint /clients
    Call<List<Client>> getAllClients();      // Возвращает список объектов Client

    /**
     * МЕТОД: createClient - создание нового клиента
     * HTTP: POST /clients
     * НАЗНАЧЕНИЕ: Создание новых записей (C в CRUD)
     * ДОСТУП: Только для авторизованных пользователей
     * ФОРМАТ: Данные передаются в теле запроса
     *
     * @param fullName - ФИО клиента (обязательное поле)
     * @param contacts - контактная информация клиента
     * @return Call<Void> - запрос без возвращаемых данных (только статус)
     *
     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 6 - "Авторизованному – всех видов запросов"
     */
    @POST("clients")                         // HTTP POST метод к endpoint /clients
    @FormUrlEncoded                          // Данные передаются в теле как form-encoded
    Call<Void> createClient(
            @Field("fullName") String fullName,   // Параметр в теле запроса
            @Field("contacts") String contacts
    );

    /**
     * МЕТОД: updateClient - обновление данных клиента
     * HTTP: PUT /clients
     * НАЗНАЧЕНИЕ: Обновление существующих записей (U в CRUD)
     * ДОСТУП: Только для авторизованных пользователей
     * ФОРМАТ: Параметры передаются в URL (query parameters)
     *
     * @param id - идентификатор клиента для обновления
     * @param fullName - новое ФИО клиента
     * @param contacts - новые контакты клиента
     * @return Call<Void> - запрос без возвращаемых данных
     *
     * ОСОБЕННОСТЬ: Использует @Query вместо @Field для совместимости с серверным ClientServlet
     * Сервер ожидает: PUT /clients?id=1&fullName=Name&contacts=Phone
     */
    @PUT("clients")                          // HTTP PUT метод к endpoint /clients
    Call<Void> updateClient(
            @Query("id") int id,             // Параметр в URL: ?id=value
            @Query("fullName") String fullName, // Параметр в URL: &fullName=value
            @Query("contacts") String contacts   // Параметр в URL: &contacts=value
    );

    /**
     * МЕТОД: deleteClient - удаление клиента
     * HTTP: DELETE /clients
     * НАЗНАЧЕНИЕ: Удаление записей (D в CRUD)
     * ДОСТУП: Только для авторизованных пользователей
     * ФОРМАТ: Параметр передается в URL
     *
     * @param id - идентификатор клиента для удаления
     * @return Call<Void> - запрос без возвращаемых данных
     *
     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 6 - полный CRUD для авторизованных пользователей
     */
    @DELETE("clients")                       // HTTP DELETE метод к endpoint /clients
    Call<Void> deleteClient(@Query("id") int id); // Параметр в URL: ?id=value
}