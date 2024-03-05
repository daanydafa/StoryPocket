package com.example.storypocket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.storypocket.databinding.ActivityReadStoryBinding;

public class ReadStoryActivity extends AppCompatActivity {
    public static final String EXTRA_TASK = "extra_task";
    private Intent story;
    private ActivityReadStoryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        story = getIntent();

        binding = ActivityReadStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Glide.with(binding.cover.getContext())
                .load(story.getStringExtra("cover"))
                .placeholder(R.drawable.blank_cover)
                .error(R.drawable.blank_cover)
                .into(binding.cover);

        binding.titleStory.setText(story.getStringExtra("title"));
        binding.authorStory.setText(story.getStringExtra("author"));
        binding.tvText.setText(story.getStringExtra("text"));

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}