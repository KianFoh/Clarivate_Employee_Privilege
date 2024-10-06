package com.example.clarivate_employee_privilege.navbar_menu.merchant_detail;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

public class Merchant_Detail_Addresses_Adapter extends RecyclerView.Adapter<Merchant_Detail_Addresses_Adapter.AddressViewHolder> {
    private List<String> addressList;

    public Merchant_Detail_Addresses_Adapter(List<String> addressList) {
        this.addressList = addressList;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.merchant_detail_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        String address = addressList.get(position);
        holder.addressTextView.setText(address);

        // Set OnLongClickListener to copy the address to clipboard
        holder.layout.setOnLongClickListener(v -> {
            // Flash animation
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(holder.container, "alpha", 1f, 0.5f);
            fadeOut.setDuration(100);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(holder.container, "alpha", 0.5f, 1f);
            fadeIn.setDuration(100);
            AnimatorSet flash = new AnimatorSet();
            flash.playSequentially(fadeOut, fadeIn);
            flash.start();

            // Copy address to clipboard
            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("address", address);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(v.getContext(), "Address copied to clipboard", Toast.LENGTH_SHORT).show();
            return true; // Return true to indicate the long click was handled
        });

        holder.copyButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(address));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            v.getContext().startActivity(mapIntent);
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        public TextView addressTextView;
        public ImageButton copyButton;
        public View layout;
        public View container;

        public AddressViewHolder(View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.merchantdetail_address);
            copyButton = itemView.findViewById(R.id.merchantdetail_copy);
            layout = itemView.findViewById(R.id.merchantdetail_address_layout);
            container = itemView.findViewById(R.id.merchantdetail_address_container);
        }
    }
}