package com.example.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.data.models.BlockedNumber;
import com.example.databinding.ActivityBlockedNumbersBinding;
import com.example.databinding.ItemBlockedNumberBinding;
import com.example.viewmodel.ContactViewModel;
import java.util.ArrayList;
import java.util.List;

public class BlockedNumbersActivity extends BaseActivity {
    private ActivityBlockedNumbersBinding binding;
    private ContactViewModel viewModel;
    private BlockedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlockedNumbersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        setupRecyclerView();

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.btnAdd.setOnClickListener(v -> {
            String number = binding.etNumber.getText().toString();
            if (!number.isEmpty()) {
                viewModel.blockNumber(number, "Manual Block");
                binding.etNumber.setText("");
            }
        });

        viewModel.getBlockedNumbers().observe(this, blockedNumbers -> adapter.setBlockedNumbers(blockedNumbers));
    }

    private void setupRecyclerView() {
        adapter = new BlockedAdapter();
        binding.rvBlocked.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBlocked.setAdapter(adapter);
    }

    class BlockedAdapter extends RecyclerView.Adapter<BlockedAdapter.ViewHolder> {
        private List<BlockedNumber> list = new ArrayList<>();

        void setBlockedNumbers(List<BlockedNumber> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemBlockedNumberBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BlockedNumber bn = list.get(position);
            holder.binding.tvNumber.setText(bn.getNumber());
            holder.binding.btnDelete.setOnClickListener(v -> viewModel.unblockNumber(bn));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemBlockedNumberBinding binding;
            ViewHolder(ItemBlockedNumberBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
