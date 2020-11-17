package com.pridetechnologies.businesscard;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddBusinessActivity extends AppCompatActivity {

    private CountryCodePicker mobileCode, whatsAppCode;

    String CompanyMobile = null;
    String CompanyWhatsApp = null;

    private String CompanyName, CMobile, CWhatsApp, UserPosition, CompanyBuildingName, CompanyStreetName, CompanyAreaLocated, CompanyDistrict, CompanyCountry, CompanyEmail, CompanyWebsite;
    private TextInputEditText companyName, userPosition, companyMobile, companyWhatsApp, companyBuildingName, companyStreetName, companyAreaLocated, companyDistrict, companyCountry, companyEmail, companyWebsite;
    private MaterialButton saveBtn;

    private String admin_id;
    private DatabaseReference AdminRef, CompanyRef, CompanyRequestRef;
    private FirebaseAuth firebaseAuth;

    private Uri codeUri;

    private StorageReference storage;
    private FirebaseFirestore db;
    private Index index;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_business);

        Client client = new Client("T5LG6WB4JZ", "e799a43983df842a91ead0dad768e64d");
        index = client.getIndex("businesses");

        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageButton99);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar6);

        firebaseAuth = FirebaseAuth.getInstance();
        admin_id = firebaseAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        AdminRef = FirebaseDatabase.getInstance().getReference().child("Users");
        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        CompanyRequestRef = FirebaseDatabase.getInstance().getReference().child("Businesses Teams");

        companyName = (TextInputEditText) findViewById(R.id.textfield1);
        userPosition = (TextInputEditText) findViewById(R.id.textfield2);
        companyMobile = (TextInputEditText) findViewById(R.id.mobileTextfield);
        companyWhatsApp = (TextInputEditText) findViewById(R.id.whatsAppTextfield);
        companyBuildingName = (TextInputEditText) findViewById(R.id.textfield3);
        companyAreaLocated = (TextInputEditText) findViewById(R.id.textfield4);
        companyDistrict = (TextInputEditText) findViewById(R.id.textfield5);
        companyCountry = (TextInputEditText) findViewById(R.id.textfield6);
        companyStreetName = (TextInputEditText) findViewById(R.id.textfield23);
        companyEmail = (TextInputEditText) findViewById(R.id.emailTextField);
        companyWebsite = (TextInputEditText) findViewById(R.id.websiteTextField);
        saveBtn = (MaterialButton) findViewById(R.id.button12);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInfo();
            }
        });

        mobileCode = (CountryCodePicker) findViewById(R.id.ccp6);
        whatsAppCode = (CountryCodePicker) findViewById(R.id.ccp7);

        CompanyMobile = mobileCode.getSelectedCountryCodeWithPlus();
        CompanyWhatsApp = whatsAppCode.getSelectedCountryCodeWithPlus();

        companyMobile.setText(CompanyMobile);
        companyWhatsApp.setText(CompanyWhatsApp);

        mobileCode.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                //Alert.showMessage(RegistrationActivity.this, ccp.getSelectedCountryCodeWithPlus());
                //selected_country_code = countryPicker.getSelectedCountryCodeWithPlus();

                CompanyMobile = mobileCode.getSelectedCountryCodeWithPlus();
                companyMobile.setText(CompanyMobile);
                //String name = countryPicker.getSelectedCountryName();
                //Toast.makeText(EditPersonDetailsActivity.this, "Updated " + code+" " + name, Toast.LENGTH_SHORT).show();
            }
        });
        whatsAppCode.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                //Alert.showMessage(RegistrationActivity.this, ccp.getSelectedCountryCodeWithPlus());
                //selected_country_code = countryPicker.getSelectedCountryCodeWithPlus();

                CompanyWhatsApp = whatsAppCode.getSelectedCountryCodeWithPlus();
                companyWhatsApp.setText(CompanyWhatsApp);
                //String name = countryPicker.getSelectedCountryName();
                //Toast.makeText(EditPersonDetailsActivity.this, "Updated " + code+" " + name, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveInfo() {


         CompanyName = companyName.getText().toString().trim();
         UserPosition = userPosition.getText().toString().trim();
         CompanyBuildingName = companyBuildingName.getText().toString().trim();
         CompanyAreaLocated = companyAreaLocated.getText().toString().trim();
         CompanyDistrict = companyDistrict.getText().toString().trim();
         CompanyCountry = companyCountry.getText().toString().trim();
         CompanyStreetName = companyStreetName.getText().toString().trim();
        CompanyEmail = companyEmail.getText().toString().trim();
        CompanyWebsite = companyWebsite.getText().toString().trim();
        //Toast.makeText(AddPersonalDetailsActivity.this, Title+FirstName+OtherNames+SurName+Profession+MobileCode+Mobile+WhatsAppCode+WhatsApp, Toast.LENGTH_LONG).show();

        if (companyMobile==null)
        {
            if (CompanyMobile== companyMobile.getText().toString())
            {
                CMobile = null;
            }
        } else
        {
            CMobile = companyMobile.getText().toString().trim();
        }
        if (companyWhatsApp==null)
        {
            if (CompanyWhatsApp== companyWhatsApp.getText().toString())
            {
                CWhatsApp = null;
            }

        } else
        {
            CWhatsApp = companyWhatsApp.getText().toString().trim();
        }
        if (TextUtils.isEmpty(CompanyName))
        {
            Toast.makeText(AddBusinessActivity.this, "Please Enter Business Name", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(UserPosition))
        {
            Toast.makeText(AddBusinessActivity.this, "Please Enter Position", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(CompanyBuildingName))
        {
            Toast.makeText(AddBusinessActivity.this, "Please Enter Building Name", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(CompanyAreaLocated))
        {
            Toast.makeText(AddBusinessActivity.this, "Please Enter Location", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(CompanyCountry))
        {
            Toast.makeText(AddBusinessActivity.this, "Please Enter Country", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(CompanyDistrict))
        {
            Toast.makeText(AddBusinessActivity.this, "Please Enter District", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(CMobile))
        {
            Toast.makeText(AddBusinessActivity.this, "Please Enter Mobile Number", Toast.LENGTH_LONG).show();
        }
        else if (CMobile !=null &&!TextUtils.isEmpty(CompanyBuildingName) && !TextUtils.isEmpty(CompanyAreaLocated))
        {
            final Dialog dialog = new Dialog(AddBusinessActivity.this);
            dialog.setContentView(R.layout.custom_dialog__message_layout);
            TextView titleView = dialog.findViewById(R.id.dialogTitle);
            TextView messageView = dialog.findViewById(R.id.dialogMessage);
            TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
            TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
            titleView.setText("Save");
            messageView.setText("Are you sure you want to save this info?");
            noBtn.setText("Cancel");
            yesBtn.setText("Save");
            yesBtn.setOnClickListener(v -> {
                Dexter.withContext(AddBusinessActivity.this)
                        .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {

                                final String Businesskey = AdminRef.push().getKey();

                                progressBar.setVisibility(ProgressBar.VISIBLE);

                                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                                try {
                                    BitMatrix bitMatrix = multiFormatWriter.encode(Businesskey, BarcodeFormat.QR_CODE,200,200);
                                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                                    codeUri = getImageUri(AddBusinessActivity.this, bitmap);

                                    StorageReference filepath = storage.child("Business QR Codes/" + System.currentTimeMillis()
                                            + "."+ getFileExtension(codeUri));

                                    filepath.putFile(codeUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                                            while (!downloadUrl.isComplete());
                                            Uri profilePhotoUri = downloadUrl.getResult();

                                            Map<String, Object> usersMap = new HashMap<>();
                                            usersMap.put("user_position", UserPosition);
                                            usersMap.put("business_admin", "admin");
                                            usersMap.put("company_key", Businesskey);

                                            final Map<String, Object> companyMap = new HashMap<>();
                                            companyMap.put("business_qr_code", profilePhotoUri.toString().trim());
                                            companyMap.put("business_name", CompanyName);
                                            companyMap.put("business_location", CompanyAreaLocated);
                                            companyMap.put("business_district", CompanyDistrict);
                                            companyMap.put("business_street", CompanyStreetName);
                                            companyMap.put("business_building", CompanyBuildingName);
                                            companyMap.put("business_mobile", CMobile);
                                            companyMap.put("business_whatsapp", CWhatsApp);
                                            companyMap.put("business_country", CompanyCountry);
                                            companyMap.put("business_email", CompanyEmail);
                                            companyMap.put("business_website", CompanyWebsite);
                                            companyMap.put("business_key", Businesskey);

                                            JSONObject object = null;
                                            try {
                                                object = new JSONObject()
                                                        .put("business_name", CompanyName)
                                                        .put("business_location", CompanyAreaLocated)
                                                        .put("business_district", CompanyDistrict)
                                                        .put("business_street", CompanyStreetName)
                                                        .put("business_building", CompanyBuildingName)
                                                        .put("business_country", CompanyCountry);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            index.addObjectAsync(object, Businesskey, null);

                                            CompanyRef.child(admin_id).child(Businesskey).setValue(usersMap);
                                            AdminRef.child(admin_id).updateChildren(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        CompanyRef.child(Businesskey).setValue(companyMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Map<String, Object > typeMap2 = new HashMap<>();
                                                                    typeMap2.put("type", "accepted");

                                                                    CompanyRequestRef.child(Businesskey).child(admin_id).setValue(typeMap2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                                                Intent intent = new Intent(AddBusinessActivity.this, BusinessBioActivity.class);
                                                                                intent.putExtra("key",Businesskey);
                                                                                startActivity(intent);
                                                                                finish();
                                                                                Animatoo.animateFade(AddBusinessActivity.this);
                                                                            }
                                                                        }
                                                                    }).addOnCanceledListener(new OnCanceledListener() {
                                                                        @Override
                                                                        public void onCanceled() {
                                                                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }).addOnCanceledListener(new OnCanceledListener() {
                                                            @Override
                                                            public void onCanceled() {
                                                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                            }
                                                        });
                                                    }
                                                }
                                            });


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddBusinessActivity.this,"failure", Toast.LENGTH_SHORT).show();

                                        }
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
                                        Toast.makeText(AddBusinessActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                })
                        .onSameThread()
                        .check();
                dialog.dismiss();
            });
            noBtn.setOnClickListener(v -> dialog.dismiss());
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
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
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, CompanyName+"'s QR Code", null);
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
                AddBusinessActivity.this.openSettings();
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
    }