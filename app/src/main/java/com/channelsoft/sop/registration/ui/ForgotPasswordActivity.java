package com.channelsoft.sop.registration.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.channelsoft.sop.R;
import com.channelsoft.sop.databinding.ActivityForgotPasswordBinding;
import com.channelsoft.sop.shareObject.ApiDataObject;
import com.channelsoft.sop.shareObject.ApiManager;
import com.channelsoft.sop.shareObject.AsyncTaskManager;
import com.channelsoft.sop.shareObject.VariableUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.channelsoft.sop.shareObject.CustomSnackBar.showSnackBar;
import static com.channelsoft.sop.shareObject.CustomToast.CustomToast;

public class ForgotPasswordActivity extends AppCompatActivity {
    ActivityForgotPasswordBinding binding;
    private String pacNo = "";
    int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        objectSetting();
    }

    private void objectSetting() {
        setPage(1);
    }

    public void clear(View view) {
        switch (view.getId()) {
            case R.id.clear_email:
                binding.email.setText("");
                break;
            case R.id.clear_pac:
                binding.pac.setText("");
                break;
            case R.id.clear_new_password:
                binding.newPassword.setText("");
                break;
            case R.id.clear_confirm_password:
                binding.confirmPassword.setText("");
                break;
        }
    }

    public void resendPac(View view) {
        checkEmail(true);
    }

    public void checkingInput(View view) {
        switch (view.getId()) {
            case R.id.send_verification:
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                if (binding.email.getText().toString().matches(emailPattern)) {
                    checkEmail(false);
                } else Toast.makeText(this, "Invalid Email!", Toast.LENGTH_SHORT).show();

                break;
            case R.id.verify:
                if (binding.pac.length() == 6 && pacNo.equals(binding.pac.getText().toString())) {
                    pacNo = "";
                    next();
                    showSnackBar(binding.getRoot(), "Verify Successfully!");
                    showProgressBar(false);
                } else Toast.makeText(this, "Invalid Pac Number!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.update_password:
                if (binding.newPassword.length() > 5 && binding.confirmPassword.length() > 5) {
                    if (binding.newPassword.getText().toString().equals(binding.confirmPassword.getText().toString())) {
                        updatePassword();
                    } else
                        Toast.makeText(this, "Your Password not matched!", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(this, "Your Password too weak!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void previous(View view) {
        if (page > 1) {
            page--;
            setPage(page);
        } else finish();
    }

    public void next() {
        if (page < 3) {
            page++;
            setPage(page);
        }
    }

    private void setPage(int page) {
        /*
         * page 1
         * */
        binding.page1.setVisibility(page == 1 ? View.VISIBLE : View.GONE);
        /*
         * page 2
         * */
        binding.page2.setVisibility(page == 2 ? View.VISIBLE : View.GONE);

        /*
         * page 3
         * */
        binding.page3.setVisibility(page == 3 ? View.VISIBLE : View.GONE);
        /*
         * content
         * */
        binding.title.setText(getContent(page)[0]);
        binding.description.setText(getContent(page)[1]);

        if (page == 1)
            binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.forgot_password_icon));
        else if (page == 2)
            binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.email_icon));
        else if (page == 3)
            binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.change_password_icon));
    }


    private String[] getContent(int page) {
        switch (page) {
            case 1:
                return new String[]{"Forgot Password?", "We just need your registered email address to send you password reset."};
            case 2:
                return new String[]{"Email Verification", "A verification is sent to your email. Please check your inbox and spam or junk email."};
            default:
                return new String[]{"Reset Password", "Please enter your new password in order to reset your password."};
        }
    }

    /*
     * check email
     * */
    private void checkEmail(final boolean isResend) {
        showProgressBar(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("forgot_password", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("email", binding.email.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("pac", generatePac()));
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
                                            if (isResend) {
                                                showSnackBar(binding.getRoot(), "Email resend!");
                                            } else {
                                                next();
                                                showSnackBar(binding.getRoot(), "Please check your email inbox.");
                                            }

                                        } else if (jsonObject.getString("status").equals("2")) {
                                            showSnackBar(binding.getRoot(), "Invalid email or password");
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

    /*
     * update password
     * */
    private void updatePassword() {
        showProgressBar(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("forgot_password", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("new_password", binding.newPassword.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("email", binding.email.getText().toString()));
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
                                            /*
                                            * redirect to login activity
                                            * */
                                            CustomToast(getApplicationContext(), "Password Reset Successfully!");
                                            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                                            finish();
                                        } else if (jsonObject.getString("status").equals("2")) {
                                            showSnackBar(binding.getRoot(), "Invalid email or password");
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

    public String generatePac() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        pacNo = String.format("%06d", number);
        return pacNo;
    }

    private void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
