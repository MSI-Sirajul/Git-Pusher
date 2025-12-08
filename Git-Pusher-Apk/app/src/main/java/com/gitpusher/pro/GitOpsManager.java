package com.gitpusher.pro;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GitOpsManager {

    private Context context;
    private ProcessListener listener;
    private SharedPreferences prefs;
    private GitHubApiService apiService;

    // কনস্ট্রাক্টর
    public GitOpsManager(Context context, ProcessListener listener) {
        this.context = context;
        this.listener = listener;
        this.prefs = context.getSharedPreferences("GitPrefs", Context.MODE_PRIVATE);

        // Retrofit সেটআপ
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(GitHubApiService.class);
    }

    // টোকেন সেভ করার ফাংশন
    public void saveToken(String username, String token) {
        prefs.edit()
                .putString("username", username)
                .putString("token", token)
                .putBoolean("isLoggedIn", true)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("isLoggedIn", false);
    }

    // মেইন পুশ প্রসেস (ব্যাকগ্রাউন্ড থ্রেডে চলবে)
    public void startPushProcess(String folderPath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String token = "token " + prefs.getString("token", "");
                String username = prefs.getString("username", "");
                String rawToken = prefs.getString("token", ""); // for JGit
                File localDir = new File(folderPath);
                String repoName = localDir.getName();

                // === STEP 1: GIT INIT ===
                listener.onLog("> Initializing Git repository...");
                Git git;
                try {
                    git = Git.init().setDirectory(localDir).call();
                } catch (Exception e) {
                    // যদি আগে থেকেই গিট থাকে, সেটা ওপেন করবে
                    git = Git.open(localDir);
                }
                
                listener.onLog("> Git Init Success!");
                Thread.sleep(3000); // ৩ সেকেন্ড ডিলে

                // === STEP 2: GIT ADD ===
                listener.onLog("> Adding all files to staging...");
                git.add().addFilepattern(".").call();
                
                listener.onLog("> Files Added Successfully!");
                Thread.sleep(3000); // ৩ সেকেন্ড ডিলে

                // === STEP 3: GIT COMMIT ===
                listener.onLog("> Committing changes...");
                git.commit().setMessage("Auto Push from Android App: " + System.currentTimeMillis()).call();
                
                listener.onLog("> Commit Successful!");
                Thread.sleep(3000); // ৩ সেকেন্ড ডিলে

                // === STEP 4: CHECK/CREATE REMOTE REPO (API) ===
                listener.onLog("> Checking remote repository on GitHub...");
                
                // গিটহাবে রিপো আছে কিনা চেক করা
                Response<ResponseBody> checkRepo = apiService.getRepo(token, username, repoName).execute();
                
                if (checkRepo.isSuccessful()) {
                    listener.onLog("> Repository exists on GitHub. Preparing to update.");
                } else {
                    listener.onLog("> Repository not found. Creating new one...");
                    RepoRequest req = new RepoRequest(repoName, false); // Public Repo
                    Response<ResponseBody> createResp = apiService.createRepo(token, req).execute();
                    
                    if (createResp.isSuccessful()) {
                        listener.onLog("> New Repository Created Successfully!");
                    } else {
                        throw new Exception("Failed to create repo: " + createResp.code());
                    }
                }
                Thread.sleep(3000); // ৩ সেকেন্ড ডিলে

                // === STEP 5: SETUP REMOTE & PUSH ===
                listener.onLog("> configuring remote origin...");
                String remoteUrl = "https://github.com/" + username + "/" + repoName + ".git";
                
                // রিমোট অরিজিন সেট করা (যদি না থাকে)
                try {
                    git.remoteAdd().setName("origin").setUri(new URIish(remoteUrl)).call();
                } catch (Exception e) {
                    // রিমোট অলরেডি থাকলে ইগনোর করবে বা আপডেট করবে
                    // JGit এ রিমোট URL আপডেট করা একটু জটিল, তাই আমরা সিম্পল রাখছি
                }

                listener.onLog("> Pushing to GitHub (Main Branch)...");
                
                // পুশ কমান্ড
                git.push()
                    .setRemote("origin")
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, rawToken))
                    .setPushAll()
                    .call();

                listener.onLog("> PUSH COMPLETED SUCCESSFULLY! 🎉");
                Thread.sleep(1000);
                
                listener.onSuccess();
                git.close();

            } catch (Exception e) {
                e.printStackTrace();
                listener.onError("Error: " + e.getMessage());
            }
        });
    }

    // ফাইল এবং ফোল্ডার গণনার ফাংশন
    public String getFileStats(String path) {
        File dir = new File(path);
        if (!dir.exists()) return "0 Folders / 0 Files";

        int files = 0;
        int folders = 0;
        
        File[] list = dir.listFiles();
        if (list != null) {
            for (File f : list) {
                if (f.isDirectory()) folders++;
                else files++;
            }
        }
        return folders + ":" + files; // Format: "folders:files"
    }

    // ইন্টারফেস UI তে আপডেট পাঠানোর জন্য
    public interface ProcessListener {
        void onLog(String message);
        void onSuccess();
        void onError(String error);
    }
}