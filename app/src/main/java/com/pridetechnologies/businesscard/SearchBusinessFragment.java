package com.pridetechnologies.businesscard;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pridetechnologies.businesscard.models.SearchBusinessesCard;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class SearchBusinessFragment extends Fragment {

    private View view;

    private DatabaseReference CompanyRef, MyCardsRef;

    //final String BusinessID[] = {"business_key"};
    private String UserID;
    private FirebaseAuth firebaseAuth;

    private RecyclerView businessCardSearchRecycler;

    public SearchBusinessFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search_business, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        UserID = firebaseAuth.getCurrentUser().getUid();

        LinearLayoutManager contactsLayoutManager
                = new LinearLayoutManager(getContext());
        businessCardSearchRecycler = (RecyclerView) view.findViewById(R.id.search_recycler);
        //contactListRecycler.setHasFixedSize(true);
        businessCardSearchRecycler.setLayoutManager(contactsLayoutManager);

        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        MyCardsRef = FirebaseDatabase.getInstance().getReference().child("My Business Cards");

        ImageButton closeBtn = (ImageButton) view.findViewById(R.id.imageButton28);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        SearchView searchView = (SearchView) view.findViewById(R.id.search_business);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() >= 1)
                {
                    searchSchools(s);
                }
                return false;
            }
        });
        return view;
    }

    private void searchSchools(String s) {

        //Toast.makeText(getContext(), "Search Working : "+s, Toast.LENGTH_SHORT).show();
        //Query query = CompanyRef.orderByChild("business_name").startAt(s.toUpperCase()).endAt(s.toLowerCase() + "\uf8ff");
         Query query = CompanyRef.orderByChild("business_name").startAt(s.toUpperCase()).endAt(s.toLowerCase() + "\uf8ff");

         FirebaseRecyclerOptions<SearchBusinessesCard> options =
                new FirebaseRecyclerOptions.Builder<SearchBusinessesCard>()
                        .setQuery(query, SearchBusinessesCard.class)
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter<SearchBusinessesCard, BusinessSearchViewHolder> adapter =
                new FirebaseRecyclerAdapter<SearchBusinessesCard, BusinessSearchViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final BusinessSearchViewHolder businessSearchViewHolder, final int i, @NonNull final SearchBusinessesCard businessesCard) {

                        final String bizKey = getRef(i).getKey();
                        CompanyRef.child(bizKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    if ((snapshot.hasChild("business_logo")))
                                    {
                                        String Image = snapshot.child("business_logo").getValue().toString();

                                        Picasso.get().load(Image).fit().centerCrop().placeholder(R.mipmap.background_icon).into(businessSearchViewHolder.business_logo);
                                    }
                                    if ((snapshot.hasChild("business_name")))
                                    {
                                        String BusinessName = snapshot.child("business_name").getValue().toString();
                                        businessSearchViewHolder.company_name.setText(BusinessName);
                                    }
                                    if ((snapshot.hasChild("business_location")))
                                    {
                                        String CompanyArea = snapshot.child("business_location").getValue().toString();
                                        String CompanDistrict = snapshot.child("business_district").getValue().toString();
                                        String CompanyCountry = snapshot.child("business_country").getValue().toString();
                                        businessSearchViewHolder.company_area_located.setText(CompanyArea+", "+CompanDistrict+", "+CompanyCountry);
                                    }
                                    businessSearchViewHolder.subscribeBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(getContext(), BasicBusinessAccountActivity.class);
                                            intent.putExtra("key", bizKey);
                                            startActivity(intent);
                                            Animatoo.animateFade(getContext());
                                        }
                                    });
                                    businessSearchViewHolder.addBusinessBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setMessage("Add This Card to My Business Cards")
                                                    .setTitle("Add Business Card");
                                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {

                                                    final android.app.AlertDialog waitingDialog=new SpotsDialog.Builder().setContext(getContext()).build();
                                                    waitingDialog.setMessage("Please Wait...");
                                                    waitingDialog.show();

                                                    final Map<String, Object> requestMap = new HashMap<>();
                                                    requestMap.put("type", "approved");

                                                    MyCardsRef.child(UserID).child(bizKey).setValue(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful())
                                                            {
                                                                MyCardsRef.child(bizKey).child(UserID).setValue(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful())
                                                                        {
                                                                            waitingDialog.dismiss();
                                                                            businessSearchViewHolder.addBusinessBtn.setVisibility(View.INVISIBLE);
                                                                            businessSearchViewHolder.subscribeBtn.setVisibility(View.VISIBLE);
                                                                            businessSearchViewHolder.noteView.setText("You have this Card Already.");
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    // User cancelled the dialog
                                                }
                                            });
                                            builder.show();

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        MyCardsRef.child(UserID).child(bizKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    businessSearchViewHolder.addBusinessBtn.setVisibility(View.INVISIBLE);
                                    businessSearchViewHolder.subscribeBtn.setVisibility(View.VISIBLE);
                                    businessSearchViewHolder.noteView.setText("You have this Card Already.");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public BusinessSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_business_search, parent, false);
                        BusinessSearchViewHolder cardViewHolder = new BusinessSearchViewHolder(view);
                        return cardViewHolder;
                    }
                };
        businessCardSearchRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class BusinessSearchViewHolder extends RecyclerView.ViewHolder
    {
        MaterialTextView company_name, company_building, company_street , company_area_located, noteView, company_country, bioHead, bioBody;
        CircleImageView business_logo;
        MaterialButton addBusinessBtn, subscribeBtn, emailBtn, websiteBtn;
        ImageView facebookBtn, twitterBtn, linkedInBtn, instagramBtn;
        Button seeMoreBtn;
        ImageButton imageButton;

        public BusinessSearchViewHolder(@NonNull View itemView) {
            super(itemView);

            company_name = itemView.findViewById(R.id.textView6);
            business_logo = itemView.findViewById(R.id.imageView2);
            //company_building = itemView.findViewById(R.id.textView52);
            company_area_located = itemView.findViewById(R.id.textView32);
            noteView = itemView.findViewById(R.id.textView63);
            //company_country = itemView.findViewById(R.id.textView56);
            //company_street = itemView.findViewById(R.id.textView53);
            //bioHead = itemView.findViewById(R.id.textView70);
            //bioBody = itemView.findViewById(R.id.textView71);
            addBusinessBtn = itemView.findViewById(R.id.button5);
            subscribeBtn = itemView.findViewById(R.id.button51);

        }
    }

    public static class BusinessCardViewHolder extends RecyclerView.ViewHolder
    {

        public BusinessCardViewHolder(@NonNull View itemView) {
            super(itemView);


        }
    }
}