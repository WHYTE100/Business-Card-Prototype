package com.pridetechnologies.businesscard;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
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
import com.pridetechnologies.businesscard.models.TeamCard;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class BusinessInfoFragment extends Fragment {

   private View view;

    private MaterialTextView companyNameView, buildingNameView, businessName, businessBuildingName, businessStreetName, businessAreaLoc, businessDistrict, businessCountry, businessSocialMedia, bioHead, bioBody;
    String CompanyId;
    LinearLayout callBusinessBtn, whatsAppBusinessBtn, emailBusinessBtn, webBusinessBtn;
    private ImageView facebookBusinessBtn, twitterBusinessBtn, linkedInBusinessBtn, instagramBusinessBtn;

    private DatabaseReference CompanyRef, CompanyTeamRef, usersRef;

    private Intent callIntent;
    private Dialog dialog;

    private RecyclerView cardListRecycler;

    public BusinessInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_business_info, container, false);

        Bundle bundle = getArguments();
        CompanyId = bundle.getString("agent_key");

        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        CompanyTeamRef = FirebaseDatabase.getInstance().getReference().child("Businesses Teams");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        businessSocialMedia = (MaterialTextView)view.findViewById(R.id.textView12);
        businessSocialMedia.setText("BUSINESS SOCIAL MEDIA");

        ImageButton button = (ImageButton) view.findViewById(R.id.imageButton9);
        businessName = (MaterialTextView) view.findViewById(R.id.textView50);
        businessBuildingName = (MaterialTextView) view.findViewById(R.id.textView52);
        businessStreetName = (MaterialTextView) view.findViewById(R.id.textView53);
        businessAreaLoc = (MaterialTextView) view.findViewById(R.id.textView54);
        businessDistrict = (MaterialTextView) view.findViewById(R.id.textView55);
        businessCountry = (MaterialTextView) view.findViewById(R.id.textView56);
        bioHead = (MaterialTextView) view.findViewById(R.id.textView70);
        bioBody = (MaterialTextView) view.findViewById(R.id.textView71);

        callBusinessBtn = (LinearLayout) view.findViewById(R.id.callBtn);
        whatsAppBusinessBtn = (LinearLayout) view.findViewById(R.id.whatsAppBtn);
        emailBusinessBtn = (LinearLayout) view.findViewById(R.id.emailBtn);
        webBusinessBtn = (LinearLayout) view.findViewById(R.id.shareBtn);

        facebookBusinessBtn = (ImageView) view.findViewById(R.id.imageView5);
        twitterBusinessBtn = (ImageView) view.findViewById(R.id.imageView6);
        instagramBusinessBtn = (ImageView) view.findViewById(R.id.imageView8);
        linkedInBusinessBtn = (ImageView) view.findViewById(R.id.imageView7);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        cardListRecycler = (RecyclerView) view.findViewById(R.id.teamRecycler2);
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

        retrieveBusinessInfo();
        retrieveTeams();

        return view;
    }

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
                        bioHead.setVisibility(MaterialTextView.VISIBLE);
                        bioBody.setVisibility(MaterialTextView.VISIBLE);
                        final String CompanyBio = snapshot.child("business_bio").getValue().toString();
                        bioBody.setText(CompanyBio);
                    }
                    callBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_mobile")))
                            {
                                final String Mobile = snapshot.child("business_mobile").getValue().toString();
                                final String xulNumber = String.format("tel: %s",Mobile);
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
                        }
                    });

                    whatsAppBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_whatsapp")))
                            {
                                final String WhatsApp = snapshot.child("business_whatsapp").getValue().toString();
                                final Uri uri = Uri.parse("smsto:" + WhatsApp);
                                Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                                i.setPackage("com.whatsapp");
                                startActivity(Intent.createChooser(i, "Open with WhatsApp"));

                            }else
                            {
                                Toast.makeText(getContext(), "No WhatsApp Number", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    emailBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_email")))
                            {
                                final String Email = snapshot.child("business_email").getValue().toString();
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + Email));
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                                startActivity(Intent.createChooser(emailIntent, "SEND_MAIL"));


                            }else
                            {
                                Toast.makeText(getContext(), "No Email Address", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    webBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("business_website")))
                            {
                                final String Website = snapshot.child("business_website").getValue().toString();

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(Website));
                                startActivity(intent);

                            }else
                            {
                                Toast.makeText(getContext(), "No WebSite", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    facebookBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if ((snapshot.hasChild("business_facebook")))
                            {

                                final String FaceBook = snapshot.child("business_facebook").getValue().toString();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + FaceBook)));
                                } catch (Exception e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + FaceBook)));
                                }
                            }else
                            {
                                Toast.makeText(getContext(), "No Facebook", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    twitterBusinessBtn.setOnClickListener(view -> {

                        if ((snapshot.hasChild("business_twitter")))
                        {
                            final String Twitter = snapshot.child("business_twitter").getValue().toString();

                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + Twitter)));
                            }catch (Exception e) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + Twitter)));
                            }
                        }else
                        {
                            Toast.makeText(getContext(), "No Twitter", Toast.LENGTH_LONG).show();
                        }
                    });
                    linkedInBusinessBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((snapshot.hasChild("business_linked_in")))
                            {
                                final String LinkedIn = snapshot.child("business_linked_in").getValue().toString();
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://add/%@" + LinkedIn));
                                final PackageManager packageManager = getContext().getPackageManager();
                                final List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                                if (list.isEmpty()) {
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.linkedin.com/profile/view?id=" + LinkedIn));
                                }
                                startActivity(intent);
                            }else
                            {
                                Toast.makeText(getContext(), "No LinkedIn", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(getContext(), "No Instagram", Toast.LENGTH_LONG).show();
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
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter<TeamCard, BusinessTeamViewHolder> adapter =
                new FirebaseRecyclerAdapter<TeamCard, BusinessTeamViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final BusinessTeamViewHolder cardViewHolder, final int i, @NonNull final TeamCard card)
                    {
                        final String TeamMemberID = getRef(i).getKey();

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
                                                Toast.makeText(getContext(), "No Bio!", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        }
                                    });

                                    cardViewHolder.requestBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {


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
                    @NonNull
                    @Override
                    public BusinessTeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.our_team_card, parent, false);
                        BusinessTeamViewHolder cardViewHolder = new BusinessTeamViewHolder(view);
                        return cardViewHolder;
                    }
                };
        cardListRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class BusinessTeamViewHolder extends RecyclerView.ViewHolder
    {
        MaterialTextView user_name, user_profession;
        CircleImageView user_image;
        MaterialButton bioBtn, requestBtn;

        public BusinessTeamViewHolder(@NonNull View itemView) {
            super(itemView);

            user_image = itemView.findViewById(R.id.circleImageView5);
            user_name = itemView.findViewById(R.id.textView36);
            user_profession = itemView.findViewById(R.id.textView37);
            bioBtn = itemView.findViewById(R.id.button15);
            requestBtn = itemView.findViewById(R.id.button16);

        }
    }
}