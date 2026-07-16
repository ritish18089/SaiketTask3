package com.example.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.R;
import com.example.databinding.ActivityFaqBinding;
import com.example.databinding.ItemFaqExpandableBinding;

public class FaqActivity extends BaseActivity {
    private ActivityFaqBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFaqBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        populateFaqs();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void populateFaqs() {
        addFaqItem(R.string.q1, R.string.a1);
        addFaqItem(R.string.q2, R.string.a2);
        addFaqItem(R.string.q3, R.string.a3);
        addFaqItem(R.string.q4, R.string.a4);
        addFaqItem(R.string.q5, R.string.a5);
        addFaqItem(R.string.q6, R.string.a6);
        addFaqItem(R.string.q7, R.string.a7);
        addFaqItem(R.string.q8, R.string.a8);
        addFaqItem(R.string.q9, R.string.a9);
        addFaqItem(R.string.q10, R.string.a10);
        addFaqItem(R.string.q11, R.string.a11);
        addFaqItem(R.string.q12, R.string.a12);
        addFaqItem(R.string.q13, R.string.a13);
        addFaqItem(R.string.q14, R.string.a14);
    }

    private void addFaqItem(int questionRes, int answerRes) {
        ItemFaqExpandableBinding faqBinding = ItemFaqExpandableBinding.inflate(getLayoutInflater(), binding.llFaqContainer, false);
        faqBinding.tvQuestion.setText(getString(questionRes));
        faqBinding.tvAnswer.setText(getString(answerRes));
        faqBinding.ivExpand.setImageResource(R.drawable.ic_chevron_down);

        faqBinding.llHeader.setOnClickListener(v -> {
            boolean isExpanded = faqBinding.tvAnswer.getVisibility() == View.VISIBLE;
            faqBinding.tvAnswer.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            faqBinding.ivExpand.setRotation(isExpanded ? 0 : 180);
        });

        binding.llFaqContainer.addView(faqBinding.getRoot());
    }
}
