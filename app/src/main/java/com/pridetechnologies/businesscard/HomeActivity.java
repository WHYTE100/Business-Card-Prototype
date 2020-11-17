package com.pridetechnologies.businesscard;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pridetechnologies.businesscard.SendNotificationPack.Token;
import com.pridetechnologies.businesscard.Sinch.BaseActivity;
import com.pridetechnologies.businesscard.models.FileCompressor;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class HomeActivity extends BaseActivity {

    private String cardKey=null;
    private String cardKey2=null;
    private String UID;

    IntentResult result;

    FileCompressor mCompressor;

    ProgressDialog progressDialog;

    static final int REQUEST_GALLERY_PHOTO = 8;
    static final int BUSINESS_REQUEST_GALLERY_PHOTO = 20;

    private IntentIntegrator qrScan;

    private ImageView indiImage, bizImage;
    private TextView indiText, bizText;

    private BusinessCard mMyApplication;

    private FirebaseAuth mAuth;
    private DatabaseReference AdminRef;

    String currentUserID;
    String userid;
    Dialog dialog;
    static final String TAG = HomeActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);


        mAuth = FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        AdminRef = FirebaseDatabase.getInstance().getReference().child("Users");



        /*Purchases.getSharedInstance().getPurchaserInfo(new ReceivePurchaserInfoListener() {
            @Override
            public void onReceived(@NonNull PurchaserInfo purchaserInfo) {
                EntitlementInfo info = purchaserInfo.getEntitlements().get("full_access");
                if (info!=null && info.isActive())
                {
                    //Toast.makeText(HomeActivity.this, "ACTIVE : ", Toast.LENGTH_LONG).show();

                } else {
                    //Log.d(TAG, "subStatus: " + info.toString());
                    //Toast.makeText(HomeActivity.this, "NOT ACTIVE : ", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(@NonNull PurchasesError error) {

            }
        });*/

        checkUserStatus();


        indiText = (TextView) findViewById(R.id.textView104);
        bizText = (TextView) findViewById(R.id.textView105);

        indiImage = (ImageView) findViewById(R.id.imageView29);
        bizImage = (ImageView) findViewById(R.id.imageView32);

        mMyApplication = BusinessCard.getInstance();
        if(savedInstanceState == null){
            openFragment(new IndividualHomeFragment());
            indiImage.setColorFilter(HomeActivity.this.getResources().getColor(R.color.colorPrimary));
            bizImage.setColorFilter(HomeActivity.this.getResources().getColor(R.color.primaryDark));
            indiText.setTextColor(Color.parseColor("#FFD700"));
            bizText.setTextColor(Color.parseColor("#9F8C25"));
        }

        progressDialog = new ProgressDialog(this);


        mCompressor = new FileCompressor(this);

        UpdateToken(FirebaseInstanceId.getInstance().getToken());

    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null)
        {
            UID = user.getUid();

        }
    }


    private void UpdateToken( String refreshToken) {

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1= new Token(refreshToken);
        ref.child(firebaseUser.getUid()).setValue(token1);
    }

    void openFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
        

    }

    public void open_buz_frag(View view) {
        openBizFragment(new BusinessesHomeFragment());
        indiImage.setColorFilter(HomeActivity.this.getResources().getColor(R.color.primaryDark));
        bizImage.setColorFilter(HomeActivity.this.getResources().getColor(R.color.colorPrimary));
        indiText.setTextColor(Color.parseColor("#9F8C25"));
        bizText.setTextColor(Color.parseColor("#FFD700"));
    }

    private void openBizFragment(BusinessesHomeFragment businessesHomeFragment) {


        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, businessesHomeFragment);
        fragmentTransaction.commit();
    }

    public void open_indi_frag(View view) {
        openIndiFrag(new IndividualHomeFragment());
        indiImage.setColorFilter(HomeActivity.this.getResources().getColor(R.color.colorPrimary));
        bizImage.setColorFilter(HomeActivity.this.getResources().getColor(R.color.primaryDark));
        indiText.setTextColor(Color.parseColor("#FFD700"));
        bizText.setTextColor(Color.parseColor("#9F8C25"));
    }

    private void openIndiFrag(IndividualHomeFragment individualHomeFragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, individualHomeFragment);
        fragmentTransaction.commit();
    }


    public void scanCode(View view) {

        dialog = new Dialog(HomeActivity.this);
        dialog.setContentView(R.layout.custom_add_card_dialog);
        ImageView scanP = (ImageView) dialog.findViewById(R.id.imageView38);
        ImageView libP = (ImageView) dialog.findViewById(R.id.imageView39);
        ImageView scanB = (ImageView) dialog.findViewById(R.id.imageView40);
        ImageView libB = (ImageView) dialog.findViewById(R.id.imageView50);
        Button cancelBtn = (Button) dialog.findViewById(R.id.button58);
        scanP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                qrScan = new IntentIntegrator(HomeActivity.this);
                qrScan.setBeepEnabled(true);
                qrScan.setOrientationLocked(false);
                qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                qrScan.setPrompt("Scan QR Code");
                qrScan.setCameraId(0);  // Use a specific camera of the device
                qrScan.setBeepEnabled(true);
                qrScan.setBarcodeImageEnabled(true);
                qrScan.initiateScan();
                dialog.dismiss();
            }
        });
        libP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setDataAndType( android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(pickIntent, REQUEST_GALLERY_PHOTO);
                dialog.dismiss();
            }
        });
        scanB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent= new Intent(HomeActivity.this, ScanBusinessActivity.class);
                startActivity(intent);
                Animatoo.animateZoom(HomeActivity.this);
                dialog.dismiss();
            }
        });
        libB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setDataAndType( android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(pickIntent, BUSINESS_REQUEST_GALLERY_PHOTO);
                dialog.dismiss();
            }
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null && resultCode == RESULT_OK && data != null) {

            if(result.getContents() == null) {
                //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                cardKey = result.getContents().toString().trim();
                Intent intent= new Intent(HomeActivity.this, NewCardActivity.class);
                intent.putExtra("qr_string",cardKey);
                startActivity(intent);
                Animatoo.animateZoom(HomeActivity.this);

            }

        } else{
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
                    Intent intent= new Intent(HomeActivity.this, NewCardActivity.class);
                    intent.putExtra("qr_string",cardKey);
                    startActivity(intent);
                    Animatoo.animateZoom(HomeActivity.this);
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
        if (requestCode == BUSINESS_REQUEST_GALLERY_PHOTO && resultCode == RESULT_OK && data != null) {
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
                    cardKey2 = result.getText().toString().trim();
                    Intent intent= new Intent(HomeActivity.this, NewBusinessCardActivity.class);
                    intent.putExtra("qr_string",cardKey2);
                    startActivity(intent);
                    Animatoo.animateFade(HomeActivity.this);
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

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}