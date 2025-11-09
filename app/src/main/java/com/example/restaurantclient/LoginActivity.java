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

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private Button btnLogin, btnRegister, btnGuest;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGuest = findViewById(R.id.btnGuest);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> showRegisterDialog());
        btnGuest.setOnClickListener(v -> enterAsGuest());

        checkExistingSession();
    }

    private void checkExistingSession() {

        AuthApi authApi = ApiClient.getAuthApi(this);

        authApi.checkAuth().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isAuthenticated()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
            }
        });
    }


    private void login() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // ЗАПРОС АВТОРИЗАЦИИ к серверу
        AuthApi authApi = ApiClient.getAuthApi(this);
        authApi.login(login, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.isSuccess()) {
                        Toast.makeText(LoginActivity.this, "Успешный вход", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, authResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Ошибка сервера", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showRegisterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_register, null);

        EditText etRegLogin = view.findViewById(R.id.etLogin);
        EditText etRegPassword = view.findViewById(R.id.etPassword);
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etEmail = view.findViewById(R.id.etEmail);

        builder.setView(view)
                .setTitle("Регистрация")
                .setPositiveButton("Зарегистрироваться", (dialog, which) -> {
                    String login = etRegLogin.getText().toString().trim();
                    String password = etRegPassword.getText().toString().trim();
                    String fullName = etFullName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();

                    if (!login.isEmpty() && !password.isEmpty()) {
                        register(login, password, fullName, email);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void register(String login, String password, String fullName, String email) {
        AuthApi authApi = ApiClient.getAuthApi(this);
        authApi.register(login, password, fullName, email).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    Toast.makeText(LoginActivity.this, authResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    if (authResponse.isSuccess()) {
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

    private void enterAsGuest() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("isGuest", true);
        startActivity(intent);
        finish();
    }
}