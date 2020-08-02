package com.channelsoft.sop;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUriExposedException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.channelsoft.sop.adapter.RecordAdapter;
import com.channelsoft.sop.databinding.ActivityMainBinding;
import com.channelsoft.sop.databinding.CustomActionBarBinding;
import com.channelsoft.sop.databinding.NoInternetConnectionBinding;
import com.channelsoft.sop.databinding.NotFoundLayoutBinding;
import com.channelsoft.sop.object.RecordObject;
import com.channelsoft.sop.others.CSVWriter;
import com.channelsoft.sop.others.PaginationListener;
import com.channelsoft.sop.registration.ui.LoginActivity;
import com.channelsoft.sop.setting.SettingActivity;
import com.channelsoft.sop.shareObject.ApiDataObject;
import com.channelsoft.sop.shareObject.ApiManager;
import com.channelsoft.sop.shareObject.AsyncTaskManager;
import com.channelsoft.sop.shareObject.NetworkConnection;
import com.channelsoft.sop.shareObject.VariableUtils;
import com.channelsoft.sop.sharePreference.SharedPreferenceManager;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.channelsoft.sop.others.PaginationListener.PAGE_START;
import static com.channelsoft.sop.shareObject.CustomSnackBar.showSnackBar;
import static com.channelsoft.sop.shareObject.CustomToast.CustomToast;
import static com.channelsoft.sop.shareObject.VariableUtils.LOGOUT;
import static com.channelsoft.sop.shareObject.VariableUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
import static com.channelsoft.sop.shareObject.VariableUtils.UPDATE_UI;
import static com.itextpdf.text.html.WebColors.getRGBColor;

public class MainActivity extends AppCompatActivity implements RecordAdapter.OrderAdapterCallBack, SwipeRefreshLayout.OnRefreshListener,
        AddRecordDialog.DialogCallBack, SortingDialog.SortingDialogCallBack, ExportDialog.ExportDialogCallBack {
    private ActivityMainBinding binding;
    private NoInternetConnectionBinding connectionBinding;
    private NotFoundLayoutBinding notFoundLayoutBinding;
    private CustomActionBarBinding customActionBarBinding;

    private RecordAdapter adapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<RecordObject> recordObjectArrayList;
    /*
     * search purpose
     * */
    private String lastQuery = "";
    /*
     * sorting purpose
     * */
    public String startDate, endDate, sortingBranchID = "0";
    private JSONArray jsonArray;
    private JSONObject jsonObject;
    /*
     * pagination purpose
     * */
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 1;
    private int itemPerPage = 10;
    int itemCount = 0;
    /*
     * export feature
     * */
    File pdfFile;
    private ArrayList<RecordObject> printRecordArrayList;
    private String exportStartDate, exportEndDate, fileType, branchID = "0";
    /*
     * print purpose
     * */
    Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
    Font smallBoldFont = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.BOLD);
    Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
    Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    Font largeFont = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD);
    Font smallFont = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
    Font mandarin;
    /*
     * auto complete purpose
     * */
    private String autoComplete = "";
    /*
     * temperature
     * */
    private String temperature = "";
    /*
     * ic
     * */
    private String ic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        connectionBinding = binding.noInternetConnection;
        notFoundLayoutBinding = binding.notFoundLayout;
        customActionBarBinding = binding.customActionBar;
        View view = binding.getRoot();
        setStatusBar();
        setContentView(view);

        startDate = getDate(true);
        endDate = getDate(false);

        exportStartDate = getDate(true);
        exportEndDate = getDate(false);

        objectSetting();
    }

    public void setStatusBar() {
        customActionBarBinding.toolbar.setTitle("Home");
        setSupportActionBar(customActionBarBinding.toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem search_item = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) search_item.getActionView();
        EditText txtSearch = (searchView.findViewById(androidx.appcompat.R.id.search_src_text));
        txtSearch.setHintTextColor(Color.LTGRAY);
        txtSearch.setTextColor(Color.WHITE);


        searchView.setFocusable(false);
        searchView.setQueryHint("Search By Name, Ic, Phone");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                adapter.removeLoading();
                reset(false);
                fetchOrderList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() <= 0) {
                    reset(true);
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!networkChecking()) return true;

        switch (item.getItemId()) {
            case R.id.setting:
                startActivityForResult(new Intent(this, SettingActivity.class), LOGOUT);
                return true;
            case R.id.export:
                printAction();
                return true;
            case R.id.sorting:
                openSortingDialog();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void logout() {
        SharedPreferenceManager.clear(this);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void objectSetting() {
        recordObjectArrayList = new ArrayList<>();
        binding.swipeRefresh.setOnRefreshListener(this);
        /*
         * product list purpose
         * */
        binding.recordList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.recordList.setLayoutManager(layoutManager);
        adapter = new RecordAdapter(getApplicationContext(), recordObjectArrayList, this);
        binding.recordList.setAdapter(adapter);
        /*
         * pagination purpose
         * */
        binding.recordList.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
                fetchOrderList(lastQuery);
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        connectionBinding.retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                netWorkChecking();
            }
        });

        netWorkChecking();
    }

    private void netWorkChecking() {
        if (networkChecking()) {
            setNetworkVisibility(false);
            showProgressBar(true);
            readUserStatus();
        } else setNetworkVisibility(true);
    }

    private boolean networkChecking() {
        boolean connection = new NetworkConnection(getApplicationContext()).checkNetworkConnection();
        if (!connection) {
            showSnackBar(binding.getRoot(), "No Internet Connection!");
        }
        return connection;
    }

    public void fetchOrderList(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                lastQuery = query;
                recordObjectArrayList = new ArrayList<>();

                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("query", query));
                apiDataObjectArrayList.add(new ApiDataObject("start_date", startDate));
                apiDataObjectArrayList.add(new ApiDataObject("end_date", endDate));
                apiDataObjectArrayList.add(new ApiDataObject("branch_id", sortingBranchID));
                apiDataObjectArrayList.add(new ApiDataObject("page", String.valueOf(currentPage)));
                apiDataObjectArrayList.add(new ApiDataObject("itemPerPage", String.valueOf(itemPerPage)));
                apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getApplicationContext())));

                AsyncTaskManager asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        jsonObject = new JSONObject(jsonObjectLoginResponse);
                                        if (jsonObject.getString("status").equals("1")) {
                                            jsonArray = new JSONArray(jsonObject.getString("record"));
                                            for (int i = 0; i < jsonArray.length(); i++) {

                                                recordObjectArrayList.add(new RecordObject(
                                                        jsonArray.getJSONObject(i).getString("created_at"),
                                                        jsonArray.getJSONObject(i).getString("age"),
                                                        jsonArray.getJSONObject(i).getString("gender"),
                                                        jsonArray.getJSONObject(i).getString("temperature"),
                                                        jsonArray.getJSONObject(i).getString("prefix"),
                                                        jsonArray.getJSONObject(i).getString("phone"),
                                                        jsonArray.getJSONObject(i).getString("ic"),
                                                        jsonArray.getJSONObject(i).getString("name"),
                                                        jsonArray.getJSONObject(i).getString("id"),
                                                        jsonArray.getJSONObject(i).getString("email"),
                                                        jsonArray.getJSONObject(i).getString("branch_id"),
                                                        jsonArray.getJSONObject(i).getString("branch")
                                                ));
                                            }
                                        }
                                        /*
                                         * not found layout
                                         * */
                                        setNotFoundVisibility();
                                        /*
                                         * pagination purpose
                                         * */
                                        if (currentPage != PAGE_START) adapter.removeLoading();
                                        adapter.addItems(recordObjectArrayList);
                                        binding.swipeRefresh.setRefreshing(false);

                                        // check weather is last page or not
                                        if (currentPage < Integer.parseInt(jsonObject.getString("total_page"))) {
                                            adapter.addLoading();
                                        } else {
                                            isLastPage = true;
                                        }
                                        isLoading = false;

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            setNetworkVisibility(true);
                            CustomToast(getApplicationContext(), "No Network Connection");
                        }
                    } catch (InterruptedException e) {
                        setNetworkVisibility(true);
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        setNetworkVisibility(true);
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        setNetworkVisibility(true);
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                    showProgressBar(false);
                }
            }
        }).start();
    }


    /*-------------------------------------------------------------visibility control------------------------------------------------------------------------*/
    private void setNetworkVisibility(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show)
                    Toast.makeText(getApplicationContext(), "Connection Problem!", Toast.LENGTH_SHORT).show();
                connectionBinding.noConnectionLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                binding.swipeRefresh.setVisibility(!show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setNotFoundVisibility() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notFoundLayoutBinding.noConnection.setVisibility(recordObjectArrayList.size() > 0 ? View.GONE : View.VISIBLE);
            }
        });
    }

    @Override
    public void onRefresh() {
        reset(true);
    }

    public void reset(final boolean fetchData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressBar(true);
                //search purpose
                lastQuery = "";
                //pagination purpose
                itemCount = 0;
                currentPage = PAGE_START;
                isLastPage = false;
                //ui purpose
//                recordObjectArrayList.clear();
                adapter.clear();
                //fetch data after cleaning
                if (fetchData) netWorkChecking();
            }
        });
    }

    private void notifyDataSetChanged() {
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void showProgressBar(final boolean show) {
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.recordList.setVisibility(show ? View.GONE : View.VISIBLE);
                    binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == UPDATE_UI) {
            reset(true);
        } else if (resultCode == LOGOUT) logout();
    }

    @Override
    public void view(RecordObject object) {
        if (!networkChecking()) return;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isUpdate", true);
        bundle.putBoolean("autoComplete", autoComplete.equals("1"));
        bundle.putBoolean("ic", !ic.equals("1"));
        bundle.putBoolean("temperature", !temperature.equals("1"));
        bundle.putSerializable("object", object);
        openRecordDialog(bundle);
    }

    public void create(View view) {
        if (!networkChecking()) return;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isUpdate", false);
        bundle.putBoolean("autoComplete", autoComplete.equals("1"));
        bundle.putBoolean("ic", !ic.equals("1"));
        bundle.putBoolean("temperature", !temperature.equals("1"));
        openRecordDialog(bundle);
    }

    public void openRecordDialog(Bundle bundle) {
        DialogFragment dialogFragment = new AddRecordDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    @Override
    public void updateList() {
        reset(true);
    }

    /*----------------------------------------------------------------------------------export purpose-------------------------------------------------*/
    private void printAction() {
        if (recordObjectArrayList.size() > 0) {
            try {
                createPdfWrapper();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            showSnackBar(binding.getRoot(), "Nothing to print!");
        }
    }

    private void createPdfWrapper() throws FileNotFoundException {
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel(
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                                }
                            });
                    return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            openExportDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                openExportDialog();
            } else {
                // Permission Denied
                showSnackBar(binding.getRoot(), "WRITE_EXTERNAL Permission Denied");
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage("You need to allow access to Storage")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    private void openExportDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("date_start", exportStartDate);
        bundle.putString("date_end", exportEndDate);

        DialogFragment dialogFragment = new ExportDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    public void exportFile(String startDate, String endDate, String fileType, String branchID) {
        this.exportStartDate = startDate;
        this.exportEndDate = endDate;
        this.fileType = fileType;
        this.branchID = branchID;

        Toast.makeText(this, "File is creating...", Toast.LENGTH_SHORT).show();
        showProgressBar(true);
        generateFile(startDate, endDate, branchID);
    }

    private String[] fileHeader() {
        ArrayList<String> documentHeader = new ArrayList<>();
        documentHeader.add("No");
        documentHeader.add("Name");
        documentHeader.add("Phone");

        if (temperature.equals("0")) documentHeader.add("Temperature");
        if (ic.equals("0")) documentHeader.add("IC");

        documentHeader.add("Date");
        return documentHeader.toArray(new String[0]);
    }

    /*
     * csv content
     * */
    private String[] csvContent(int i) {
        ArrayList<String> csvContent = new ArrayList<>();
        csvContent.add(String.valueOf(i + 1));
        csvContent.add(printRecordArrayList.get(i).getName());
        csvContent.add(printRecordArrayList.get(i).getPhone());

        if (temperature.equals("0")) csvContent.add(printRecordArrayList.get(i).getTemperature());
        if (ic.equals("0")) csvContent.add(printRecordArrayList.get(i).getIc());

        csvContent.add(printRecordArrayList.get(i).getDate());
        return csvContent.toArray(new String[0]);
    }

    private void createCSV() {
        try {
            File docsFolder = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + "Sop");
            if (!docsFolder.exists()) {
                docsFolder.mkdir();
            }

            String fileName = getDate(false) + ".csv";
            File file = new File(docsFolder.getAbsolutePath(), fileName);

            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
//            csvWrite.writeNext(new String[]{"No", "Name", "Phone", "Temperature", "IC", "Date"});
            csvWrite.writeNext(fileHeader());

            for (int i = 0; i < jsonArray.length(); i++) {
                csvWrite.writeNext(csvContent(i));
            }

            csvWrite.close();
            showSnackBar(binding.getRoot(), "File Created!");
            shareFile(file);
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }

    public void generateFile(final String startDate, final String endDate, final String branchID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                printRecordArrayList = new ArrayList<>();
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("export", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("start_date", startDate));
                apiDataObjectArrayList.add(new ApiDataObject("end_date", endDate));
                apiDataObjectArrayList.add(new ApiDataObject("branch_id", branchID));
                apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getApplicationContext())));

                AsyncTaskManager asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        jsonObject = new JSONObject(jsonObjectLoginResponse);
                                        if (jsonObject.getString("status").equals("1")) {
                                            jsonArray = new JSONArray(jsonObject.getString("export_data"));
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                printRecordArrayList.add(
                                                        new RecordObject(
                                                                jsonArray.getJSONObject(i).getString("created_at"),
                                                                jsonArray.getJSONObject(i).getString("temperature"),
                                                                "6" + jsonArray.getJSONObject(i).getString("prefix") + "-" + jsonArray.getJSONObject(i).getString("phone"),
                                                                jsonArray.getJSONObject(i).getString("ic"),
                                                                jsonArray.getJSONObject(i).getString("name"),
                                                                String.valueOf(i + 1)
                                                        )
                                                );
                                            }
                                            if (fileType.equals("PDF")) createPdf();
                                            else createCSV();
                                        } else {
                                            showSnackBar(binding.getRoot(), "Nothing to print!");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            setNetworkVisibility(true);
                            CustomToast(getApplicationContext(), "No Network Connection");
                        }
                    } catch (InterruptedException e) {
                        setNetworkVisibility(true);
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        setNetworkVisibility(true);
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        setNetworkVisibility(true);
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                    showProgressBar(false);
                }
            }
        }).start();
    }

    /*
     * print invoice or quotation
     * */
//    private void createPdf() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                File docsFolder = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + "Sop");
//                if (!docsFolder.exists()) {
//                    docsFolder.mkdir();
//                    Log.i("Detail Activity", "Created a new directory for PDF");
//                }
//
//                pdfFile = new File(docsFolder.getAbsolutePath(), getDate(false) + ".pdf");
//                OutputStream output = null;
//                try {
//                    output = new FileOutputStream(pdfFile);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//
//                Document document = new Document();
//                try {
////                    String[] documentHeader = {"No", "Name", "Phone", "Temperature", "IC", "Date"};
//                    String[] documentHeader = fileHeader();
//                    //use to set background color
//                    BaseColor white = getRGBColor("#ffffff");
//
//                    PdfWriter.getInstance(document, output);
//                    document.open();
//                    document.add(new Paragraph(""));
//
//                    try {
//                        PdfPCell cell;
//                        try {
//                            /*-----------------------------------------------------header------------------------------------------------------------*/
//                            //create table
//                            PdfPTable headerTable = new PdfPTable(6);
//                            headerTable.setWidthPercentage(100);
//                            float[] width = new float[]{5, 20, 20, 15, 20, 20};
//                            headerTable.setWidths(width);
//                            /*
//                             * form header
//                             * */
//                            cell = new PdfPCell();
//                            cell.setBorder(Rectangle.NO_BORDER);
//                            cell.setColspan(6);
//
//
//                            Paragraph formHeader = new Paragraph();
//                            formHeader.setAlignment(Element.ALIGN_CENTER);
//                            formHeader.setExtraParagraphSpace(2);
//                            formHeader.setFont(largeFont);
//                            formHeader.add(SharedPreferenceManager.getUsername(getApplicationContext()) + " Visitor List");
//                            cell.addElement(formHeader);
//
//                            formHeader = new Paragraph();
//                            formHeader.setAlignment(Element.ALIGN_CENTER);
//                            formHeader.setExtraParagraphSpace(5);
//                            formHeader.setFont(normalFont);
//                            formHeader.add("Date: " + exportStartDate + " - " + exportEndDate);
//                            cell.addElement(formHeader);
//
//
//                            headerTable.addCell(cell);
//
//                            /*
//                             * table header
//                             * */
//                            for (int i = 0; i < documentHeader.length; i++) {
//
//                                cell = new PdfPCell(new Paragraph(documentHeader[i], headerFont));
//                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                                cell.setBackgroundColor(white);
//                                headerTable.addCell(cell);
//                            }
//
//                            for (int i = 0; i < printRecordArrayList.size(); i++) {
////                                for (int j = 0; j < 6; j++) {
////                                    cell = new PdfPCell();
////                                    cell.setColspan(1);
////                                    cell.addElement(returnDoColumnData(i, j));
////                                    headerTable.addCell(cell);
////                                }
//                                for (int j = 0; j < documentHeader.length; j++) {
//                                    cell = new PdfPCell();
//                                    cell.setColspan(1);
//                                    cell.addElement(returnDoColumnData(i, j));
//                                    headerTable.addCell(cell);
//                                }
//                            }
//
//                            document.add(headerTable);
//
//                        } catch (NullPointerException e) {
//                            Log.e("Null", "null exception:" + e);
//                            CustomToast(getApplicationContext(), "Something is null!");
//                        } finally {
//                            document.close();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                showProgressBar(false);
//                previewPdf();
//            }
//        }).start();
//    }

    private float[] countWidth() {
        int i = 4;
        if (temperature.equals("0")) i++;
        if (ic.equals("0")) i++;

        if (i == 6) return new float[]{5, 20, 20, 15, 20, 20};
        else if (i == 5) return new float[]{5, 25, 25, 20, 25};
        else return new float[]{10, 30, 30, 30};
    }


    private void createPdf() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File docsFolder = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + "Sop");
                if (!docsFolder.exists()) {
                    docsFolder.mkdir();
                    Log.i("Detail Activity", "Created a new directory for PDF");
                }

                pdfFile = new File(docsFolder.getAbsolutePath(), getDate(false) + ".pdf");
                OutputStream output = null;
                try {
                    output = new FileOutputStream(pdfFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Document document = new Document();
                try {
                    String[] documentHeader = fileHeader();
                    //use to set background color
                    BaseColor white = getRGBColor("#ffffff");

                    PdfWriter.getInstance(document, output);
                    document.open();
                    document.add(new Paragraph(""));

                    try {
                        PdfPCell cell;
                        Paragraph itemDetail;
                        try {
                            /*-----------------------------------------------------header------------------------------------------------------------*/
                            //create table
                            PdfPTable headerTable = new PdfPTable(documentHeader.length);
                            headerTable.setWidthPercentage(100);
                            headerTable.setWidths(countWidth());
                            /*
                             * form header
                             * */
                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.setColspan(documentHeader.length);


                            Paragraph formHeader = new Paragraph();
                            formHeader.setAlignment(Element.ALIGN_CENTER);
                            formHeader.setExtraParagraphSpace(2);
                            formHeader.setFont(largeFont);
                            formHeader.add(SharedPreferenceManager.getUsername(getApplicationContext()) + " Visitor List");
                            cell.addElement(formHeader);

                            formHeader = new Paragraph();
                            formHeader.setAlignment(Element.ALIGN_CENTER);
                            formHeader.setExtraParagraphSpace(5);
                            formHeader.setFont(normalFont);
                            formHeader.add("Date: " + exportStartDate + " - " + exportEndDate);
                            cell.addElement(formHeader);


                            headerTable.addCell(cell);
                            /*
                             * table header
                             * */
                            for (int i = 0; i < documentHeader.length; i++) {

                                cell = new PdfPCell(new Paragraph(documentHeader[i], headerFont));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setBackgroundColor(white);
                                headerTable.addCell(cell);
                            }

                            /*
                            * table content
                            * */
                            for (int i = 0; i < printRecordArrayList.size(); i++) {
                                //count
                                cell = new PdfPCell();
                                cell.setColspan(1);
                                itemDetail = new Paragraph();
                                itemDetail.setExtraParagraphSpace(5);
                                itemDetail.setAlignment(Element.ALIGN_CENTER);
                                itemDetail.setFont(normalFont);
                                itemDetail.add(String.valueOf(i + 1));
                                cell.addElement(itemDetail);
                                headerTable.addCell(cell);

                                //customer name
                                cell = new PdfPCell();
                                cell.setColspan(1);
                                itemDetail = new Paragraph();
                                itemDetail.setExtraParagraphSpace(5);
                                itemDetail.setAlignment(Element.ALIGN_CENTER);
                                itemDetail.setFont(normalFont);
                                itemDetail.add(printRecordArrayList.get(i).getName());
                                cell.addElement(itemDetail);
                                headerTable.addCell(cell);

                                //customer phone
                                cell = new PdfPCell();
                                cell.setColspan(1);
                                itemDetail = new Paragraph();
                                itemDetail.setExtraParagraphSpace(5);
                                itemDetail.setAlignment(Element.ALIGN_CENTER);
                                itemDetail.setFont(normalFont);
                                itemDetail.add("+" + printRecordArrayList.get(i).getPhone());
                                cell.addElement(itemDetail);
                                headerTable.addCell(cell);

                                //customer temperature
                                if (temperature.equals("0")) {
                                    cell = new PdfPCell();
                                    cell.setColspan(1);
                                    itemDetail = new Paragraph();
                                    itemDetail.setExtraParagraphSpace(5);
                                    itemDetail.setAlignment(Element.ALIGN_CENTER);
                                    itemDetail.setFont(normalFont);
                                    itemDetail.add(printRecordArrayList.get(i).getTemperature());
                                    cell.addElement(itemDetail);
                                    headerTable.addCell(cell);
                                }

                                //customer ic
                                if (ic.equals("0")) {
                                    cell = new PdfPCell();
                                    cell.setColspan(1);
                                    itemDetail = new Paragraph();
                                    itemDetail.setExtraParagraphSpace(5);
                                    itemDetail.setAlignment(Element.ALIGN_CENTER);
                                    itemDetail.setFont(normalFont);
                                    itemDetail.add(printRecordArrayList.get(i).getIc());
                                    cell.addElement(itemDetail);
                                    headerTable.addCell(cell);
                                }

                                //record date
                                cell = new PdfPCell();
                                cell.setColspan(1);
                                itemDetail = new Paragraph();
                                itemDetail.setExtraParagraphSpace(5);
                                itemDetail.setAlignment(Element.ALIGN_CENTER);
                                itemDetail.setFont(normalFont);
                                itemDetail.add(printRecordArrayList.get(i).getDate());
                                cell.addElement(itemDetail);
                                headerTable.addCell(cell);

                            }
                            document.add(headerTable);

                        } catch (NullPointerException e) {
                            Log.e("Null", "null exception:" + e);
                            CustomToast(getApplicationContext(), "Something is null!");
                        } finally {
                            document.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                showProgressBar(false);
                previewPdf();
            }
        }).start();
    }

    private Paragraph returnDoColumnData(int i, int j) {
        Paragraph itemDetail = new Paragraph();
        itemDetail.setExtraParagraphSpace(5);
        itemDetail.setAlignment(Element.ALIGN_CENTER);
        itemDetail.setFont(normalFont);
        switch (j) {
            case 0:
                itemDetail.add(String.valueOf(i + 1));
                break;
            case 1:
                itemDetail.add(printRecordArrayList.get(i).getName());
                break;
            case 2:
                itemDetail.add("+" + printRecordArrayList.get(i).getPhone());
                break;
            case 3:
                itemDetail.add(printRecordArrayList.get(i).getTemperature());
                break;
            case 4:
                itemDetail.add(printRecordArrayList.get(i).getIc());
                break;
            default:
                itemDetail.add(printRecordArrayList.get(i).getDate());
        }
        return itemDetail;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void previewPdf() {
        try {
            PackageManager packageManager = getPackageManager();
            Intent testIntent = new Intent(Intent.ACTION_VIEW);
            testIntent.setType("application/pdf");
            List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
            if (list.size() > 0) {
                Uri uri = FileProvider.getUriForFile(this, getResources().getString(R.string.file_provider_authority), pdfFile);
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");

                startActivity(intent);
            } else {
                CustomToast(getApplicationContext(), "Download a PDF Viewer to see the generated PDF");
            }
        } catch (FileUriExposedException e) {
            CustomToast(getApplicationContext(), "Unable to preview your PDF");
        }
    }

    private void shareFile(File file) {
        Uri uriToImage = FileProvider.getUriForFile(
                this, getResources().getString(R.string.file_provider_authority), file);

        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setStream(uriToImage)
                .getIntent()
                //provide read access
                .setData(uriToImage)
                .setType("text/plain")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(shareIntent);
    }

    /*------------------------------------------------sorting purpose------------------------------------------------------------------*/
    private void openSortingDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("date_start", startDate);
        bundle.putString("date_end", endDate);
        bundle.putString("branch_id", sortingBranchID);

        DialogFragment dialogFragment = new SortingDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    @Override
    public void applySorting(String dateStart, String dateEnd, String branchID) {
        this.startDate = dateStart;
        this.endDate = dateEnd;
        this.sortingBranchID = branchID;

        reset(false);
        fetchOrderList(lastQuery);
    }

    /*--------------------------------------------------------------date----------------------------------------------------------------------------*/
    private String getDate(boolean from) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = (from ? calendar.getActualMinimum(Calendar.DATE) : calendar.get(Calendar.DAY_OF_MONTH));
        return year + "-" + String.format(Locale.getDefault(), "%02d", (month + (from ? 0 : 1))) + "-" + String.format(Locale.getDefault(), "%02d", day);
    }

    /*---------------------------------------------------------------------check user status-------------------------------------------------------------*/
    public void readUserStatus() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                printRecordArrayList = new ArrayList<>();
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read_status", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getApplicationContext())));

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
                                        jsonObject = new JSONObject(jsonObjectLoginResponse);
                                        if (jsonObject.getString("status").equals("1")) {
                                            String status = new JSONArray(jsonObject.getString("user_status")).getJSONObject(0).getString("status");
                                            autoComplete = new JSONArray(jsonObject.getString("user_status")).getJSONObject(0).getString("auto_complete");
                                            temperature = new JSONArray(jsonObject.getString("user_status")).getJSONObject(0).getString("temperature");
                                            ic = new JSONArray(jsonObject.getString("user_status")).getJSONObject(0).getString("ic");

                                            fetchOrderList(lastQuery);
                                            if (status.equals("1")) forceCloseApp();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            setNetworkVisibility(true);
                            CustomToast(getApplicationContext(), "No Network Connection");
                        }
                    } catch (InterruptedException e) {
                        setNetworkVisibility(true);
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        setNetworkVisibility(true);
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        setNetworkVisibility(true);
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void forceCloseApp() {
        final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText("Warning");
        dialog.setContentText("Something Wrong with your account!");
        dialog.setCancelable(false);
        dialog.setConfirmText("Close");
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                finish();
                dialog.dismissWithAnimation();
            }
        });
        dialog.show();
    }
}
