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


 // Связывает данные о клиентах (List<Client>) с элементами списка в UI

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ViewHolder> {
    private List<Client> clients;
    private OnClientClickListener listener;

    public interface OnClientClickListener {
        void onEditClick(Client client);
        void onDeleteClick(Client client);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvContacts;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvContacts = itemView.findViewById(R.id.tvContacts);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public ClientAdapter(List<Client> clients, OnClientClickListener listener) {
        this.clients = clients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Client client = clients.get(position);
        holder.tvFullName.setText(client.getFullName());
        holder.tvContacts.setText(client.getContacts() != null ? client.getContacts() : "Нет контактов");

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(client);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(client);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }
}