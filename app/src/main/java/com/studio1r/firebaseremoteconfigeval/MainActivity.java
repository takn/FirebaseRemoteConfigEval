package com.studio1r.firebaseremoteconfigeval;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {

    private static final java.lang.String BRAND_NAME = "brandName";
    private static final java.lang.String PROGRAM_SERVICE_ENDPOINT = "programService";
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private TextView appTitle;
    private TextView appEndpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        appTitle = (TextView) findViewById(R.id.app_title);
        appEndpoint = (TextView) findViewById(R.id.app_endpoint);


        findViewById(R.id.btn_fetch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchRemoteConfigValues();
            }
        });
        // [END get_remote_config_instance]

        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        // [START enable_dev_mode]

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        displayConfigValues();

    }

    public void fetchRemoteConfigValues() {
        long cacheExpiration = 3600; // 1 hour in seconds.
        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from
        // the server.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch().addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Fetch Failed",
                        Toast.LENGTH_SHORT).show();
            }
        });

        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Fetch Succeeded",
                                    Toast.LENGTH_SHORT).show();

                            // Once the config is successfully fetched it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            Toast.makeText(MainActivity.this, "Fetch Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        displayConfigValues();
                    }
                });
    }

    private void displayConfigValues() {
        String brandName = mFirebaseRemoteConfig.getString(BRAND_NAME);
        String endpoint = mFirebaseRemoteConfig.getString(PROGRAM_SERVICE_ENDPOINT);
        appTitle.setText(brandName);
        appEndpoint.setText(endpoint);
    }
}
