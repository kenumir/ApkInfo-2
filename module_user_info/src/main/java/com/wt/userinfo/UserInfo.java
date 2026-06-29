package com.wt.userinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.UUID;

public class UserInfo {

    public static final String KEY_UUID = "uuid";
    public static final String KEY_VERSION_NAME = "version_name";
    public static final String KEY_INSTALL_REFERRER = "install_referrer";

    private static final String PREF_KEY_LAST_APP_VERSION_NAME = "app_last_version_name";

    private static volatile UserInfo sInstance;
    public synchronized static UserInfo setup(@NonNull Context ctx, @NonNull String appVersion, @Nullable OnInitError initError) {
        if (sInstance == null) {
            sInstance = new UserInfo(ctx, appVersion, initError);
        }
        return sInstance;
    }

    //private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final SharedPreferences pref;
    private String uuid = null;
    private final String appVersionName;
    private final Context appContext;

    private UserInfo(Context ctx, @NonNull final String avn, @Nullable OnInitError initError) {
        appContext = ctx.getApplicationContext();
        appVersionName = avn;
        pref = appContext.getSharedPreferences(ctx.getPackageName() + ".userinfo", Context.MODE_PRIVATE);

        try {
            WorkManager wm = WorkManager.getInstance(appContext);

            if (!pref.getBoolean("user_info_work_enqueued", false)) {
                pref.edit().putBoolean("user_info_work_enqueued", true).apply();
                Data inputData = new Data.Builder()
                    .putString(KEY_UUID, getId())
                    .putString(KEY_VERSION_NAME, appVersionName)
                    .build();
                wm.enqueue(
                    new OneTimeWorkRequest.Builder(SaveUserInfoWork.class)
                        .setInputData(inputData)
                        .setConstraints(
                            new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                );
            }

            String lastVersionName = pref.getString(PREF_KEY_LAST_APP_VERSION_NAME, null);
            if (TextUtils.isEmpty(lastVersionName) || !appVersionName.equals(lastVersionName)) {
                pref.edit().putString(PREF_KEY_LAST_APP_VERSION_NAME, appVersionName).apply();
                Data inputData = new Data.Builder()
                    .putString(KEY_UUID, getId())
                    .putString(KEY_VERSION_NAME, appVersionName)
                    .build();
                wm.enqueue(
                    new OneTimeWorkRequest.Builder(SaveVersionWork.class)
                        .setInputData(inputData)
                        .setConstraints(
                            new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                );
            }
        } catch (Exception e) {
            if (initError != null) {
                initError.onInitError(e);
            }
        }
    }

    @Nullable
    public String getInstallReferrer() {
        return pref.getString("install_referrer", null);
    }

    public void saveInstallReferrer(String value) {
        pref.edit()
                .putString("install_referrer", value)
                .apply();
        if (!TextUtils.isEmpty(value)) {
            Data inputData = new Data.Builder()
                    .putString(KEY_UUID, getId())
                    .putString(KEY_VERSION_NAME, appVersionName)
                    .putString(KEY_INSTALL_REFERRER, value)
                    .build();
            WorkManager.getInstance(appContext).enqueue(
                new OneTimeWorkRequest.Builder(SaveInstallReferrerWork.class)
                    .setInputData(inputData)
                    .setConstraints(
                        new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
            );
        }
    }

    @NonNull
    public String getId() {
        if (uuid == null) {
            uuid = pref.getString("uuid", null);
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
                pref.edit().putString("uuid", uuid).apply();
            }
        }
        return uuid;
    }

}
