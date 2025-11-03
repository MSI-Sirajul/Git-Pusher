package com.sirajul.gitpuaher.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sirajul.gitpuaher.R;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private static final int STORAGE_PERMISSION_CODE = 100;
    
    // Storage permissions for all Android versions
    private static final String[] STORAGE_PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // For Android 13+ (API 33) we'll use the old permissions as fallback
    // since we don't have TIRAMISU constant

    public static boolean checkStoragePermissions(Context context) {
        // For all versions, use the traditional storage permissions
        return hasPermissions(context, STORAGE_PERMISSIONS);
    }

    private static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestStoragePermissions(Activity activity) {
        String[] requiredPermissions = STORAGE_PERMISSIONS;

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity,
                    permissionsToRequest.toArray(new String[0]),
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    public static void showPermissionDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.permission_message)
                .setPositiveButton(R.string.grant_permission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestStoragePermissions(activity);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(activity, "Permission denied! App may not work properly.", 
                                Toast.LENGTH_LONG).show();
                        activity.finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static boolean handlePermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}