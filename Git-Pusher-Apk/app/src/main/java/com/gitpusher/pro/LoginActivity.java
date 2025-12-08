package com.gitpusher.pro;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etToken;
    private Button btnLogin;
    private GitOpsManager gitManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // GitOpsManager ইনিশিয়ালাইজ করা (Listener null কারণ এখানে লগ দেখার দরকার নেই)
        gitManager = new GitOpsManager(this, null);

        // যদি ইউজার আগে থেকেই লগইন করা থাকে, সরাসরি মেইন পেজে যাও
        if (gitManager.isLoggedIn()) {
            gotoMain();
            return;
        }

        setContentView(R.layout.activity_login);

        // ভিউগুলো খুঁজে বের করা
        etUsername = findViewById(R.id.etUsername);
        etToken = findViewById(R.id.etToken);
        btnLogin = findViewById(R.id.btnLogin);

        // লগইন বাটনের কাজ
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUsername.getText().toString().trim();
                String token = etToken.getText().toString().trim();

                if (validateInput(user, token)) {
                    // ডাটা সেভ করা
                    gitManager.saveToken(user, token);
                    Toast.makeText(LoginActivity.this, "Login Saved!", Toast.LENGTH_SHORT).show();
                    gotoMain();
                }
            }
        });
    }

    // ইনপুট ভ্যালিডেশন
    private boolean validateInput(String user, String token) {
        if (TextUtils.isEmpty(user)) {
            etUsername.setError("Username required");
            return false;
        }
        if (TextUtils.isEmpty(token)) {
            etToken.setError("Token required");
            return false;
        }
        if (!token.startsWith("ghp_") && !token.startsWith("github_pat_")) {
            Toast.makeText(this, "Warning: Token usually starts with 'ghp_'", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    // মেইন অ্যাক্টিভিটিতে যাওয়ার ফাংশন
    private void gotoMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // যাতে ব্যাক বাটনে চাপলে আবার লগইন পেজে না আসে
    }
}