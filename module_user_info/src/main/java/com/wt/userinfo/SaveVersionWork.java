package com.wt.userinfo;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

//import com.google.android.gms.security.ProviderInstaller;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.LinkedHashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SaveVersionWork extends Worker {

    private static final int MAX_RETRY_COUNT = 15;

    public SaveVersionWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        final int retryCount = getRunAttemptCount();
        if (retryCount >= MAX_RETRY_COUNT) {
            if (BuildConfig.DEBUG) {
                Log.e("apkinfo", "SaveInstallReferrerWork: Worker MAX_RETRY_COUNT attempt (" + MAX_RETRY_COUNT + ")");
            }
            return Result.failure();
        }
        //try {
        //    ProviderInstaller.installIfNeeded(getApplicationContext());
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

        String uuid = getInputData().getString(UserInfo.KEY_UUID);
        String appVersionName = getInputData().getString(UserInfo.KEY_VERSION_NAME);
        String installerPackage = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                installerPackage = getApplicationContext().getPackageManager().getInstallSourceInfo("com.wt.apkinfo").getInstallingPackageName();
            } else {
                installerPackage = getApplicationContext().getPackageManager().getInstallerPackageName("com.wt.apkinfo");
            }
        } catch (Exception e) {
            Log.e("apkinfo", "SaveVersionWork: getInstallerPackage", e);
        }

        try {
            OkHttpClient client = new OkHttpClient.Builder().build();
            Gson gson = new GsonBuilder().serializeNulls().create();
            LinkedHashMap<String, String> json = new LinkedHashMap<>();
            json.put("uuid", uuid);
            json.put("app_version_name_update", appVersionName);
            json.put("installer_package", installerPackage);
            json.put("build_board", Build.BOARD);
            json.put("build_brand", Build.BRAND);
            json.put("build_manufacturer", Build.MANUFACTURER);
            json.put("build_display", Build.DISPLAY);
            json.put("build_model", Build.MODEL);
            json.put("build_product", Build.PRODUCT);
            json.put("build_version_sdk_int", ""+ Build.VERSION.SDK_INT);
            json.put("build_version_codename", ""+ Build.VERSION.CODENAME);
            json.put("build_version_release", ""+ Build.VERSION.RELEASE);
            if (Build.VERSION.SDK_INT > 23) {
                json.put("build_version_security_patch", "" + Build.VERSION.SECURITY_PATCH);
            } else {
                json.put("build_version_security_patch", "");
            }

            if (BuildConfig.DEBUG) {
                Log.i("apkinfo", "SaveInstallReferrerWork: POST=" + json + ", retryCount=" + retryCount);
            }

            RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), gson.toJson(json));
            Response response = client.newCall(new Request.Builder()
                .url("https://fpstest.kenumir.pl/api/v2/apkinfo_update")
                .header("User-Agent", "Apk Info v" + appVersionName)
                .header("X-Api-Secret", BuildConfig.API_SECRET)
                .post(body)
                .build()
            ).execute();

            if (response.isSuccessful()) {
                if (BuildConfig.DEBUG) {
                    Log.i("apkinfo", "SaveInstallReferrerWork: success save");
                }
                return Result.success();
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e("apkinfo", "SaveInstallReferrerWork: response error code=" + response.code() + ", body=" + response.body().string());
                }
            }
        } catch (Exception e) {
            Log.e("apkinfo", "SaveVersionWork: request failed", e);
        }
        return Result.retry();
    }

}
