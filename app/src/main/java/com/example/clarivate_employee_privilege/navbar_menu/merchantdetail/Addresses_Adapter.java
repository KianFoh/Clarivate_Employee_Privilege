package com.example.clarivate_employee_privilege.navbar_menu.merchantdetail;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clarivate_employee_privilege.R;

import java.util.List;

public class Addresses_Adapter extends RecyclerView.Adapter<Addresses_Adapter.AddressViewHolder> {
    private List<String> addressList;

    public Addresses_Adapter(List<String> addressList) {
        this.addressList = addressList;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.merchantdetail_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        String address = addressList.get(position);
        holder.addressTextView.setText(address);
        holder.copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("address", address);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(v.getContext(), "Address copied to clipboard", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        public TextView addressTextView;
        public ImageButton copyButton;

        public AddressViewHolder(View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.merchantdetail_address);
            copyButton = itemView.findViewById(R.id.merchantdetail_copy);
        }
    }
}