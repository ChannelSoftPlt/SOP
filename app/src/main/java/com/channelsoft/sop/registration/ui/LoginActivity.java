package com.channelsoft.sop.registration.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import com.channelsoft.sop.MainActivity;
import com.channelsoft.sop.R;
import com.channelsoft.sop.databinding.ActivityLoginBinding;
import com.channelsoft.sop.object.UserObject;
import com.channelsoft.sop.shareObject.ApiDataObject;
import com.channelsoft.sop.shareObject.ApiManager;
import com.channelsoft.sop.shareObject.AsyncTaskManager;
import com.channelsoft.sop.shareObject.NetworkConnection;
import com.channelsoft.sop.shareObject.VariableUtils;
import com.channelsoft.sop.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.channelsoft.sop.shareObject.CustomSnackBar.showSnackBar;
import static com.channelsoft.sop.shareObject.CustomToast.CustomToast;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityLoginBinding binding;
    private boolean show = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        isLogin();
        objectSetting();
    }

    private void objectSetting() {
        binding.setObj(new UserObject());
        /*
         * full screen
         * */
        Window window = getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        window.setAttributes(winParams);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        binding.clear.setOnClickListener(this);
        binding.showPassword.setOnClickListener(this);
        displayVersion();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_password:
                showPasswordSetting();
                break;
            case R.id.clear:
                binding.getObj().setEmail("");
                break;
        }
    }

    //    show/ hide password setting
    private void showPasswordSetting() {
        show = !show;
        binding.showPassword.setImageDrawable(show ? getResources().getDrawable(R.drawable.activity_login_hide_icon) : getResources().getDrawable(R.drawable.activity_login_show_icon));
        binding.password.setTransformationMethod(show ? null : new PasswordTransformationMethod());
    }

    public void forgotPassword(View v) {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
    }

    //    sign in setting
    public void checking(View v) {
        showProgressBar(true);
        final String email = binding.getObj().getEmail();
        final String password = binding.getObj().getPassword();
        closeKeyBoard();

        if (new NetworkConnection(this).checkNetworkConnection()) {
            if (!email.equals("") && !password.equals("")) {
                signIn(email, password);
            } else {
                showSnackBar(binding.getRoot(), "Invalid email or password!");
                showProgressBar(false);
            }

        } else {
            showSnackBar(binding.getRoot(), "No Internet connection!");
            showProgressBar(false);
        }
    }

    public void closeKeyBoard() {
        View view = getCurrentFocus();
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void signIn(final String email, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("email", email));
                apiDataObjectArrayList.add(new ApiDataObject("password", password));
                apiDataObjectArrayList.add(new ApiDataObject("login", "1"));
                AsyncTaskManager asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        VariableUtils.POST,
                        new ApiManager().registration,
                        new ApiManager().getResultParameter(
                                "",
                                new ApiManager().setData(apiDataObjectArrayList),
                                ""
                        )
                );
                asyncTaskManager.execute();

                if (!asyncTaskManager.isCancelled()) {

                    try {
                        final String jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);
                        if (jsonObjectLoginResponse != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject jsonObject = new JSONObject(jsonObjectLoginResponse);
                                        if (jsonObject.getString("status").equals("1")) {
                                            whenLoginSuccessful(jsonObject.getJSONObject("user_detail"));

                                        } else if (jsonObject.getString("status").equals("2")) {
                                            showSnackBar(binding.getRoot(), "Invalid email or password");
                                            showProgressBar(false);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            CustomToast(getApplicationContext(), "No Network Connection");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
                showProgressBar(false);
            }
        }).start();
    }

    private void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void whenLoginSuccessful(JSONObject jsonObject) {
        try {
            SharedPreferenceManager.setUser(this, new UserObject(
                    jsonObject.getString("user_id"),
                    jsonObject.getString("name"),
                    jsonObject.getString("email")));

            //intent
            startActivity(new Intent(this, MainActivity.class));
            finish();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void isLogin() {
        Log.d("haha", "haha: " + SharedPreferenceManager.getUser(this));
        if (!SharedPreferenceManager.getUser(this).equals("default")) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void displayVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = "Version " + pInfo.versionName;
            binding.activityLoginVersionName.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
