package com.pridetechnologies.businesscard;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

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
import com.pridetechnologies.businesscard.models.BusinessesCard;
import com.pridetechnologies.businesscard.models.TeamCard;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.co.prnd.readmore.ReadMoreTextView;


public class BusinessesHomeFragment extends Fragment {

    private View view;

    private String UserID;
    private String CompanyId=null;
    private Dialog dialog;

    private FirebaseAuth auth;
    private CircleImageView imageView;

    private Intent callIntent;

    String Website=null;

    private ViewPager2 cardListRecycler;
    private ProgressBar psBar;

    private ConstraintLayout noCards;

    private DatabaseReference userRef, CompanyRef, MyCardsRef, CompanyTeamRef, CardRequestRef;

    FirebaseRecyclerAdapter<TeamCard, MyBottomSheetFragment.BusinessTeamViewHolder> teamsAdapter;

    public BusinessesHomeFragment() {
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
        view = inflater.inflate(R.layout.fragment_businesses_home, container, false);

        imageView = (CircleImageView) view.findViewById(R.id.imageView37);

        ImageView searchBtn = (ImageView) view.findViewById(R.id.imageView28);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchBusinessFragment searchBusinessFragment = new SearchBusinessFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, searchBusinessFragment,"open this fragment")
                        .addToBackStack(null)
                        .commit();
            }
        });

        noCards = (ConstraintLayout) view.findViewById(R.id.noBusinessCards);

        psBar = (ProgressBar) view.findViewById(R.id.progressBar3);
        psBar.setVisibility(ProgressBar.VISIBLE);

        //LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        cardListRecycler = (ViewPager2) view.findViewById(R.id.recyclerView24);
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

        auth = FirebaseAuth.getInstance();
        UserID = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        CompanyTeamRef = FirebaseDatabase.getInstance().getReference().child("Businesses Teams");
        CardRequestRef = FirebaseDatabase.getInstance().getReference().child("Card Requests");
        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        MyCardsRef = FirebaseDatabase.getInstance().getReference().child("My Business Cards");
        MyCardsRef.keepSynced(true);
        CompanyRef.keepSynced(true);
        userRef.keepSynced(true);

        retrieveBusinessProfile();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CompanyId == null)
                {
                    Intent intent = new Intent(getContext(), CreateNewBusinessActivity.class);
                    startActivity(intent);
                    Animatoo.animateFade(getContext());
                }else
                    {
                        Intent intent = new Intent(getContext(), BasicBusinessAccountActivity.class);
                        intent.putExtra("key", CompanyId);
                        startActivity(intent);
                        Animatoo.animateFade(getContext());
                    }
            }
        });

        MyCardsRef.child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {

                }else
                {
                    psBar.setVisibility(ProgressBar.GONE);
                    noCards.setVisibility(ConstraintLayout.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        retrieveBusinessCards();

        return view;
    }

    private void retrieveBusinessCards() {

        FirebaseRecyclerOptions<BusinessesCard> options =
                new FirebaseRecyclerOptions.Builder<BusinessesCard>()
                        .setQuery(MyCardsRef.child(UserID), BusinessesCard.class)
                        //.setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter<BusinessesCard, BusinessCardViewHolder> adapter =
                new FirebaseRecyclerAdapter<BusinessesCard, BusinessCardViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final BusinessCardViewHolder cardViewHolder, final int i, @NonNull final BusinessesCard card)
                    {

                        final String BusinessKey = getRef(i).getKey();

                        psBar.setVisibility(ProgressBar.GONE);
                        noCards.setVisibility(ConstraintLayout.GONE);

                        CompanyRef.child(BusinessKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    if ((snapshot.hasChild("business_logo")))
                                    {
                                        final String Image = snapshot.child("business_logo").getValue().toString();
                                        Picasso.get().load(Image).fit().centerCrop().placeholder(R.mipmap.background_icon).into(cardViewHolder.business_logo);
                                    }
                                    if ((snapshot.hasChild("business_building")))
                                    {
                                        final String CompanyBuilding = snapshot.child("business_building").getValue().toString();
                                        cardViewHolder.company_building.setText(CompanyBuilding);
                                    }
                                    if (snapshot.hasChild("business_name")){
                                        final String CompanyName = snapshot.child("business_name").getValue().toString();
                                        cardViewHolder.company_name.setText(CompanyName);
                                    }
                                    if (snapshot.hasChild("business_street")){
                                        final String CompanyStreet = snapshot.child("business_street").getValue().toString();
                                        cardViewHolder.company_street.setText(CompanyStreet);
                                    }
                                    if (snapshot.hasChild("business_location")){
                                        final String CompanyArea = snapshot.child("business_location").getValue().toString();
                                        cardViewHolder.company_area_located.setText(CompanyArea);
                                    }
                                    if (snapshot.hasChild("business_district")){
                                        final String CompanDistrict = snapshot.child("business_district").getValue().toString();
                                        cardViewHolder.company_district.setText(CompanDistrict);
                                    }
                                    if (snapshot.hasChild("business_country")){
                                        final String CompanyCountry = snapshot.child("business_country").getValue().toString();
                                        cardViewHolder.company_country.setText(CompanyCountry);
                                    }
                                    if ((snapshot.hasChild("business_bio")))
                                    {
                                        final String CompanyBio = snapshot.child("business_bio").getValue().toString();
                                        cardViewHolder.bioBody.setText(CompanyBio);
                                        if(!CompanyBio.isEmpty())
                                        {
                                            cardViewHolder.bioHead.setVisibility(MaterialTextView.VISIBLE);
                                            cardViewHolder.bioBody.setVisibility(MaterialTextView.VISIBLE);
                                        }
                                        cardViewHolder.bioBody.setChangeListener(new ReadMoreTextView.ChangeListener() {
                                            @Override
                                            public void onStateChange(@NotNull ReadMoreTextView.State state) {

                                            }
                                        });
                                    }
                                    cardViewHolder.seeMoreBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Intent intent = new Intent(getContext(), BasicBusinessAccountActivity.class);
                                            intent.putExtra("key", BusinessKey);
                                            startActivity(intent);
                                            Animatoo.animateFade(getContext());
                                        }
                                    });
                                    cardViewHolder.callBtn.setOnClickListener(new View.OnClickListener() {
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
                                        }
                                    });

                                    cardViewHolder.whatsAppBtn.setOnClickListener(new View.OnClickListener() {
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
                                                    Toast.makeText(getContext(), "No WhatsApp Number", Toast.LENGTH_LONG).show();
                                                }

                                            }else
                                            {
                                                Toast.makeText(getContext(), "No WhatsApp Number", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                                    cardViewHolder.emailBtn.setOnClickListener(new View.OnClickListener() {
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
                                                    Toast.makeText(getContext(), "No Email Address", Toast.LENGTH_LONG).show();
                                                }
                                            }else
                                            {
                                                Toast.makeText(getContext(), "No Email Address", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                    cardViewHolder.websiteBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            if ((snapshot.hasChild("business_website")))
                                            {
                                                Website = snapshot.child("business_website").getValue().toString();
                                                if(!Website.isEmpty())
                                                {
                                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                                    intent.setData(Uri.parse(Website));
                                                    startActivity(intent);
                                                }else {
                                                    Toast.makeText(getContext(), "No WebSite", Toast.LENGTH_LONG).show();
                                                }
                                            }else
                                            {
                                                Toast.makeText(getContext(), "No WebSite", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                    cardViewHolder.facebookBtn.setOnClickListener(new View.OnClickListener() {
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
                                                Toast.makeText(getContext(), "No Facebook", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                    cardViewHolder.twitterBtn.setOnClickListener(new View.OnClickListener() {
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
                                                Toast.makeText(getContext(), "No Twitter", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                    cardViewHolder.linkedInBtn.setOnClickListener(new View.OnClickListener() {
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
                                                Toast.makeText(getContext(), "No LinkedIn", Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });
                                    cardViewHolder.instagramBtn.setOnClickListener(new View.OnClickListener() {
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
                                    cardViewHolder.imageButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            final String CompanyName = snapshot.child("business_name").getValue().toString();

                                            final Dialog dialog = new Dialog(getContext());
                                            dialog.setContentView(R.layout.custom_dialog__message_layout);
                                            TextView titleView = dialog.findViewById(R.id.dialogTitle);
                                            TextView messageView = dialog.findViewById(R.id.dialogMessage);
                                            TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
                                            TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
                                            titleView.setText("Delete Card");
                                            messageView.setText("Are you sure you want to delete "+CompanyName+"'s Card?");
                                            noBtn.setText("Cancel");
                                            yesBtn.setText("Delete");
                                            yesBtn.setOnClickListener(v -> {
                                                MyCardsRef.child(UserID).child(BusinessKey).removeValue();
                                            });
                                            noBtn.setOnClickListener(v -> dialog.dismiss());
                                            dialog.show();
                                        }
                                    });

                                    //retrieveTeams();
                                }else
                                    {
                                        psBar.setVisibility(ProgressBar.GONE);
                                        noCards.setVisibility(ConstraintLayout.VISIBLE);
                                    }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    @NonNull
                    @Override
                    public BusinessCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.business_card_screen_slide_page, parent, false);
                        BusinessCardViewHolder cardViewHolder = new BusinessCardViewHolder(view);
                        return cardViewHolder;
                    }
                };
        cardListRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class BusinessCardViewHolder extends RecyclerView.ViewHolder
    {
        MaterialTextView company_name, company_building, company_street , company_area_located, company_district, company_country, bioHead;
        CircleImageView business_logo;
        LinearLayout callBtn, whatsAppBtn, emailBtn, websiteBtn;
        ImageView facebookBtn, twitterBtn, linkedInBtn, instagramBtn;
        Button seeMoreBtn;
        ImageButton imageButton;
        ReadMoreTextView bioBody;

        public BusinessCardViewHolder(@NonNull View itemView) {
            super(itemView);

            company_name = itemView.findViewById(R.id.textView50);
            business_logo = itemView.findViewById(R.id.circleImageView7);
            company_building = itemView.findViewById(R.id.textView52);
            company_area_located = itemView.findViewById(R.id.textView54);
            company_district = itemView.findViewById(R.id.textView55);
            company_country = itemView.findViewById(R.id.textView56);
            company_street = itemView.findViewById(R.id.textView53);
            bioHead = itemView.findViewById(R.id.textView70);
            bioBody = itemView.findViewById(R.id.textView71);
            callBtn = itemView.findViewById(R.id.callBtn);
            seeMoreBtn = itemView.findViewById(R.id.button32);
            whatsAppBtn = itemView.findViewById(R.id.whatsAppBtn);
            emailBtn = itemView.findViewById(R.id.emailBtn);
            websiteBtn = itemView.findViewById(R.id.shareBtn);
            facebookBtn = itemView.findViewById(R.id.imageView5);
            twitterBtn = itemView.findViewById(R.id.imageView6);
            linkedInBtn = itemView.findViewById(R.id.imageView7);
            instagramBtn = itemView.findViewById(R.id.imageView8);

            imageButton = itemView.findViewById(R.id.imageView19);

        }
    }

    private void retrieveBusinessProfile() {

        userRef.child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (snapshot.hasChild("company_key"))
                    {
                        CompanyId = snapshot.child("company_key").getValue().toString();

                        CompanyRef.child(CompanyId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    if ((snapshot.hasChild("business_logo")))
                                    {
                                        String Image = snapshot.child("business_logo").getValue().toString();

                                        Picasso.get().load(Image).fit().centerCrop().placeholder(R.mipmap.background_icon).into(imageView);
                                    }

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
    }
}