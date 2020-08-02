package com.channelsoft.sop;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.channelsoft.sop.databinding.AddRecordDialogBinding;
import com.channelsoft.sop.object.BranchObject;
import com.channelsoft.sop.object.RecordObject;
import com.channelsoft.sop.others.SwipeDismissTouchListener;
import com.channelsoft.sop.shareObject.ApiDataObject;
import com.channelsoft.sop.shareObject.ApiManager;
import com.channelsoft.sop.shareObject.AsyncTaskManager;
import com.channelsoft.sop.shareObject.VariableUtils;
import com.channelsoft.sop.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.channelsoft.sop.shareObject.CustomSnackBar.showSnackBar;
import static com.channelsoft.sop.shareObject.CustomToast.CustomToast;


public class AddRecordDialog extends DialogFragment implements View.OnClickListener, TextWatcher, AdapterView.OnItemClickListener {
    private AddRecordDialogBinding binding;
    private DialogCallBack dialogCallBack;

    private ArrayList<BranchObject> branchObjectArrayList;
    private ArrayList<RecordObject> recordObjectArrayList;

    private boolean isUpdate = false;
    private boolean autoComplete = false;
    private boolean ic = false;
    private boolean temperature = false;

    private Handler handler;
    public AddRecordDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = AddRecordDialogBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        objectSetting();
        return view;
    }

    private void objectSetting() {
        handler = new Handler();
        dialogCallBack = (DialogCallBack) getActivity();
        binding.actionButton.setOnClickListener(this);
        binding.closeButton.setOnClickListener(this);
        binding.deleteButton.setOnClickListener(this);

        Bundle bundle = getArguments();
        if (getArguments() != null) {
            isUpdate = bundle.getBoolean("isUpdate");
            autoComplete = bundle.getBoolean("autoComplete");
            ic = bundle.getBoolean("ic");
            temperature = bundle.getBoolean("temperature");

            if (isUpdate) {
                binding.setObj((RecordObject) bundle.getSerializable("object"));
            } else {
                binding.deleteButton.setVisibility(View.GONE);
                binding.setObj(new RecordObject());
            }
            showProgressBar(true);
            setView();
            fetchBranch();
        }
    }

    private void setView(){
        binding.layoutGenderAge.setVisibility(ic ? View.VISIBLE : View.GONE);
        binding.layoutIc.setVisibility(ic ? View.VISIBLE : View.GONE);
        binding.layoutTemperature.setVisibility(temperature ? View.VISIBLE : View.GONE);
    }

    private void checking() {
        /*
        * check temperature status
        * */
        if(temperature && binding.getObj().getTemperature().equals("")) {
            showSnackBar(binding.getRoot(), "All fields are required!");
            return;
        }

        if (!binding.getObj().getPrefix().equals("") && !binding.getObj().getPhone().equals("") && !binding.getObj().getName().equals("")) {
            showProgressBar(true);
            if (!isUpdate) addRecord();
            else updateRecord();
        } else {
            showSnackBar(binding.getRoot(), "All fields are required!");
        }
    }

    public void addRecord() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("age", binding.getObj().getAge()));
                apiDataObjectArrayList.add(new ApiDataObject("gender", binding.getObj().getGender()));
                apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getActivity())));
                apiDataObjectArrayList.add(new ApiDataObject("branch_id", branchObjectArrayList.get(binding.branch.getSelectedItemPosition()).getId()));
                apiDataObjectArrayList.add(new ApiDataObject("temperature", binding.getObj().getTemperature()));
                apiDataObjectArrayList.add(new ApiDataObject("prefix", binding.getObj().getPrefix()));
                apiDataObjectArrayList.add(new ApiDataObject("phone", binding.getObj().getPhone()));
                apiDataObjectArrayList.add(new ApiDataObject("ic", binding.getObj().getIc()));
                apiDataObjectArrayList.add(new ApiDataObject("name", binding.getObj().getName()));
                apiDataObjectArrayList.add(new ApiDataObject("email", binding.getObj().getEmail()));

                AsyncTaskManager asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        VariableUtils.POST,
                        new ApiManager().record,
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
                                    CustomToast(getActivity(), "Added Successfully!");
                                    dialogCallBack.updateList();
                                    binding.setObj(new RecordObject());
                                } else {
                                    showSnackBar(binding.getRoot(), "Something Went Wrong!");
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

    public void updateRecord() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("update", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("id", binding.getObj().getId()));
                apiDataObjectArrayList.add(new ApiDataObject("age", binding.getObj().getAge()));
                apiDataObjectArrayList.add(new ApiDataObject("gender", binding.getObj().getGender()));
                apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getActivity())));
                apiDataObjectArrayList.add(new ApiDataObject("branch_id", branchObjectArrayList.get(binding.branch.getSelectedItemPosition()).getId()));
                apiDataObjectArrayList.add(new ApiDataObject("temperature", binding.getObj().getTemperature()));
                apiDataObjectArrayList.add(new ApiDataObject("prefix", binding.getObj().getPrefix()));
                apiDataObjectArrayList.add(new ApiDataObject("phone", binding.getObj().getPhone()));
                apiDataObjectArrayList.add(new ApiDataObject("ic", binding.getObj().getIc()));
                apiDataObjectArrayList.add(new ApiDataObject("name", binding.getObj().getName()));
                apiDataObjectArrayList.add(new ApiDataObject("email", binding.getObj().getEmail()));

                AsyncTaskManager asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        VariableUtils.POST,
                        new ApiManager().record,
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
                                    dialogCallBack.updateList();
                                } else {
                                    showSnackBar(binding.getRoot(), "Something Went Wrong!");
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

    /*---------------------------------------------------------------delete purpose-----------------------------------------------------------------------*/
    public void deleteConfirmation() {
        final SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText("Warning");
        dialog.setContentText("Are you sure to delete this record?");
        dialog.setConfirmText("Okay");
        dialog.setCancelText("Cancel");
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                showProgressBar(true);
                delete();
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

    public void delete() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("delete", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("id", binding.getObj().getId()));

                AsyncTaskManager asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        VariableUtils.POST,
                        new ApiManager().record,
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
                                    CustomToast(getActivity(), "Delete Successfully!");
                                    dialogCallBack.updateList();
                                    dismiss();
                                } else {
                                    showSnackBar(binding.getRoot(), "Something Went Wrong!");
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

    public void fetchBranch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getActivity())));

                AsyncTaskManager asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        VariableUtils.POST,
                        new ApiManager().branch,
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
                            if (getActivity() != null)
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject jsonObject = new JSONObject(jsonObjectLoginResponse);

                                            branchObjectArrayList = new ArrayList<>();

                                            if (jsonObject.getString("status").equals("1")) {
                                                JSONArray jsonArray = new JSONArray(jsonObject.getString("branch"));

                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    branchObjectArrayList.add(new BranchObject(jsonArray.getJSONObject(i).getString("id"), jsonArray.getJSONObject(i).getString("name")));
                                                }

                                                binding.labelBranch.setVisibility(branchObjectArrayList.size() <= 1 ? View.GONE : View.VISIBLE);
                                                binding.branch.setVisibility(branchObjectArrayList.size() <= 1 ? View.GONE : View.VISIBLE);
                                                binding.branchHint.setVisibility(branchObjectArrayList.size() <= 1 ? View.GONE : View.VISIBLE);
                                            }

                                            ArrayAdapter<BranchObject> dataAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.custom_spinner_layout, branchObjectArrayList);
                                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            binding.branch.setAdapter(dataAdapter);
                                            //set existing branch position
                                            if (isUpdate) selectExistingPosition();
                                            //auto complete purpose
                                            setAutoComplete();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
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
                }
                showProgressBar(false);
            }
        }).start();
    }

    private void setAutoComplete() {
        if (autoComplete) {
            binding.name.addTextChangedListener(this);
            binding.name.setOnItemClickListener(this);
        }
    }

    private void selectExistingPosition() {
        if (isUpdate)
            for (int i = 0; i < branchObjectArrayList.size(); i++) {
                if (branchObjectArrayList.get(i).getId().equals(binding.getObj().getBranch_id())) {
                    binding.branch.setSelection(i);
                    return;
                }
            }
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
            case R.id.action_button:
                checking();
                break;
            case R.id.close_button:
                dismiss();
                break;
            case R.id.delete_button:
                deleteConfirmation();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(final CharSequence s, int start, int before, int count) {
        if (s.length() >= 4 && !isUpdate) {
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    searchRelatedRecord(s.toString());
                }
            }, 300);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void searchRelatedRecord(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                    apiDataObjectArrayList.add(new ApiDataObject("auto_complete", "1"));
                    apiDataObjectArrayList.add(new ApiDataObject("query", query));
                    apiDataObjectArrayList.add(new ApiDataObject("branch_id", branchObjectArrayList.get(binding.branch.getSelectedItemPosition()).getId()));
                    apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getActivity())));

                    AsyncTaskManager asyncTaskManager = new AsyncTaskManager(
                            getActivity(),
                            VariableUtils.POST,
                            new ApiManager().record,
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
                                if (getActivity() != null)
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                JSONObject jsonObject = new JSONObject(jsonObjectLoginResponse);
                                                if (jsonObject.getString("status").equals("1")) {
                                                    JSONArray jsonArray = new JSONArray(jsonObject.getString("auto_complete_record"));

                                                    recordObjectArrayList = new ArrayList<>();

                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        recordObjectArrayList.add(new RecordObject(
                                                                jsonArray.getJSONObject(i).getString("age"),
                                                                jsonArray.getJSONObject(i).getString("gender"),
                                                                "",
                                                                jsonArray.getJSONObject(i).getString("prefix"),
                                                                jsonArray.getJSONObject(i).getString("phone"),
                                                                jsonArray.getJSONObject(i).getString("ic"),
                                                                jsonArray.getJSONObject(i).getString("name")
                                                        ));
                                                    }

                                                    ArrayAdapter<RecordObject> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, recordObjectArrayList);
                                                    binding.name.setThreshold(1);
                                                    binding.name.setAdapter(adapter);

                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
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
                    }
                } catch (Exception e) {

                }
            }
        }).start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        binding.setObj(
                new RecordObject(
                        recordObjectArrayList.get(position).getAge(),
                        recordObjectArrayList.get(position).getGender(),
                        "",
                        recordObjectArrayList.get(position).getPrefix(),
                        recordObjectArrayList.get(position).getPhone(),
                        recordObjectArrayList.get(position).getIc(),
                        recordObjectArrayList.get(position).getName()
                ));
    }

    public interface DialogCallBack {
        void updateList();
    }
}
