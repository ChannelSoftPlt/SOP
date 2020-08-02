package com.channelsoft.sop;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;

import com.channelsoft.sop.object.BranchObject;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.channelsoft.sop.shareObject.CustomToast.CustomToast;


public class ExportDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private TextView tvDateStart, tvDateEnd;
    private AppCompatSpinner fileType, branch;

    private ArrayList<BranchObject> branchObjectArrayList;
    private TextView labelBranch;

    private Button applyButton;
    private ImageView cancelButton;

    public ExportDialogCallBack exportDialogCallBack;

    public ExportDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.export_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        exportDialogCallBack = (ExportDialogCallBack) getActivity();

        tvDateStart = rootView.findViewById(R.id.sorting_dialog_start_date);
        tvDateEnd = rootView.findViewById(R.id.sorting_dialog_end_date);
        fileType = rootView.findViewById(R.id.file_type);

        branch = rootView.findViewById(R.id.branch);
        labelBranch = rootView.findViewById(R.id.label_branch);

        applyButton = rootView.findViewById(R.id.sorting_dialog_apply_button);
        cancelButton = rootView.findViewById(R.id.sorting_dialog_cancel_button);
    }

    private void objectSetting() {
        tvDateStart.setOnClickListener(this);
        tvDateEnd.setOnClickListener(this);

        applyButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            tvDateStart.setText(bundle.getString("date_start"));
            tvDateEnd.setText(bundle.getString("date_end"));
            fetchBranch();
            setupSpinner();
        }
    }

    private void setupSpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("PDF");
        categories.add("CSV");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), R.layout.custom_spinner_layout, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        fileType.setAdapter(dataAdapter);
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
    public void onClick(View view) {
        switch (view.getId()) {
            /*
             * date
             * */
            case R.id.sorting_dialog_start_date:
                selectDate(true);
                break;
            case R.id.sorting_dialog_end_date:
                selectDate(false);
                break;
            /*
             * button
             * */
            case R.id.sorting_dialog_apply_button:
                applySorting();
                break;
            case R.id.sorting_dialog_cancel_button:
                dismiss();
        }
    }

    private void selectDate(final boolean dateStart) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectedDate = String.format("%s", String.format(Locale.getDefault(), "%d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth));
                        if (dateStart) tvDateStart.setText(selectedDate);
                        else tvDateEnd.setText(selectedDate);
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    private void applySorting() {
        exportDialogCallBack.exportFile(tvDateStart.getText().toString(), tvDateEnd.getText().toString(), fileType.getSelectedItem().toString(), branchObjectArrayList.get(branch.getSelectedItemPosition()).getId());
        dismiss();
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
                                            branchObjectArrayList.add(new BranchObject("0", "All"));

                                            if (jsonObject.getString("status").equals("1")) {
                                                JSONArray jsonArray = new JSONArray(jsonObject.getString("branch"));

                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    branchObjectArrayList.add(new BranchObject(jsonArray.getJSONObject(i).getString("id"), jsonArray.getJSONObject(i).getString("name")));
                                                }
                                            }

                                            ArrayAdapter<BranchObject> dataAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.custom_spinner_layout, branchObjectArrayList);
                                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            branch.setAdapter(dataAdapter);
                                            /*
                                            * set visibility
                                            * */
                                            labelBranch.setVisibility(branchObjectArrayList.size() <= 2 ? View.GONE : View.VISIBLE);
                                            branch.setVisibility(branchObjectArrayList.size() <= 2 ? View.GONE : View.VISIBLE);

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
            }
        }).start();
    }

    public interface ExportDialogCallBack {
        void exportFile(String dateStart, String dateEnd, String fileType, String branch);
    }
}
