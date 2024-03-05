package com.example.storypocket;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storypocket.databinding.ItemStoryBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StoryAdapter extends FirebaseRecyclerAdapter<Story,StoryAdapter.myViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */

    public StoryAdapter(@NonNull FirebaseRecyclerOptions<Story> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull Story model) {

        holder._title.setText(model.getTitle());
        holder._author.setText(model.getAuthor());
        holder._desc.setText(model.getDesc());

        Glide.with(holder._cover.getContext())
                .load(model.getCover())
                .placeholder(R.drawable.blank_cover)
                .error(com.firebase.ui.auth.R.drawable.common_google_signin_btn_icon_dark_normal)
                .centerCrop()
                .into(holder._cover);

        holder.binding.storyLoyout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent readIntent = new Intent(v.getContext(), ReadStoryActivity.class);
                readIntent.putExtra("title", model.getTitle());
                readIntent.putExtra("author", model.getAuthor());
                readIntent.putExtra("text", model.getText());
                readIntent.putExtra("cover", model.getCover());

                v.getContext().startActivity(readIntent);
            }
        });
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStoryBinding binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new myViewHolder(binding);
    }

    static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView _cover;
        TextView _title, _author, _desc;

        final ItemStoryBinding binding;

        public myViewHolder(@NonNull ItemStoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            _cover = binding.cover;
            _title = binding.titleStory;
            _author = binding.authorStory;
            _desc = binding.descStory;
        }
    }
}
