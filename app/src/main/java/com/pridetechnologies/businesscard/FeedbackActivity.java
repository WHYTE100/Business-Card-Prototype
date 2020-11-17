package com.pridetechnologies.businesscard;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class FeedbackActivity extends AppCompatActivity {


    //private static final int BRAINTREE_REQUEST_CODE = 80 ;
    //TextView amount;
    //Intent service_intent;

    private Dialog dialog;
    private String UserID;

    private ConstraintLayout cL1, cL2, cL3;
    private TextView thanksTextView, titleView,patronTitle;
    private Button finishBtn, backBtn;

    private FirebaseAuth auth;
   // final String get_token = "https://us-central1-busbooking-81761.cloudfunctions.net/widgets/client_token";

    String Amount = null;
    String PreviousAmount = "0";

    private DatabaseReference userRef, donationsRef;

    //CompositeDisposable compositeDisposable = new CompositeDisposable();
    //IBraintreeAPI braintreeAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        //braintreeAPI = RetrofitBraintreeClient.getInstance().create(IBraintreeAPI.class);


        auth = FirebaseAuth.getInstance();
        UserID = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Feedback");
        donationsRef = FirebaseDatabase.getInstance().getReference().child("Donations");


        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageView22);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        thanksTextView = (TextView) findViewById(R.id.textView92);
        titleView = (TextView) findViewById(R.id.textView89);
        patronTitle = (TextView) findViewById(R.id.textView95);
        cL1 = (ConstraintLayout) findViewById(R.id.cL1);
        cL2 = (ConstraintLayout) findViewById(R.id.cL2);
        cL3 = (ConstraintLayout) findViewById(R.id.cL3);
        finishBtn = (Button) findViewById(R.id.button42);
        backBtn = (Button) findViewById(R.id.button45);


        retriveDonation();

    }

    private void retriveDonation() {

        donationsRef.child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    PreviousAmount = snapshot.child("donation_amount").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void finishActivity(View view) {
        finish();
    }

    public void sendFeedbackNow(View view) {

        dialog = new Dialog(FeedbackActivity.this);
        dialog.setContentView(R.layout.feedback_input);
        TextInputEditText feedbackView = (TextInputEditText) dialog.findViewById(R.id.editTextTextMultiLine);
        Button sendDialog  = (Button) dialog.findViewById(R.id.button40);
        sendDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String FeedbackView = feedbackView.getText().toString();

                if (TextUtils.isEmpty(FeedbackView))
                {
                    Toast.makeText(FeedbackActivity.this, "Please Type Something..", Toast.LENGTH_LONG).show();

                }
                else if (!TextUtils.isEmpty(FeedbackView))
                {
                    final android.app.AlertDialog waitingDialog=new SpotsDialog.Builder().setContext(FeedbackActivity.this).build();
                    waitingDialog.setTitle("Sending Feedback");
                    waitingDialog.show();
                    sendDialog.setEnabled(false);

                    Map<String, Object> usersMap = new HashMap<>();
                    usersMap.put("feedback_note", FeedbackView);
                    usersMap.put("feedback_sender_id", UserID);

                    userRef.push().setValue(usersMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        dialog.dismiss();
                                        waitingDialog.dismiss();
                                        cL1.setVisibility(ConstraintLayout.GONE);
                                        cL2.setVisibility(ConstraintLayout.VISIBLE);
                                        backBtn.setVisibility(Button.GONE);

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            waitingDialog.dismiss();
                            Toast.makeText(FeedbackActivity.this, "Failed to send feedback", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
        Button cancelDialog  = (Button) dialog.findViewById(R.id.button39);
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

    public void becomePatron(View view) {
        thanksTextView.setVisibility(TextView.GONE);
        titleView.setText("Become a Patron");
        patronTitle.setVisibility(TextView.GONE);
        cL1.setVisibility(ConstraintLayout.GONE);
        cL2.setVisibility(ConstraintLayout.VISIBLE);
        finishBtn.setVisibility(Button.GONE);
        backBtn.setVisibility(Button.VISIBLE);
    }

    public void backPage(View view) {
        thanksTextView.setVisibility(TextView.VISIBLE);
        titleView.setText("Feedback");
        patronTitle.setVisibility(TextView.VISIBLE);
        cL1.setVisibility(ConstraintLayout.VISIBLE);
        cL2.setVisibility(ConstraintLayout.GONE);
        finishBtn.setVisibility(Button.VISIBLE);
        backBtn.setVisibility(Button.VISIBLE);
    }

    public void donateNow(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.paypal.com/donate?hosted_button_id=NJT5LSLSJCHFL"));
        startActivity(intent);
        /*dialog = new Dialog(FeedbackActivity.this);
        dialog.setContentView(R.layout.donation_input);
        TextInputEditText amount = (TextInputEditText) dialog.findViewById(R.id.editTextTextMultiLine3);
        Button sendDialog  = (Button) dialog.findViewById(R.id.button51);
        sendDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Amount = amount.getText().toString();

                if (TextUtils.isEmpty(Amount))
                {

                    Toast.makeText(FeedbackActivity.this, "Please select Amount..", Toast.LENGTH_LONG).show();

                }
                else if (!TextUtils.isEmpty(Amount))
                {
                    dialog.dismiss();

                    if (!TextUtils.isEmpty(Common.currentToken))
                    {
                        DropInRequest dropInRequest = new DropInRequest().clientToken(Common.currentToken);
                        startActivityForResult(dropInRequest.getIntent(FeedbackActivity.this),BRAINTREE_REQUEST_CODE);

                    }
                }


            }
        });
        Button cancelDialog  = (Button) dialog.findViewById(R.id.button50);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();*/

    }

    private void getPaymentToken() {

        /*compositeDisposable.add(braintreeAPI.getToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(braintreeToken -> {

                    Common.currentToken = braintreeToken.getToken();

                }, (Throwable throwable) -> {
                    Log.d("braintreeLog", "Key : " + throwable.getMessage());
                    Toast.makeText(FeedbackActivity.this, "[GET TOKEN]" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                }));*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        /*if (requestCode == BRAINTREE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();

                compositeDisposable.add(braintreeAPI.submitPayment(Double.parseDouble(Amount),nonce.getNonce())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BraintreeTransaction>() {
                    @Override
                    public void accept(BraintreeTransaction braintreeTransaction) throws Exception {
                        cL3.setVisibility(ConstraintLayout.VISIBLE);
                        cL2.setVisibility(ConstraintLayout.GONE);
                        titleView.setText("Donation Summary");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        cL3.setVisibility(ConstraintLayout.VISIBLE);
                        cL2.setVisibility(ConstraintLayout.GONE);
                        titleView.setText("Donation Summary");
                    }
                }));
            } else if (resultCode == RESULT_CANCELED) {
                // the user canceled
            } else {
                // handle errors here, an exception may be available in
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
            }
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //compositeDisposable.clear();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //compositeDisposable.clear();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //getPaymentToken();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //getPaymentToken();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cL1.setVisibility(ConstraintLayout.VISIBLE);
        cL2.setVisibility(ConstraintLayout.GONE);
        titleView.setText("FEEDBACK");
    }
}