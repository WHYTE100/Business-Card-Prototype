package com.pridetechnologies.businesscard;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pridetechnologies.businesscard.Sinch.GoToVideoCallActivity;
import com.pridetechnologies.businesscard.models.Card;
import com.pridetechnologies.businesscard.models.FileCompressor;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class IndividualHomeFragment extends Fragment {

    private View view;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private Intent callIntent;
    private Dialog dialog;

    private String cardKey=null;

    IntentResult result, result2;

    FileCompressor mCompressor;

    File mPhotoFile;
    OutputStream outputStream;

    // private String firstName;
    private ProgressBar psBar;

    ProgressDialog progressDialog;

    static final int REQUEST_GALLERY_PHOTO = 8;

    private IntentIntegrator qrScan, qrScan2;


    private ViewPager2 cardListRecycler;
    private ConstraintLayout noCards;
    //private RelativeLayout yesCards;
    private ProgressBar progressBar;

    private ImageView imageView;

    private HomeActivity activity;
    private static String callType="";
    String userid= "";
    ProgressDialog loadingBar;



    private BusinessCard mMyApplication;

    private BottomSheetBehavior requestBottomSheetBehavior;

    private DatabaseReference AdminRef, MyCardsRef, CompanyRef, requestRef;
    private FirebaseAuth mAuth;
    private String currentUserID, userName;

    public IndividualHomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_individual_home, container, false);

        noCards = (ConstraintLayout) view.findViewById(R.id.noCards);

        loadingBar = new ProgressDialog(getContext());
        progressDialog = new ProgressDialog(getContext());
        psBar = (ProgressBar) view.findViewById(R.id.progressBar3);
        psBar.setVisibility(ProgressBar.VISIBLE);

        imageView = view.findViewById(R.id.imageView27);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        AdminRef = FirebaseDatabase.getInstance().getReference().child("Users");
        MyCardsRef = FirebaseDatabase.getInstance().getReference().child("My Cards");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Card Requests");
        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        MyCardsRef.keepSynced(true);
        CompanyRef.keepSynced(true);
        AdminRef.keepSynced(true);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        cardListRecycler = (ViewPager2) view.findViewById(R.id.recyclerView23);
        cardListRecycler.setClipToPadding(false);
        cardListRecycler.setClipChildren(false);
        cardListRecycler.setOffscreenPageLimit(3);
        cardListRecycler.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(4));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float v = 1 - Math.abs(position);
                page.setScaleY(0.8f + v * 0.2f);
            }
        });

        cardListRecycler.setPageTransformer(compositePageTransformer);

        AdminRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    if ((dataSnapshot.hasChild("user_image")))
                    {
                        String Image = dataSnapshot.child("user_image").getValue().toString();

                        Picasso.get().load(Image).fit().centerCrop().placeholder(R.mipmap.user_gold).into(imageView);

                    }
                    if ((dataSnapshot.hasChild("user_first_name")))
                    {
                        String FirstName = dataSnapshot.child("user_first_name").getValue().toString();
                        //textView.setText(FirstName);

                        SharedPreferences.Editor editor=getActivity().getSharedPreferences("Sinch",MODE_PRIVATE).edit();
                        editor.putString("Username",currentUserID);
                        editor.apply();

                    }
                    if ((dataSnapshot.hasChild("user_mobile")))
                    {
                        String userMobile = dataSnapshot.child("user_mobile").getValue().toString();

                        SharedPreferences.Editor editor=getActivity().getSharedPreferences("Phone",MODE_PRIVATE).edit();
                        editor.putString("number",userMobile);
                        editor.apply();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        MyCardsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {noCards.setVisibility(ConstraintLayout.GONE);
                    cardListRecycler.setVisibility(View.VISIBLE);
                }else
                    {
                        psBar.setVisibility(ProgressBar.GONE);
                        noCards.setVisibility(ConstraintLayout.VISIBLE);
                        cardListRecycler.setVisibility(View.GONE);
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        requestRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists())
                {//fReqBtn.setText("Request More Cards");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent= new Intent(getContext(), ProfileActivity.class);
                startActivity(intent);
                Animatoo.animateFade(getContext());
            }
        });

        retrieveCards();
        return view;
    }

    private void retrieveCards() {

        FirebaseRecyclerOptions<Card> options =
                new FirebaseRecyclerOptions.Builder<Card>()
                        .setQuery(MyCardsRef.child(currentUserID), Card.class)
                        //.setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter<Card, CardViewHolder> adapter =
                new FirebaseRecyclerAdapter<Card, CardViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final CardViewHolder cardViewHolder, final int i, @NonNull final Card card)
                    {
                        final String AgentID = getRef(i).getKey();

                        psBar.setVisibility(ProgressBar.GONE);
                        noCards.setVisibility(ConstraintLayout.GONE);

                        AdminRef.child(AgentID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists())
                                {

                                    final String[] UID = {"uid"};
                                    //yesCards.setVisibility(RelativeLayout.VISIBLE);
                                    if ((dataSnapshot.hasChild("user_uid"))) {
                                        UID[0]= dataSnapshot.child("user_uid").getValue().toString();
                                        userid = UID[0];
                                        //Toast.makeText(getContext().getApplicationContext(), "UserID: " + userid, Toast.LENGTH_SHORT).show();
                                    }
                                    if ((dataSnapshot.hasChild("user_image")))
                                    {
                                        String Image = dataSnapshot.child("user_image").getValue().toString();

                                        Picasso.get().load(Image).fit().centerCrop().placeholder(R.mipmap.user_gold).into(cardViewHolder.user_image);
                                    }
                                    if ((dataSnapshot.hasChild("user_first_name")))
                                    {
                                        ////firstName = dataSnapshot.child("user_first_name").getValue().toString();
                                        final String FirstName = dataSnapshot.child("user_first_name").getValue().toString();
                                        String OtherNames= dataSnapshot.child("user_other_names").getValue().toString();
                                        String Surname= dataSnapshot.child("user_surname").getValue().toString();
                                        cardViewHolder.user_name.setText(FirstName+" "+OtherNames+" "+Surname);
                                        cardViewHolder.userBio.setText(FirstName+"'s Bio");
                                        cardViewHolder.userBio.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (dataSnapshot.hasChild("user_bio"))
                                                {

                                                    String BIO= dataSnapshot.child("user_bio").getValue().toString();
                                                    dialog = new Dialog(getContext());
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
                                                    Toast.makeText(getContext().getApplicationContext(), "No Bio!", Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            }
                                        });
                                    }
                                    if ((dataSnapshot.hasChild("user_profession")))
                                    {
                                        String Profession = dataSnapshot.child("user_profession").getValue().toString();
                                        cardViewHolder.user_profession.setText(Profession);

                                    }
                                    if ((dataSnapshot.hasChild("user_position")))
                                    {
                                        String Position = dataSnapshot.child("user_position").getValue().toString();
                                        cardViewHolder.user_position.setText(Position);

                                    }
                                    if ((dataSnapshot.hasChild("company_key")))
                                    {

                                        cardViewHolder.noInfo.setVisibility(View.GONE);
                                        cardViewHolder.yesInfo.setVisibility(View.VISIBLE);
                                        final String CompanyId = dataSnapshot.child("company_key").getValue().toString();
                                        CompanyRef.child(CompanyId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                                                if (snapshot.exists())
                                                {

                                                    if ((snapshot.hasChild("business_logo")))
                                                    {
                                                        final String Image = snapshot.child("business_logo").getValue().toString();
                                                        Picasso.get().load(Image).fit().centerCrop().placeholder(R.mipmap.background_icon).into(cardViewHolder.business_logo);
                                                    }
                                                    if ((snapshot.hasChild("business_name")))
                                                    {
                                                        final String CompanyBuilding = snapshot.child("business_building").getValue().toString();
                                                        cardViewHolder.user_company_building.setText(CompanyBuilding);
                                                    }
                                                    if (snapshot.hasChild("business_name")){
                                                        final String CompanyName = snapshot.child("business_name").getValue().toString();
                                                        cardViewHolder.user_company_name.setText(CompanyName);
                                                    }
                                                    if (snapshot.hasChild("business_street")){
                                                        final String CompanyStreet = snapshot.child("business_street").getValue().toString();
                                                        cardViewHolder.user_company_street.setText(CompanyStreet);
                                                    }
                                                    if (snapshot.hasChild("business_location")){
                                                        final String CompanyArea = snapshot.child("business_location").getValue().toString();
                                                        cardViewHolder.user_company_area_located.setText(CompanyArea);
                                                    }
                                                    if (snapshot.hasChild("business_district")){
                                                        final String CompanDistrict = snapshot.child("business_district").getValue().toString();
                                                        cardViewHolder.user_company_district.setText(CompanDistrict);
                                                    }
                                                    if (snapshot.hasChild("business_country")){
                                                        final String CompanyCountry = snapshot.child("business_country").getValue().toString();
                                                        cardViewHolder.user_company_country.setText(CompanyCountry);
                                                    }
                                                    cardViewHolder.seeMore.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {

                                                            Intent intent = new Intent(getContext(), BasicBusinessAccountActivity.class);
                                                            intent.putExtra("key", CompanyId);
                                                            startActivity(intent);
                                                            Animatoo.animateFade(getContext());
                                                        }
                                                    });

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }else {
                                        cardViewHolder.noInfo.setVisibility(View.VISIBLE);
                                        cardViewHolder.yesInfo.setVisibility(View.GONE);
                                    }
                                    cardViewHolder.smsBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            if ((dataSnapshot.hasChild("user_mobile")))
                                            {
                                                String Mobile = dataSnapshot.child("user_mobile").getValue().toString();
                                                if(!Mobile.isEmpty())
                                                {
                                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+Mobile));
                                                    startActivity(intent);
                                                    Animatoo.animateFade(getContext());
                                                }else
                                                {
                                                    Toast.makeText(getContext(), "No Mobile Number", Toast.LENGTH_LONG).show();
                                                }
                                            }else
                                            {
                                                Toast.makeText(getContext(), "No Mobile Number", Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });
                                    cardViewHolder.callVoiceBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            if ((dataSnapshot.hasChild("user_mobile")))
                                            {
                                                String Mobile = dataSnapshot.child("user_mobile").getValue().toString();
                                                if(!Mobile.isEmpty())
                                                {
                                                    String xulNumber = String.format("tel: %s", Mobile);
                                                    callIntent = new Intent(Intent.ACTION_CALL);
                                                    callIntent.setData(Uri.parse(xulNumber));
                                                    Dexter.withContext(getContext())
                                                            .withPermissions(Manifest.permission.CALL_PHONE)
                                                            .withListener(new MultiplePermissionsListener() {
                                                                @Override
                                                                public void onPermissionsChecked(MultiplePermissionsReport report) {
                                                                    // check if all permissions are granted
                                                                    if (report.areAllPermissionsGranted()) {
                                                                        startActivity(callIntent);
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
                                                                            Toast.makeText(getContext().getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                                                                    .show();
                                                                        }
                                                                    })
                                                            .onSameThread()
                                                            .check();
                                                }else
                                                {
                                                    Toast.makeText(getContext(), "No Mobile Number", Toast.LENGTH_LONG).show();
                                                }
                                            }else
                                            {
                                                Toast.makeText(getContext(), "No Mobile Number", Toast.LENGTH_LONG).show();
                                            }
                                            /*Intent voiceCallIntent;
                                            voiceCallIntent=new Intent(getContext(), GoToVoiceCallActivity.class);
                                            voiceCallIntent.putExtra("userid", UID[0]);
                                            startActivity(voiceCallIntent);*/
                                        }
                                    });
                                    cardViewHolder.callVideoBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent videoCallIntent;
                                            videoCallIntent=new Intent(getContext(), GoToVideoCallActivity.class);
                                            videoCallIntent.putExtra("userid", UID[0]);
                                            startActivity(videoCallIntent);

                                        }
                                    });
                                    if (dataSnapshot.hasChild("user_whatsapp")) {
                                        cardViewHolder.whatsAppBtn.setVisibility(View.VISIBLE);
                                        String WhatsApp = dataSnapshot.child("user_whatsapp").getValue().toString();
                                        cardViewHolder.whatsAppBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                if(!WhatsApp.isEmpty())
                                                {
                                                    Uri uri = Uri.parse("smsto:" + WhatsApp);
                                                    Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                                                    i.setPackage("com.whatsapp");
                                                    startActivity(Intent.createChooser(i, "Open with WhatsApp"));
                                                }
                                            }
                                        });
                                    }else{
                                        cardViewHolder.whatsAppBtn.setVisibility(View.GONE);
                                    }

                                    if (dataSnapshot.hasChild("user_facebook")) {
                                        cardViewHolder.facebookBtn.setVisibility(View.VISIBLE);
                                        cardViewHolder.facebookBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                String FaceBook = dataSnapshot.child("user_facebook").getValue().toString();
                                                if(!FaceBook.isEmpty())
                                                {
                                                    Intent intent= new Intent(getContext(), BrowserWebViewActivity.class);
                                                    intent.putExtra("urlLink",FaceBook);
                                                    startActivity(intent);
                                                    Animatoo.animateFade(getContext());
                                                }
                                            }
                                        });
                                    } else {
                                        cardViewHolder.facebookBtn.setVisibility(View.GONE);
                                    }

                                    if (dataSnapshot.hasChild("user_twitter")) {
                                        cardViewHolder.twitterBtn.setVisibility(View.VISIBLE);
                                        cardViewHolder.twitterBtn.setOnClickListener(v -> {
                                            String Twitter = dataSnapshot.child("user_twitter").getValue().toString();
                                            if(!Twitter.isEmpty())
                                            {
                                                Intent intent= new Intent(getContext(), BrowserWebViewActivity.class);
                                                intent.putExtra("urlLink",Twitter);
                                                startActivity(intent);
                                                Animatoo.animateFade(getContext());
                                            }
                                        });
                                    } else {
                                        cardViewHolder.twitterBtn.setVisibility(View.GONE);
                                    }
                                    if (dataSnapshot.hasChild("user_linked_in")) {
                                        cardViewHolder.linkedInBtn.setVisibility(View.VISIBLE);
                                        cardViewHolder.linkedInBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                String LinkedIn = dataSnapshot.child("user_linked_in").getValue().toString();
                                                if(!LinkedIn.isEmpty())
                                                {
                                                    Intent intent= new Intent(getContext(), BrowserWebViewActivity.class);
                                                    intent.putExtra("urlLink",LinkedIn);
                                                    startActivity(intent);
                                                    Animatoo.animateFade(getContext());
                                                }
                                            }
                                        });
                                    } else {
                                        cardViewHolder.linkedInBtn.setVisibility(View.GONE);
                                    }
                                    if (dataSnapshot.hasChild("user_instagram")) {
                                        cardViewHolder.instagramBtn.setVisibility(View.VISIBLE);
                                       cardViewHolder.instagramBtn.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               String Instagram = dataSnapshot.child("user_instagram").getValue().toString();
                                               if(!Instagram.isEmpty())
                                               {
                                                   Intent intent= new Intent(getContext(), BrowserWebViewActivity.class);
                                                   intent.putExtra("urlLink",Instagram);
                                                   startActivity(intent);
                                                   Animatoo.animateFade(getContext());
                                               }
                                           }
                                       });
                                    } else {
                                        cardViewHolder.instagramBtn.setVisibility(View.GONE);
                                    }
                                    /*if (dataSnapshot.hasChild("user_tik_tok")) {
                                        val tikTokValue = dataSnapshot.child("user_tik_tok").value.toString()
                                        cardViewHolder.tikTokBtn.setOnClickListener {
                                            if (tikTokValue.isNotEmpty()) {
                                                val intent = Intent(context, BrowserWebViewActivity::class.java)
                                                intent.putExtra("urlLink", tikTokValue)
                                                startActivity(intent)
                                                Animatoo.animateFade(context)
                                            }
                                        }
                                    } else {
                                        cardViewHolder.tikTokBtn.visibility = View.GONE
                                    }
                                    if (dataSnapshot.hasChild("user_we_chat")) {
                                        val weChatValue = dataSnapshot.child("user_we_chat").value.toString()
                                        cardViewHolder.weChatBtn.setOnClickListener {
                                            if (weChatValue.isNotEmpty()) {
                                                val intent = Intent(context, BrowserWebViewActivity::class.java)
                                                intent.putExtra("urlLink", weChatValue)
                                                startActivity(intent)
                                                Animatoo.animateFade(context)
                                            }
                                        }
                                    } else {
                                        cardViewHolder.weChatBtn.visibility = View.GONE
                                    }
                                    if (dataSnapshot.hasChild("user_youtube")) {
                                        val youtubeValue = dataSnapshot.child("user_youtube").value.toString()
                                        cardViewHolder.youtubeBtn.setOnClickListener {
                                            if (youtubeValue.isNotEmpty()) {
                                                val intent = Intent(context, BrowserWebViewActivity::class.java)
                                                intent.putExtra("urlLink", youtubeValue)
                                                startActivity(intent)
                                                Animatoo.animateFade(context)
                                            }
                                        }
                                    } else {
                                        cardViewHolder.youtubeBtn.visibility = View.GONE
                                    }*/
                                    cardViewHolder.emailBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if ((dataSnapshot.hasChild("user_email")))
                                            {
                                                String Email = dataSnapshot.child("user_email").getValue().toString();
                                                if(!Email.isEmpty())
                                                {
                                                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + Email));
                                                    startActivity(Intent.createChooser(emailIntent, "SEND_MAIL"));
                                                }else
                                                {
                                                    Toast.makeText(getContext(), "No Email Address", Toast.LENGTH_LONG).show();
                                                }
                                            }else
                                            {
                                                Toast.makeText(getContext(), "No Email Address", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                    cardViewHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if ((dataSnapshot.hasChild("user_qr_code")))
                                            {

                                                /*if (mInterstitialAd.isLoaded()) {
                                                    mInterstitialAd.show();
                                                } else {
                                                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                                                }*/
                                                final String QRCode = dataSnapshot.child("user_qr_code").getValue().toString();

                                                final String FirstName = dataSnapshot.child("user_first_name").getValue().toString();

                                                dialog = new Dialog(getContext());
                                                dialog.setContentView(R.layout.custom_qr_code_dialog);
                                                TextView nameView = (TextView) dialog.findViewById(R.id.textView29);
                                                nameView.setText(FirstName+"'s"+" Card");
                                                final ImageView qrCodeView = (ImageView) dialog.findViewById(R.id.imageView9);
                                                Picasso.get().load(QRCode).fit().centerCrop().placeholder(R.drawable.share_qr_code).into(qrCodeView);
                                                Button downloadDialog  = (Button) dialog.findViewById(R.id.button26);
                                                downloadDialog.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        //Intent intent = new Intent(Intent.ACTION_VIEW);
                                                        //intent.setData(Uri.parse(QRCode));
                                                        //startActivity(intent);

                                                        FileOutputStream fileOutputStream=null;
                                                        File file=getdisc();
                                                        if (!file.exists() && !file.mkdirs())
                                                        {
                                                            Toast.makeText(getContext(),"sorry can not make dir",Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        String name=FirstName+"'sCode"+".jpeg";
                                                        String file_name=file.getAbsolutePath()+"/"+name;
                                                        File new_file=new File(file_name);
                                                        try {
                                                            fileOutputStream =new FileOutputStream(new_file);
                                                            Bitmap bitmap=viewToBitmap(qrCodeView,qrCodeView.getWidth(),qrCodeView.getHeight());
                                                            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                                                            Toast.makeText(getContext(),"Download Complete", Toast.LENGTH_LONG).show();
                                                            fileOutputStream.flush();
                                                            fileOutputStream.close();
                                                        }
                                                        catch
                                                        (FileNotFoundException e) {

                                                        } catch (IOException e) {

                                                        } refreshGallary(file);
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
                                                Toast.makeText(getContext(), "No Code", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                                    cardViewHolder.imageButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            String FirstName = dataSnapshot.child("user_first_name").getValue().toString();

                                            final Dialog dialog = new Dialog(getContext());
                                            dialog.setContentView(R.layout.custom_dialog__message_layout);
                                            TextView titleView = dialog.findViewById(R.id.dialogTitle);
                                            TextView messageView = dialog.findViewById(R.id.dialogMessage);
                                            TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
                                            TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
                                            titleView.setText("Delete Card");
                                            messageView.setText("Are you sure you want to delete "+FirstName+"'s Card?");
                                            noBtn.setText("Cancel");
                                            yesBtn.setText("Delete");
                                            yesBtn.setOnClickListener(v -> {
                                                MyCardsRef.child(currentUserID).child(AgentID).removeValue();
                                                dialog.dismiss();
                                            });
                                            noBtn.setOnClickListener(v -> dialog.dismiss());
                                            dialog.show();
                                        }
                                    });

                                    if(cardViewHolder.facebookBtn.getVisibility() == View.GONE && cardViewHolder.twitterBtn.getVisibility() == View.GONE && cardViewHolder.whatsAppBtn.getVisibility() == View.GONE && cardViewHolder.linkedInBtn.getVisibility() == View.GONE && cardViewHolder.youtubeBtn.getVisibility() == View.GONE && cardViewHolder.instagramBtn.getVisibility() == View.GONE){
                                      cardViewHolder.textView12.setVisibility(View.GONE) ;
                                    }
                                }else {
                                    psBar.setVisibility(ProgressBar.GONE);
                                    noCards.setVisibility(ConstraintLayout.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                    }
                    @NonNull
                    @Override
                    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_screen_slide_page, parent, false);
                        CardViewHolder cardViewHolder = new CardViewHolder(view);
                        return cardViewHolder;
                    }
                };
        cardListRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder
    {
        MaterialTextView user_name, user_profession, user_company_name, user_position, user_company_building, user_company_street , user_company_area_located, user_company_district, user_company_country ,textView12;
        CircleImageView user_image, business_logo;
        LinearLayout smsBtn,callVoiceBtn, callVideoBtn, emailBtn, shareBtn;
        ImageView user_qr_code, facebookBtn, twitterBtn, linkedInBtn, instagramBtn,whatsAppBtn, youtubeBtn;
        ConstraintLayout noInfo, yesInfo;
        MaterialButton seeMore;
        ImageButton imageButton;
        Button userBio;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);

            user_image = itemView.findViewById(R.id.circleImageView);
            user_name = itemView.findViewById(R.id.textView);
            user_profession = itemView.findViewById(R.id.textView2);
            user_company_name = itemView.findViewById(R.id.textView14);
            user_position = itemView.findViewById(R.id.textView15);
            business_logo = itemView.findViewById(R.id.circleImageView6);
            user_company_building = itemView.findViewById(R.id.textView21);
            user_company_area_located = itemView.findViewById(R.id.textView23);
            user_company_district = itemView.findViewById(R.id.textView24);
            user_company_country = itemView.findViewById(R.id.textView25);
            user_company_street = itemView.findViewById(R.id.textView60);
            user_qr_code = itemView.findViewById(R.id.imageView4);
            smsBtn = itemView.findViewById(R.id.mobileBtn);
            whatsAppBtn = itemView.findViewById(R.id.whatsAppBtn);
            emailBtn = itemView.findViewById(R.id.emailBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            facebookBtn = itemView.findViewById(R.id.imageView5);
            twitterBtn = itemView.findViewById(R.id.imageView6);
            linkedInBtn = itemView.findViewById(R.id.imageView7);
            instagramBtn = itemView.findViewById(R.id.imageView8);
            youtubeBtn = itemView.findViewById(R.id.imageView58);

            imageButton = itemView.findViewById(R.id.imageView18);

            textView12 = itemView.findViewById(R.id.textView12);

            callVoiceBtn = itemView.findViewById(R.id.callBtn);
            callVideoBtn = itemView.findViewById(R.id.callVideoBtn);

            seeMore = itemView.findViewById(R.id.button6);
            userBio = itemView.findViewById(R.id.button28);

            noInfo = itemView.findViewById(R.id.noInfo);
            yesInfo = itemView.findViewById(R.id.yesInfo);

        }
    }


    private void refreshGallary(File file)
    { Intent i=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        i.setData(Uri.fromFile(file));
        getContext().sendBroadcast(i);
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


}