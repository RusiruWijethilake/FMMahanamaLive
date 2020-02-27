package lk.developments.microlion.fmmahanamalive;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.pm.PackageInfoCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class fmmToolKit {

    private static String SharedKey = "lk.developments.microlion.fmmahanamalive.SHARED_KEY";
    private static String DayNightModeType = "lk.developments.microlion.fmmahanamalive.SHARED_KEY.DayNightModeType";
    private static String FirstTime = "lk.developments.microlion.fmmahanamalive.SHARED_KEY.FirstTime";
    private static String FirstTimeScore = "lk.developments.microlion.fmmahanamalive.SHARED_KEY.FirstTimeScore";

    private static SharedPreferences sharedPref;

    public static void setDefaultTheme(Context context) {
        sharedPref = context.getSharedPreferences(SharedKey, Context.MODE_PRIVATE);
        int modeType = sharedPref.getInt(DayNightModeType, AppCompatDelegate.MODE_NIGHT_NO);

        if (modeType == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (modeType == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
        } else if (modeType == AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (modeType == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void darkModeToggle(Context context) {
        sharedPref = context.getSharedPreferences(SharedKey, Context.MODE_PRIVATE);

        int appCurrent = AppCompatDelegate.getDefaultNightMode();
        int current = 0;

        if (appCurrent == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            current = 0;
        } else if (appCurrent == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY) {
            current = 0;
        } else if (appCurrent == AppCompatDelegate.MODE_NIGHT_NO) {
            current = 1;
        } else if (appCurrent == AppCompatDelegate.MODE_NIGHT_YES) {
            current = 2;
        }
        final int[] selected = {AppCompatDelegate.MODE_NIGHT_UNSPECIFIED};

        String[] strThemes = new String[3];
        strThemes[0] = "Set auto";
        strThemes[1] = "Light mode";
        strThemes[2] = "Dark mode";

        new MaterialAlertDialogBuilder(context)
                .setTitle("Select a theme")
                .setSingleChoiceItems(strThemes, current, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                                selected[0] = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
                            } else {
                                selected[0] = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                            }
                        } else if (which == 1) {
                            selected[0] = AppCompatDelegate.MODE_NIGHT_NO;
                        } else if (which == 2) {
                            selected[0] = AppCompatDelegate.MODE_NIGHT_YES;
                        }
                    }
                })
                .setPositiveButton("Set theme", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppCompatDelegate.setDefaultNightMode(selected[0]);
                        sharedPref.edit().putInt(DayNightModeType, selected[0]).apply();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void openFacebookPage(Context context, String pageid) {
        String FACEBOOK_URL = "https://www.facebook.com/" + pageid;
        String FACEBOOK_PAGE_ID = pageid;
        String INTENT_URL = "";
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            int versionCode = (int) PackageInfoCompat.getLongVersionCode(pInfo);
            if (versionCode >= 3002850) {
                INTENT_URL = "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else {
                INTENT_URL = "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            INTENT_URL = FACEBOOK_URL;
        }
        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
        facebookIntent.setData(Uri.parse(INTENT_URL));
        context.startActivity(facebookIntent);
    }

    public static void openLink(Context context, String link) {
        Uri uri = Uri.parse(link);
        Intent linkIntent = new Intent(Intent.ACTION_VIEW);
        linkIntent.setData(uri);
        context.startActivity(linkIntent);
    }

    public static boolean isThisFirstTime(Context context) {
        sharedPref = context.getSharedPreferences(SharedKey, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(FirstTime, true);
    }

    public static void setNotFirstTime(Context context) {
        sharedPref = context.getSharedPreferences(SharedKey, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(FirstTime, false).apply();
    }

    public static boolean isThisFirstTimeScore(Context context) {
        sharedPref = context.getSharedPreferences(SharedKey, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(FirstTimeScore, true);
    }

    public static void setNotFirstTimeScore(Context context) {
        sharedPref = context.getSharedPreferences(SharedKey, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(FirstTimeScore, false).apply();
    }
}
