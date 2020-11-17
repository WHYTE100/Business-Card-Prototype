package com.pridetechnologies.businesscard;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pridetechnologies.businesscard.models.BusinessContactsCard;
import com.pridetechnologies.businesscard.models.TeamCard;
import com.squareup.picasso.Picasso;

import java.util.List;

import kr.co.prnd.readmore.ReadMoreTextView;

public class BasicBusinessAccountActivity extends AppCompatActivity {

    private MaterialTextView companyNameView, buildingNameView, businessName, businessBuildingName, businessStreetName, businessAreaLoc, businessDistrict, businessCountry, businessSocialMedia;
    String CompanyId, currentUserID;
    LinearLayout callBusinessBtn, whatsAppBusinessBtn, emailBusinessBtn, shareBtn, webBusinessBtn;
    private ImageView facebookBusinessBtn, twitterBusinessBtn, linkedInBusinessBtn, instagramBusinessBtn;

    private DatabaseReference CompanyRef, CompanyTeamRef, usersRef, MyCardsRef, CardRequestRef, CompanyContactsRef;

    private Intent callIntent;
    private Dialog dialog;
    private Button manegeAccount, goToSettings;

    private MaterialTextView noDeptContactsView;

    private FirebaseAuth mAuth;

    private ReadMoreTextView bioBody;

    private RecyclerView cardListRecycler, contactListRecycler;
    FirebaseRecyclerAdapter<TeamCard, MyBottomSheetFragment.BusinessTeamViewHolder> adapter;
    FirebaseRecyclerAdapter<BusinessContactsCard, BasicBusinessContactsViewHolder> departmentContactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_business_account);

        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageButton119);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        CompanyId = getIntent().getExtras().get("key").toString();

        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        CompanyTeamRef = FirebaseDatabase.getInstance().getReference().child("Businesses Teams");
        MyCardsRef = FirebaseDatabase.getInstance().getReference().child("My Cards");
        CardRequestRef = FirebaseDatabase.getInstance().getReference().child("Card Requests");
        CompanyContactsRef = FirebaseDatabase.getInstance().getReference().child("Business Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        businessSocialMedia = (MaterialTextView) findViewById(R.id.textView12);

        noDeptContactsView = (MaterialTextView) findViewById(R.id.textView80);
        bioBody = (ReadMoreTextView) findViewById(R.id.textView71);

        manegeAccount = (Button) findViewById(R.id.button49);
        goToSettings = (Button) findViewById(R.id.button55);
        //ImageButton button = (ImageButton) findViewById(R.id.imageButton9);
        businessName = (MaterialTextView) findViewById(R.id.textView50);
        businessBuildingName = (MaterialTextView) findViewById(R.id.textView52);
        businessStreetName = (MaterialTextView) findViewById(R.id.textView53);
        businessAreaLoc = (MaterialTextView) findViewById(R.id.textView54);
        businessDistrict = (MaterialTextView) findViewById(R.id.textView55);
        businessCountry = (MaterialTextView) findViewById(R.id.textView56);

        callBusinessBtn = (LinearLayout) findViewById(R.id.callBtn);
        whatsAppBusinessBtn = (LinearLayout) findViewById(R.id.whatsAppBtn);
        emailBusinessBtn = (LinearLayout) findViewById(R.id.emailBtn);
        shareBtn = (LinearLayout) findViewById(R.id.shareBtn);
        webBusinessBtn = (LinearLayout) findViewById(R.id.websiteBtn);

        facebookBusinessBtn = (ImageView) findViewById(R.id.imageView5);
        twitterBusinessBtn = (ImageView) findViewById(R.id.imageView6);
        instagramBusinessBtn = (ImageView) findViewById(R.id.imageView8);
        linkedInBusinessBtn = (ImageView) findViewById(R.id.imageView7);

        LinearLayoutManager contactsLayoutManager
                = new LinearLayoutManager(this);
        contactListRecycler = (RecyclerView) findViewById(R.id.recyclerView28);
        //contactListRecycler.setHasFixedSize(true);
        contactListRecycler.setLayoutManager(contactsLayoutManager);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(BasicBusinessAccountActivity.this, LinearLayoutManager.HORIZONTAL, false);
        cardListRecycler = (RecyclerView) findViewById(R.id.teamRecycler2);
        cardListRecycler.setHasFixedSize(true);
        cardListRecycler.setLayoutManager(layoutManager);

        PagerSnapHelper snapHelper = new PagerSnapHelper() {
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

        manegeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BasicBusinessAccountActivity.this, BusinessAccountActivity.class);
                intent.putExtra("key", CompanyId);
                startActivity(intent);
                Animatoo.animateFade(BasicBusinessAccountActivity.this);
            }
        });

        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (snapshot.hasChild("business_admin"))
                    {
                        String AdminPriv = snapshot.child("business_admin").getValue().toString();

                        if (AdminPriv.equals("admin"))
                        {
                            if (snapshot.hasChild("company_key"))
                            {
                                String BusinessKey = snapshot.child("company_key").getValue().toString();

                                if (BusinessKey.equals(CompanyId))
                                {
                                    goToSettings.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        goToSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BasicBusinessAccountActivity.this, BusinessAccountActivity.class);
                intent.putExtra("key", CompanyId);
                startActivity(intent);
                Animatoo.animateFade(BasicBusinessAccountActivity.this);
            }
        });

        CompanyContactsRef.child(CompanyId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists())
                {
                    noDeptContactsView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        retrieveContactsCards();
        retrieveBusinessInfo();
        retrieveTeams();
        //retrieveUser();

    }

    private void retrieveContactsCards() {

        FirebaseRecyclerOptions<BusinessContactsCard> options =
                new FirebaseRecyclerOptions.Builder<BusinessContactsCard>()
                        .setQuery(CompanyContactsRef.child(CompanyId), BusinessContactsCard.class)
                        //.setLifecycleOwner(this)
                        .build();

        departmentContactsAdapter = new FirebaseRecyclerAdapter<BusinessContactsCard, BasicBusinessContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final BasicBusinessContactsViewHolder businessContactsViewHolder, final int i, @NonNull final BusinessContactsCard businessContactsCard) {

                final String ContactsID = getRef(i).getKey();

                CompanyContactsRef.child(CompanyId).child(ContactsID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            noDeptContactsView.setVisibility(View.VISIBLE);
                            if ((snapshot.hasChild("department_name")))
                            {
                                String DepartmentName = snapshot.child("department_name").getValue().toString();
                                businessContactsViewHolder.departmentName.setText(DepartmentName);
                            }
                            if ((snapshot.hasChild("department_desc")))
                            {
                                String DepartmentDesc = snapshot.child("department_desc").getValue().toString();
                                businessContactsViewHolder.departmentDesc.setText(DepartmentDesc);
                            }
                            businessContactsViewHolder.departmentEmail.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if ((snapshot.hasChild("department_email")))
                                    {
                                        String DepartmentEmail= snapshot.child("department_email").getValue().toString();
                                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + DepartmentEmail));
                                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                                        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                                        startActivity(Intent.createChooser(emailIntent, "SEND_MAIL"));
                                    }
                                }
                            });
                            businessContactsViewHolder.departmentMobile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if ((snapshot.hasChild("department_mobile")))
                                    {
                                        String DepartmentMobile = snapshot.child("department_mobile").getValue().toString();

                                        String xulNumber = String.format("tel: %s", DepartmentMobile);
                                        callIntent = new Intent(Intent.ACTION_CALL);
                                        callIntent.setData(Uri.parse(xulNumber));
                                        Dexter.withContext(BasicBusinessAccountActivity.this)
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
                                                                Toast.makeText(BasicBusinessAccountActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                                                        .show();
                                                            }
                                                        })
                                                .onSameThread()
                                                .check();
                                    }else
                                    {
                                        Toast.makeText(BasicBusinessAccountActivity.this, "No Phone Number", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            businessContactsViewHolder.departmentWhatsApp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if ((snapshot.hasChild("department_whatsapp")))
                                    {
                                        String DepartmentWhatsApp = snapshot.child("department_whatsapp").getValue().toString();
                                        Uri uri = Uri.parse("smsto:" + DepartmentWhatsApp);
                                        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                                        i.setPackage("com.whatsapp");
                                        startActivity(Intent.createChooser(i, "Open with WhatsApp"));
                                    }else
                                    {
                                        Toast.makeText(BasicBusinessAccountActivity.this, "No WhatsApp Number", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        }else
                            {
                                noDeptContactsView.setVisibility(View.GONE);
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public BasicBusinessContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.basic_department_contacts_card, parent, false);
                BasicBusinessContactsViewHolder cardViewHolder = new BasicBusinessContactsViewHolder(view);
                return cardViewHolder;
            }
        };
        contactListRecycler.setAdapter(departmentContactsAdapter);
        departmentContactsAdapter.startListening();
    }

    public static class BasicBusinessContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView departmentName, departmentDesc;
        ImageView departmentEmail, departmentMobile, departmentWhatsApp;

        public BasicBusinessContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            departmentName = itemView.findViewById(R.id.textView111);
            departmentDesc = itemView.findViewById(R.id.textView112);
            departmentEmail = itemView.findViewById(R.id.imageView34);
            departmentMobile = itemView.findViewById(R.id.imageView30);
            departmentWhatsApp = itemView.findViewById(R.id.imageView33);
        }
    }

   /* private void retrieveUser() {

        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (snapshot.hasChild("business_admin"))
                    {
                        String AdminPriv = snapshot.child("business_admin").getValue().toString();

                        if (!AdminPriv.equals("admin"))
                        {
                            manegeAccount.setVisibility(View.GONE);
                        }
                        if (AdminPriv.equals("admin"))
                        {
                            manegeAccount.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    private void retrieveBusinessInfo() {

        CompanyRef.child(CompanyId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if ((snapshot.hasChild("business_name")))
                    {
                        final String CompanyBuilding = snapshot.child("business_building").getValue().toString();
                        final String CompanyName = snapshot.child("business_name").getValue().toString();
                        final String CompanyStreet = snapshot.child("business_street").getValue().toString();
                        //final String CompanyPostal = snapshot.child("business_postal_address").getValue().toString();
                        final String CompanyArea = snapshot.child("business_location").getValue().toString();
                        final String CompanDistrict = snapshot.child("business_district").getValue().toString();
                        final String CompanyCountry = snapshot.child("business_country").getValue().toString();

                        businessName.setText(CompanyName);
                        businessStreetName.setText(CompanyStreet);
                        businessBuildingName.setText(CompanyBuilding);
                        businessStreetName.setText(CompanyStreet);
                        businessAreaLoc.setText(CompanyArea);
                        businessDistrict.setText(CompanDistrict);
                        businessCountry.setText(CompanyCountry);
                    }
                    if ((snapshot.hasChild("business_bio")))
                    {
                        final String CompanyBio = snapshot.child("business_bio").getValue().toString();
                        bioBody.setText(CompanyBio);
                        if(!CompanyBio.isEmpty())
                        {
                            bioBody.setVisibility(MaterialTextView.VISIBLE);
                        }
                    }
                    shareBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("business_qr_code")))
                            {

                                String QRCode = snapshot.child("business_qr_code").getValue().toString();
                                final String CompanyName = snapshot.child("business_name").getValue().toString();

                                Dialog dialog2 = new Dialog(BasicBusinessAccountActivity.this);
                                dialog2.setContentView(R.layout.custom_qr_code_dialog);
                                TextView nameView = (TextView) dialog2.findViewById(R.id.textView29);
                                TextView textView0 = (TextView) dialog2.findViewById(R.id.textView28);
                                nameView.setText("Business Card");
                                textView0.setVisibility(TextView.INVISIBLE);
                                ImageView qrCodeView = (ImageView) dialog2.findViewById(R.id.imageView9);
                                Picasso.get().load(QRCode).fit().centerCrop().placeholder(R.drawable.share_qr_code).into(qrCodeView);
                                Button downloadDialog  = (Button) dialog2.findViewById(R.id.button26);
                                downloadDialog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(QRCode));
                                        startActivity(intent);
                                    }
                                });
                                Button cancelDialog  = (Button) dialog2.findViewById(R.id.button10);
                                cancelDialog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        dialog2.dismiss();
                                    }
                                });
                                dialog2.setCancelable(true);
                                dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog2.show();

                            }else
                            {
                                Toast.makeText(BasicBusinessAccountActivity.this, "No Code", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    callBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_mobile")))
                            {
                                final String Mobile = snapshot.child("business_mobile").getValue().toString();
                                if(!Mobile.isEmpty())
                                {
                                    final String xulNumber = String.format("tel: %s",Mobile);
                                    callIntent = new Intent(Intent.ACTION_CALL);
                                    callIntent.setData(Uri.parse(xulNumber));
                                    Dexter.withContext(BasicBusinessAccountActivity.this)
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
                                                            Toast.makeText(BasicBusinessAccountActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                                                    .show();
                                                        }
                                                    })
                                            .onSameThread()
                                            .check();
                                }else
                                {
                                    Toast.makeText(BasicBusinessAccountActivity.this, "No Mobile Number", Toast.LENGTH_LONG).show();
                                }

                            }else
                            {
                                Toast.makeText(BasicBusinessAccountActivity.this, "No Mobile Number", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    whatsAppBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_whatsapp")))
                            {
                                final String WhatsApp = snapshot.child("business_whatsapp").getValue().toString();
                                if(!WhatsApp.isEmpty())
                                {
                                    final Uri uri = Uri.parse("smsto:" + WhatsApp);
                                    Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                                    i.setPackage("com.whatsapp");
                                    startActivity(Intent.createChooser(i, "Open with WhatsApp"));
                                }else
                                {
                                    Toast.makeText(BasicBusinessAccountActivity.this, "No WhatsApp Number", Toast.LENGTH_LONG).show();
                                }
                            }else
                            {
                                Toast.makeText(BasicBusinessAccountActivity.this, "No WhatsApp Number", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    emailBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_email")))
                            {
                                final String Email = snapshot.child("business_email").getValue().toString();
                                if(!Email.isEmpty())
                                {
                                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + Email));
                                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                                    startActivity(Intent.createChooser(emailIntent, "SEND_MAIL"));
                                }else
                                {
                                    Toast.makeText(BasicBusinessAccountActivity.this, "No Email Address", Toast.LENGTH_LONG).show();
                                }
                            }else
                            {
                                Toast.makeText(BasicBusinessAccountActivity.this, "No Email Address", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    webBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("business_website")))
                            {
                                final String Website = snapshot.child("business_website").getValue().toString();

                                if(!Website.isEmpty())
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(Website));
                                    startActivity(intent);
                                }else
                                {
                                    Toast.makeText(BasicBusinessAccountActivity.this, "No WebSite", Toast.LENGTH_LONG).show();
                                }

                            }else
                            {
                                Toast.makeText(BasicBusinessAccountActivity.this, "No WebSite", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    facebookBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("business_facebook")))
                            {

                                final String FaceBook = snapshot.child("business_facebook").getValue().toString();
                                if(!FaceBook.isEmpty())
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(FaceBook));
                                    startActivity(intent);
                                }
                            }else
                            {
                                Toast.makeText(BasicBusinessAccountActivity.this, "No Facebook", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    twitterBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("business_twitter")))
                            {
                                final String Twitter = snapshot.child("business_twitter").getValue().toString();

                                if(!Twitter.isEmpty())
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(Twitter));
                                    startActivity(intent);
                                }
                            }else
                            {
                                Toast.makeText(BasicBusinessAccountActivity.this, "No Twitter", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    linkedInBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_linked_in")))
                            {
                                final String LinkedIn = snapshot.child("business_linked_in").getValue().toString();
                                if(!LinkedIn.isEmpty())
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(LinkedIn));
                                    startActivity(intent);
                                }
                            }else
                            {
                                Toast.makeText(BasicBusinessAccountActivity.this, "No LinkedIn", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                    instagramBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_instagram")))
                            {

                                final String Instagram = snapshot.child("business_instagram").getValue().toString();
                                Uri uri = Uri.parse(Instagram);
                                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

                                likeIng.setPackage("com.instagram.android");

                                try {
                                    startActivity(likeIng);
                                } catch (ActivityNotFoundException e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(Instagram)));
                                }
                            }else
                            {
                                Toast.makeText(BasicBusinessAccountActivity.this, "No Instagram", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieveTeams() {
        FirebaseRecyclerOptions<TeamCard> options =
                new FirebaseRecyclerOptions.Builder<TeamCard>()
                        .setQuery(CompanyTeamRef.child(CompanyId), TeamCard.class)
                        //.setLifecycleOwner(this)
                        .build();

        adapter =
                new FirebaseRecyclerAdapter<TeamCard, MyBottomSheetFragment.BusinessTeamViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final MyBottomSheetFragment.BusinessTeamViewHolder cardViewHolder, final int i, @NonNull final TeamCard card)
                    {
                        final String TeamMemberID = getRef(i).getKey();

                        CompanyTeamRef.child(CompanyId).child(TeamMemberID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    String type = snapshot.child("type").getValue().toString();
                                    if(type.equals("accepted")){
                                        usersRef.child(TeamMemberID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                                                if (snapshot.exists())
                                                {
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
                                                        cardViewHolder.bioBtn.setText(FirstName+"'s Bio");

                                                    }
                                                    if ((snapshot.hasChild("user_position")))
                                                    {
                                                        String Profession = snapshot.child("user_position").getValue().toString();
                                                        cardViewHolder.user_profession.setText(Profession);

                                                    }
                                                    cardViewHolder.bioBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            if (snapshot.hasChild("user_bio"))
                                                            {
                                                                String FirstName = snapshot.child("user_first_name").getValue().toString();
                                                                String BIO= snapshot.child("user_bio").getValue().toString();
                                                                dialog = new Dialog(BasicBusinessAccountActivity.this);
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
                                                                Toast.makeText(BasicBusinessAccountActivity.this, "No Bio!", Toast.LENGTH_SHORT)
                                                                        .show();
                                                            }
                                                        }
                                                    });

                                                    cardViewHolder.requestBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            Intent intent= new Intent(BasicBusinessAccountActivity.this, NewCardActivity.class);
                                                            intent.putExtra("qr_string",TeamMemberID);
                                                            startActivity(intent);
                                                            Animatoo.animateZoom(BasicBusinessAccountActivity.this);
                                                        }
                                                    });
                                                }else
                                                {
                                                    //Toast.makeText(getContext(), "No Team", Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                        if (currentUserID.equals(TeamMemberID))
                        {
                            cardViewHolder.requestBtn.setVisibility(View.INVISIBLE);
                        }

                        CardRequestRef.child(TeamMemberID).child(currentUserID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    cardViewHolder.requestBtn.setEnabled(false);
                                    cardViewHolder.requestBtn.setText("Wait for Approval");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        CardRequestRef.child(currentUserID).child(TeamMemberID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    cardViewHolder.requestBtn.setText("Request Sent");
                                    cardViewHolder.requestBtn.setEnabled(false);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    @NonNull
                    @Override
                    public MyBottomSheetFragment.BusinessTeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.our_team_card, parent, false);
                        MyBottomSheetFragment.BusinessTeamViewHolder cardViewHolder = new MyBottomSheetFragment.BusinessTeamViewHolder(view);
                        return cardViewHolder;
                    }
                };
        cardListRecycler.setAdapter(adapter);
        adapter.startListening();
    }
}