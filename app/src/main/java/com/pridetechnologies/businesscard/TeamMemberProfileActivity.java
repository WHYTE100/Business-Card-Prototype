package com.pridetechnologies.businesscard;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class TeamMemberProfileActivity extends AppCompatActivity {

    private Dialog dialog;
    private String TeamMemberID, BusinessKey;
    private MaterialTextView nameView, user_profession, positionView;
    private MaterialButton bioBtn, shareBtn, removeBtn;
    private DatabaseReference CompanyRef,usersRef, CompanyTeamRef;
    private CircleImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_member_profile);

        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageButton31);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TeamMemberID = getIntent().getExtras().get("member_key").toString();
        BusinessKey = getIntent().getExtras().get("business_key").toString();

        imageView = (CircleImageView) findViewById(R.id.imageView31);
        nameView = (MaterialTextView)findViewById(R.id.textView811);
        user_profession = (MaterialTextView)findViewById(R.id.user_profession_1);
        positionView = (MaterialTextView)findViewById(R.id.textView64);

        bioBtn = (MaterialButton)findViewById(R.id.button121);
        shareBtn = (MaterialButton)findViewById(R.id.button11);
        removeBtn = (MaterialButton)findViewById(R.id.button61);

        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        CompanyTeamRef = FirebaseDatabase.getInstance().getReference().child("Businesses Teams");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        
        retrieveTeamMember();
    }

    private void retrieveTeamMember() {

        usersRef.child(TeamMemberID).addValueEventListener(new ValueEventListener() {
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
                        String FirstName = snapshot.child("user_first_name").getValue().toString();
                        String OtherNames= snapshot.child("user_other_names").getValue().toString();
                        String Surname= snapshot.child("user_surname").getValue().toString();
                        nameView.setText(FirstName+" "+OtherNames+" "+Surname);
                        shareBtn.setText("Share "+FirstName+"'s Card");
                        bioBtn.setText(FirstName+"'s Bio");
                        bioBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (snapshot.hasChild("user_bio"))
                                {

                                    String BIO= snapshot.child("user_bio").getValue().toString();
                                    dialog = new Dialog(TeamMemberProfileActivity.this);
                                    dialog.setContentView(R.layout.custom_user_bio);
                                    TextView nameView = (TextView) dialog.findViewById(R.id.textView65);
                                    nameView.setText(FirstName+"'s Bio");
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
                                    Toast.makeText(TeamMemberProfileActivity.this.getApplicationContext(), "No Bio!", Toast.LENGTH_SHORT)
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
                    shareBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("user_qr_code")))
                            {
                                String FirstName = snapshot.child("user_first_name").getValue().toString();
                                String Surname= snapshot.child("user_surname").getValue().toString();
                                String QRCode = snapshot.child("user_qr_code").getValue().toString();

                                dialog = new Dialog(TeamMemberProfileActivity.this);
                                dialog.setContentView(R.layout.custom_qr_code_dialog);
                                TextView nameView = (TextView) dialog.findViewById(R.id.textView29);
                                nameView.setText(FirstName+"'s Card");
                                ImageView qrCodeView = (ImageView) dialog.findViewById(R.id.imageView9);
                                Picasso.get().load(QRCode).fit().centerCrop().placeholder(R.drawable.share_qr_code).into(qrCodeView);
                                Button downloadDialog  = (Button) dialog.findViewById(R.id.button26);
                                downloadDialog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(QRCode));
                                        startActivity(intent);
                                       /* Dexter.withContext(TeamMemberProfileActivity.this)
                                                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                                                .withListener(new MultiplePermissionsListener() {
                                                    @Override
                                                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                                                        // check if all permissions are granted
                                                        if (report.areAllPermissionsGranted()) {
                                                            //new Downloading().execute(QRCode);

                                                            File mainfile;
                                                            String fpath;


                                                            BitmapDrawable drawable = (BitmapDrawable) qrCodeView.getDrawable();
                                                            Bitmap bitmap = drawable.getBitmap();
                                                            try {

                                                                qrCodeView.setDrawingCacheEnabled(true);

                                                                bitmap = qrCodeView.getDrawingCache();

                                                                new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)+File.separator+"/MyFolder");

                                                                Random random=new Random();
                                                                int ii=100000;
                                                                ii=random.nextInt(ii);
                                                                String fname=FirstName+Surname+"Code" + ".jpg";
                                                                File direct = new File(Environment.getExternalStorageDirectory() + "/BusinessCardQRCodes");

                                                                if (!direct.exists()) {
                                                                    File wallpaperDirectory = new File("/sdcard/BusinessCardQRCodes/");
                                                                    wallpaperDirectory.mkdirs();
                                                                }

                                                                mainfile = new File(new File("/sdcard/BusinessCardQRCodes/"), fname);
                                                                if (mainfile.exists()) {
                                                                    mainfile.delete();
                                                                }

                                                                FileOutputStream fileOutputStream;
                                                                fileOutputStream = new FileOutputStream(mainfile);

                                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                                                Toast.makeText(TeamMemberProfileActivity.this.getApplicationContext(), "Saved in Gallery..", Toast.LENGTH_LONG).show();
                                                                fileOutputStream.flush();
                                                                fileOutputStream.close();
                                                                fpath=mainfile.toString();
                                                                galleryAddPic(fpath);
                                                            } catch(FileNotFoundException e){
                                                                e.printStackTrace();
                                                            } catch (IOException e) {
                                                                // TODO Auto-generated catch block
                                                                e.printStackTrace();
                                                            }
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
                                                                Toast.makeText(TeamMemberProfileActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                                                        .show();
                                                            }
                                                        })
                                                .onSameThread()
                                                .check();*/
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
                                dialog.setCancelable(true);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.show();

                            }else
                            {
                                Toast.makeText(TeamMemberProfileActivity.this, "No Code", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    removeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final String FirstName = snapshot.child("user_first_name").getValue().toString();
                            final Dialog dialog = new Dialog(TeamMemberProfileActivity.this);
                            dialog.setContentView(R.layout.custom_dialog__message_layout);
                            TextView titleView = dialog.findViewById(R.id.dialogTitle);
                            TextView messageView = dialog.findViewById(R.id.dialogMessage);
                            TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
                            TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
                            titleView.setText("Remove Member");
                            messageView.setText("Remove "+FirstName+" from Team");
                            noBtn.setText("Cancel");
                            yesBtn.setText("Remove");
                            yesBtn.setOnClickListener(v -> {
                                CompanyTeamRef.child(BusinessKey).child(TeamMemberID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {

                                            usersRef.child(TeamMemberID).child("company_key").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        usersRef.child(TeamMemberID).child("user_position").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    finish();
                                                                    Toast.makeText(TeamMemberProfileActivity.this, FirstName+" has been removed from Team", Toast.LENGTH_LONG).show();                                                                     }
                                                            }
                                                        });                                                         }
                                                }
                                            });                                                            }
                                    }
                                });

                            });
                            noBtn.setOnClickListener(v -> dialog.dismiss());
                            dialog.setCancelable(true);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void galleryAddPic(String fpath) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(fpath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}