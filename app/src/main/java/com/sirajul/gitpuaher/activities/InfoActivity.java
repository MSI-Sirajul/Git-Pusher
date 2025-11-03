package com.sirajul.gitpuaher.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sirajul.gitpuaher.R;

public class InfoActivity extends AppCompatActivity {

    private TextView tvDeveloperName, tvDeveloperId, tvAppConcept, tvAppVersion;
    private ImageView ivGithub, ivProfile;
    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        initViews();
        setupClickListeners();
        setDeveloperInfo();
    }

    private void initViews() {
        tvDeveloperName = findViewById(R.id.tv_developer_name);
        tvDeveloperId = findViewById(R.id.tv_developer_id);
        tvAppConcept = findViewById(R.id.tv_app_concept);
        tvAppVersion = findViewById(R.id.tv_app_version);
        ivGithub = findViewById(R.id.iv_github);
        ivProfile = findViewById(R.id.iv_profile);
        btnClose = findViewById(R.id.btn_close_info);
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGithubProfile();
            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeveloperProfile();
            }
        });
    }

    private void setDeveloperInfo() {
        // Info is already set from strings.xml in layout
        // We can add dynamic data here if needed
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText("Version " + versionName);
        } catch (Exception e) {
            tvAppVersion.setText("Version 1.0");
        }
    }

    private void openGithubProfile() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/sirajul"));
            startActivity(intent);
        } catch (Exception e) {
            // Handle error
        }
    }

    private void openDeveloperProfile() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://example.com/sirajul")); // Replace with actual profile URL
            startActivity(intent);
        } catch (Exception e) {
            // Handle error
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}