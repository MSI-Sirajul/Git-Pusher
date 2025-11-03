package com.sirajul.gitpuaher.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sirajul.gitpuaher.R;
import com.sirajul.gitpuaher.adapters.FileListAdapter;
import com.sirajul.gitpuaher.models.FileItem;
import com.sirajul.gitpuaher.utils.FileUtils;
import com.sirajul.gitpuaher.utils.GitHubHelper;
import com.sirajul.gitpuaher.utils.PermissionManager;
import com.sirajul.gitpuaher.utils.SharedPrefManager;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_DIRECTORY_REQUEST = 1001;

    private EditText etProjectName, etDescription;
    private Button btnSelectDirectory, btnGitPush, btnLogout;
    private TextView tvSelectedPath, tvFilesCount, tvProgressText, tvProgressPercent;
    private ProgressBar progressBarUpload;
    private CardView cardProgress, cardFilesContainer;
    private RecyclerView recyclerFiles;
    private TextView tvEmptyFiles;
    private ImageView infoIcon;

    private SharedPrefManager sharedPrefManager;
    private GitHubHelper gitHubHelper;
    private FileListAdapter fileListAdapter;
    private List<FileItem> fileList;
    
    private String selectedDirectoryPath = "";
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check login
        sharedPrefManager = new SharedPrefManager(this);
        if (!sharedPrefManager.isLoggedIn()) {
            startLoginActivity();
            return;
        }

        // Initialize GitHub helper
        gitHubHelper = new GitHubHelper(this, 
            sharedPrefManager.getUsername(), 
            sharedPrefManager.getToken());

        // Check permissions
        if (!PermissionManager.checkStoragePermissions(this)) {
            PermissionManager.showPermissionDialog(this);
        }

        executorService = Executors.newSingleThreadExecutor();
        initViews();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        etProjectName = findViewById(R.id.et_project_name);
        etDescription = findViewById(R.id.et_description);
        btnSelectDirectory = findViewById(R.id.btn_select_directory);
        btnGitPush = findViewById(R.id.btn_git_push);
        btnLogout = findViewById(R.id.btn_logout);
        tvSelectedPath = findViewById(R.id.tv_selected_path);
        tvFilesCount = findViewById(R.id.tv_files_count);
        progressBarUpload = findViewById(R.id.progress_bar_upload);
        tvProgressText = findViewById(R.id.tv_progress_text);
        tvProgressPercent = findViewById(R.id.tv_progress_percent);
        cardProgress = findViewById(R.id.card_progress);
        cardFilesContainer = findViewById(R.id.card_files_container);
        recyclerFiles = findViewById(R.id.recycler_files);
        tvEmptyFiles = findViewById(R.id.tv_empty_files);
        infoIcon = findViewById(R.id.info_icon_main);
    }

    private void setupRecyclerView() {
        fileListAdapter = new FileListAdapter(this);
        recyclerFiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerFiles.setAdapter(fileListAdapter);
    }

    private void setupClickListeners() {
        btnSelectDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDirectory();
            }
        });

        btnGitPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptGitPush();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        infoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoActivity();
            }
        });
    }

    private void selectDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, PICK_DIRECTORY_REQUEST);
    }

    private void attemptGitPush() {
        String projectName = etProjectName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (projectName.isEmpty()) {
            etProjectName.setError("Please enter project name");
            etProjectName.requestFocus();
            return;
        }

        if (selectedDirectoryPath.isEmpty()) {
            Toast.makeText(this, "Please select a project directory", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!FileUtils.isValidDirectory(selectedDirectoryPath)) {
            Toast.makeText(this, "Invalid directory selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start upload process
        startGitPushProcess(projectName, description);
    }

    private void startGitPushProcess(String projectName, String description) {
        showProgress(true);
        
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Step 1: Create repository
                    updateProgress("Creating repository...", 10);
                    boolean repoCreated = gitHubHelper.createRepository(projectName, description);
                    
                    if (!repoCreated) {
                        showError("Failed to create repository");
                        return;
                    }

                    // Step 2: Upload files
                    updateProgress("Uploading files...", 30);
                    boolean uploadSuccess = uploadFilesRecursive(new File(selectedDirectoryPath), projectName);
                    
                    if (uploadSuccess) {
                        showSuccess("Project pushed successfully!");
                    } else {
                        showError("Some files failed to upload");
                    }

                } catch (Exception e) {
                    showError("Error: " + e.getMessage());
                }
            }
        });
    }

    private boolean uploadFilesRecursive(File directory, String repoName) {
        if (directory == null || !directory.exists()) return false;

        File[] files = directory.listFiles();
        if (files == null) return true;

        int totalFiles = FileUtils.countFilesRecursive(directory);
        int uploadedFiles = 0;

        for (File file : files) {
            if (file.isDirectory()) {
                if (!uploadFilesRecursive(file, repoName)) {
                    return false;
                }
            } else {
                // Upload individual file
                String fileContent = gitHubHelper.encodeFileToBase64(file);
                if (fileContent != null) {
                    boolean fileUploaded = gitHubHelper.uploadFileToRepo(repoName, file.getAbsolutePath(), fileContent);
                    if (!fileUploaded) {
                        return false;
                    }
                }

                uploadedFiles++;
                int progress = 30 + (int) ((uploadedFiles / (float) totalFiles) * 70);
                updateProgress("Uploading: " + file.getName(), progress);
            }
        }

        return true;
    }

    private void updateProgress(String message, int progress) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                tvProgressText.setText(message);
                progressBarUpload.setProgress(progress);
                tvProgressPercent.setText(progress + "%");
            }
        });
    }

    private void showProgress(boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cardProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                btnGitPush.setEnabled(!show);
            }
        });
    }

    private void showSuccess(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logout() {
        sharedPrefManager.logout();
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showInfoActivity() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_DIRECTORY_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri treeUri = data.getData();
                selectedDirectoryPath = FileUtils.getPathFromUri(this, treeUri);
                
                if (selectedDirectoryPath != null) {
                    updateSelectedDirectory(selectedDirectoryPath);
                } else {
                    Toast.makeText(this, "Failed to get directory path", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateSelectedDirectory(String directoryPath) {
        tvSelectedPath.setText(directoryPath);
        
        // Get files from directory
        fileList = FileUtils.getFilesFromDirectory(directoryPath);
        
        // Update files count
        int totalFiles = FileUtils.countFilesRecursive(new File(directoryPath));
        tvFilesCount.setText("Files Selected: " + totalFiles);
        
        // Update recycler view
        if (fileList.isEmpty()) {
            tvEmptyFiles.setVisibility(View.VISIBLE);
            recyclerFiles.setVisibility(View.GONE);
        } else {
            tvEmptyFiles.setVisibility(View.GONE);
            recyclerFiles.setVisibility(View.VISIBLE);
            fileListAdapter.setFileList(fileList);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (PermissionManager.handlePermissionResult(requestCode, grantResults)) {
            Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
        }
    }
}