package com.pridetechnologies.businesscard;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pridetechnologies.businesscard.Sinch.BaseActivity;
import com.pridetechnologies.businesscard.models.RequestsCard;
import com.revenuecat.purchases.Purchases;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class ProfileActivity extends BaseActivity {

    IntentResult result;

    static final int REQUEST_GALLERY_PHOTO = 8;

    private String cardKey=null;
    private String CompanyId=null;
    private String firstName;

    File mainfile;

    OutputStream outputStream;
    private ProgressBar progressBar;

    private RecyclerView cardListRecycler;

    private Dialog dialog;
    private String UserID;
    private MaterialTextView nameView, user_profession, positionView, countryView, areaView, districtView, companyNameView, companyStreetName, buildingNameView, businessName, businessBuildingName, businessStreetName, businessAreaLoc, businessDistrict, businessCountry, businessBio;
    private CircleImageView imageView, logoImageView;
    private MaterialButton manegeAccount;
    private ImageButton closeBtn;
    private ImageView facebookBtn, twitterBtn, linkedInBtn, instagramBtn, businessFacebookBtn, businessTwitterBtn, businessLinkedInBtn, businessInstagramBtn;
    private LinearLayout businessCallBtn, businessWhatsApp, businessEmailBtn, businessWebsiteBtn;
    private MaterialCardView noRequest;


    private IntentIntegrator qrScan;

    private LinearLayout bioButton, shareButton, editButton;

    private ConstraintLayout noBusiness, yesBusiness, requestSentBusiness;
    private BottomSheetBehavior requestBottomSheetBehavior;

    FirebaseRecyclerAdapter<RequestsCard, RequestsCardViewHolder> adapter = null;

    private FirebaseAuth auth;
    private StorageReference storage;
    private FirebaseFirestore db;

    private DatabaseReference userRef, CompanyRef, MyCardsRef, CardRequestRef, CompanyRequestRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        progressBar = (ProgressBar) findViewById(R.id.profileProgressBar);

        closeBtn = (ImageButton) findViewById(R.id.imageButton3);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        nameView = (MaterialTextView)findViewById(R.id.textView8);
        user_profession = (MaterialTextView)findViewById(R.id.user_profession);
        companyNameView = (MaterialTextView)findViewById(R.id.textView14);
        positionView = (MaterialTextView)findViewById(R.id.textView15);
        buildingNameView = (MaterialTextView)findViewById(R.id.textView21);
        companyStreetName = (MaterialTextView) findViewById(R.id.textView60);
        businessStreetName = (MaterialTextView) findViewById(R.id.textView53);
        areaView = (MaterialTextView)findViewById(R.id.textView23);
        districtView = (MaterialTextView)findViewById(R.id.textView24);
        countryView = (MaterialTextView)findViewById(R.id.textView25);
        imageView = (CircleImageView) findViewById(R.id.imageView3);
        logoImageView = (CircleImageView) findViewById(R.id.circleImageView6);
        manegeAccount = (MaterialButton)findViewById(R.id.manage_account);

        bioButton = (LinearLayout) findViewById(R.id.bioButton);
        shareButton = (LinearLayout) findViewById(R.id.shareButton);
        editButton = (LinearLayout) findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> editPersonalBasics());

        noBusiness = (ConstraintLayout)findViewById(R.id.noBusiness);
        yesBusiness = (ConstraintLayout)findViewById(R.id.yesBusiness);
        requestSentBusiness = (ConstraintLayout)findViewById(R.id.teamMemberRequestSent);

        facebookBtn = (ImageView)findViewById(R.id.imageView5);
        twitterBtn = (ImageView)findViewById(R.id.imageView6);
        linkedInBtn = (ImageView) findViewById(R.id.imageView7);
        instagramBtn = (ImageView) findViewById(R.id.imageView8);


        noRequest = (MaterialCardView)findViewById(R.id.reqCard);

        auth = FirebaseAuth.getInstance();
        UserID = auth.getCurrentUser().getUid();
        storage = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        MyCardsRef = FirebaseDatabase.getInstance().getReference().child("My Cards");
        CompanyRequestRef = FirebaseDatabase.getInstance().getReference().child("Businesses Teams");
        CardRequestRef = FirebaseDatabase.getInstance().getReference().child("Card Requests");

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        cardListRecycler = (RecyclerView) findViewById(R.id.recyclerView2);
        cardListRecycler.setHasFixedSize(true);
        cardListRecycler.setLayoutManager(layoutManager);

        LinearSnapHelper snapHelper = new LinearSnapHelper() {
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager lm, int velocityX, int velocityY) {
                View centerView = findSnapView(lm);
                if (centerView == null)
                    return RecyclerView.NO_POSITION;

                int position = lm.getPosition(centerView);
                int targetPosition = -1;
                if (lm.canScrollHorizontally()) {
                    if (velocityX < 0) {
                        targetPosition = position - 1;
                    } else {
                        targetPosition = position + 1;
                    }
                }

                if (lm.canScrollVertically()) {
                    if (velocityY < 0) {
                        targetPosition = position - 1;
                    } else {
                        targetPosition = position + 1;
                    }
                }

                final int firstItem = 0;
                final int lastItem = lm.getItemCount() - 1;
                targetPosition = Math.min(lastItem, Math.max(targetPosition, firstItem));
                return targetPosition;
            }
        };
        snapHelper.attachToRecyclerView(cardListRecycler);

        userRef.child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (snapshot.hasChild("user_image"))
                    {
                        String Image = snapshot.child("user_image").getValue().toString();
                        Picasso.get().load(Image).placeholder(R.mipmap.user_gold).into(imageView);
                    }
                    if ((snapshot.hasChild("user_first_name")))
                    {
                        firstName = snapshot.child("user_first_name").getValue().toString();
                        String OtherNames= snapshot.child("user_other_names").getValue().toString();
                        String Surname= snapshot.child("user_surname").getValue().toString();
                        nameView.setText(firstName+" "+OtherNames+" "+Surname);
                        bioButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (snapshot.hasChild("user_bio"))
                                {

                                    String BIO= snapshot.child("user_bio").getValue().toString();
                                    dialog = new Dialog(ProfileActivity.this);
                                    dialog.setContentView(R.layout.custom_user_bio);
                                    TextView nameView = (TextView) dialog.findViewById(R.id.textView65);
                                    nameView.setText("My Bio");
                                    final TextView bioView = (TextView) dialog.findViewById(R.id.textView66);
                                    bioView.setText(BIO);
                                    Button cancelDialog  = (Button) dialog.findViewById(R.id.button29);
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

                                }else {
                                    Toast.makeText(ProfileActivity.this.getApplicationContext(), "No Bio!", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });

                    }
                    if ((snapshot.hasChild("user_profession")))
                    {
                        String Profession = snapshot.child("user_profession").getValue().toString();
                       user_profession.setText(Profession);

                    }
                    if ((snapshot.hasChild("user_position")))
                    {
                        String Position = snapshot.child("user_position").getValue().toString();
                        positionView.setText(Position);

                    }
                    if (snapshot.hasChild("business_admin"))
                    {
                        String AdminPriv = snapshot.child("business_admin").getValue().toString();

                        if (!AdminPriv.equals("admin"))
                        {
                            manegeAccount.setVisibility(View.VISIBLE);
                        }
                        if (AdminPriv.equals("admin"))
                        {
                            manegeAccount.setVisibility(View.VISIBLE);
                        }
                    }
                    if (snapshot.hasChild("company_key"))
                    {
                        CompanyId = snapshot.child("company_key").getValue().toString();

                        CompanyRequestRef.child(CompanyId).child(UserID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    String type = snapshot.child("type").getValue().toString();
                                    if(type.equals("sent")){
                                        requestSentBusiness.setVisibility(View.VISIBLE);
                                        noBusiness.setVisibility(View.GONE);
                                        yesBusiness.setVisibility(View.GONE);
                                    }else {
                                        requestSentBusiness.setVisibility(View.GONE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        CompanyRef.child(CompanyId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    noBusiness.setVisibility(View.GONE);
                                    yesBusiness.setVisibility(View.VISIBLE);


                                    if ((snapshot.hasChild("business_logo")))
                                    {
                                        String Image = snapshot.child("business_logo").getValue().toString();

                                        Picasso.get().load(Image).fit().centerCrop().placeholder(R.mipmap.background_icon).into(logoImageView);
                                    }
                                    if ((snapshot.hasChild("business_name")))
                                    {
                                        final String CompanyName = snapshot.child("business_name").getValue().toString();
                                        companyNameView.setText(CompanyName);

                                    }
                                    if ((snapshot.hasChild("business_building")))
                                    {
                                        String CompanyBuilding = snapshot.child("business_building").getValue().toString();
                                        buildingNameView.setText(CompanyBuilding);

                                    }
                                    if ((snapshot.hasChild("business_street")))
                                    {
                                        String CompanyStreet = snapshot.child("business_street").getValue().toString();
                                        companyStreetName.setText(CompanyStreet);

                                    }
                                    if ((snapshot.hasChild("business_location")))
                                    {
                                        String CompanyArea = snapshot.child("business_location").getValue().toString();
                                        areaView.setText(CompanyArea);

                                    }
                                    if ((snapshot.hasChild("business_district")))
                                    {
                                        String CompanyDistrict = snapshot.child("business_district").getValue().toString();
                                        districtView.setText(CompanyDistrict);

                                    }
                                    if ((snapshot.hasChild("business_mobile")))
                                    {
                                        String CompanyName = snapshot.child("business_mobile").getValue().toString();
                                        //user_profession.setText(Profession);

                                    }
                                    if ((snapshot.hasChild("business_whatsapp")))
                                    {
                                        String CompanyName = snapshot.child("business_whatsapp").getValue().toString();
                                        //user_profession.setText(Profession);

                                    }
                                    if ((snapshot.hasChild("business_country")))
                                    {
                                        String CompanyCountry = snapshot.child("business_country").getValue().toString();
                                        countryView.setText(CompanyCountry);

                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        noBusiness.setVisibility(View.VISIBLE);
                        yesBusiness.setVisibility(View.GONE);
                    }
                    shareButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final String FirstName = snapshot.child("user_first_name").getValue().toString();
                            String Surname= snapshot.child("user_surname").getValue().toString();
                            if ((snapshot.hasChild("user_qr_code")))
                            {
                                String QRCode = snapshot.child("user_qr_code").getValue().toString();

                                dialog = new Dialog(ProfileActivity.this);
                                dialog.setContentView(R.layout.custom_qr_code_dialog);
                                TextView nameView = (TextView) dialog.findViewById(R.id.textView29);
                                ImageView resetBtn = (ImageView) dialog.findViewById(R.id.imageView23);
                                resetBtn.setVisibility(View.VISIBLE);
                                nameView.setText("My Card");
                                final ImageView qrCodeView = (ImageView) dialog.findViewById(R.id.imageView9);
                                Picasso.get().load(QRCode).fit().centerCrop().placeholder(R.drawable.share_qr_code).into(qrCodeView);
                                Button saveBtn = (Button) dialog.findViewById(R.id.button26);
                                saveBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        //Intent intent = new Intent(Intent.ACTION_VIEW);
                                        //intent.setData(Uri.parse(QRCode));
                                        //startActivity(intent);

                                        final FileOutputStream[] fileOutputStream = {null};
                                        File file=getdisc();
                                        if (!file.exists() && !file.mkdirs())
                                        {
                                            Toast.makeText(getApplicationContext(),"sorry can not make dir",Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        Dexter.withContext(ProfileActivity.this)
                                                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                .withListener(new MultiplePermissionsListener() {
                                                    @Override
                                                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                                                        progressBar.setVisibility(ProgressBar.VISIBLE);
                                                        String name=FirstName+"'sCode"+".jpeg";
                                                        String file_name=file.getAbsolutePath()+"/"+name;
                                                        File new_file=new File(file_name);
                                                        try {
                                                            fileOutputStream[0] =new FileOutputStream(new_file);
                                                            Bitmap bitmap=viewToBitmap(qrCodeView,qrCodeView.getWidth(),qrCodeView.getHeight());
                                                            bitmap.compress(Bitmap.CompressFormat.JPEG,100, fileOutputStream[0]);
                                                            Toast.makeText(getApplicationContext(),"Download Complete", Toast.LENGTH_LONG).show();
                                                            fileOutputStream[0].flush();
                                                            fileOutputStream[0].close();
                                                        } catch
                                                        (IOException e) {

                                                        }
                                                        refreshGallery(file);

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
                                                                Toast.makeText(ProfileActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                                                        .show();
                                                            }
                                                        })
                                                .onSameThread()
                                                .check();
                                    }
                                });
                                Button cancelDialog  = (Button) dialog.findViewById(R.id.button10);
                                cancelDialog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        dialog.dismiss();
                                    }
                                });
                                resetBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        final Dialog dialog = new Dialog(ProfileActivity.this);
                                        dialog.setContentView(R.layout.custom_dialog__message_layout);
                                        TextView titleView = dialog.findViewById(R.id.dialogTitle);
                                        TextView messageView = dialog.findViewById(R.id.dialogMessage);
                                        TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
                                        TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
                                        titleView.setText("Reset Code");
                                        messageView.setText("Are you sure you want to reset your code?");
                                        noBtn.setText("Cancel");
                                        yesBtn.setText("Reset");
                                        yesBtn.setOnClickListener(view -> {
                                            Dexter.withContext(ProfileActivity.this)
                                                    .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                    .withListener(new MultiplePermissionsListener() {
                                                        @Override
                                                        public void onPermissionsChecked(MultiplePermissionsReport report) {

                                                            progressBar.setVisibility(ProgressBar.VISIBLE);

                                                            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                                                            try {
                                                                BitMatrix bitMatrix = multiFormatWriter.encode(UserID, BarcodeFormat.QR_CODE,200,200);
                                                                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                                                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                                                                Uri qrCodeUri = getBitmapToUri(ProfileActivity.this, bitmap);

                                                                StorageReference filepath = storage.child("User QR Codes/" + UserID
                                                                        + "."+ getFileExtension(qrCodeUri));

                                                                filepath.putFile(qrCodeUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                        Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                                                                        while (!downloadUrl.isComplete());
                                                                        Uri profilePhotoUri = downloadUrl.getResult();

                                                                        Map<String, Object> usersMap = new HashMap<>();
                                                                        usersMap.put("user_qr_code", profilePhotoUri.toString().trim());

                                                                        userRef.child(UserID).updateChildren(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                progressBar.setVisibility(ProgressBar.INVISIBLE);

                                                                            }
                                                                        });


                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                                        Toast.makeText(ProfileActivity.this,"Failed to save Code", Toast.LENGTH_SHORT).show();

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
                                                                    Toast.makeText(ProfileActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                                                            .show();
                                                                }
                                                            })
                                                    .onSameThread()
                                                    .check();
                                        });
                                        noBtn.setOnClickListener(view -> dialog.dismiss());
                                        dialog.setCancelable(true);
                                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        dialog.show();
                                    }
                                });
                                dialog.setCancelable(true);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.show();

                            }else
                            {
                                final Dialog dialog = new Dialog(ProfileActivity.this);
                                dialog.setContentView(R.layout.custom_dialog__message_layout);
                                TextView titleView = dialog.findViewById(R.id.dialogTitle);
                                TextView messageView = dialog.findViewById(R.id.dialogMessage);
                                TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
                                TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
                                titleView.setText("Reset Code");
                                messageView.setText("It looks like your code is missing, please reset your code.");
                                noBtn.setText("Cancel");
                                yesBtn.setText("Reset");
                                yesBtn.setOnClickListener(v -> {
                                    Dexter.withActivity(ProfileActivity.this)
                                            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            .withListener(new MultiplePermissionsListener() {
                                                @Override
                                                public void onPermissionsChecked(MultiplePermissionsReport report) {

                                                    progressBar.setVisibility(ProgressBar.VISIBLE);

                                                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                                                    try {
                                                        BitMatrix bitMatrix = multiFormatWriter.encode(UserID, BarcodeFormat.QR_CODE,200,200);
                                                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                                                        Uri codeUri = getBitmapToUri(ProfileActivity.this, bitmap);

                                                        StorageReference filepath = storage.child("User QR Codes/" + UserID
                                                                + "."+ getFileExtension(codeUri));

                                                        filepath.putFile(codeUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                                                                while (!downloadUrl.isComplete());
                                                                Uri profilePhotoUri = downloadUrl.getResult();

                                                                Map<String, Object> usersMap = new HashMap<>();
                                                                usersMap.put("user_qr_code", profilePhotoUri.toString().trim());


                                                                userRef.child(UserID).updateChildren(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        progressBar.setVisibility(ProgressBar.INVISIBLE);

                                                                    }
                                                                });


                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                                Toast.makeText(ProfileActivity.this,"Failed to save Code", Toast.LENGTH_SHORT).show();

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
                                                            Toast.makeText(ProfileActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
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
                    });

                    facebookBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("user_facebook")))
                            {
                                String FaceBook = snapshot.child("user_facebook").getValue().toString();
                                if(!FaceBook.isEmpty())
                                {
                                    //Intent intent = new Intent(Intent.ACTION_VIEW);
                                    //intent.setData(Uri.parse(FaceBook));
                                    //startActivity(intent);
                                    Intent intent= new Intent(ProfileActivity.this, BrowserWebViewActivity.class);
                                    intent.putExtra("urlLink",FaceBook);
                                    startActivity(intent);
                                    Animatoo.animateFade(ProfileActivity.this);
                                }
                            }else
                            {
                                Toast.makeText(ProfileActivity.this, "No Facebook", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    twitterBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("user_twitter")))
                            {

                                String Twitter = snapshot.child("user_twitter").getValue().toString();
                                if(!Twitter.isEmpty())
                                {
                                    Intent intent= new Intent(ProfileActivity.this, BrowserWebViewActivity.class);
                                    intent.putExtra("urlLink",Twitter);
                                    startActivity(intent);
                                    Animatoo.animateFade(ProfileActivity.this);

                                }
                            }else
                            {
                                Toast.makeText(ProfileActivity.this, "No Twitter", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    linkedInBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("user_linked_in")))
                            {
                                String LinkedIn = snapshot.child("user_linked_in").getValue().toString();
                                if(!LinkedIn.isEmpty())
                                {
                                    Intent intent= new Intent(ProfileActivity.this, BrowserWebViewActivity.class);
                                    intent.putExtra("urlLink",LinkedIn);
                                    startActivity(intent);
                                    Animatoo.animateFade(ProfileActivity.this);
                                }
                            }else
                            {
                                Toast.makeText(ProfileActivity.this, "No LinkedIn", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                    instagramBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("user_instagram")))
                            {

                                String Instagram = snapshot.child("user_instagram").getValue().toString();
                                if(!Instagram.isEmpty())
                                {
                                    Intent intent= new Intent(ProfileActivity.this, BrowserWebViewActivity.class);
                                    intent.putExtra("urlLink",Instagram);
                                    startActivity(intent);
                                    Animatoo.animateFade(ProfileActivity.this);
                                }
                            }else
                            {
                                Toast.makeText(ProfileActivity.this, "No Instagram", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        CardRequestRef.child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    noRequest.setVisibility(View.VISIBLE);
                    cardListRecycler.setVisibility(View.VISIBLE);
                }else
                    {
                        noRequest.setVisibility(View.GONE);
                        cardListRecycler.setVisibility(View.GONE);
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        retrieveCards();
        RequestBottomSheet();
    }


    private void RequestBottomSheet() {

        // get the bottom sheet view
        MaterialCardView bottomSheet = (MaterialCardView) findViewById(R.id.business_bottom_sheet);
        ImageButton button = (ImageButton) findViewById(R.id.imageButton9);
        businessName = (MaterialTextView) findViewById(R.id.textView50);
        businessBuildingName = (MaterialTextView) findViewById(R.id.textView52);
        businessStreetName = (MaterialTextView) findViewById(R.id.textView53);
        businessAreaLoc = (MaterialTextView) findViewById(R.id.textView54);
        businessDistrict = (MaterialTextView) findViewById(R.id.textView55);
        businessCountry = (MaterialTextView) findViewById(R.id.textView56);

        //ImageButton mecBtn = (ImageButton) findViewById(R.id.button13);


        // init the bottom sheet behavior
        requestBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // change the state of the bottom sheet
        requestBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        requestBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // set hideable or not
        requestBottomSheetBehavior.setHideable(true);

        // set callback for changes
        requestBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset)
            {
                //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                // set the peek height

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change the state of the bottom sheet
                requestBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                requestBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            }
        });
    }


    public void signOut(View view) {

        final Dialog dialog = new Dialog(ProfileActivity.this);
        dialog.setContentView(R.layout.custom_dialog__message_layout);
        TextView titleView = dialog.findViewById(R.id.dialogTitle);
        TextView messageView = dialog.findViewById(R.id.dialogMessage);
        TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
        TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
        titleView.setText("SignOut");
        messageView.setText("Are you sure you want to sign out?");
        noBtn.setText("Cancel");
        yesBtn.setText("SignOut");
        yesBtn.setOnClickListener(v -> {
            if (getSinchServiceInterface() != null) {
                getSinchServiceInterface().stopClient();
            }
            auth.signOut();
            Purchases.getSharedInstance().logOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            Animatoo.animateSlideRight(ProfileActivity.this);
            finishAffinity();
            dialog.dismiss();
        });
        noBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void addNewBusiness(View view) {

        Intent intent = new Intent(ProfileActivity.this, AddBusinessActivity.class);
        startActivity(intent);
        Animatoo.animateFade(ProfileActivity.this);
    }

    public void existingBusiness(View view) {

        dialog = new Dialog(ProfileActivity.this);
        dialog.setContentView(R.layout.custom_link_company_dialog);
        ImageView scanB = (ImageView) dialog.findViewById(R.id.imageView40);
        ImageView libB = (ImageView) dialog.findViewById(R.id.imageView50);
        Button cancelBtn = (Button) dialog.findViewById(R.id.button58);
        scanB.setOnClickListener(view13 -> {
            qrScan = new IntentIntegrator(ProfileActivity.this);
            qrScan.setBeepEnabled(true);
            qrScan.setOrientationLocked(false);
            qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            qrScan.setPrompt("Scan a QR code");
            qrScan.setCameraId(0);  // Use a specific camera of the device
            qrScan.setBeepEnabled(true);
            qrScan.setBarcodeImageEnabled(true);
            qrScan.initiateScan();
            dialog.dismiss();
        });
        libB.setOnClickListener(view12 -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setDataAndType( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(pickIntent, REQUEST_GALLERY_PHOTO);
            dialog.dismiss();
        });
        cancelBtn.setOnClickListener(view1 -> dialog.dismiss());
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
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

                        dialog = new Dialog(ProfileActivity.this);
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

                                final android.app.AlertDialog waitingDialog=new SpotsDialog.Builder().setContext(ProfileActivity.this).build();
                                waitingDialog.setMessage("Please Wait...");
                                waitingDialog.show();

                                final Map<String, Object > typeMap = new HashMap<>();
                                typeMap.put("user_position", PositionInput);
                                typeMap.put("business_admin", "not_admin");

                                final Map<String, Object > typeMap2 = new HashMap<>();
                                typeMap2.put("type", "sent");


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
                        companyNameView.setText(CompanyName);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieveCards() {

        FirebaseRecyclerOptions<RequestsCard> options =
                new FirebaseRecyclerOptions.Builder<RequestsCard>()
                        .setQuery(CardRequestRef.child(UserID), RequestsCard.class)
                        .setLifecycleOwner(this)
                        .build();

        adapter =
                new FirebaseRecyclerAdapter<RequestsCard, RequestsCardViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsCardViewHolder cardViewHolder, final int i, @NonNull final RequestsCard card)
                    {
                        final String AgentID = getRef(i).getKey();


                        userRef.child(AgentID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    noRequest.setVisibility(View.VISIBLE);
                                    cardListRecycler.setVisibility(View.VISIBLE);

                                    if ((snapshot.hasChild("user_image")))
                                    {
                                        String Image = snapshot.child("user_image").getValue().toString();
                                        Picasso.get().load(Image).fit().centerCrop().placeholder(R.mipmap.user_gold).into(cardViewHolder.user_image);
                                    }

                                    if ((snapshot.hasChild("user_first_name")))
                                    {
                                        String FirstName = snapshot.child("user_first_name").getValue().toString();
                                        String OtherNames= snapshot.child("user_other_names").getValue().toString();
                                        String Surname= snapshot.child("user_surname").getValue().toString();
                                        cardViewHolder.user_name.setText(FirstName+" "+OtherNames+" "+Surname);

                                    }
                                    if ((snapshot.hasChild("user_profession")))
                                    {
                                        String Profession = snapshot.child("user_profession").getValue().toString();
                                        cardViewHolder.user_profession.setText(Profession);

                                    }

                                    cardViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Intent intent = new Intent(ProfileActivity.this, UserRequestActivity.class);
                                            intent.putExtra("key", AgentID);
                                            startActivity(intent);
                                            Animatoo.animateFade(ProfileActivity.this);

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    @NonNull
                    @Override
                    public RequestsCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_card, parent, false);
                        RequestsCardViewHolder cardViewHolder = new RequestsCardViewHolder(view);
                        return cardViewHolder;
                    }
                };
        cardListRecycler.setAdapter(adapter);
        adapter.startListening();
    }


    public void editPersonalBasics() {
        Intent intent = new Intent(ProfileActivity.this, PersonalBasicsActivity.class);
        startActivity(intent);
        Animatoo.animateFade(ProfileActivity.this);
    }

    public void sendFeedback(View view) {
        Intent intent = new Intent(ProfileActivity.this, FeedbackActivity.class);
        startActivity(intent);
        Animatoo.animateFade(ProfileActivity.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    public void cancelRequest(View view) {

        CompanyRequestRef.child(cardKey).child(UserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                requestSentBusiness.setVisibility(View.GONE);
                noBusiness.setVisibility(View.VISIBLE);
                yesBusiness.setVisibility(View.GONE);
            }
        });
    }

    public static class RequestsCardViewHolder extends RecyclerView.ViewHolder
    {
        MaterialTextView user_name, user_profession;
        CircleImageView user_image;

        public RequestsCardViewHolder(@NonNull View itemView) {
            super(itemView);

            user_image = itemView.findViewById(R.id.circleImageView5);
            user_name = itemView.findViewById(R.id.textView36);
            user_profession = itemView.findViewById(R.id.textView37);

        }
    }

    public void manageSocialMedia(View view) {

        Intent intent = new Intent(ProfileActivity.this, SocialMediaActivity.class);
        startActivity(intent);
        Animatoo.animateFade(ProfileActivity.this);
    }

    public void manageBusinessAccount(View view) {

        Intent intent = new Intent(ProfileActivity.this, BasicBusinessAccountActivity.class);
        intent.putExtra("key", CompanyId);
        startActivity(intent);
        Animatoo.animateFade(ProfileActivity.this);

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
                ProfileActivity.this.openSettings();
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
    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void refreshGallery(File file)
    { Intent i=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        i.setData(Uri.fromFile(file));
        sendBroadcast(i);
    }
    private File getdisc(){
        File file= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return new File(file,"Business Card Codes");
    }
    private static Bitmap viewToBitmap(View view, int widh, int hight)
    {
        Bitmap bitmap=Bitmap.createBitmap(widh,hight, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap); view.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        CardRequestRef.child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists())
                {
                    noRequest.setVisibility(View.GONE);
                    cardListRecycler.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public Uri getBitmapToUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, firstName+"'s QR Code", null);
        return Uri.parse(path);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CardRequestRef.child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists())
                {
                    noRequest.setVisibility(View.GONE);
                    cardListRecycler.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}