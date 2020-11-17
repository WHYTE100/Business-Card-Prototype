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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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
import com.pridetechnologies.businesscard.models.AddBusinessContactsCard;
import com.pridetechnologies.businesscard.models.TeamCard;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BusinessAccountActivity extends AppCompatActivity {

    private Intent callIntent;
    private Dialog dialog;
    private MaterialButton shareBtn;
    //private MaterialCardView departmentCardView;

    private CircleImageView logoImageView;

    private String UserID;
    private FirebaseAuth auth;

    private MaterialTextView companyName, companyBuildingName, companyStreetName, companyAreaLocated, companyDistrict, companyCountry;
    String BusinessKey;
    private DatabaseReference CompanyRef,usersRef, CompanyTeamRef, CardRequestRef, CompanyContactsRef;

    private ImageView facebookBusinessBtn, twitterBusinessBtn, linkedInBusinessBtn, instagramBusinessBtn, callBusinessBtn, whatsAppBusinessBtn, emailBusinessBtn, webBusinessBtn;

    private RecyclerView cardListRecycler, contactListRecycler;

    FirebaseRecyclerAdapter<TeamCard, TeamViewHolder> adapter;
    //FirebaseRecyclerAdapter<RequestsCard, BusinessRequestsCardViewHolder> requestsAdapter;
    FirebaseRecyclerAdapter<AddBusinessContactsCard, BusinessContactsViewHolder> recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_account);

        shareBtn = (MaterialButton) findViewById(R.id.button60);


        companyName = (MaterialTextView) findViewById(R.id.textView43);
        companyBuildingName = (MaterialTextView) findViewById(R.id.textView44);
        companyAreaLocated = (MaterialTextView) findViewById(R.id.textView46);
        companyDistrict = (MaterialTextView) findViewById(R.id.textView47);
        companyCountry = (MaterialTextView) findViewById(R.id.textView48);
        companyStreetName = (MaterialTextView) findViewById(R.id.textView45);
        //departmentCardView = (MaterialCardView) findViewById(R.id.materialCardView51);
        //cardRequestView.setVisibility(View.GONE);
        callBusinessBtn = (ImageView) findViewById(R.id.imageView51);
        whatsAppBusinessBtn = (ImageView) findViewById(R.id.imageView61);
        emailBusinessBtn = (ImageView) findViewById(R.id.imageView71);
        webBusinessBtn = (ImageView) findViewById(R.id.imageView4);

        facebookBusinessBtn = (ImageView) findViewById(R.id.imageView5);
        twitterBusinessBtn = (ImageView) findViewById(R.id.imageView6);
        instagramBusinessBtn = (ImageView) findViewById(R.id.imageView8);
        linkedInBusinessBtn = (ImageView) findViewById(R.id.imageView7);

        logoImageView = (CircleImageView) findViewById(R.id.imageView14);

        auth = FirebaseAuth.getInstance();
        UserID = auth.getCurrentUser().getUid();
        BusinessKey = getIntent().getExtras().get("key").toString();
        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        CompanyTeamRef = FirebaseDatabase.getInstance().getReference().child("Businesses Teams");
        CardRequestRef = FirebaseDatabase.getInstance().getReference().child("Business Card Requests");
        CompanyContactsRef = FirebaseDatabase.getInstance().getReference().child("Business Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageButton8);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        LinearLayoutManager layoutManager20
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        contactListRecycler = (RecyclerView) findViewById(R.id.recyclerView222);
        contactListRecycler.setLayoutManager(layoutManager20);
        //contactListRecycler.setHasFixedSize(true);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        cardListRecycler = (RecyclerView) findViewById(R.id.teamRecycler);
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

        contactsCards();
        retrieveInfo();
        retrieveTeams();
        //retrieveRequestedCards();

    }



    private void retrieveTeams() {
        FirebaseRecyclerOptions<TeamCard> options =
                new FirebaseRecyclerOptions.Builder<TeamCard>()
                        .setQuery(CompanyTeamRef.child(BusinessKey), TeamCard.class)
                        //.setLifecycleOwner(this)
                        .build();

        adapter =
                new FirebaseRecyclerAdapter<TeamCard, TeamViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final TeamViewHolder cardViewHolder, final int i, @NonNull final TeamCard card)
                    {
                        final String TeamMemberID = getRef(i).getKey();

                        DatabaseReference request_type = CompanyTeamRef.child(BusinessKey).child(TeamMemberID).child("type").getRef();

                        request_type.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    String type = snapshot.getValue().toString();

                                    if (type.equals("accepted"))
                                    {
                                        cardViewHolder.approveBtn.setVisibility(View.GONE);
                                        cardViewHolder.declineBtn.setVisibility(View.GONE);
                                        cardViewHolder.removeBtn.setVisibility(View.VISIBLE);
                                    }
                                    if (type.equals("sent"))
                                    {
                                        cardViewHolder.approveBtn.setVisibility(View.VISIBLE);
                                        cardViewHolder.declineBtn.setVisibility(View.VISIBLE);
                                        cardViewHolder.removeBtn.setVisibility(View.GONE);
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

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

                                    }
                                    if ((snapshot.hasChild("user_position")))
                                    {
                                        String Profession = snapshot.child("user_position").getValue().toString();
                                        cardViewHolder.user_profession.setText(Profession);

                                    }
                                    cardViewHolder.approveBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            final String FirstName = snapshot.child("user_first_name").getValue().toString();
                                            final Dialog dialog = new Dialog(BusinessAccountActivity.this);
                                            dialog.setContentView(R.layout.custom_dialog__message_layout);
                                            TextView titleView = dialog.findViewById(R.id.dialogTitle);
                                            TextView messageView = dialog.findViewById(R.id.dialogMessage);
                                            TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
                                            TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
                                            titleView.setText("Add Member");
                                            messageView.setText("Add "+FirstName+" to Team");
                                            noBtn.setText("Cancel");
                                            yesBtn.setText("Add");
                                            yesBtn.setOnClickListener(v -> {
                                                final Map<String, Object> teamMap = new HashMap<>();
                                                teamMap.put("company_key", BusinessKey);
                                                teamMap.put("business_admin", "not_admin");

                                                final Map<String, Object> requestMap = new HashMap<>();
                                                requestMap.put("type", "accepted");


                                                usersRef.child(TeamMemberID).updateChildren(teamMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            CompanyTeamRef.child(BusinessKey).child(TeamMemberID).updateChildren(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        Toast.makeText(BusinessAccountActivity.this, FirstName+" has been added to Team", Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });                                                            }
                                                    }
                                                });

                                            });
                                            noBtn.setOnClickListener(v -> dialog.dismiss());
                                            dialog.show();
                                        }
                                    });

                                    cardViewHolder.declineBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            String FirstName = snapshot.child("user_first_name").getValue().toString();

                                            final Dialog dialog = new Dialog(BusinessAccountActivity.this);
                                            dialog.setContentView(R.layout.custom_dialog__message_layout);
                                            TextView titleView = dialog.findViewById(R.id.dialogTitle);
                                            TextView messageView = dialog.findViewById(R.id.dialogMessage);
                                            TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
                                            TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
                                            titleView.setText("Decline Card");
                                            messageView.setText("Decline "+FirstName+" 's Request");
                                            noBtn.setText("Cancel");
                                            yesBtn.setText("Decline");
                                            yesBtn.setOnClickListener(v -> {
                                                CompanyTeamRef.child(BusinessKey).child(TeamMemberID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            Toast.makeText(BusinessAccountActivity.this, "Request has been Declined", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            });
                                            noBtn.setOnClickListener(v -> dialog.dismiss());
                                            dialog.show();
                                        }
                                    });
                                    cardViewHolder.removeBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Intent intent = new Intent(BusinessAccountActivity.this, TeamMemberProfileActivity.class);
                                            intent.putExtra("member_key", TeamMemberID);
                                            intent.putExtra("business_key", BusinessKey);
                                            startActivity(intent);
                                            Animatoo.animateFade(BusinessAccountActivity.this);
                                        }
                                    });
                                }else
                                {
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    @NonNull
                    @Override
                    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.team_members_card, parent, false);
                        TeamViewHolder cardViewHolder = new TeamViewHolder(view);
                        return cardViewHolder;
                    }
                };
        cardListRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public void addDepartmentContacts(View view) {
        Intent intent = new Intent(BusinessAccountActivity.this, AddDepartmentContactsActivity.class);
        intent.putExtra("key",BusinessKey);
        startActivity(intent);
        Animatoo.animateFade(BusinessAccountActivity.this);
    }


    public static class TeamViewHolder extends RecyclerView.ViewHolder
    {
        MaterialTextView user_name, user_profession;
        CircleImageView user_image;
        MaterialButton approveBtn, declineBtn, removeBtn;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);

            user_image = itemView.findViewById(R.id.circleImageView5);
            user_name = itemView.findViewById(R.id.textView36);
            user_profession = itemView.findViewById(R.id.textView37);
            approveBtn = itemView.findViewById(R.id.button15);
            declineBtn = itemView.findViewById(R.id.button16);
            removeBtn = itemView.findViewById(R.id.button119);

        }
    }

    private void contactsCards() {

        FirebaseRecyclerOptions<AddBusinessContactsCard> contacts_options =
                new FirebaseRecyclerOptions.Builder<AddBusinessContactsCard>()
                        .setQuery(CompanyContactsRef.child(BusinessKey), AddBusinessContactsCard.class)
                        //.setLifecycleOwner(this)
                        .build();

        recyclerAdapter = new FirebaseRecyclerAdapter<AddBusinessContactsCard, BusinessContactsViewHolder>(contacts_options) {
            @Override
            protected void onBindViewHolder(@NonNull final BusinessContactsViewHolder businessContactsViewHolder, final int i, @NonNull final AddBusinessContactsCard businessContactsCard) {
                final String ContactsID = getRef(i).getKey();

                CompanyContactsRef.child(BusinessKey).child(ContactsID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            //departmentCardView.setVisibility(View.VISIBLE);

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
                                        Dexter.withContext(BusinessAccountActivity.this)
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
                                                                Toast.makeText(BusinessAccountActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                                                        .show();
                                                            }
                                                        })
                                                .onSameThread()
                                                .check();
                                    }else
                                    {
                                        Toast.makeText(BusinessAccountActivity.this, "No Phone Number", Toast.LENGTH_LONG).show();
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
                                        Toast.makeText(BusinessAccountActivity.this, "No WhatsApp Number", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            businessContactsViewHolder.editContacts.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(BusinessAccountActivity.this, EditDepartmentContactsActivity.class);
                                    intent.putExtra("key", BusinessKey);
                                    intent.putExtra("card_key", ContactsID);
                                    startActivity(intent);
                                    Animatoo.animateFade(BusinessAccountActivity.this);
                                }
                            });
                            businessContactsViewHolder.deleteContacts.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    final Dialog dialog = new Dialog(BusinessAccountActivity.this);
                                    dialog.setContentView(R.layout.custom_dialog__message_layout);
                                    TextView titleView = dialog.findViewById(R.id.dialogTitle);
                                    TextView messageView = dialog.findViewById(R.id.dialogMessage);
                                    TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
                                    TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
                                    titleView.setText("Delete Contacts Card");
                                    messageView.setText("Are you sure you want to delete this Contacts Card");
                                    noBtn.setText("Cancel");
                                    yesBtn.setText("Delete");
                                    yesBtn.setOnClickListener(v -> {
                                        CompanyContactsRef.child(BusinessKey).child(ContactsID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                {
                                                    Toast.makeText(BusinessAccountActivity.this, "Contact Deleted", Toast.LENGTH_LONG).show();
                                                }else
                                                {Toast.makeText(BusinessAccountActivity.this, "Failed to Delete Contact", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    });
                                    noBtn.setOnClickListener(v -> dialog.dismiss());
                                    dialog.show();
                                }
                            });

                        }else
                        {
                            Toast.makeText(BusinessAccountActivity.this, "No Contacts Available", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public BusinessContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.department_contacts_card, parent, false);
                BusinessContactsViewHolder cardViewHolder = new BusinessContactsViewHolder(view);
                return cardViewHolder;
            }
        };
        contactListRecycler.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();

    }
    public static class BusinessContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView departmentName, departmentDesc;
        ImageView departmentEmail, departmentMobile, departmentWhatsApp, editContacts, deleteContacts;

        public BusinessContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            departmentName = itemView.findViewById(R.id.textView111);
            departmentDesc = itemView.findViewById(R.id.textView112);
            departmentEmail = itemView.findViewById(R.id.imageView34);
            departmentMobile = itemView.findViewById(R.id.imageView30);
            departmentWhatsApp = itemView.findViewById(R.id.imageView33);
            editContacts = itemView.findViewById(R.id.imageView36);
            deleteContacts = itemView.findViewById(R.id.imageView35);
        }
    }

    private void retrieveInfo() {

        CompanyRef.child(BusinessKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if ((snapshot.hasChild("business_logo")))
                    {
                        String Image = snapshot.child("business_logo").getValue().toString();

                        Picasso.get().load(Image).fit().centerCrop().placeholder(R.mipmap.background_icon).into(logoImageView);
                    }
                    if ((snapshot.hasChild("business_name")))
                    {
                        final String CompanyName = snapshot.child("business_name").getValue().toString();
                        companyName.setText(CompanyName);

                    }
                    if ((snapshot.hasChild("business_building")))
                    {
                        String CompanyBuilding = snapshot.child("business_building").getValue().toString();
                        companyBuildingName.setText(CompanyBuilding);

                    }
                    if ((snapshot.hasChild("business_street")))
                    {
                        String CompanyStreet = snapshot.child("business_street").getValue().toString();
                        companyStreetName.setText(CompanyStreet);

                    }
                    if ((snapshot.hasChild("business_location")))
                    {
                        String CompanyArea = snapshot.child("business_location").getValue().toString();
                        companyAreaLocated.setText(CompanyArea);

                    }
                    if ((snapshot.hasChild("business_district")))
                    {
                        String CompanyDistrict = snapshot.child("business_district").getValue().toString();
                        companyDistrict.setText(CompanyDistrict);

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
                        companyCountry.setText(CompanyCountry);

                    }
                    callBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_mobile")))
                            {
                                String Mobile = snapshot.child("business_mobile").getValue().toString();
                                String xulNumber = String.format("tel: %s", Mobile);
                                callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse(xulNumber));
                                Dexter.withContext(BusinessAccountActivity.this)
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
                                                        Toast.makeText(BusinessAccountActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                                                .show();
                                                    }
                                                })
                                        .onSameThread()
                                        .check();


                            }else
                            {
                                Toast.makeText(BusinessAccountActivity.this, "No Mobile Number", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    whatsAppBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_whatsapp")))
                            {
                                String WhatsApp = snapshot.child("business_whatsapp").getValue().toString();
                                Uri uri = Uri.parse("smsto:" + WhatsApp);
                                Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                                i.setPackage("com.whatsapp");
                                startActivity(Intent.createChooser(i, "Open with WhatsApp"));

                            }else
                            {
                                Toast.makeText(BusinessAccountActivity.this, "No WhatsApp Number", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    emailBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_email")))
                            {
                                String Email = snapshot.child("business_email").getValue().toString();
                                if (Email == null)
                                {
                                    Toast.makeText(BusinessAccountActivity.this, "No WebSite", Toast.LENGTH_LONG).show();

                                }else
                                {
                                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + Email));
                                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                                    startActivity(Intent.createChooser(emailIntent, "SEND_MAIL"));
                                }

                            }else
                            {
                                Toast.makeText(BusinessAccountActivity.this, "No Email Address", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    webBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("business_website")))
                            {
                                String Website = snapshot.child("business_website").getValue().toString();

                                if(!Website.isEmpty())
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(Website));
                                    startActivity(intent);
                                }else
                                {
                                    Toast.makeText(BusinessAccountActivity.this, "No WebSite", Toast.LENGTH_LONG).show();
                                }

                            }else
                            {
                                Toast.makeText(BusinessAccountActivity.this, "No WebSite", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    facebookBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("business_facebook")))
                            {

                                String FaceBook = snapshot.child("business_facebook").getValue().toString();
                                if(!FaceBook.isEmpty())
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(FaceBook));
                                    startActivity(intent);
                                }else
                                {
                                    Toast.makeText(BusinessAccountActivity.this, "No Facebook", Toast.LENGTH_LONG).show();
                                }
                            }else
                            {
                                Toast.makeText(BusinessAccountActivity.this, "No Facebook", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    twitterBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("business_twitter")))
                            {
                                String Twitter = snapshot.child("business_twitter").getValue().toString();

                                if(!Twitter.isEmpty())
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(Twitter));
                                    startActivity(intent);
                                }else
                                {
                                    Toast.makeText(BusinessAccountActivity.this, "No Twitter", Toast.LENGTH_LONG).show();
                                }
                            }else
                            {
                                Toast.makeText(BusinessAccountActivity.this, "No Twitter", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    linkedInBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_linked_in")))
                            {
                                String LinkedIn = snapshot.child("business_linked_in").getValue().toString();
                                if(!LinkedIn.isEmpty())
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(LinkedIn));
                                    startActivity(intent);
                                }else
                                {
                                    Toast.makeText(BusinessAccountActivity.this, "No LinkedIn", Toast.LENGTH_LONG).show();
                                }
                            }else
                            {
                                Toast.makeText(BusinessAccountActivity.this, "No LinkedIn", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                    instagramBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_instagram")))
                            {

                                String Instagram = snapshot.child("business_instagram").getValue().toString();
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
                                Toast.makeText(BusinessAccountActivity.this, "No Instagram", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    shareBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("business_qr_code")))
                            {

                                String QRCode = snapshot.child("business_qr_code").getValue().toString();
                                final String CompanyName = snapshot.child("business_name").getValue().toString();

                                dialog = new Dialog(BusinessAccountActivity.this);
                                dialog.setContentView(R.layout.custom_qr_code_dialog);
                                TextView nameView = (TextView) dialog.findViewById(R.id.textView29);
                                TextView textView0 = (TextView) dialog.findViewById(R.id.textView28);
                                nameView.setText("Business Card");
                                textView0.setVisibility(TextView.INVISIBLE);
                                ImageView qrCodeView = (ImageView) dialog.findViewById(R.id.imageView9);
                                Picasso.get().load(QRCode).fit().centerCrop().placeholder(R.drawable.share_qr_code).into(qrCodeView);
                                Button downloadDialog  = (Button) dialog.findViewById(R.id.button26);
                                downloadDialog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(QRCode));
                                        startActivity(intent);
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
                                Toast.makeText(BusinessAccountActivity.this, "No Code", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void manageBusinessContact(View view) {

        Intent intent = new Intent(BusinessAccountActivity.this, ManageBusinessActivity.class);
        intent.putExtra("key",BusinessKey);
        startActivity(intent);
        Animatoo.animateFade(BusinessAccountActivity.this);
    }

    public void manageBusinessSocialMedia(View view) {

        Intent intent = new Intent(BusinessAccountActivity.this, BusinessSocialMediaActivity.class);
        intent.putExtra("key",BusinessKey);
        startActivity(intent);
        Animatoo.animateFade(BusinessAccountActivity.this);
    }

    public void editBasics(View view) {
        Intent intent = new Intent(BusinessAccountActivity.this, BusinessBasicsActivity.class);
        intent.putExtra("key",BusinessKey);
        startActivity(intent);
        Animatoo.animateFade(BusinessAccountActivity.this);
    }

    private void galleryAddPic(String fpath) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(fpath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void deleteBusinessAccount(View view) {

        final Dialog dialog = new Dialog(BusinessAccountActivity.this);
        dialog.setContentView(R.layout.custom_dialog__message_layout);
        TextView titleView = dialog.findViewById(R.id.dialogTitle);
        TextView messageView = dialog.findViewById(R.id.dialogMessage);
        TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
        TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
        titleView.setText("Delete Account");
        messageView.setText("Are you sure you want to delete this Account?");
        noBtn.setText("Cancel");
        yesBtn.setText("Delete");
        yesBtn.setOnClickListener(v -> {
            CompanyRef.child(BusinessKey).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                        {
                            CompanyTeamRef.child(BusinessKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        usersRef.child(UserID).child("company_key").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                {
                                                    usersRef.child(UserID).child("user_position").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful())
                                                            {
                                                                finish();
                                                            }
                                                        }
                                                    });                                                         }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });

            dialog.dismiss();
        });
        noBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    /* Dexter.withContext(BusinessAccountActivity.this)
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
                                                                String fname=CompanyName+"Code" + ".jpg";
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
                                                                Toast.makeText(BusinessAccountActivity.this.getApplicationContext(), "Saved in Gallery..", Toast.LENGTH_LONG).show();
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
                                                                Toast.makeText(BusinessAccountActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                                                        .show();
                                                            }
                                                        })
                                                .onSameThread()
                                                .check();*/
}