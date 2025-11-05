package com.example.restaurantclient.adapter;

import com.example.restaurantclient.R;
import com.example.restaurantclient.models.Client;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * КЛАСС: ClientAdapter (Адаптер для RecyclerView)
 * НАЗНАЧЕНИЕ: Связывает данные о клиентах (List<Client>) с элементами списка в UI
 * ПАТТЕРН: Adapter Pattern - мост между данными и представлением
 * КОМПОНЕНТ: Часть Android Architecture - RecyclerView.Adapter
 */
public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ViewHolder> {
    // Источник данных - список клиентов для отображения
    private List<Client> clients;

    // Колбэк для обработки кликов по элементам списка
    private OnClientClickListener listener;

    /**
     * ИНТЕРФЕЙС: OnClientClickListener
     * НАЗНАЧЕНИЕ: Определяет контракт для обработки действий пользователя
     * ПАТТЕРН: Callback Interface - позволяет Activity/Fragment реагировать на клики
     */
    public interface OnClientClickListener {
        void onEditClick(Client client);  // Вызывается при нажатии "Редактировать"
        void onDeleteClick(Client client); // Вызывается при нажатии "Удалить"
    }

    /**
     * ВЛОЖЕННЫЙ КЛАСС: ViewHolder
     * НАЗНАЧЕНИЕ: Кэширует ссылки на View элементы для быстрого доступа
     * ОПТИМИЗАЦИЯ: Избегает многократного findViewById() при прокрутке
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Элементы UI для отображения данных клиента
        TextView tvFullName, tvContacts;

        // Кнопки действий для каждого клиента
        ImageButton btnEdit, btnDelete;

        /**
         * КОНСТРУКТОР ViewHolder: инициализирует ссылки на View элементы
         * @param itemView - корневое View элемента списка (item_client.xml)
         */
        public ViewHolder(View itemView) {
            super(itemView);
            // Находим все View элементы по их ID из макета
            tvFullName = itemView.findViewById(R.id.tvFullName);    // Поле для ФИО
            tvContacts = itemView.findViewById(R.id.tvContacts);    // Поле для контактов
            btnEdit = itemView.findViewById(R.id.btnEdit);          // Кнопка редактирования (✏️)
            btnDelete = itemView.findViewById(R.id.btnDelete);      // Кнопка удаления (🗑️)
        }
    }

    /**
     * КОНСТРУКТОР ClientAdapter: инициализация адаптера
     * @param clients - список клиентов для отображения
     * @param listener - обработчик кликов (обычно Activity/Fragment)
     */
    public ClientAdapter(List<Client> clients, OnClientClickListener listener) {
        this.clients = clients;
        this.listener = listener;
    }

    /**
     * МЕТОД: onCreateViewHolder - создание нового ViewHolder
     * ВЫЗЫВАЕТСЯ: Когда RecyclerView нужен новый элемент списка
     * @param parent - контейнер RecyclerView
     * @param viewType - тип View (не используется в этом адаптере)
     * @return новый экземпляр ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Надуваем" макет элемента списка из XML
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);
        return new ViewHolder(view);
    }

    /**
     * МЕТОД: onBindViewHolder - привязка данных к ViewHolder
     * ВЫЗЫВАЕТСЯ: Для заполнения элемента списка данными
     * ОПТИМИЗАЦИЯ: Вызывается только для видимых элементов
     * @param holder - ViewHolder для заполнения
     * @param position - позиция в списке данных
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Получаем клиента для текущей позиции
        Client client = clients.get(position);

        // Устанавливаем данные в TextView
        holder.tvFullName.setText(client.getFullName());

        // Проверяем наличие контактов, чтобы избежать отображения "null"
        holder.tvContacts.setText(client.getContacts() != null ? client.getContacts() : "Нет контактов");

        /**
         * ОБРАБОТЧИК КЛИКА: Редактирование клиента
         * ПЕРЕДАЧА УПРАВЛЕНИЯ: Вызывается колбэк в Activity/Fragment
         * ДЕЛЕГИРОВАНИЕ: Адаптер не знает как обрабатывать клики, только уведомляет
         */
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(client); // Передаем кликаемого клиента
            }
        });

        /**
         * ОБРАБОТЧИК КЛИКА: Удаление клиента
         * БЕЗОПАСНОСТЬ: Проверка listener != null перед вызовом
         * ДЕЛЕГИРОВАНИЕ: Activity/Fragment покажет диалог подтверждения
         */
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(client); // Передаем кликаемого клиента
            }
        });
    }

    /**
     * МЕТОД: getItemCount - количество элементов в списке
     * ВЫЗЫВАЕТСЯ: RecyclerView для определения размера списка
     * @return количество клиентов для отображения
     */
    @Override
    public int getItemCount() {
        return clients.size();
    }
}