package net.afterday.compas.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import net.afterday.compas.R;
import net.afterday.compas.settings.Constants;
import net.afterday.compas.settings.Settings;

/**
 * Created by spaka on 7/21/2018.
 */

public class SettingsFragment extends DialogFragment {

    final String TAG = "SettingsFragment";
    private Settings settings;
    private Activity activity;

    private ConstraintLayout gpsCL;
    private ConstraintLayout gpsBgCL;
    private ConstraintLayout devModeCL;
    private ConstraintLayout notificationsCL;
    private ConstraintLayout wifiThrottlingCL;
    private ConstraintLayout nearbyCL;
    private boolean isWifiThrottlingDisabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogStyle);
        activity = getActivity();
        settings = Settings.instance(getActivity().getApplication());
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P || Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            WifiManager mWifi = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            isWifiThrottlingDisabled = true;
            for (int i = 0; i < 5; i++) {
                if (!mWifi.startScan()) {
                    isWifiThrottlingDisabled = false;
                    break;
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //Observable.timer(300, TimeUnit.SECONDS).take(1).subscribe((t) -> closePopup(v));
        View v = inflater.inflate(R.layout.settings_fragment, container, false);
        ((SwitchCompat) v.findViewById(R.id.vibroSwitch)).setChecked(settings.getBoolSetting(Constants.VIBRATION));
        ((SwitchCompat) v.findViewById(R.id.compassSwitch)).setChecked(settings.getBoolSetting(Constants.COMPASS));
        ((SwitchCompat) v.findViewById(R.id.vibroSwitch)).setOnCheckedChangeListener((btn, on) -> {
            settings.setBoolSetting(Constants.VIBRATION, on);
        });
        ((SwitchCompat) v.findViewById(R.id.compassSwitch)).setOnCheckedChangeListener((btn, on) -> {
            settings.setBoolSetting(Constants.COMPASS, on);
        });
        v.findViewById(R.id.close).setOnClickListener(v1 -> {
            try {
                dismiss();
            } catch (Exception e) {
            }

        });
        RadioButton p = (RadioButton) v.findViewById(R.id.orientationPort);//.setOnClickListener((e) -> orientationPort());
        RadioButton h = (RadioButton) v.findViewById(R.id.orientationLand);//.setOnClickListener((e) -> orientationLand());
        gpsCL = v.findViewById(R.id.gpsPermission);
        gpsCL.setOnClickListener(v1 -> {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.getPackageName(), null)));
            }
            else {
                ((CheckBox) gpsCL.findViewById(R.id.checkBoxGps)).setChecked(true);
            }
        });
        gpsBgCL = v.findViewById(R.id.gpsBackgroundPermission);
        gpsBgCL.setOnClickListener(v1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.getPackageName(), null)));
                }
                else {
                    ((CheckBox) gpsBgCL.findViewById(R.id.checkBoxGpsBg)).setChecked(true);
                }
            }
            else {
                ((CheckBox) gpsBgCL.findViewById(R.id.checkBoxGpsBg)).setChecked(true);
            }
        });
        devModeCL = v.findViewById(R.id.developmentMode);
        devModeCL.setOnClickListener(v1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (android.provider.Settings.Global.getInt(activity.getContentResolver(), android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1) == 0) {
                        startActivity(new Intent(android.provider.Settings.ACTION_DEVICE_INFO_SETTINGS));
                    } else {
                        ((CheckBox) devModeCL.findViewById(R.id.checkBoxDev)).setChecked(true);
                    }
                }
                else {
                    Toast.makeText(activity, R.string.development_mode_noneed, Toast.LENGTH_LONG).show();
                }
            } else {
                ((CheckBox) devModeCL.findViewById(R.id.checkBoxDev)).setChecked(true);
                Toast.makeText(activity, R.string.development_mode_noneed, Toast.LENGTH_LONG).show();
            }
        });
        notificationsCL = v.findViewById(R.id.notificationPermission);
        notificationsCL.setOnClickListener(v1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.getPackageName(), null)));
                } else {
                    ((CheckBox) notificationsCL.findViewById(R.id.checkBoxNot)).setChecked(true);
                }
            } else {
                ((CheckBox) notificationsCL.findViewById(R.id.checkBoxNot)).setChecked(true);
            }
        });
        nearbyCL = v.findViewById(R.id.nearbyPermission);
        nearbyCL.setOnClickListener(v1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.getPackageName(), null)));
                } else {
                    ((CheckBox) nearbyCL.findViewById(R.id.checkBoxNearby)).setChecked(true);
                }
            } else {
                ((CheckBox) nearbyCL.findViewById(R.id.checkBoxNearby)).setChecked(true);
            }
        });
        wifiThrottlingCL = v.findViewById(R.id.wifiThrottling);
        wifiThrottlingCL.setOnClickListener(v1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (((WifiManager )activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isScanThrottleEnabled()) {
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                    }
                    else {
                        ((CheckBox) wifiThrottlingCL.findViewById(R.id.checkBoxWifiThr)).setChecked(true);
                    }
                } else {
                    ((CheckBox) wifiThrottlingCL.findViewById(R.id.checkBoxWifiThr)).setChecked(isWifiThrottlingDisabled);
                    if (!isWifiThrottlingDisabled) {
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                        } else {
                            Toast.makeText(activity, R.string.wifi_throttling_nodisable, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            } else {
                ((CheckBox) wifiThrottlingCL.findViewById(R.id.checkBoxWifiThr)).setChecked(true);
            }
        });
        int o = settings.getIntSetting(Constants.ORIENTATION);
        if (o == Constants.ORIENTATION_PORTRAIT) {
            p.setChecked(true);
        } else if (o == Constants.ORIENTATION_LANDSCAPE) {
            h.setChecked(true);
        }
        p.setOnClickListener((e) -> orientationPort());
        h.setOnClickListener((e) -> orientationLand());
        return v;
    }

    public void onResume()
    {
        super.onResume();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        getDialog().getWindow().setLayout(width, height);
        ((CheckBox) gpsCL.findViewById(R.id.checkBoxGps)).setChecked(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                ((CheckBox) gpsBgCL.findViewById(R.id.checkBoxGpsBg)).setChecked(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED);
            }
        }
        else {
            ((CheckBox) gpsBgCL.findViewById(R.id.checkBoxGpsBg)).setChecked(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ((CheckBox) devModeCL.findViewById(R.id.checkBoxDev)).setChecked(android.provider.Settings.Secure.getInt(activity.getContentResolver(), android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1) == 1);
        } else {
            ((CheckBox) devModeCL.findViewById(R.id.checkBoxDev)).setChecked(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ((CheckBox) notificationsCL.findViewById(R.id.checkBoxNot)).setChecked(ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED);
        } else {
            ((CheckBox) notificationsCL.findViewById(R.id.checkBoxNot)).setChecked(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ((CheckBox) nearbyCL.findViewById(R.id.checkBoxNearby)).setChecked(ContextCompat.checkSelfPermission(activity, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED);
        } else {
            ((CheckBox) nearbyCL.findViewById(R.id.checkBoxNearby)).setChecked(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ((CheckBox) wifiThrottlingCL.findViewById(R.id.checkBoxWifiThr)).setChecked(!((WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isScanThrottleEnabled());
            } else {
                ((CheckBox) wifiThrottlingCL.findViewById(R.id.checkBoxWifiThr)).setChecked(isWifiThrottlingDisabled);
            }
        } else {
            ((CheckBox) wifiThrottlingCL.findViewById(R.id.checkBoxWifiThr)).setChecked(true);
        }

    }

    public void orientationPort() {
        settings.setIntSetting(Constants.ORIENTATION, Constants.ORIENTATION_PORTRAIT);
    }

    public void orientationLand() {
        settings.setIntSetting(Constants.ORIENTATION, Constants.ORIENTATION_LANDSCAPE);
    }
}
