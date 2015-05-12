package com.buitio.builtgeolocation;

import android.os.Bundle;
import android.widget.Toast;

import com.raweng.built.BuiltError;
import com.raweng.built.BuiltUser;
import com.raweng.built.userInterface.BuiltUISignUpController;

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
public class SignUpActivity extends BuiltUISignUpController {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		/// BuiltUISignUpController can be used by extending user class.
        setApplicationKey("blt9f2f3c1d77c907e0");
    }

	@Override
	public void signUpSuccess(BuiltUser user) {
		
		/// After sign up successful BuiltUser object provided in success callback.
		/// then activate account from built.io using confirmation mail on your email id.
		
		Toast.makeText(SignUpActivity.this, "Activate account from web and go for login...", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void signUpError(BuiltError builtErrorObject) {

		/// there is an error while signUp.
		/// builtErrorObject contains more details of error.
		
		Toast.makeText(SignUpActivity.this, "Error : "+builtErrorObject.getErrorMessage(), Toast.LENGTH_SHORT).show();
	}

}
