package com.pridetechnologies.businesscard;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pridetechnologies.businesscard.models.FileCompressor;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalBasicsActivity extends AppCompatActivity {

    private TextInputEditText firstName, otherNames, surname, profession, userBioInput, mobile, whatsApp;

    static final int REQUEST_TAKE_PHOTO = 9;
    static final int REQUEST_GALLERY_PHOTO = 2;
    File mPhotoFile;
    FileCompressor mCompressor;
    private Uri resultUri = null;

    //private Button addBtn, saveBtn;
    private CircleImageView capturedImg;

    private String admin_id;
    private StorageReference storage;
    private DatabaseReference AdminRef;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_basics);

        progressBar = (ProgressBar) findViewById(R.id.basicsProgressBar);

        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageButton99);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mCompressor = new FileCompressor(this);
        capturedImg = findViewById(R.id.imageView15);

        firebaseAuth = FirebaseAuth.getInstance();
        admin_id = firebaseAuth.getCurrentUser().getUid();
        storage = FirebaseStorage.getInstance().getReference();
        AdminRef = FirebaseDatabase.getInstance().getReference().child("Users");
        db = FirebaseFirestore.getInstance();

        firstName = (TextInputEditText) findViewById(R.id.first_name);
        otherNames = (TextInputEditText) findViewById(R.id.other_name);
        surname = (TextInputEditText) findViewById(R.id.surname);
        profession = (TextInputEditText) findViewById(R.id.profession);
        userBioInput = (TextInputEditText) findViewById(R.id.user_bio);
        mobile = (TextInputEditText) findViewById(R.id.mobile);
        whatsApp = (TextInputEditText) findViewById(R.id.whatsApp);

        retrieveUserInfo();
    }

    private void retrieveUserInfo() {

        AdminRef.child(admin_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (snapshot.hasChild("user_image"))
                    {
                        String Image = snapshot.child("user_image").getValue().toString();
                        Picasso.get().load(Image).placeholder(R.mipmap.user_gold).into(capturedImg);
                    }
                    if ((snapshot.hasChild("user_first_name")))
                    {
                        String FirstName = snapshot.child("user_first_name").getValue().toString();
                        String OtherNames= snapshot.child("user_other_names").getValue().toString();
                        String Surname= snapshot.child("user_surname").getValue().toString();
                        firstName.setText(FirstName);
                        otherNames.setText(OtherNames);
                        surname.setText(Surname);

                    }
                    if ((snapshot.hasChild("user_profession")))
                    {
                        String Profession = snapshot.child("user_profession").getValue().toString();
                        profession.setText(Profession);
                    }
                    if ((snapshot.hasChild("user_bio")))
                    {
                        String Bio = snapshot.child("user_bio").getValue().toString();
                        userBioInput.setText(Bio);
                    }
                    if ((snapshot.hasChild("user_mobile")))
                    {
                        String Mobile = snapshot.child("user_mobile").getValue().toString();
                        mobile.setText(Mobile);
                    }
                    if ((snapshot.hasChild("user_whatsapp")))
                    {
                        String WhatsApp = snapshot.child("user_whatsapp").getValue().toString();
                        whatsApp.setText(WhatsApp);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void changePhoto(View view) {
        Dialog dialog = new Dialog(PersonalBasicsActivity.this);
        dialog.setContentView(R.layout.custom_link_company_dialog);
        ImageView scanB = (ImageView) dialog.findViewById(R.id.imageView40);
        ImageView libB = (ImageView) dialog.findViewById(R.id.imageView50);
        Button cancelBtn = (Button) dialog.findViewById(R.id.button58);
        TextView titleView = (TextView) dialog.findViewById(R.id.textView123);
        titleView.setText("Change Image");
        TextView textView = (TextView) dialog.findViewById(R.id.textView126);
        textView.setText("CAMERA");
        scanB.setImageDrawable(getResources().getDrawable(R.drawable.camera));
        scanB.setOnClickListener(v -> {
            requestStoragePermission(true);
            dialog.dismiss();
        });
        libB.setOnClickListener(v -> {
            dialog.dismiss();
            requestStoragePermission(false);
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
    private void requestStoragePermission(final boolean isCamera) {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            if (isCamera) {
                                dispatchTakePictureIntent();
                            } else {
                                dispatchGalleryIntent();
                            }
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(
                        new PermissionRequestErrorListener() {
                            @Override
                            public void onError(DexterError error) {
                                Toast.makeText(PersonalBasicsActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Need Permissions");
        builder.setMessage(
                "This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                PersonalBasicsActivity.this.openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                mPhotoFile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
    /**
     * Select image fro gallery
     */
    private void dispatchGalleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    private File createImageFile()  throws IOException{

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }

    /**
     * Get real file path from URI
     */
    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    mPhotoFile = mCompressor.compressToFile(mPhotoFile);
                    resultUri = Uri.fromFile(mPhotoFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressBar.setVisibility(ProgressBar.VISIBLE);

                StorageReference filepath = storage.child("User Profile Images/" + admin_id
                        + "."+ getFileExtension(resultUri));

                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                        while (!downloadUrl.isComplete());
                        Uri profilePhotoUri = downloadUrl.getResult();

                        Map<String, Object> applicantMap = new HashMap<>();
                        applicantMap.put("user_image", profilePhotoUri.toString().trim());

                        AdminRef.child(admin_id).updateChildren(applicantMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    db.collection("Users").document(admin_id)
                                            .update(applicantMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                        Toast.makeText(PersonalBasicsActivity.this, "Update Successful", Toast.LENGTH_LONG).show();

                                                    }else
                                                    {
                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(PersonalBasicsActivity.this, "Error:" + error, Toast.LENGTH_LONG).show();
                                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                    }
                                                }
                                            });
                                }else
                                {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(PersonalBasicsActivity.this, "Error:" + error, Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                                }

                            }
                        });
                    }
                });
                Picasso.get()
                        .load(mPhotoFile)
                        .fit()
                        .centerCrop().into(capturedImg);
            } else if (requestCode == REQUEST_GALLERY_PHOTO) {
                Uri selectedImage = data.getData();
                try {
                    mPhotoFile = mCompressor.compressToFile(new File(getRealPathFromUri(selectedImage)));
                    resultUri = Uri.fromFile(mPhotoFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressBar.setVisibility(ProgressBar.VISIBLE);

                StorageReference filepath = storage.child("User Profile Images/" + admin_id
                        + "."+ getFileExtension(resultUri));

                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                        while (!downloadUrl.isComplete());
                        Uri profilePhotoUri = downloadUrl.getResult();

                        Map<String, Object> applicantMap = new HashMap<>();
                        applicantMap.put("user_image", profilePhotoUri.toString().trim());

                        AdminRef.child(admin_id).updateChildren(applicantMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                                    Toast.makeText(PersonalBasicsActivity.this, "Update Successful", Toast.LENGTH_LONG).show();

                                }else
                                {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(PersonalBasicsActivity.this, "Error:" + error, Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                                }
                            }
                        });
                    }
                });
                Picasso.get()
                        .load(mPhotoFile)
                        .fit()
                        .centerCrop().into(capturedImg);
            }
        }
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void saveDetails(View view) {

        progressBar.setVisibility(ProgressBar.VISIBLE);


        final String FirstName = firstName.getText().toString().trim();
        final String OtherNames = otherNames.getText().toString().trim();
        final String SurName = surname.getText().toString().trim();
        final String Profession = profession.getText().toString().trim();
        final String BIO = userBioInput.getText().toString().trim();
        final String Mobile = mobile.getText().toString().trim();
        final String WhatsApp = whatsApp.getText().toString().trim();

        Map<String, Object> usersMap = new HashMap<>();
        usersMap.put("user_first_name", FirstName);
        usersMap.put("user_other_names", OtherNames);
        usersMap.put("user_surname", SurName);
        usersMap.put("user_mobile", Mobile);
        usersMap.put("user_whatsapp", WhatsApp);
        usersMap.put("user_profession", Profession);
        usersMap.put("user_bio", BIO);


        AdminRef.child(admin_id).updateChildren(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    Toast.makeText(PersonalBasicsActivity.this, "Update Successful", Toast.LENGTH_LONG).show();

                }else
                {
                    String error = task.getException().getMessage();
                    Toast.makeText(PersonalBasicsActivity.this, "Error:" + error, Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                }

            }
        });
    }
}