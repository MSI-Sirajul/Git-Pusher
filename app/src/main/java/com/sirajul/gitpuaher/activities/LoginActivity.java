package com.sirajul.gitpuaher.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sirajul.gitpuaher.R;
import com.sirajul.gitpuaher.utils.GitHubHelper;
import com.sirajul.gitpuaher.utils.PermissionManager;
import com.sirajul.gitpuaher.utils.SharedPrefManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etToken;
    private Button btnLogin;
    private ProgressBar progressBar;
    private ImageView infoIcon;
    
    private SharedPrefManager sharedPrefManager;
    private GitHubHelper gitHubHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize utils
        sharedPrefManager = new SharedPrefManager(this);

        // Check if already logged in
        if (sharedPrefManager.isLoggedIn()) {
            startMainActivity();
            return;
        }

        // Check permissions
        if (!PermissionManager.checkStoragePermissions(this)) {
            PermissionManager.showPermissionDialog(this);
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etToken = findViewById(R.id.et_token);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        infoIcon = findViewById(R.id.info_icon_login);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        infoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoActivity();
            }
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String token = etToken.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Please enter GitHub username");
            etUsername.requestFocus();
            return;
        }

        if (token.isEmpty()) {
            etToken.setError("Please enter access token");
            etToken.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Validate credentials with GitHub
        new Thread(new Runnable() {
            @Override
            public void run() {
                gitHubHelper = new GitHubHelper(LoginActivity.this, username, token);
                final boolean isValid = gitHubHelper.validateCredentials();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);

                        if (isValid) {
                            // Save credentials and proceed
                            sharedPrefManager.saveUserCredentials(username, token);
                            Toast.makeText(LoginActivity.this, 
                                "Login successful!", Toast.LENGTH_SHORT).show();
                            startMainActivity();
                        } else {
                            Toast.makeText(LoginActivity.this, 
                                "Invalid credentials! Please check username and token.", 
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showInfoActivity() {
        Intent intent = new Intent(LoginActivity.this, InfoActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (PermissionManager.handlePermissionResult(requestCode, grantResults)) {
            Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission denied! App may not work properly.", 
                Toast.LENGTH_LONG).show();
        }
    }
}