package com.buitio.builtgeolocation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.raweng.built.Built;
import com.raweng.built.BuiltApplication;
import com.raweng.built.BuiltError;
import com.raweng.built.BuiltUser;
import com.raweng.built.userInterface.BuiltUILoginController;

/**
 * This is built.io android tutorial.
 * 
 * Short introduction of some classes with some methods.
 * Contain classes: 
 * 1. BuiltUILoginController 
 * 2. BuiltUISignUpController 
 * 3. BuiltUIListViewController 
 * 4. BuiltLocation
 * 5. BuiltObject
 * 6. BuiltQuery
 * 7. BuiltUser
 * 
 * For quick start with built.io refer "http://docs.built.io/quickstart/index.html#android"
 * 
 * @author raw engineering, Inc
 *
 */
public class LoginActivity extends BuiltUILoginController{

    private BuiltApplication builtApplication;
    private ProgressDialog progressDialog;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/// BuiltUILoginController can be used by extending user class.
		/// To use BuiltUILoginController
		/// Note : Add required tags in manifest for reference check "http://docs.built.io/quickstart/index.html#android"
		
		/*
		 * Checking user already logged in.
		 * Checking user data is on disc.
		 */

        try {
            builtApplication = Built.application(LoginActivity.this, "blt9f2f3c1d77c907e0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(builtApplication.getCurrentUser() != null){
			Intent intent = new Intent(LoginActivity.this, LocationListActivity.class);
			startActivity(intent);
			finish();
		}

        initProgressbar();
        /*
         * set api key
         */
        setApplicationKey("blt9f2f3c1d77c907e0");

		/*
		 * set twitter application credential for login with twitter.
		 */
		setUpTwitter("Your_consumerKey", "Your_consumerSecret");
		
		/*
		 * set intent to open Sign Up activity with sign up button.
		 */
		Intent signUpintent = new Intent(LoginActivity.this, SignUpActivity.class);
		setSignUpIntent(signUpintent);

        setProgressDialog(progressDialog);

	}

    private void initProgressbar() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

    }

    @Override
	public void loginSuccess(BuiltUser user) {
		/// After login successful BuiltUser object provided in success callback.

		Intent intent = new Intent(LoginActivity.this, LocationListActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void loginError(BuiltError builtErrorObject) {
		
		/// there is an error while login.
		/// builtErrorObject contains more details of error.
		
		Toast.makeText(LoginActivity.this, "Error : "+builtErrorObject.getErrorMessage(), Toast.LENGTH_SHORT).show();
	}

}
