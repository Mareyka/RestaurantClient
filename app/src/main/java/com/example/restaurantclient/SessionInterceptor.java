package com.example.restaurantclient;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import java.util.List;

/**
 * КЛАСС SESSION INTERCEPTOR - ИНТЕРЦЕПТОР ДЛЯ АВТОМАТИЧЕСКОГО УПРАВЛЕНИЯ СЕССИЕЙ
 *
 * Основное назначение:
 * Этот класс реализует паттерн Interceptor для автоматической обработки сессионных данных
 * в HTTP-запросах. Он отвечает за:
 * - Добавление сессионных cookies к исходящим запросам
 * - Извлечение и сохранение сессионных cookies из входящих ответов
 * - Полную автоматизацию управления сессией без ручного вмешательства
 *
 * Является критически важным компонентом для поддержания аутентификации пользователя
 * между запусками приложения.
 */
public class SessionInterceptor implements Interceptor {
    private SharedPreferences preferences;

    /**
     * КОНСТРУКТОР - ИНИЦИАЛИЗАЦИЯ МЕНЕДЖЕРА ХРАНЕНИЯ
     *
     * @param preferences SharedPreferences для постоянного хранения сессионных данных
     *                    между запусками приложения
     */
    public SessionInterceptor(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    /**
     * ОСНОВНОЙ МЕТОД ПЕРЕХВАТА И ОБРАБОТКИ HTTP-ЗАПРОСОВ И ОТВЕТОВ
     *
     * Реализует двустороннюю обработку:
     * 1. На исходящем запросе: добавляет сессионную cookie если она есть
     * 2. На входящем ответе: извлекает и сохраняет новую сессионную cookie
     *
     * @param chain Цепочка интерцепторов, предоставляющая доступ к запросу и возможность его продолжения
     * @return Ответ от сервера после обработки
     * @throws IOException При ошибках сетевого взаимодействия
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        // ШАГ 1: ПОЛУЧЕНИЕ ОРИГИНАЛЬНОГО ЗАПРОСА
        Request originalRequest = chain.request();

        // ШАГ 2: ПОЛУЧЕНИЕ СЕССИОННОГО ID ИЗ ПОСТОЯННОГО ХРАНИЛИЩА
        // "session_id" - ключ для хранения идентификатора сессии
        // null - значение по умолчанию если сессия отсутствует
        String sessionId = preferences.getString("session_id", null);

        // Создание билдера для модификации запроса
        Request.Builder requestBuilder = originalRequest.newBuilder();

        // ШАГ 3: ДОБАВЛЕНИЕ СЕССИОННОЙ COOKIE К ИСХОДЯЩЕМУ ЗАПРОСУ
        // Если у нас есть активная сессия, добавляем ее ко всем запросам
        if (sessionId != null) {
            // Формат: "Cookie: JSESSIONID=значение_сессии"
            requestBuilder.addHeader("Cookie", "JSESSIONID=" + sessionId);

            // Теперь каждый запрос автоматически будет содержать идентификатор сессии,
            // что позволяет серверу распознавать аутентифицированного пользователя
        }

        // ШАГ 4: ВЫПОЛНЕНИЕ ЗАПРОСА С ДОБАВЛЕННЫМИ ЗАГОЛОВКАМИ
        Response response = chain.proceed(requestBuilder.build());

        // ШАГ 5: ОБРАБОТКА ВХОДЯЩЕГО ОТВЕТА - ПОИСК И СОХРАНЕНИЕ НОВОЙ СЕССИИ
        // Получаем все cookies из заголовков ответа
        List<String> cookies = response.headers("Set-Cookie");

        // Перебираем все cookies в поисках сессионной
        for (String cookie : cookies) {
            if (cookie.contains("JSESSIONID")) {
                // НАЙДЕНА СЕССИОННАЯ COOKIE - ИЗВЛЕКАЕМ И СОХРАНЯЕМ ЕЕ

                // Формат cookie: "JSESSIONID=abc123; Path=/; HttpOnly"
                // Разделяем по точке с запятой и берем первую часть: "JSESSIONID=abc123"
                String[] cookieParts = cookie.split(";")[0].split("=");

                // Проверяем что cookie имеет правильный формат (ключ=значение)
                if (cookieParts.length > 1) {
                    // parts[0] = "JSESSIONID"
                    // parts[1] = значение сессии (например, "abc123")
                    String newSessionId = cookieParts[1];

                    // СОХРАНЕНИЕ СЕССИИ В PERSISTENT STORAGE
                    preferences.edit()
                            .putString("session_id", newSessionId)
                            .apply(); // Асинхронное сохранение

                    // Сессия сохранена и будет автоматически использоваться в следующих запросах
                    break; // Прерываем цикл после нахождения JSESSIONID
                }
            }
        }

        // ШАГ 6: ВОЗВРАТ ОБРАБОТАННОГО ОТВЕТА
        return response;
    }
}