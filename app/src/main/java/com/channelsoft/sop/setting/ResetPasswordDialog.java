package com.channelsoft.sop.setting;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.channelsoft.sop.R;
import com.channelsoft.sop.databinding.ResetPasswordDialogBinding;
import com.channelsoft.sop.others.SwipeDismissTouchListener;
import com.channelsoft.sop.shareObject.ApiDataObject;
import com.channelsoft.sop.shareObject.ApiManager;
import com.channelsoft.sop.shareObject.AsyncTaskManager;
import com.channelsoft.sop.shareObject.VariableUtils;
import com.channelsoft.sop.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.channelsoft.sop.shareObject.CustomSnackBar.showSnackBar;
import static com.channelsoft.sop.shareObject.CustomToast.CustomToast;


public class ResetPasswordDialog extends DialogFragment implements View.OnClickListener {
    private ResetPasswordDialogBinding binding;

    private boolean show = false, showConfirm = false, showNew = false;

    public ResetPasswordDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = ResetPasswordDialogBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        objectSetting();
        return view;
    }

    private void objectSetting() {
        binding.showConfirmPassword.setOnClickListener(this);
        binding.showNewPassword.setOnClickListener(this);
        binding.showPassword.setOnClickListener(this);
        binding.updatePassword.setOnClickListener(this);
        binding.back.setOnClickListener(this);

    }

    private void checkInput() {
        if (binding.password.getText().length() > 0 && binding.newPassword.getText().length() > 0 && binding.confirmationPassword.getText().length() > 0) {
            if (binding.newPassword.getText().length() >= 6 && binding.confirmationPassword.getText().length() >= 6) {
                if (binding.newPassword.getText().toString().equals(binding.confirmationPassword.getText().toString())) {
                    showProgressBar(true);
                    updatePassword();
                } else
                    showSnackBar(binding.getRoot(), "Your new password not matched with confirm password!");
            } else showSnackBar(binding.getRoot(), "Please Enter a Stronger Password!");
        } else showSnackBar(binding.getRoot(), "All Fields above are required!");
    }

    public void updatePassword() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("update", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("current_password", binding.password.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("new_password", binding.newPassword.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("email", SharedPreferenceManager.getUserEmail(getActivity())));

                AsyncTaskManager asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
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
                        String jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);
                        if (jsonObjectLoginResponse != null) {
                            try {
                                final JSONObject jsonObject = new JSONObject(jsonObjectLoginResponse);
                                if (jsonObject.getString("status").equals("1")) {
                                    CustomToast(getActivity(), "Update Successfully!");
                                    dismiss();
                                } else {
                                    showSnackBar(binding.getRoot(), "Current Password Not Matched!");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            CustomToast(getActivity(), "No Network Connection");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(getActivity(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getActivity(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getActivity(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                    showProgressBar(false);
                }
            }
        }).start();
    }

    private void showProgressBar(final boolean show) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(d.getWindow()).setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setWindowAnimations(R.style.dialog_up_down);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog d = getDialog();
        Objects.requireNonNull(d.getWindow()).getDecorView().setOnTouchListener(new SwipeDismissTouchListener(d.getWindow().getDecorView(), null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        dismiss();
                    }
                }));
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_password:
                showPasswordSetting();
                break;
            case R.id.show_new_password:
                showNewPasswordSetting();
                break;
            case R.id.show_confirm_password:
                showConfirmPasswordSetting();
                break;
            case R.id.update_password:
                checkInput();
                break;
            case R.id.back:
                dismiss();
                break;
        }
    }

    //    show/ hide password setting
    private void showPasswordSetting() {
        show = !show;
        binding.showPassword.setImageDrawable(show ? getResources().getDrawable(R.drawable.activity_login_hide_icon) : getResources().getDrawable(R.drawable.activity_login_show_icon));
        binding.password.setTransformationMethod(show ? null : new PasswordTransformationMethod());
    }

    //    show/ hide password setting
    private void showNewPasswordSetting() {
        showNew = !showNew;
        binding.showNewPassword.setImageDrawable(showNew ? getResources().getDrawable(R.drawable.activity_login_hide_icon) : getResources().getDrawable(R.drawable.activity_login_show_icon));
        binding.newPassword.setTransformationMethod(showNew ? null : new PasswordTransformationMethod());
    }

    //    show/ hide password setting
    private void showConfirmPasswordSetting() {
        showConfirm = !showConfirm;
        binding.showConfirmPassword.setImageDrawable(showConfirm ? getResources().getDrawable(R.drawable.activity_login_hide_icon) : getResources().getDrawable(R.drawable.activity_login_show_icon));
        binding.confirmationPassword.setTransformationMethod(showConfirm ? null : new PasswordTransformationMethod());
    }
}
