package com.pridetechnologies.businesscard;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class CreateNewBusinessActivity extends AppCompatActivity {

    IntentResult result;
    private String cardKey=null;
    static final int REQUEST_GALLERY_PHOTO = 18;
    private IntentIntegrator qrScan;
    private Dialog dialog;
    private String UserID;
    private FirebaseAuth auth;
    private StorageReference storage;
    private FirebaseFirestore db;

    private DatabaseReference userRef, CompanyRef, MyCardsRef, CardRequestRef, CompanyRequestRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_business);

        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageButton333);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        auth = FirebaseAuth.getInstance();
        UserID = auth.getCurrentUser().getUid();
        storage = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        CompanyRequestRef = FirebaseDatabase.getInstance().getReference().child("Businesses Teams");
        CardRequestRef = FirebaseDatabase.getInstance().getReference().child("Card Requests");

    }

    public void addNewBusiness(View view) {

        Intent intent = new Intent(CreateNewBusinessActivity.this, AddBusinessActivity.class);
        startActivity(intent);
        finish();
        Animatoo.animateFade(CreateNewBusinessActivity.this);
    }

    public void existingBusiness(View view) {

        dialog = new Dialog(CreateNewBusinessActivity.this);
        dialog.setContentView(R.layout.custom_link_company_dialog);
        ImageView scanB = (ImageView) dialog.findViewById(R.id.imageView40);
        ImageView libB = (ImageView) dialog.findViewById(R.id.imageView50);
        Button cancelBtn = (Button) dialog.findViewById(R.id.button58);
        scanB.setOnClickListener(view13 -> {
            qrScan = new IntentIntegrator(CreateNewBusinessActivity.this);
            qrScan.setBeepEnabled(true);
            qrScan.setOrientationLocked(false);
            qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            qrScan.setPrompt("Scan QR Code");
            qrScan.setCameraId(0);  // Use a specific camera of the device
            qrScan.setBeepEnabled(true);
            qrScan.setBarcodeImageEnabled(true);
            qrScan.initiateScan();

        });
        libB.setOnClickListener(view12 -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setDataAndType( android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(pickIntent, REQUEST_GALLERY_PHOTO);
        });
        cancelBtn.setOnClickListener(view1 -> dialog.dismiss());
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        final CharSequence[] items = {
                "Scan Business Code", "Get Business Code from Library",
                "Cancel"
        };

    }

    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {

            if(result.getContents() == null) {
                //finish();
                //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                cardKey = result.getContents().trim();
                sendBusinessRequest();

            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);

        }
        if (requestCode == REQUEST_GALLERY_PHOTO && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try
            {
                InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap == null)
                {
                    Log.e("TAG", "uri is not a bitmap," + selectedImage.toString());
                    return;
                }
                int width = bitmap.getWidth(), height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                bitmap.recycle();
                bitmap = null;
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                MultiFormatReader reader = new MultiFormatReader();
                try
                {
                    Result result = reader.decode(bBitmap);
                    cardKey = result.getText().toString().trim();
                    sendBusinessRequest();
                }
                catch (NotFoundException e)
                {
                    // Toast.makeText(this, "This Code is NOT VALID", Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "decode exception", e);
                }

            }
            catch (FileNotFoundException e)
            {
                //Log.e("TAG", "can not open file" + selectedImage.toString(), e);
            }
        }else
        {
            super.onActivityResult(requestCode, resultCode, data);
            //Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        }

    }

    private void sendBusinessRequest() {
        CompanyRef.child(cardKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if ((snapshot.hasChild("business_name")))
                    {
                        String CompanyName = snapshot.child("business_name").getValue().toString();

                        dialog = new Dialog(CreateNewBusinessActivity.this);
                        dialog.setContentView(R.layout.custom_business_dialog);
                        TextView nameView = (TextView) dialog.findViewById(R.id.textView28);
                        nameView.setText(CompanyName);
                        final TextInputEditText positionInput = (TextInputEditText) dialog.findViewById(R.id.textField10);
                        MaterialButton sendDialog  = (MaterialButton) dialog.findViewById(R.id.button13);
                        sendDialog.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {

                                String PositionInput = positionInput.getText().toString();

                                final android.app.AlertDialog waitingDialog=new SpotsDialog.Builder().setContext(CreateNewBusinessActivity.this).build();
                                waitingDialog.setMessage("Please Wait...");
                                waitingDialog.show();

                                final Map<String, Object > typeMap = new HashMap<>();
                                typeMap.put("user_position", PositionInput);
                                typeMap.put("business_admin", "not_admin");

                                final Map<String, Object > typeMap2 = new HashMap<>();
                                typeMap2.put("type", "sent");

                                db.collection("Users").document(UserID)
                                        .set(typeMap, SetOptions.merge());
                                db.collection("Businesses").document(UserID)
                                        .set(typeMap, SetOptions.merge());

                                userRef.child(UserID).updateChildren(typeMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    CompanyRequestRef.child(cardKey).child(UserID).setValue(typeMap2)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        dialog.dismiss();
                                                                        waitingDialog.dismiss();
                                                                        Toast.makeText(CreateNewBusinessActivity.this, "Your Request has been sent!!!", Toast.LENGTH_SHORT).show();
                                                                        finish();
                                                                    }
                                                                }
                                                            });}
                                            }
                                        });

                            }
                        });
                        MaterialButton cancelDialog  = (MaterialButton) dialog.findViewById(R.id.button14);
                        cancelDialog.setOnClickListener(new View.OnClickListener() {
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}