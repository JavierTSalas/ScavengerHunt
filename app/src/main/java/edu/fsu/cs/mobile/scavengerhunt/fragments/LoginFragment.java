package edu.fsu.cs.mobile.scavengerhunt.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.fsu.cs.mobile.scavengerhunt.R;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = LoginFragment.class.getCanonicalName();
    public static final String FRAGMENT_TAG = "Login_Fragment";
    private TextInputLayout inputLayoutEmail, inputLayoutPassword, inputLayoutPasswordConfirm, inputLayoutName;
    private EditText inputEmail, inputPassword, inputPasswordConfirm, inputName;

    private FirebaseAuth mAuth;
    private Button btnLogin, btnSignOut, btnSignUp;
    private View globalView;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView tvTitle;
    private FirebaseFirestore db;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        globalView = inflater.inflate(R.layout.fragment_login, container, false);

        tvTitle = globalView.findViewById(R.id.tvLoginTitle);
        inputLayoutName = globalView.findViewById(R.id.TextInputLayoutName);
        inputLayoutEmail = globalView.findViewById(R.id.TextInputLayoutEmail);
        inputLayoutPassword = globalView.findViewById(R.id.TextInputLayoutPassword);
        inputLayoutPasswordConfirm = globalView.findViewById(R.id.TextInputLayoutPasswordConfirm);

        inputName = globalView.findViewById(R.id.input_name);
        inputEmail = globalView.findViewById(R.id.input_email);
        inputPassword = globalView.findViewById(R.id.input_password);
        inputPasswordConfirm = globalView.findViewById(R.id.input_password_confirm);
        btnSignUp = globalView.findViewById(R.id.bRegister);
        btnLogin = globalView.findViewById(R.id.bSignIn);
        btnSignOut = globalView.findViewById(R.id.bSignOut);

        inputName.addTextChangedListener(new MyTextWatcher(inputName));
        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));
        inputPasswordConfirm.addTextChangedListener(new MyTextWatcher(inputPasswordConfirm));

        btnSignUp.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        // Set the display name for the user
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    final String displayName = inputName.getText().toString();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(inputName.getText().toString()).build();
                    user.updateProfile(profileUpdates);

                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
        // Remove in live version
        //signOut();


        return globalView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser, R.string.successful_login);
    }

    /**
     * This should bring the user to their school fragment
     *
     * @param currentUser FirebaseUser that we want get information from to update UI
     * @param stringRes   Snackbar text
     */
    private void updateUI(FirebaseUser currentUser, int stringRes) {
        hideProgressDialog();
        if (currentUser != null) {

            FragmentManager manager = (getActivity()).getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(manager.findFragmentByTag(LoginFragment.FRAGMENT_TAG));
            trans.commit();

            String strRes = getActivity().getResources().getString(stringRes);
            Snackbar snack = Snackbar.make(getActivity().findViewById(android.R.id.content), strRes, Snackbar.LENGTH_LONG);
            snack.show();
            /*
            View view = snack.getView();
            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
            tv.setText(R.string.successful_register);
             */
        }

    }

    private void sendEmailVerification() {

// Send verification email
// [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email.trim());
        showProgressDialog();

        // [START create_user_with_email]


        mAuth.createUserWithEmailAndPassword(email.trim(), password.trim())
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user, R.string.successful_register);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getActivity().getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null, 0);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]

                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            inputLayoutPassword.setError(e.getReason());
                            inputLayoutPassword.requestFocus();
                            hideProgressDialog();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            inputLayoutPassword.setError(e.getMessage());
                            inputLayoutPassword.requestFocus();
                            hideProgressDialog();
                        } catch (FirebaseAuthUserCollisionException e) {
                            inputLayoutEmail.setError(e.getMessage());
                            inputLayoutEmail.requestFocus();
                            hideProgressDialog();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                            hideProgressDialog();
                        }

                    }
                });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user, R.string.successful_login);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null, 0);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "SignIn task was not successful");

                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null, 0);
    }

    private void hideProgressDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (fm.getFragments().size() > 0) {
            Fragment fragment = fm.getFragments().get(fm.getFragments().size() - 1);
            //fm.findFragmentByTag(DelayedProgressDialog.FRAGMENT_TAG);
            if (fragment instanceof DelayedProgressDialog) {
                ((DelayedProgressDialog) fragment).cancel();
            }
        }
    }

    private void showProgressDialog() {
        DelayedProgressDialog dialog = new DelayedProgressDialog();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        dialog.show(fm, DelayedProgressDialog.FRAGMENT_TAG);
    }


    /**
     * Check if our form is completed
     */
    private boolean validForm() {
        return validateEmail() && validatePassword() && validatePasswordConfirm() && validateName();
    }

//Toast.makeText(getActivity().getApplicationContext(), "Thank You!", Toast.LENGTH_SHORT).show();


    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePasswordConfirm() {
        if (!inputPassword.getText().toString().trim().equals(inputPasswordConfirm.getText().toString().trim())) {
            inputLayoutPasswordConfirm.setError(getString(R.string.err_msg_password_no_match));
            requestFocus(inputPasswordConfirm);
            return false;
        } else {
            inputLayoutPasswordConfirm.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (validForm())
            switch (v.getId()) {
                case R.id.bSignIn:
                    signIn(inputEmail.getText().toString(), inputPassword.getText().toString());
                    break;
                case R.id.bSignOut:
                    signOut();
                    break;
                case R.id.bRegister:
                    createAccount(inputEmail.getText().toString(), inputPassword.getText().toString());

                    break;
            }
    }

    private class MyTextWatcher implements TextWatcher {


        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_name:
                    validateName();
                    break;
                case R.id.input_email:
                    validateEmail();
                    break;
                case R.id.input_password:
                    validatePassword();
                    break;
                case R.id.input_password_confirm:
                    validatePasswordConfirm();
                    break;
            }
        }

    }

}
