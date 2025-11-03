package com.sirajul.gitpuaher.utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.sirajul.gitpuaher.models.Repository;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GitHubHelper {
    private static final String TAG = "GitHubHelper";
    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    
    private Context context;
    private String username;
    private String token;
    private OkHttpClient client;

    public GitHubHelper(Context context, String username, String token) {
        this.context = context;
        this.username = username;
        this.token = token;
        
        // Configure OkHttpClient with timeout
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public boolean validateCredentials() {
        try {
            String credential = Credentials.basic(username, token);
            
            Request request = new Request.Builder()
                    .url(GITHUB_API_BASE + "/user")
                    .header("Authorization", credential)
                    .header("User-Agent", "GitPush-Android-App")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();

            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();
            response.close();
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating credentials: " + e.getMessage());
            return false;
        }
    }

    public boolean createRepository(String repoName, String description) {
        try {
            JSONObject repoJson = new JSONObject();
            repoJson.put("name", repoName);
            repoJson.put("description", description != null ? description : "");
            repoJson.put("auto_init", false);
            repoJson.put("private", false);

            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, repoJson.toString());
            String credential = Credentials.basic(username, token);
            
            Request request = new Request.Builder()
                    .url(GITHUB_API_BASE + "/user/repos")
                    .header("Authorization", credential)
                    .header("User-Agent", "GitPush-Android-App")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();
            response.close();
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating repository: " + e.getMessage());
            return false;
        }
    }

    public boolean uploadFileToRepo(String repoName, String filePath, String content) {
        try {
            if (content == null) {
                Log.e(TAG, "Content is null for file: " + filePath);
                return false;
            }

            JSONObject fileJson = new JSONObject();
            fileJson.put("message", "Add file: " + new File(filePath).getName());
            fileJson.put("content", content);

            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, fileJson.toString());
            String credential = Credentials.basic(username, token);
            String url = GITHUB_API_BASE + "/repos/" + username + "/" + repoName + "/contents/" + getRelativePath(filePath);
            
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", credential)
                    .header("User-Agent", "GitPush-Android-App")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("Content-Type", "application/json")
                    .put(body)
                    .build();

            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();
            
            if (!success) {
                Log.e(TAG, "Upload failed for: " + filePath + ", Response: " + response.code());
            }
            
            response.close();
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Error uploading file: " + e.getMessage());
            return false;
        }
    }

    private String getRelativePath(String filePath) {
        // Implement logic to get relative path from base directory
        return new File(filePath).getName();
    }

    public String encodeFileToBase64(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            int bytesRead = fileInputStream.read(bytes);
            
            if (bytesRead != file.length()) {
                Log.e(TAG, "Failed to read entire file: " + file.getName());
                return null;
            }
            
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error encoding file: " + e.getMessage());
            return null;
        }
    }

    public Repository getRepository(String repoName) {
        return new Repository(repoName, username, "");
    }

    public boolean repositoryExists(String repoName) {
        try {
            String credential = Credentials.basic(username, token);
            String url = GITHUB_API_BASE + "/repos/" + username + "/" + repoName;
            
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", credential)
                    .header("User-Agent", "GitPush-Android-App")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();

            Response response = client.newCall(request).execute();
            boolean exists = response.isSuccessful();
            response.close();
            return exists;
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking repository: " + e.getMessage());
            return false;
        }
    }
}