package com.example.restaurantclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.restaurantclient.api.AuthApi;
import com.example.restaurantclient.models.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * КЛАСС LOGIN ACTIVITY - ЭКРАН АУТЕНТИФИКАЦИИ И РЕГИСТРАЦИИ ПОЛЬЗОВАТЕЛЕЙ
 *
 * Основное назначение:
 * Этот класс представляет стартовый экран приложения, обеспечивающий:
 * - Авторизацию существующих пользователей
 * - Регистрацию новых пользователей
 * - Гостевой вход без регистрации
 * - Проверку активной сессии для автоматического входа
 *
 * Является точкой входа в приложение после запуска
 */
public class LoginActivity extends AppCompatActivity {
    // ДЕКЛАРАЦИЯ ЭЛЕМЕНТОВ ПОЛЬЗОВАТЕЛЬСКОГО ИНТЕРФЕЙСА
    private EditText etLogin, etPassword;    // Поля ввода логина и пароля
    private Button btnLogin, btnRegister, btnGuest; // Кнопки действий
    private ProgressBar progressBar;         // Индикатор загрузки при сетевых операциях

    /**
     * МЕТОД СОЗДАНИЯ АКТИВНОСТИ - ОСНОВНОЙ ЖИЗНЕННЫЙ ЦИКЛ
     * Вызывается при создании активности, выполняет первоначальную настройку
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Установка макета из XML ресурса

        // ИНИЦИАЛИЗАЦИЯ ЭЛЕМЕНТОВ UI - связывание Java объектов с XML элементами
        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGuest = findViewById(R.id.btnGuest);
        progressBar = findViewById(R.id.progressBar);

        // УСТАНОВКА ОБРАБОТЧИКОВ КЛИКОВ - назначение действий на кнопки
        btnLogin.setOnClickListener(v -> login());           // Вход по логину/паролю
        btnRegister.setOnClickListener(v -> showRegisterDialog()); // Показать диалог регистрации
        btnGuest.setOnClickListener(v -> enterAsGuest());    // Вход как гость

        // ПРОВЕРЯЕМ СУЩЕСТВУЮЩУЮ СЕССИЮ - автоматический вход если пользователь уже авторизован
        checkExistingSession();
    }

    /**
     * ПРОВЕРКА АКТИВНОЙ СЕССИИ ПОЛЬЗОВАТЕЛЯ
     * Выполняет запрос к серверу для проверки валидности текущей сессии
     * Если пользователь уже авторизован, автоматически переходит в главное приложение
     */
    private void checkExistingSession() {
        // Получаем API клиент для операций аутентификации
        AuthApi authApi = ApiClient.getAuthApi(this);

        // Асинхронный запрос проверки авторизации
        authApi.checkAuth().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                // Проверяем успешность ответа и статус аутентификации
                if (response.isSuccessful() && response.body() != null && response.body().isAuthenticated()) {
                    // Если пользователь уже авторизован, переходим к главному экрану
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish(); // Закрываем текущую активность чтобы нельзя было вернуться назад
                }
                // Если не авторизован - остаемся на экране логина (ничего не делаем)
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // Ошибка проверки сессии - ничего не делаем, остаемся на экране логина
                // Это нормальная ситуация при первом запуске или при отсутствии сети
            }
        });
    }

    /**
     * МЕТОД АВТОРИЗАЦИИ ПОЛЬЗОВАТЕЛЯ
     * Выполняет вход по логину и паролю с валидацией и обработкой результатов
     */
    private void login() {
        // Получаем введенные данные из полей ввода
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // ПРОВЕРКА ЗАПОЛНЕНИЯ ПОЛЕЙ - базовая валидация на клиенте
        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return; // Прерываем выполнение если поля не заполнены
        }

        // ПОКАЗЫВАЕМ ИНДИКАТОР ЗАГРУЗКИ - визуальная обратная связь пользователю
        progressBar.setVisibility(View.VISIBLE);

        // ВЫПОЛНЯЕМ ЗАПРОС АВТОРИЗАЦИИ к серверу
        AuthApi authApi = ApiClient.getAuthApi(this);
        authApi.login(login, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                // Скрываем индикатор загрузки после получения ответа
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.isSuccess()) {
                        // УСПЕШНАЯ АВТОРИЗАЦИЯ
                        Toast.makeText(LoginActivity.this, "Успешный вход", Toast.LENGTH_SHORT).show();
                        // Переходим к главному экрану приложения
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish(); // Закрываем экран логина
                    } else {
                        // ОШИБКА АВТОРИЗАЦИИ (неверные данные и т.д.)
                        Toast.makeText(LoginActivity.this, authResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // ОШИБКА СЕРВЕРА (HTTP код не 200-299)
                    Toast.makeText(LoginActivity.this, "Ошибка сервера", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // ОШИБКА СЕТИ (нет соединения, таймаут и т.д.)
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ПОКАЗ ДИАЛОГА РЕГИСТРАЦИИ
     * Создает всплывающее окно с формой регистрации нового пользователя
     */
    private void showRegisterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Загружаем кастомный макет для диалога регистрации
        View view = getLayoutInflater().inflate(R.layout.dialog_register, null);

        // Инициализация полей формы регистрации
        EditText etRegLogin = view.findViewById(R.id.etLogin);
        EditText etRegPassword = view.findViewById(R.id.etPassword);
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etEmail = view.findViewById(R.id.etEmail);

        // НАСТРОЙКА ДИАЛОГОВОГО ОКНА
        builder.setView(view)
                .setTitle("Регистрация")
                .setPositiveButton("Зарегистрироваться", (dialog, which) -> {
                    // ОБРАБОТЧИК КНОПКИ РЕГИСТРАЦИИ
                    String login = etRegLogin.getText().toString().trim();
                    String password = etRegPassword.getText().toString().trim();
                    String fullName = etFullName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();

                    // Базовая проверка обязательных полей
                    if (!login.isEmpty() && !password.isEmpty()) {
                        register(login, password, fullName, email);
                    }
                })
                .setNegativeButton("Отмена", null) // Кнопка отмены - закрывает диалог
                .show(); // Отображение диалога
    }

    /**
     * МЕТОД РЕГИСТРАЦИИ НОВОГО ПОЛЬЗОВАТЕЛЯ
     * Отправляет данные регистрации на сервер и обрабатывает результат
     */
    private void register(String login, String password, String fullName, String email) {
        AuthApi authApi = ApiClient.getAuthApi(this);
        authApi.register(login, password, fullName, email).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    Toast.makeText(LoginActivity.this, authResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    if (authResponse.isSuccess()) {
                        // АВТОМАТИЧЕСКИ ЛОГНИМСЯ ПОСЛЕ УСПЕШНОЙ РЕГИСТРАЦИИ
                        // Заполняем поля логина и пароля и вызываем метод входа
                        etLogin.setText(login);
                        etPassword.setText(password);
                        login();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Ошибка сети при регистрации", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ГОСТЕВОЙ ВХОД БЕЗ РЕГИСТРАЦИИ
     * Позволяет пользователю войти в приложение без создания учетной записи
     * с ограниченным функционалом
     */
    private void enterAsGuest() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("isGuest", true); // Передаем флаг гостя для ограничения функционала
        startActivity(intent);
        finish(); // Закрываем экран логина
    }
}