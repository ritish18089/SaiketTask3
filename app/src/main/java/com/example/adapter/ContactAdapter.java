package com.example.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.databinding.ItemContactBinding;
import com.example.model.Contact;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<Contact> contacts = new ArrayList<>();
    private final OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(Contact contact, View sharedElement);
        void onCallClick(Contact contact);
    }

    public ContactAdapter(OnContactClickListener listener) {
        this.listener = listener;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContactBinding binding = ItemContactBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        final ItemContactBinding binding;

        ContactViewHolder(ItemContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Contact contact) {
            String name = contact.getName();
            if (name == null) name = "Unknown";
            binding.tvContactName.setText(name);
            
            // Set Primary Number
            if (!contact.getPhoneNumbers().isEmpty()) {
                String number = contact.getPhoneNumbers().get(0).getValue();
                binding.tvPrimaryNumber.setVisibility(View.VISIBLE);
                binding.tvPrimaryNumber.setText(number != null ? number : "");
            } else {
                binding.tvPrimaryNumber.setVisibility(View.GONE);
            }

            // Set Avatar or Initials
            String photoUri = contact.getPhotoUri();
            if (photoUri != null && !photoUri.isEmpty()) {
                binding.ivContactAvatar.setVisibility(View.VISIBLE);
                binding.tvInitials.setVisibility(View.GONE);
                try {
                    binding.ivContactAvatar.setImageURI(Uri.parse(photoUri));
                } catch (Exception e) {
                    binding.ivContactAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } else {
                binding.ivContactAvatar.setVisibility(View.GONE);
                binding.tvInitials.setVisibility(View.VISIBLE);
                binding.tvInitials.setText(getInitials(name));
                
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.OVAL);
                shape.setColor(getColorForName(name));
                binding.avatarContainer.setBackground(shape);
            }

            // Click Listeners
            binding.getRoot().setOnClickListener(v -> listener.onContactClick(contact, binding.ivContactAvatar));
            binding.btnCallAction.setOnClickListener(v -> listener.onCallClick(contact));
        }

        private String getInitials(String name) {
            if (name == null || name.isEmpty()) return "?";
            String[] parts = name.split(" ");
            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < Math.min(parts.length, 2); i++) {
                if (!parts[i].isEmpty()) initials.append(parts[i].charAt(0));
            }
            return initials.toString().toUpperCase();
        }

        private int getColorForName(String name) {
            if (name == null || name.isEmpty()) return Color.GRAY;
            int hash = name.hashCode();
            int r = (hash & 0xFF0000) >> 16;
            int g = (hash & 0x00FF00) >> 8;
            int b = (hash & 0x0000FF);
            // Ensure colors are not too light for white text
            return Color.rgb(Math.min(r, 200), Math.min(g, 200), Math.min(b, 200));
        }
    }
}
