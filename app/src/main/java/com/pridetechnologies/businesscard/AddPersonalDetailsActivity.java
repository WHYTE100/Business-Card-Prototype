package com.pridetechnologies.businesscard;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.hbb20.CountryCodePicker;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pridetechnologies.businesscard.Sinch.BaseActivity;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPersonalDetailsActivity extends BaseActivity {

    private TextInputEditText firstName, otherNames, surname, profession, mobile, whatsApp;

    private CountryCodePicker mobileCode, whatsAppCode;

    String MobileCode = null;
    String WhatsAppCode = null;

    String Mobile = "";
    String WhatsApp ="";


    private String adminId;
    private DatabaseReference AdminRef;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;

    private Uri codeUri;

    private StorageReference storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_personal_details);
        progressBar = (ProgressBar) findViewById(R.id.addProgressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        adminId = firebaseAuth.getCurrentUser().getUid();
        storage = FirebaseStorage.getInstance().getReference();
        AdminRef = FirebaseDatabase.getInstance().getReference().child("Users");
        db = FirebaseFirestore.getInstance();

        firstName = (TextInputEditText) findViewById(R.id.first_name);
        otherNames = (TextInputEditText) findViewById(R.id.other_name);
        surname = (TextInputEditText) findViewById(R.id.surname);
        profession = (TextInputEditText) findViewById(R.id.profession);
        mobile = (TextInputEditText) findViewById(R.id.mobile);
        whatsApp = (TextInputEditText) findViewById(R.id.whatsApp);
        mobileCode = (CountryCodePicker) findViewById(R.id.ccp3);
        whatsAppCode = (CountryCodePicker) findViewById(R.id.ccp4);

        MobileCode = mobileCode.getSelectedCountryCodeWithPlus();
        WhatsAppCode = whatsAppCode.getSelectedCountryCodeWithPlus();

        mobile.setText(MobileCode);
        whatsApp.setText(WhatsAppCode);

        mobileCode.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                //Alert.showMessage(RegistrationActivity.this, ccp.getSelectedCountryCodeWithPlus());
                //selected_country_code = countryPicker.getSelectedCountryCodeWithPlus();

                MobileCode = mobileCode.getSelectedCountryCodeWithPlus();
                //String name = countryPicker.getSelectedCountryName();
                mobile.setText(MobileCode);
                //Toast.makeText(EditPersonDetailsActivity.this, "Updated " + code+" " + name, Toast.LENGTH_SHORT).show();
            }
        });
        whatsAppCode.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                //Alert.showMessage(RegistrationActivity.this, ccp.getSelectedCountryCodeWithPlus());
                //selected_country_code = countryPicker.getSelectedCountryCodeWithPlus();

                WhatsAppCode = whatsAppCode.getSelectedCountryCodeWithPlus();
                //String name = countryPicker.getSelectedCountryName();
                //Toast.makeText(EditPersonDetailsActivity.this, "Updated " + code+" " + name, Toast.LENGTH_SHORT).show();
                whatsApp.setText(WhatsAppCode);
            }
        });
    }

    public void proceed(View view) {
        final String FirstName = firstName.getText().toString().trim();
        final String OtherNames = otherNames.getText().toString().trim();
        final String SurName = surname.getText().toString().trim();
        final String Profession = profession.getText().toString().trim();
        //Toast.makeText(AddPersonalDetailsActivity.this, Title+FirstName+OtherNames+SurName+Profession+MobileCode+Mobile+WhatsAppCode+WhatsApp, Toast.LENGTH_LONG).show();

        if (mobile==null)
        {
            Mobile = "";
        } else
        {
            if (MobileCode.equals(mobile.getText().toString()))
            {
                Mobile = "";
            }else {
                Mobile = mobile.getText().toString().trim();
            }
        }
        if (whatsApp==null)
        {
            Mobile = "";
        } else
        {
            if (WhatsAppCode.equals(whatsApp.getText().toString()))
            {
                WhatsApp = "";
            }else {
                WhatsApp = whatsApp.getText().toString().trim();
            }
        }

        if (TextUtils.isEmpty(FirstName))
        {
            Toast.makeText(AddPersonalDetailsActivity.this, "Please Enter First Name", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(SurName))
        {
            Toast.makeText(AddPersonalDetailsActivity.this, "Please Enter Surname", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(Profession))
        {
            Toast.makeText(AddPersonalDetailsActivity.this, "Please State Your Profession", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(Mobile))
        {
            Toast.makeText(AddPersonalDetailsActivity.this, "Please Enter Your Mobile Number", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(WhatsApp))
        {
            Toast.makeText(AddPersonalDetailsActivity.this, "Please State Your Profession", Toast.LENGTH_LONG).show();
        }
        else if (Mobile !=null &&!TextUtils.isEmpty(FirstName) && !TextUtils.isEmpty(SurName))
        {
            Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {

                            progressBar.setVisibility(View.VISIBLE);
                            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                            try {
                                BitMatrix bitMatrix = multiFormatWriter.encode(adminId, BarcodeFormat.QR_CODE,200,200);
                                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                                codeUri = getImageUri(AddPersonalDetailsActivity.this, bitmap);

                                StorageReference filepath = storage.child("User QR Codes/" + adminId
                                        + "."+ getFileExtension(codeUri));

                                filepath.putFile(codeUri).addOnSuccessListener(taskSnapshot -> {
                                    Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                                    while (!downloadUrl.isComplete());
                                    Uri profilePhotoUri = downloadUrl.getResult();

                                    Map<String, Object> usersMap = new HashMap<>();
                                    usersMap.put("user_qr_code", profilePhotoUri.toString().trim());
                                    usersMap.put("user_first_name", FirstName);
                                    usersMap.put("user_other_names", OtherNames);
                                    usersMap.put("user_surname", SurName);
                                    usersMap.put("user_mobile", Mobile);
                                    usersMap.put("user_whatsapp", WhatsApp);
                                    usersMap.put("user_profession", Profession);

                                    AdminRef.child(adminId).updateChildren(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Intent intent = new Intent(AddPersonalDetailsActivity.this, AddPhotoActivity.class);
                                                    startActivity(intent);
                                                    Animatoo.animateFade(AddPersonalDetailsActivity.this);
                                                    finish();
                                                }else
                                                {
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(AddPersonalDetailsActivity.this, "Error:" + error, Toast.LENGTH_LONG).show();
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }

                                            }else {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(AddPersonalDetailsActivity.this, "Failed to Save Personal Data. Try Again!!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                });

                            } catch (WriterException e) {
                                e.printStackTrace();
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
                                    Toast.makeText(AddPersonalDetailsActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            })
                    .onSameThread()
                    .check();



        }


    }
    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, firstName.getText().toString().trim()+"'s QR Code", null);
        return Uri.parse(path);
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
                AddPersonalDetailsActivity.this.openSettings();
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

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}