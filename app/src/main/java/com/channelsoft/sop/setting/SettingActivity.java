package com.channelsoft.sop.setting;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.channelsoft.sop.R;
import com.channelsoft.sop.databinding.ActivitySettingBinding;
import com.channelsoft.sop.databinding.CustomActionBarBinding;
import com.channelsoft.sop.shareObject.NetworkConnection;
import com.channelsoft.sop.sharePreference.SharedPreferenceManager;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.channelsoft.sop.shareObject.CustomSnackBar.showSnackBar;
import static com.channelsoft.sop.shareObject.VariableUtils.LOGOUT;

public class SettingActivity extends AppCompatActivity {
    ActivitySettingBinding binding;
    private CustomActionBarBinding customActionBarBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        customActionBarBinding = binding.customActionBar;
        View view = binding.getRoot();
        objectSetting();
        setContentView(view);
    }

    private void objectSetting() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Setting");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding.customer.setText(SharedPreferenceManager.getUsername(this));
        binding.email.setText(SharedPreferenceManager.getUserEmail(this));
        displayVersion();
        setStatusBar();
    }

    public void setStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        customActionBarBinding.toolbar.setTitle("Setting");
        setSupportActionBar(customActionBarBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        customActionBarBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /*---------------------------------------------------------------menu-------------------------------------------------------------------------------*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    public void updatePassword(View view) {
        if(!networkChecking()) return;
        DialogFragment dialogFragment = new ResetPasswordDialog();
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    private void displayVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = "Version " + pInfo.versionName;
            binding.version.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void contactUs(View view) {
        if(!networkChecking()) return;
        DialogFragment dialogFragment = new ContactUsDialog();
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    public void logoutConfirmation(View view) {
        if(!networkChecking()) return;
        final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText("Warning");
        dialog.setContentText("Are you sure to log out?");
        dialog.setConfirmText("Confirm");
        dialog.setCancelText("Cancel");
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                logOut();
                dialog.dismissWithAnimation();
            }
        });
        dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                dialog.dismissWithAnimation();
            }
        });
        dialog.show();
    }

    private void logOut() {
        setResult(LOGOUT);
        finish();
    }

    public void openWebView(View view) {
        if(!networkChecking()) return;

        Bundle bundle = new Bundle();
        bundle.putString("url", view.getId() == R.id.company ? "https://channelsoft.com.my" : "https://channelsoft.com.my/privacy-policy/");
        bundle.putString("label", view.getId() == R.id.company ? "Channel Soft PLT Website" : "Privacy Policy");
        DialogFragment dialogFragment = new WebViewDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    private boolean networkChecking() {
        boolean connection = new NetworkConnection(getApplicationContext()).checkNetworkConnection();
        if (!connection) {
            showSnackBar(binding.getRoot(), "No Internet Connection!");
        }
        return connection;
    }
}
