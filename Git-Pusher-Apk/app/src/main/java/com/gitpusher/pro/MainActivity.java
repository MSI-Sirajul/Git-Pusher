package com.gitpusher.pro;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements GitOpsManager.ProcessListener {

    // UI ভিউস
    private TextView tvNetworkStatus, tvCurrentPath, tvFolderCount, tvFileCount, tvConsoleLog;
    private Button btnSelectFolder, btnPushNow;
    private ScrollView scrollView;

    // ভেরিয়েবলস
    private GitOpsManager gitManager;
    private String selectedFolderPath = null;
    private Handler handler = new Handler();
    private boolean isOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ভিউ ইনিশিয়ালাইজেশন
        initViews();

        // পারমিশন চেক (Android 11+ এর জন্য বিশেষ ব্যবস্থা)
        checkPermissions();

        // গিট ম্যানেজার সেটআপ
        gitManager = new GitOpsManager(this, this);

        // প্রতি ৩ সেকেন্ড পর পর ইন্টারনেট চেক করার লুপ
        startNetworkCheckLoop();

        // ফোল্ডার সিলেক্ট বাটন
        btnSelectFolder.setOnClickListener(v -> openFolderPicker());

        // পুশ বাটন
        btnPushNow.setOnClickListener(v -> {
            if (!isOnline) {
                Toast.makeText(this, "Device is Offline! Please connect to internet.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedFolderPath == null) {
                Toast.makeText(this, "Please select a project folder first!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // UI ক্লিয়ার করা (স্ক্রিপ্টের মতো)
            tvConsoleLog.setText("> Starting Process...\n");
            btnPushNow.setEnabled(false); // বাটন ডিজেবল যাতে বারবার চাপ না লাগে
            
            // পুশ প্রসেস শুরু
            gitManager.startPushProcess(selectedFolderPath);
        });
    }

    private void initViews() {
        tvNetworkStatus = findViewById(R.id.tvNetworkStatus);
        tvCurrentPath = findViewById(R.id.tvCurrentPath);
        tvFolderCount = findViewById(R.id.tvFolderCount);
        tvFileCount = findViewById(R.id.tvFileCount);
        tvConsoleLog = findViewById(R.id.tvConsoleLog);
        btnSelectFolder = findViewById(R.id.btnSelectFolder);
        btnPushNow = findViewById(R.id.btnPushNow);
        scrollView = (ScrollView) tvConsoleLog.getParent();
    }

    // === ফোল্ডার পিকার লজিক ===
    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderPickerLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> folderPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    // পাথ কনভার্ট করা
                    String path = PathUtil.getPathFromUri(uri);
                    
                    if (path != null) {
                        selectedFolderPath = path;
                        tvCurrentPath.setText(path);
                        
                        // ফাইল কাউন্ট আপডেট করা
                        String stats = gitManager.getFileStats(path);
                        String[] split = stats.split(":");
                        tvFolderCount.setText(split[0]);
                        tvFileCount.setText(split[1]);
                        
                        appendLog("> Selected: " + path);
                    } else {
                        Toast.makeText(this, "Could not resolve path. Try Internal Storage.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    // === ইন্টারনেট চেক লুপ ===
    private void startNetworkCheckLoop() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                checkConnection();
                handler.postDelayed(this, 3000); // ৩ সেকেন্ড পর পর
            }
        };
        handler.post(runnable);
    }

    private void checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        isOnline = netInfo != null && netInfo.isConnected();

        if (isOnline) {
            tvNetworkStatus.setText("Online");
            tvNetworkStatus.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess));
        } else {
            tvNetworkStatus.setText("Offline");
            tvNetworkStatus.setTextColor(ContextCompat.getColor(this, R.color.colorError));
        }
    }

    // === GitOpsManager থেকে আসা কলব্যাক (UI আপডেট) ===
    @Override
    public void onLog(String message) {
        runOnUiThread(() -> appendLog(message));
    }

    @Override
    public void onSuccess() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Project Pushed Successfully!", Toast.LENGTH_LONG).show();
            btnPushNow.setEnabled(true);
            btnPushNow.setText("Push Completed");
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            appendLog("[ERROR] " + error);
            Toast.makeText(this, "Failed: " + error, Toast.LENGTH_LONG).show();
            btnPushNow.setEnabled(true);
        });
    }

    private void appendLog(String msg) {
        tvConsoleLog.append("\n" + msg);
        // অটোমেটিক স্ক্রল ডাউন
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    // === পারমিশন লজিক ===
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Please grant 'All Files Access' to manage Git repos", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }
}