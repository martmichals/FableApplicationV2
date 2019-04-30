package com.example.fableapplicationv2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

//Activity in order to sign up a user, creates database entry and authenticated user
public class SignUpActivity extends AppCompatActivity {
    public final static String TAG = "SignUpActivity";
    public static boolean isFormCorrectlyCompleted;

    //Firebase objects
    private FirebaseAuth firebaseAuth;

    //All fields in the activity
    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private EditText firstNameEditText, lastNameEditText;
    private Spinner userTypeSpinner, stateSpinner;
    private EditText addressEditText, cityEditText, zipCodeEditText, phoneNumberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Initialize Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        initializeAllFields();
        isFormCorrectlyCompleted = false;
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void addDataToFirestore() {
        FirestoreHelper helper = new FirestoreHelper();
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String streetAddress = addressEditText.getText().toString();
        String city = cityEditText.getText().toString();
        String zipCode = zipCodeEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String state = stateSpinner.getSelectedItem().toString();
        String email = emailEditText.getText().toString();

        helper.addNewUser(firstName, lastName, streetAddress, city, zipCode,
                    phoneNumber, state, email, getApplicationContext());
    }

    //Method to create a firebase account
    private void createUserAccount(String email, String password) {
        Log.d(this.TAG, "Creating user account: " + email);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(SignUpActivity.TAG, "Success creating account");

                            addDataToFirestore();
                            goToMainActivity();
                        } else {
                            Log.w(SignUpActivity.TAG, "Failure creating account ", task.getException());

                            //Track down some of the Firebase specific exceptions
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                Log.e(SignUpActivity.TAG, "Weak password (Firebase Exception)");
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Log.e(SignUpActivity.TAG, "Invalid Credentials (Firebase Exception)");
                                createErrorDialog(getString(R.string.emailNotValid));
                            } catch (FirebaseAuthUserCollisionException e) {
                                Log.e(SignUpActivity.TAG, "User Collision (Firebase Exception)");
                                createErrorDialog(getString(R.string.emailAlreadyInUse));
                            } catch (Exception e) {
                                Log.e(SignUpActivity.TAG, "Uncaught Firebase exception encountered " +
                                        "on account creation");
                                createErrorDialog(getString(R.string.miscDatabaseError));
                            }
                        }
                    }
                });
    }

    //Method to initialize all variables for fields in the activity
    private void initializeAllFields() {
        emailEditText = findViewById(R.id.idEmailEditText);
        passwordEditText = findViewById(R.id.idPasswordField);
        confirmPasswordEditText = findViewById(R.id.idConfirmPasswordField);

        firstNameEditText = findViewById(R.id.idFirstNameEditText);
        lastNameEditText = findViewById(R.id.idLastNameEditText);

        userTypeSpinner = findViewById(R.id.idUserTypeSpinner);
        String[] userTypes = getResources().getStringArray(R.array.userTypes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                userTypes);
        userTypeSpinner.setAdapter(adapter);

        stateSpinner = findViewById(R.id.idStateSpinner);
        String[] states = getResources().getStringArray(R.array.state_names);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                states);
        stateSpinner.setAdapter(adapter);


        addressEditText = findViewById(R.id.idAddressEditText);
        cityEditText = findViewById(R.id.idCityEditText);
        zipCodeEditText = findViewById(R.id.idZipCodeEditText);
        phoneNumberEditText = findViewById(R.id.idPhoneNumberEditText);
    }

    /**
     * Method to create a pop-up error for the user to see, read and then dismiss
     *
     * @param error : String error to display in the dialog
     */
    private void createErrorDialog(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        builder.setTitle(R.string.accountCreationAlert)
                .setMessage(error)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Destroys the alert automatically
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Method that handles string errors thrown by the DataVerification class
     *
     * @param genericTextView : the textView to write the error to
     * @param error           : the string error given by DataVerification
     * @return : true if an error is written to the activity
     */
    private boolean handleError(TextView genericTextView, String error) {
        if (error != null) {
            genericTextView.setVisibility(View.VISIBLE);
            genericTextView.setText(error);
            return true;
        } else {
            genericTextView.setVisibility(View.GONE);
            genericTextView.setText("");
            return false;
        }
    }

    //Method executed on finished button pressed
    public void finishedButtonPressed(View v) {
        Context applicationContext = getApplicationContext();
        isFormCorrectlyCompleted = true;

        TextView emailErrorView = findViewById(R.id.idEmailError);
        String email = emailEditText.getText().toString();
        String emailError = DataVerification.checkEmail(applicationContext, email);

        TextView passwordErrorView = findViewById(R.id.idPasswordError);
        String password = passwordEditText.getText().toString();
        String passwordConfirmation = confirmPasswordEditText.getText().toString();
        String passwordError = DataVerification.checkPassword(applicationContext,
                password,
                passwordConfirmation);

        TextView nameErrorView = findViewById(R.id.idNameError);
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String firstNameError = DataVerification.checkName(applicationContext,
                firstName);
        String lastNameError = DataVerification.checkName(applicationContext,
                lastName);

        TextView addressErrorView = findViewById(R.id.idStreetAddressError);
        String address = addressEditText.getText().toString();
        String addressError = DataVerification.checkStreetAddress(applicationContext, address);

        TextView cityErrorView = findViewById(R.id.idCityError);
        String city = cityEditText.getText().toString();
        String cityError = DataVerification.checkCityValidity(applicationContext, city);

        TextView zipCodeErrorView = findViewById(R.id.idZipCodeError);
        TextView phoneNumberErrorView = findViewById(R.id.idPhoneNumberError);
        String zipCode = zipCodeEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String zipCodeError = DataVerification.checkZipValidity(applicationContext, zipCode);
        String phoneNumberError = DataVerification.checkPhoneNumberValidity(applicationContext, phoneNumber);

        boolean isDataIncorrectlyEntered[] = {handleError(emailErrorView, emailError)
                , handleError(passwordErrorView, passwordError)
                , handleError(nameErrorView, firstNameError)
                , handleError(nameErrorView, lastNameError)
                , handleError(addressErrorView, addressError)
                , handleError(cityErrorView, cityError)
                , handleError(zipCodeErrorView, zipCodeError)
                , handleError(phoneNumberErrorView, phoneNumberError)};

        for (boolean flag : isDataIncorrectlyEntered)
            if (flag) {
                isFormCorrectlyCompleted = false;
                break;
            }

        if (isFormCorrectlyCompleted) {
            Log.d(TAG, "The form was correctly completed");
            createUserAccount(email, password);
        }
    }
}
