package com.nibm.pharmagomadproject.customer.activities.auth;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.db.SupabaseStorageHelper;


import java.util.HashMap;
import java.util.Map;



public class RegisterRiderActivity extends AppCompatActivity {



    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SupabaseStorageHelper storageHelper;


    private Uri licenseUri;



    private TextInputEditText etName,
            etNic,
            etEmail,
            etPhone,
            etVehicleType,
            etVehicleReg,
            etPassword,
            etConfirmPassword;



    private static final int PICK_LICENSE = 101;





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_register_rider);



        if(getSupportActionBar()!=null)
            getSupportActionBar().hide();





        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();


        storageHelper =
                new SupabaseStorageHelper(this);





        etName =
                findViewById(R.id.etName);

        etNic =
                findViewById(R.id.etNic);

        etEmail =
                findViewById(R.id.etEmail);

        etPhone =
                findViewById(R.id.etPhone);

        etVehicleType =
                findViewById(R.id.etVehicleType);

        etVehicleReg =
                findViewById(R.id.etVehicleReg);


        etPassword =
                findViewById(R.id.etPassword);


        etConfirmPassword =
                findViewById(R.id.etConfirmPassword);





        ImageView btnBack =
                findViewById(R.id.btnBack);


        btnBack.setOnClickListener(v -> finish());





        LinearLayout uploadArea =
                findViewById(R.id.uploadLicenseArea);



        uploadArea.setOnClickListener(v -> {


            Intent intent =
                    new Intent(Intent.ACTION_PICK);


            intent.setType("image/*");


            startActivityForResult(
                    intent,
                    PICK_LICENSE
            );


        });






        MaterialButton btnSubmit =
                findViewById(R.id.btnSubmitForApproval);



        btnSubmit.setOnClickListener(v -> {


            registerRider();


        });





        TextView tvLogin =
                findViewById(R.id.tvLogin);



        tvLogin.setOnClickListener(v -> {


            startActivity(
                    new Intent(
                            this,
                            LoginActivity.class
                    )
            );


            finish();


        });



    }







    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ){

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );



        if(requestCode==PICK_LICENSE &&
                resultCode==RESULT_OK &&
                data!=null){


            licenseUri =
                    data.getData();



            Toast.makeText(
                    this,
                    "License selected",
                    Toast.LENGTH_SHORT
            ).show();


        }



    }









    private void registerRider(){



        String name =
                etName.getText()
                        .toString()
                        .trim();


        String nic =
                etNic.getText()
                        .toString()
                        .trim();


        String email =
                etEmail.getText()
                        .toString()
                        .trim();


        String phone =
                etPhone.getText()
                        .toString()
                        .trim();


        String vehicleType =
                etVehicleType.getText()
                        .toString()
                        .trim();


        String vehicleReg =
                etVehicleReg.getText()
                        .toString()
                        .trim();



        String password =
                etPassword.getText()
                        .toString()
                        .trim();


        String confirm =
                etConfirmPassword.getText()
                        .toString()
                        .trim();






        if(TextUtils.isEmpty(name)){

            etName.setError("Required");
            return;

        }


        if(TextUtils.isEmpty(email)){

            etEmail.setError("Required");
            return;

        }


        if(TextUtils.isEmpty(phone)){

            etPhone.setError("Required");
            return;

        }


        if(licenseUri==null){

            Toast.makeText(
                    this,
                    "Upload driving license",
                    Toast.LENGTH_SHORT
            ).show();

            return;

        }



        if(password.length()<6){

            etPassword.setError(
                    "Minimum 6 characters"
            );

            return;

        }




        if(!password.equals(confirm)){


            etConfirmPassword.setError(
                    "Password not match"
            );

            return;

        }





        createAccount(
                email,
                password,
                name,
                nic,
                phone,
                vehicleType,
                vehicleReg
        );



    }









    private void createAccount(
            String email,
            String password,
            String name,
            String nic,
            String phone,
            String vehicleType,
            String vehicleReg
    ){



        mAuth.createUserWithEmailAndPassword(
                        email,
                        password
                )


                .addOnSuccessListener(authResult -> {



                    String uid =
                            mAuth.getCurrentUser()
                                    .getUid();




                    uploadLicense(
                            uid,
                            name,
                            nic,
                            email,
                            phone,
                            vehicleType,
                            vehicleReg
                    );



                })



                .addOnFailureListener(e -> {


                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();


                });



    }









    private void uploadLicense(
            String uid,
            String name,
            String nic,
            String email,
            String phone,
            String vehicleType,
            String vehicleReg
    ){



        String path =
                "riders/"+uid+"/license.jpg";





        storageHelper.uploadFile(
                SupabaseStorageHelper.BUCKET_LICENSES,
                path,
                licenseUri,

                new SupabaseStorageHelper.UploadCallback() {


                    @Override
                    public void onSuccess(String url) {



                        saveFirestore(
                                uid,
                                name,
                                nic,
                                email,
                                phone,
                                vehicleType,
                                vehicleReg,
                                url
                        );


                    }



                    @Override
                    public void onFailure(String error) {


                        Toast.makeText(
                                RegisterRiderActivity.this,
                                error,
                                Toast.LENGTH_SHORT
                        ).show();


                    }


                }
        );



    }









    private void saveFirestore(
            String uid,
            String name,
            String nic,
            String email,
            String phone,
            String vehicleType,
            String vehicleReg,
            String licenseUrl
    ){



        Map<String,Object> user =
                new HashMap<>();


        user.put("name",name);
        user.put("email",email);
        user.put("phone",phone);
        user.put("role","rider");
        user.put("isApproved",false);
        user.put("status","pending");
        user.put("createdAt", Timestamp.now());






        Map<String,Object> rider =
                new HashMap<>();


        rider.put("userId",uid);
        rider.put("name",name);
        rider.put("nic",nic);
        rider.put("email",email);
        rider.put("phone",phone);
        rider.put("vehicleType",vehicleType);
        rider.put("vehicleReg",vehicleReg);
        rider.put("licenseUrl",licenseUrl);
        rider.put("isApproved",false);
        rider.put("status","pending");
        rider.put("rating",0);
        rider.put("createdAt",Timestamp.now());







        db.collection("users")
                .document(uid)
                .set(user);



        db.collection("riders")
                .add(rider)

                .addOnSuccessListener(ref -> {


                    mAuth.signOut();



                    Toast.makeText(
                            this,
                            "Registration submitted. Wait for approval",
                            Toast.LENGTH_LONG
                    ).show();




                    startActivity(
                            new Intent(
                                    this,
                                    AccountStatusActivity.class
                            )
                    );


                    finish();



                });



    }



}