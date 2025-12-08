package com.gitpusher.pro;

import android.net.Uri;
import android.os.Environment;

public class PathUtil {
    // এটি একটি বেসিক পাথ কনভার্টার। Android 11+ এর Scoped Storage এর জন্য 
    // এটি কিছুটা ট্রিকি। আমরা ধরে নিচ্ছি ইউজার Primary Storage ব্যবহার করছে।
    
    public static String getPathFromUri(Uri uri) {
        String path = uri.getPath();
        // টার্মাক্স বা ফাইল ম্যানেজার থেকে সিলেক্ট করলে পাথ সাধারণত এমন হয়:
        // /tree/primary:MyProject -> /storage/emulated/0/MyProject
        
        if (path != null && path.contains("primary:")) {
            String id = path.split("primary:")[1];
            return Environment.getExternalStorageDirectory() + "/" + id;
        }
        // যদি সরাসরি পাথ না পাওয়া যায়
        return null;
    }
}