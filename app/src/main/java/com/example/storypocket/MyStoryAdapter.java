package com.example.storypocket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storypocket.databinding.ItemMystoryBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyStoryAdapter extends FirebaseRecyclerAdapter<Story, MyStoryAdapter.myViewModel> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    private DatabaseReference database  = FirebaseDatabase.getInstance().getReference("storys");;

    public MyStoryAdapter(@NonNull FirebaseRecyclerOptions<Story> options) {
        super(options);
    }

    @SuppressLint("RecyclerView")
    @Override
    protected void onBindViewHolder(@NonNull myViewModel holder, int position, @NonNull Story model) {


        //Date formating
        long timestamp = (long) model.getDate_upload();
        Date date = new Date(timestamp);
        SimpleDateFormat sdfFormatted = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String formattedDate = sdfFormatted.format(date);

        holder._title.setText(model.getTitle());
        holder._date.setText(formattedDate);

        Glide.with(holder._cover.getContext())
                .load(model.getCover())
                .placeholder(R.drawable.blank_cover)
                .error(com.firebase.ui.auth.R.drawable.common_google_signin_btn_icon_dark_normal)
                .centerCrop()
                .into(holder._cover);

        holder.binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.child(getRef(position).getKey()).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(v.getContext(), "Delete berhasil", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        holder.binding.mystoryLoyout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get cover ID
                String link = model.getCover();
                String path = link.substring(link.indexOf("%2F") + 1, link.indexOf("?"));
                String coverId = path.substring(path.indexOf("%2F") + 3);

                Intent readIntent = new Intent(v.getContext(), CreateUpdateStoryActivity.class);
                readIntent.putExtra("title", model.getTitle());
                readIntent.putExtra("desc", model.getDesc());
                readIntent.putExtra("text", model.getText());
                readIntent.putExtra("cover", model.getCover());
                readIntent.putExtra("coverID", coverId);
                readIntent.putExtra("position", getRef(position).getKey());
                v.getContext().startActivity(readIntent);
            }
        });
    }

    @NonNull
    @Override
    public myViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMystoryBinding binding = ItemMystoryBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new myViewModel(binding);
    }

    public class myViewModel extends RecyclerView.ViewHolder {
        ImageView _cover, _pen;
        TextView _title, _date;
        final ItemMystoryBinding binding;


        public myViewModel(ItemMystoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            _cover = binding.cover;
            _title = binding.titleMystory;
            _date = binding.updatedDate;
            _pen = binding.pen;
        }
    }

}
