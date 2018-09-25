package com.example.harshitjoshi.chatters.Ui.Activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.harshitjoshi.chatters.MainActivity;
import com.example.harshitjoshi.chatters.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private TextInputEditText fullName;
    private TextInputEditText id;
    private TextInputEditText pass;
    private static final Pattern PASSWORD_PATTERN=Pattern.compile("^"+".{6,20}"+"$");
    //p{L} is a Unicode Character Property that matches any kind of letter from any language
    private static final Pattern NAME_PATTERN=Pattern.compile("^[\\p{L} .'-]+$");

    private TextInputEditText conformPassword;

    private Button createAccountButton;
    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar toolbar;
    private ProgressDialog regProgress;
    private DatabaseReference databaseReference;

    private TextInputEditText dob;
    private DatePickerDialog.OnDateSetListener mDatasetListener;


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        toolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        regProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        fullName = findViewById(R.id.registerName);
        id = findViewById(R.id.registerEmail);
        pass = findViewById(R.id.registerPassword);
        conformPassword=findViewById(R.id.registerConformPassword);
        createAccountButton = findViewById(R.id.registerCreateAccountButton);
        //For Date of Birth
        dob=findViewById(R.id.registerDob);
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar=Calendar.getInstance();
                int year=calendar.get(Calendar.YEAR);
                int month=calendar.get(Calendar.MONTH);
                int day=calendar.get(Calendar.DAY_OF_MONTH);
//                int minYear=year-12;
//                int minMonth=month;
//                int minDay=day;
//
//                int maxYear=year-72;
//                int maxMonth=month;
//                int maxDay=day;
                DatePickerDialog dialog=new DatePickerDialog(RegisterActivity.this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,mDatasetListener,year,month,day);
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

            }


        });
        mDatasetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
            {
               //Month is counted from 0 january=0
                month=month+1;
                String date=month +"/" +dayOfMonth +"/" +year;
                dob.setTextColor(Color.WHITE);
                dob.setText(date);

            }
        };
         //For spinner
        //Spinner spinner=findViewById(R.id.registerGenderSpinner);
        //  ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(RegisterActivity.this,R.array.gender,android.R.layout.simple_spinner_item);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);
        //spinner.setOnItemSelectedListener(this);


        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    String name = fullName.getText().toString();
                    String email = id.getText().toString();
                    String password = pass.getText().toString();
                    String conform=conformPassword.getText().toString();
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(conform)) {
                        if (password.equals(conform))
                        {
                            regProgress.setTitle("Registering User");
                            regProgress.setMessage("Registering your Details");
                            regProgress.setCanceledOnTouchOutside(false);
                            regProgress.show();
                            registerUser(name, email, password);
                        }
                        else
                        {
                            StyleableToast.makeText(getApplicationContext(),"Password Does'nt match",Toast.LENGTH_LONG,R.style.myToast).show();
                        }

                    }
                    else
                    {
                        StyleableToast.makeText(getApplicationContext(),"Enter All Details",Toast.LENGTH_LONG,R.style.myToast).show();
                    }

                }
            }
        });
        fullName.addTextChangedListener(registerTextWatcher);
        id.addTextChangedListener(registerTextWatcher);
        pass.addTextChangedListener(registerTextWatcher);
        conformPassword.addTextChangedListener(registerTextWatcher);

      fullName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
          @Override
          public void onFocusChange(View v, boolean hasFocus)
          {
              validateName();
          }
      });
        id.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                validateEmail();
            }
        });
        pass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                validatePassword();
            }
        });
        conformPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                validateConform();
            }
        });

    }


    private void registerUser(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();
                            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("status", "Hey this is default status ");
                            userMap.put("image", "default");
                            userMap.put("thumb_nail", "default");
                            databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("successful", "createUserWithEmail:success");
                                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    // FLAG_ACTIVITY_CLEAR_TASK: clears any existing task that would be associated with the Activity before the Activity is started. This Activity then becomes the new root of the task and old Activities are finished. It can only be used in conjunction with FLAG_ACTIVITY_NEW_TASK.
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    finish();
                                }
                            });
                        } else {
                            regProgress.dismiss();
                            String error = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                error = "Weak Password ";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                error = "Wrong Email";
                            } catch (FirebaseAuthUserCollisionException e) {
                                error = "Account Already Exists!";
                            } catch (Exception e) {
                                Log.w("Not Success", e);
                                error = "There is something Wrong,Please Enter your details again" ;
                            }
                            StyleableToast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG,R.style.myToast).show();
                        }

                    }
                });
    }
    //Spinner option
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        String text=parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(),text,Toast.LENGTH_LONG).show();

    }
    //Spinner option
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    private Boolean validateName()
    {
        String nameInput=fullName.getText().toString().trim();
        if(nameInput.isEmpty())
        {
            fullName.setError("Fields can't be empty");
            return false;
        }
        else if(!NAME_PATTERN.matcher(nameInput).matches())
        {
            fullName.setError("Input alphabet characters only");
            return false;
        }

        else
        {
            fullName.setError(null);
            return true;
        }
    }
    private Boolean validateEmail()
    {
        String emailInput=id.getText().toString().trim();
        if(emailInput.isEmpty())
        {
            id.setError("Fields can't be empty");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches())
        {
            id.setError("enter a valid email Id");
            return false;
        }
        else
        {
            id.setError(null);
            return true;
        }
    }
    private Boolean validatePassword()
    {
        String passwordInput=pass.getText().toString().trim();
        if(passwordInput.isEmpty())
        {
            pass.setError("Fields can't be empty");
            return false;
        }
        else if (!PASSWORD_PATTERN.matcher(passwordInput).matches())
        {
            pass.setError("Password to short");
            return false;
        }
        else
        {
            pass.setError(null);
            return true;
        }
    }
    private Boolean validateConform()
    {
        String passwordInput=pass.getText().toString().trim();
        String conformInput=conformPassword.getText().toString().trim();
        if(passwordInput.isEmpty())
        {
            conformPassword.setError("Fields can't be empty");
            return false;
        }
        else if (!PASSWORD_PATTERN.matcher(conformInput).matches())
        {
            conformPassword.setError("Password to short");
            return false;
        }
        else if(!conformInput.equals(passwordInput))
        {
            conformPassword.setError("Password does'nt match");
            return false;
        }
        else
        {
            conformPassword.setError(null);
            return true;
        }
    }

    TextWatcher registerTextWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            fullName.setError(null);
            id.setError(null);
            pass.setError(null);
            conformPassword.setError(null);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            fullName.setError(null);
            id.setError(null);
            pass.setError(null);
            conformPassword.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s)
        {

            if (validateName() && validateEmail() && validatePassword() && validateConform())
            {
                createAccountButton.setEnabled(true);
            }

        }
    };

}
