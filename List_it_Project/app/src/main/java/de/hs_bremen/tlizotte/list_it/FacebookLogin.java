package de.hs_bremen.tlizotte.list_it;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
/**
 * Created by Leo on 20.06.2017.
 */

public class FacebookLogin extends AppCompatActivity {

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.facebooklogin_layout);


        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker(){
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken currentToken){

            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                nextActivity(newProfile);
            }
        };
        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        LoginButton loginButton = (LoginButton)findViewById(R.id.login_button);
        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Profile profile = Profile.getCurrentProfile();
                nextActivity(profile);
                Toast.makeText(getApplicationContext(), "Loggin in...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Loggin canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "Loggin error " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Error ", error.getMessage());
            }
        };
        loginButton.setReadPermissions("user_friends");
        loginButton.registerCallback(callbackManager, callback);
    }


    @Override
    protected void onResume(){
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        nextActivity(profile);
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    protected void onStop(){
        super.onStop();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void nextActivity(Profile profile){
        if(profile==null)
            Toast.makeText(getApplicationContext(),"Profile == null", Toast.LENGTH_SHORT);

        if(profile != null){
            try{
                Log.e("Current profile is", profile.getName());
                Intent main = new Intent(FacebookLogin.this, MainActivity.class);
                main.putExtra("name", profile.getFirstName());
                main.putExtra("surname", profile.getLastName());
                main.putExtra("image", profile.getProfilePictureUri(200,200));
                startActivity(main);
            }catch (Exception e) {
                Log.e("Intent failed", e.getMessage());
            }
        }
    }
}
