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

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ClientAdapter adapter;
    private List<Client> clientList = new ArrayList<>();
    private FloatingActionButton fabAdd, fabLogout;
    private TextView tvUserInfo;
    private User currentUser;
    private boolean isGuest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isGuest = getIntent().getBooleanExtra("isGuest", false);

        initViews();
        setupRecyclerView();

        if (isGuest) {
            setupGuestMode();
        } else {
            loadUserInfo();
        }

        loadClients();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        fabLogout = findViewById(R.id.fabLogout);
        tvUserInfo = findViewById(R.id.tvUserInfo);

        fabAdd.setOnClickListener(v -> showAddClientDialog());
        fabLogout.setOnClickListener(v -> logout());
    }

    private void setupRecyclerView() {
        if (isGuest) {
            adapter = new ClientAdapter(clientList, new ClientAdapter.OnClientClickListener() {
                @Override
                public void onEditClick(Client client) {
                    Toast.makeText(MainActivity.this, "Неавторизованные пользователи не могут редактировать", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDeleteClick(Client client) {
                    Toast.makeText(MainActivity.this, "Неавторизованные пользователи не могут удалять", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            adapter = new ClientAdapter(clientList, new ClientAdapter.OnClientClickListener() {
                @Override
                public void onEditClick(Client client) {
                    showEditClientDialog(client);
                }

                @Override
                public void onDeleteClick(Client client) {
                    deleteClient(client);
                }
            });
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupGuestMode() {
        tvUserInfo.setText("Неавторизованный пользователь");
        fabAdd.setVisibility(View.GONE);
    }

    private void loadUserInfo() {
        AuthApi authApi = ApiClient.getAuthApi(this);
        authApi.checkAuth().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isAuthenticated()) {
                    currentUser = response.body().getUser();
                    String userInfo = String.format("Пользователь: %s (%s)",
                            currentUser.getLogin(), currentUser.getRole());
                    tvUserInfo.setText(userInfo);

                    fabAdd.setVisibility(View.VISIBLE);
                } else {
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

    private void loadClients() {
        ClientApi clientApi = ApiClient.getClientApi(this);
        clientApi.getAllClients().enqueue(new Callback<List<Client>>() {
            @Override
            public void onResponse(Call<List<Client>> call, Response<List<Client>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    clientList.clear();
                    clientList.addAll(response.body());
                    adapter.notifyDataSetChanged();
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
                    loadClients();
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

    private void showEditClientDialog(Client client) {
        if (isGuest) {
            Toast.makeText(this, "Неавторизованные пользователи не могут редактировать клиентов", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_client, null);

        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etContacts = view.findViewById(R.id.etContacts);

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

    private void updateClient(int id, String fullName, String contacts) {
        if (isGuest) {
            Toast.makeText(this, "Неавторизованные пользователи не могут редактировать клиентов", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("UPDATE_CLIENT", "Sending UPDATE - ID: " + id + ", Name: " + fullName + ", Contacts: " + contacts);

        ClientApi clientApi = ApiClient.getClientApi(this);
        clientApi.updateClient(id, fullName, contacts).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Клиент успешно обновлен!", Toast.LENGTH_SHORT).show();
                    loadClients();
                } else {
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

    private void deleteClient(Client client) {
        if (isGuest) {
            Toast.makeText(this, "Неавторизованные пользователи не могут удалять клиентов", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Удаление клиента")
                .setMessage("Вы уверены, что хотите удалить клиента " + client.getFullName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    ClientApi clientApi = ApiClient.getClientApi(this);
                    clientApi.deleteClient(client.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Клиент удален", Toast.LENGTH_SHORT).show();
                                loadClients();
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

    private void logout() {
        if (isGuest) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        AuthApi authApi = ApiClient.getAuthApi(this);
        authApi.logout().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
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