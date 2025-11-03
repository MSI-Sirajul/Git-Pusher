package com.sirajul.gitpuaher.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.sirajul.gitpuaher.models.User;

public class SharedPrefManager {
    private static final String PREF_NAME = "GitPushPrefs";
    private static final String KEY_USERNAME = "github_username";
    private static final String KEY_TOKEN = "github_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_LAST_USED_DIRECTORY = "last_used_directory";
    private static final String KEY_APP_FIRST_LAUNCH = "app_first_launch";
    private static final String KEY_REMEMBER_ME = "remember_me";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;

    // Singleton instance
    private static SharedPrefManager mInstance;

    public SharedPrefManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }

    // Singleton pattern to ensure single instance
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    // User authentication methods
    public void saveUserCredentials(String username, String token) {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void saveUserObject(User user) {
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER_DATA, userJson);
        editor.apply();
    }

    public User getUserObject() {
        String userJson = sharedPreferences.getString(KEY_USER_DATA, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_DATA);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    // App settings methods
    public void setLastUsedDirectory(String directoryPath) {
        editor.putString(KEY_LAST_USED_DIRECTORY, directoryPath);
        editor.apply();
    }

    public String getLastUsedDirectory() {
        return sharedPreferences.getString(KEY_LAST_USED_DIRECTORY, null);
    }

    public void setAppFirstLaunch(boolean isFirstLaunch) {
        editor.putBoolean(KEY_APP_FIRST_LAUNCH, isFirstLaunch);
        editor.apply();
    }

    public boolean isAppFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_APP_FIRST_LAUNCH, true);
    }

    public void setRememberMe(boolean rememberMe) {
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    public boolean isRememberMe() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, true);
    }

    // Clear all data
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    // Check if specific data exists
    public boolean hasUserData() {
        return sharedPreferences.contains(KEY_USERNAME) && 
               sharedPreferences.contains(KEY_TOKEN);
    }

    // Get user credentials as formatted string for GitHub API
    public String getCredentialsForGitHub() {
        String username = getUsername();
        String token = getToken();
        if (username != null && token != null) {
            return username + ":" + token;
        }
        return null;
    }

    // Save repository creation preferences
    public void setDefaultRepoVisibility(boolean isPrivate) {
        editor.putBoolean("default_repo_private", isPrivate);
        editor.apply();
    }

    public boolean getDefaultRepoVisibility() {
        return sharedPreferences.getBoolean("default_repo_private", false);
    }

    public void setDefaultBranch(String branchName) {
        editor.putString("default_branch", branchName);
        editor.apply();
    }

    public String getDefaultBranch() {
        return sharedPreferences.getString("default_branch", "main");
    }
}