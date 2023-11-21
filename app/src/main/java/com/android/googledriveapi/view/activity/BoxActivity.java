package com.android.googledriveapi.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.android.googledriveapi.R;
import com.box.sdk.BoxAPIConnection;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BoxActivity extends AppCompatActivity {

    private String state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box);

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(view -> launchBoxTab());
    }



    private void launchBoxTab() {
        List<String> scopes = Collections.singletonList("root_readonly");
//        android:host="boxinandroid"
//        android:scheme="oauth"
        URI redirectUri = URI.create("oauth://boxinandroid");
        state = UUID.randomUUID().toString();
        URL authURL = BoxAPIConnection.getAuthorizationURL(getString(R.string.client_id),
                redirectUri, state, scopes);
        new CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(authURL.toString()));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("State", state);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        state = savedInstanceState.getString("State");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            Uri callback = intent.getData();
            String callbackState = callback.getQueryParameter("state");

            if (!Objects.equals(callbackState, state)) {
                Toast.makeText(BoxActivity.this, "State validation failed!", Toast.LENGTH_LONG).show();
                return;
            }

            String code = callback.getQueryParameter("code");
            Intent itemsIntent = new Intent(BoxActivity.this, BoxItemsActivity.class);
            itemsIntent.putExtra("code", code);
            BoxActivity.this.startActivity(itemsIntent);
        }
    }
}