package com.example.storypocket;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.storypocket.databinding.ActivityMainBinding;
import com.example.storypocket.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;

public class ProfileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1 ;
    private Uri uriImg;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        database = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("UsersPhoto");

        binding.addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Panggil metode untuk memuat profil
        loadProfile();

        return root;
    }

    // Pop up user pilih file
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Gambar pilihan user ke UI
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1 && data != null && data.getData() != null){
            uriImg = data.getData();

            Glide.with(binding.photo.getContext())
                    .load(uriImg)
                    .placeholder(R.drawable.blank_profile)
                    .error(R.drawable.baseline_person_24)
                    .circleCrop()
                    .into(binding.photo);
        }
    }

    private void loadProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail().replace(".", "");
            DatabaseReference profileRef = database.child(email);

            // Ambil gambar dari database
            Uri uri = currentUser.getPhotoUrl();

            // Gambar ke UI
            Glide.with(binding.photo.getContext())
                    .load(uri)
                    .placeholder(R.drawable.blank_profile)
                    .error(R.drawable.baseline_person_24)
                    .circleCrop()
                    .into(binding.photo);

            // Mendengarkan perubahan pada data di database
            profileRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        String fullName = snapshot.child("fullname").getValue(String.class);
                        String phoneNumber = snapshot.child("phonenumber").getValue(String.class);
                        String userName = snapshot.child("username").getValue(String.class);

                        // Set data ke EditText
                        binding.etFullName.setText(fullName);
                        binding.etPhoneNum.setText(phoneNumber);
                        binding.etUserName.setText(userName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void logoutUser() {
        mAuth.signOut();

        // Beralih ke halaman login (atau halaman lain yang sesuai)
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail().replace(".", "");
            String fullName = binding.etFullName.getText().toString().trim();
            String phoneNumber = binding.etPhoneNum.getText().toString().trim();
            String userName = binding.etUserName.getText().toString().trim();

            DatabaseReference profileRef = database.child(email);

            if (!fullName.isEmpty() && !phoneNumber.isEmpty() && !userName.isEmpty()) {
                // Upload  photo di Firebase Storage
                if (uriImg != null) {
                    StorageReference fileRef = storageReference.child(email + "." + getFileExtension(uriImg));
                    fileRef.putFile(uriImg).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUri = uri;
                                    UserProfileChangeRequest profUpdate = new  UserProfileChangeRequest.Builder()
                                            .setPhotoUri(downloadUri).build();
                                    currentUser.updateProfile(profUpdate);
                                }
                            });
                            Toast.makeText(getContext(), "Upload Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                // Update data di Firebase Realtime Database
                profileRef.child("fullname").setValue(fullName);
                profileRef.child("phonenumber").setValue(phoneNumber);
                profileRef.child("username").setValue(userName);

                Toast.makeText(getContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFileExtension(Uri uriImg) {
        ContentResolver cr = getContext().getContentResolver();
        MimeTypeMap mime =  MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uriImg));
    }

}