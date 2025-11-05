package com.example.restaurantclient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantclient.adapter.ClientAdapter;
import com.example.restaurantclient.api.AuthApi;
import com.example.restaurantclient.api.ClientApi;
import com.example.restaurantclient.models.AuthResponse;
import com.example.restaurantclient.models.Client;
import com.example.restaurantclient.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * КЛАСС MAIN ACTIVITY - ГЛАВНЫЙ ЭКРАН ПРИЛОЖЕНИЯ ДЛЯ УПРАВЛЕНИЯ КЛИЕНТАМИ
 *
 * Основное назначение:
 * Этот класс представляет основной рабочий экран приложения после авторизации,
 * обеспечивающий полный CRUD (Create, Read, Update, Delete) функционал для управления
 * клиентами ресторана. Поддерживает два режима работы: авторизованный пользователь и гость.
 */
public class MainActivity extends AppCompatActivity {
    // ДЕКЛАРАЦИЯ КОМПОНЕНТОВ UI И ДАННЫХ
    private RecyclerView recyclerView;          // Для отображения списка клиентов
    private ClientAdapter adapter;              // Адаптер для связи данных и RecyclerView
    private List<Client> clientList = new ArrayList<>(); // Список клиентов для отображения
    private FloatingActionButton fabAdd, fabLogout; // Кнопки действий: добавление и выход
    private TextView tvUserInfo;                // Отображение информации о пользователе
    private User currentUser;                   // Текущий авторизованный пользователь
    private boolean isGuest = false;            // Флаг гостевого режима

    /**
     * МЕТОД СОЗДАНИЯ АКТИВНОСТИ - ОСНОВНАЯ ИНИЦИАЛИЗАЦИЯ
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Получаем флаг гостевого режима из Intent (передается из LoginActivity)
        isGuest = getIntent().getBooleanExtra("isGuest", false);

        // ИНИЦИАЛИЗАЦИЯ И НАСТРОЙКА КОМПОНЕНТОВ
        initViews();            // Находим View элементы и настраиваем обработчики
        setupRecyclerView();    // Настраиваем RecyclerView и адаптер

        // РАЗДЕЛЕНИЕ ПОВЕДЕНИЯ ПО РЕЖИМАМ
        if (isGuest) {
            setupGuestMode();   // Ограниченный функционал для гостей
        } else {
            loadUserInfo();     // Загрузка информации об авторизованном пользователе
        }

        // Загрузка списка клиентов (общая для обоих режимов)
        loadClients();
    }

    /**
     * ИНИЦИАЛИЗАЦИЯ VIEW ЭЛЕМЕНТОВ И ОБРАБОТЧИКОВ СОБЫТИЙ
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        fabLogout = findViewById(R.id.fabLogout);
        tvUserInfo = findViewById(R.id.tvUserInfo);

        // Обработчики кликов для кнопок действий
        fabAdd.setOnClickListener(v -> showAddClientDialog()); // Диалог добавления клиента
        fabLogout.setOnClickListener(v -> logout());           // Выход из системы
    }

    /**
     * НАСТРОЙКА RECYCLERVIEW С УЧЕТОМ РЕЖИМА ДОСТУПА
     * Создает разные реализации адаптера для гостей и авторизованных пользователей
     */
    private void setupRecyclerView() {
        if (isGuest) {
            // РЕЖИМ ГОСТЯ - ТОЛЬКО ПРОСМОТР
            adapter = new ClientAdapter(clientList, new ClientAdapter.OnClientClickListener() {
                @Override
                public void onEditClick(Client client) {
                    // Запрет редактирования для гостей
                    Toast.makeText(MainActivity.this, "Неавторизованные пользователи не могут редактировать", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDeleteClick(Client client) {
                    // Запрет удаления для гостей
                    Toast.makeText(MainActivity.this, "Неавторизованные пользователи не могут удалять", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // РЕЖИМ АВТОРИЗОВАННОГО ПОЛЬЗОВАТЕЛЯ - ПОЛНЫЙ ДОСТУП
            adapter = new ClientAdapter(clientList, new ClientAdapter.OnClientClickListener() {
                @Override
                public void onEditClick(Client client) {
                    showEditClientDialog(client); // Редактирование клиента
                }

                @Override
                public void onDeleteClick(Client client) {
                    deleteClient(client); // Удаление клиента
                }
            });
        }

        // Настройка менеджера компоновки и назначение адаптера
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * НАСТРОЙКА РЕЖИМА ГОСТЯ - ОГРАНИЧЕННЫЙ ФУНКЦИОНАЛ
     */
    private void setupGuestMode() {
        tvUserInfo.setText("Неавторизованный пользователь");
        fabAdd.setVisibility(View.GONE); // Скрываем кнопку добавления для гостей
    }

    /**
     * ЗАГРУЗКА ИНФОРМАЦИИ О ТЕКУЩЕМ АВТОРИЗОВАННОМ ПОЛЬЗОВАТЕЛЕ
     * Проверяет валидность сессии и загружает данные пользователя
     */
    private void loadUserInfo() {
        AuthApi authApi = ApiClient.getAuthApi(this);
        authApi.checkAuth().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isAuthenticated()) {
                    // УСПЕШНАЯ ПРОВЕРКА АУТЕНТИФИКАЦИИ
                    currentUser = response.body().getUser();
                    String userInfo = String.format("Пользователь: %s (%s)",
                            currentUser.getLogin(), currentUser.getRole());
                    tvUserInfo.setText(userInfo);

                    // ИСПРАВЛЕНО: Все авторизованные пользователи видят кнопку добавления
                    fabAdd.setVisibility(View.VISIBLE);
                } else {
                    // СЕССИЯ НЕВАЛИДНА - ПЕРЕХОД НА ЭКРАН ЛОГИНА
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка загрузки данных пользователя", Toast.LENGTH_SHORT).show();
                tvUserInfo.setText("Ошибка загрузки");
            }
        });
    }

    /**
     * ЗАГРУЗКА СПИСКА КЛИЕНТОВ ИЗ СЕРВЕРА
     * Общий метод для обоих режимов (гости и авторизованные пользователи)
     * это и есть пример использования Retrofit для получения всей таблицы clients из БД в виде списка.
     */
    private void loadClients() {
        // Получаем реализацию API через ApiClient
        ClientApi clientApi = ApiClient.getClientApi(this);
        // Вызываем метод getAllClients() — Retrofit создаёт реализацию
        clientApi.getAllClients().enqueue(new Callback<List<Client>>() {
            @Override
            public void onResponse(Call<List<Client>> call, Response<List<Client>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ОБНОВЛЕНИЕ СПИСКА КЛИЕНТОВ
                    // response.body() — это List<Client>, полученный из JSON-ответа сервера
                    clientList.clear();
                    clientList.addAll(response.body()); // Заполняем список данными из БД
                    adapter.notifyDataSetChanged(); // Уведомление адаптера об изменениях
                } else {
                    Toast.makeText(MainActivity.this, "Ошибка загрузки клиентов", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Client>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ПОКАЗ ДИАЛОГА ДОБАВЛЕНИЯ НОВОГО КЛИЕНТА
     */
    private void showAddClientDialog() {
        if (isGuest) {
            Toast.makeText(this, "Неавторизованные пользователи не могут добавлять клиентов", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_client, null);

        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etContacts = view.findViewById(R.id.etContacts);

        builder.setView(view)
                .setTitle("Добавить клиента")
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String fullName = etFullName.getText().toString().trim();
                    String contacts = etContacts.getText().toString().trim();

                    if (!fullName.isEmpty()) {
                        createClient(fullName, contacts);
                    } else {
                        Toast.makeText(MainActivity.this, "Введите ФИО клиента", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    /**
     * СОЗДАНИЕ НОВОГО КЛИЕНТА НА СЕРВЕРЕ
     */
    private void createClient(String fullName, String contacts) {
        if (isGuest) {
            Toast.makeText(this, "Неавторизованные пользователи не могут добавлять клиентов", Toast.LENGTH_SHORT).show();
            return;
        }

        ClientApi clientApi = ApiClient.getClientApi(this);
        clientApi.createClient(fullName, contacts).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Клиент добавлен", Toast.LENGTH_SHORT).show();
                    loadClients(); // Перезагрузка списка после добавления
                } else {
                    Toast.makeText(MainActivity.this, "Ошибка добавления", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ПОКАЗ ДИАЛОГА РЕДАКТИРОВАНИЯ КЛИЕНТА
     */
    private void showEditClientDialog(Client client) {
        if (isGuest) {
            Toast.makeText(this, "Неавторизованные пользователи не могут редактировать клиентов", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_client, null);

        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etContacts = view.findViewById(R.id.etContacts);

        // Заполняем поля текущими данными клиента
        etFullName.setText(client.getFullName());
        etContacts.setText(client.getContacts() != null ? client.getContacts() : "");

        builder.setView(view)
                .setTitle("Редактировать клиента: " + client.getFullName())
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String fullName = etFullName.getText().toString().trim();
                    String contacts = etContacts.getText().toString().trim();

                    if (!fullName.isEmpty()) {
                        updateClient(client.getId(), fullName, contacts);
                    } else {
                        Toast.makeText(MainActivity.this, "Введите ФИО клиента", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .create()
                .show();
    }

    /**
     * ОБНОВЛЕНИЕ ДАННЫХ КЛИЕНТА НА СЕРВЕРЕ
     * Включает расширенное логирование для отладки
     */
    private void updateClient(int id, String fullName, String contacts) {
        if (isGuest) {
            Toast.makeText(this, "Неавторизованные пользователи не могут редактировать клиентов", Toast.LENGTH_SHORT).show();
            return;
        }

        // ДЕТАЛЬНОЕ ЛОГИРОВАНИЕ ДЛЯ ОТЛАДКИ
        Log.d("UPDATE_CLIENT", "Sending UPDATE - ID: " + id + ", Name: " + fullName + ", Contacts: " + contacts);

        ClientApi clientApi = ApiClient.getClientApi(this);
        clientApi.updateClient(id, fullName, contacts).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Клиент успешно обновлен!", Toast.LENGTH_SHORT).show();
                    loadClients(); // Перезагрузка обновленного списка
                } else {
                    // РАСШИРЕННАЯ ОБРАБОТКА ОШИБОК С ЧТЕНИЕМ ERROR BODY
                    String errorMessage = "Ошибка обновления: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("UPDATE_CLIENT", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UPDATE_CLIENT", "Network error: " + t.getMessage());
            }
        });
    }

    /**
     * УДАЛЕНИЕ КЛИЕНТА С ПОДТВЕРЖДЕНИЕМ
     */
    private void deleteClient(Client client) {
        if (isGuest) {
            Toast.makeText(this, "Неавторизованные пользователи не могут удалять клиентов", Toast.LENGTH_SHORT).show();
            return;
        }

        // ДИАЛОГ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ
        new AlertDialog.Builder(this)
                .setTitle("Удаление клиента")
                .setMessage("Вы уверены, что хотите удалить клиента " + client.getFullName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    // ИСПРАВЛЕНО: Убираем проверку на admin - все авторизованные могут удалять
                    ClientApi clientApi = ApiClient.getClientApi(this);
                    clientApi.deleteClient(client.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Клиент удален", Toast.LENGTH_SHORT).show();
                                loadClients(); // Перезагрузка списка после удаления
                            } else {
                                Toast.makeText(MainActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    /**
     * ВЫХОД ИЗ СИСТЕМЫ С УЧЕТОМ РЕЖИМА
     */
    private void logout() {
        if (isGuest) {
            // ПРОСТОЙ ПЕРЕХОД ДЛЯ ГОСТЕЙ
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // ПОЛНОЦЕННЫЙ ВЫХОД ДЛЯ АВТОРИЗОВАННЫХ ПОЛЬЗОВАТЕЛЕЙ
        AuthApi authApi = ApiClient.getAuthApi(this);
        authApi.logout().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                // ОЧИСТКА СЕССИОННЫХ ДАННЫХ
                getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка выхода", Toast.LENGTH_SHORT).show();
            }
        });
    }
}