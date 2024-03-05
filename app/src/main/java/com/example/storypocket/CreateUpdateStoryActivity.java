package com.example.storypocket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.storypocket.databinding.ActivityCreateUpdateStoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.internal.operators.observable.ObservableElementAt;

public class CreateUpdateStoryActivity extends AppCompatActivity {
    private final String editHead = "Update Story";
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private StorageReference storageReference;
    private Intent myStory;
    private static final int PICK_IMAGE_REQUEST = 1 ;
    private Uri uriImg;


    private ActivityCreateUpdateStoryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        binding = ActivityCreateUpdateStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myStory = getIntent();

        database  = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("StorysCover");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        boolean isEdit = myStory.hasExtra("title");

        String actionBarTitle = isEdit ? getString(R.string.update) : getString(R.string.create_title);
        String btnTitle = isEdit ? getString(R.string.save) : getString(R.string.publish);

        if (isEdit) {
                binding.etTitle.setText(myStory.getStringExtra("title"));
                binding.etDesc.setText(myStory.getStringExtra("desc"));
                binding.etText.setText(myStory.getStringExtra("text"));

                Glide.with(binding.cover.getContext())
                        .load(myStory.getStringExtra("cover"))
                        .placeholder(R.drawable.blank_cover)
                        .error(R.drawable.blank_cover)
                        .centerCrop()
                        .into(binding.cover);
        }

        binding.btnAdd.setText(btnTitle);
        binding.header.setText(actionBarTitle);

        binding.cover.setOnClickListener(view -> {
            openFileChooser();
        });

        binding.btnAdd.setOnClickListener(view -> {
            String _title = binding.etTitle.getText().toString().trim();
            String _desc = binding.etDesc.getText().toString().trim();
            String _text = binding.etText.getText().toString().trim();

            if (_title.isEmpty()) {
                binding.etTitle.setError(getString(R.string.empty));
            } else if (_desc.isEmpty()) {
                binding.etDesc.setError(getString(R.string.empty));
            } else if (_text.isEmpty()) {
                binding.etText.setError(getString(R.string.empty));
            } else {
                Map <String,Object> map = new HashMap<>();

                if (isEdit) {
                    //Update Cerita
                    String coverId = myStory.getStringExtra("coverID");

                    StorageReference fileRef = storageReference.child(coverId);
                    fileRef.putFile(uriImg).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CreateUpdateStoryActivity.this, "Upload cover berhasil", Toast.LENGTH_SHORT).show();

                            // Menggunakan addOnSuccessListener untuk mendapatkan URL unduhan
                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUrl) {
                                    // Mendapatkan URL unduhan
                                    if (downloadUrl != null){
                                        map.put("cover", downloadUrl.toString());
                                    }
                                    map.put("title", _title);
                                    map.put("desc", _desc);
                                    map.put("text", _text);
                                    map.put("date_upload", ServerValue.TIMESTAMP);

                                    FirebaseDatabase.getInstance().getReference().child("storys")
                                            .child(myStory.getStringExtra("position")).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(getBaseContext(), "Cerita berhasil diubah", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(binding.head.getContext(), "Cerita gagal diubah", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    });
                } else {
                    // Buat cerita
                    FirebaseUser user = mAuth.getCurrentUser();

                    String email = user.getEmail().replace(".","");
                    DatabaseReference profileRef = database.child(email);

                    profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String fullName = snapshot.child("fullname").getValue(String.class);
                                if (fullName != null) {
                                    if (uriImg!=null){
                                        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uriImg));

                                        fileRef.putFile(uriImg)
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        Toast.makeText(CreateUpdateStoryActivity.this, "Upload cover berhasil", Toast.LENGTH_SHORT).show();

                                                        // Menggunakan addOnSuccessListener untuk mendapatkan URL unduhan
                                                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri downloadUrl) {
                                                                // Mendapatkan URL unduhan

                                                                map.put("cover", downloadUrl.toString());
                                                                map.put("author", fullName);
                                                                map.put("date_upload", ServerValue.TIMESTAMP);
                                                                map.put("desc", _desc);
                                                                map.put("text", _text);
                                                                map.put("title", _title);

                                                                FirebaseDatabase.getInstance().getReference().child("storys").push()
                                                                        .setValue(map)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                Toast.makeText(CreateUpdateStoryActivity.this, "Cerita berhasil diunggah", Toast.LENGTH_SHORT).show();
                                                                                finish();
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Toast.makeText(CreateUpdateStoryActivity.this, "Cerita gagal diunggah", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(CreateUpdateStoryActivity.this, "Upload cover gagal", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(CreateUpdateStoryActivity.this, "Cover cerita perlu dilengkapi", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Handle the case where fullname is null
                                    Toast.makeText(CreateUpdateStoryActivity.this, "Fullname is null", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Handle the case where the snapshot does not exist
                                Toast.makeText(CreateUpdateStoryActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error
                            Toast.makeText(CreateUpdateStoryActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        binding.btnBack.setOnClickListener(view -> {
            showAlertDialog();
        });
    }

    private void showAlertDialog() {
        String dialogTitle, dialogMessage;

        dialogTitle = binding.header.getText().equals(editHead)? getString(R.string.cancel_edit) : getString(R.string.cancel);
        dialogMessage = binding.header.getText().equals(editHead)? getString(R.string.message_cancel_edit) : getString(R.string.message_cancel);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(dialogTitle);

        alertDialogBuilder
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes),
                        (dialog, id) -> {
                            finish();
                        })
                .setNegativeButton(getString(R.string.no),
                        (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1 && data != null && data.getData() != null){
            uriImg = data.getData();

            Glide.with(binding.cover.getContext())
                    .load(uriImg)
                    .placeholder(R.drawable.blank_cover)
                    .error(R.drawable.blank_cover)
                    .centerCrop()
                    .into(binding.cover);
        }
    }

    private String getFileExtension(Uri uriImg) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime =  MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uriImg));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}