package com.channelsoft.sop.shareObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by wypan on 2/14/2017.
 */

public class AsyncTaskManager extends AsyncTask<String, String, String> {

    private Context context;
    private String url;
    private String dataPost;
    private String method;

    public AsyncTaskManager(Context context, String method, String url, String dataPost) {
        this.context = context;
        this.method = method;
        this.url = url;
        this.dataPost = dataPost;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (NetworkUtils.getConnectivityStatus(this.context) == NetworkUtils.TYPE_NOT_CONNECTED)
            cancel(true);
    }

    @Override
    protected String doInBackground(String... params) {
        return new JSONParser().getJSONFromUrl(this.context, this.method, this.url, this.dataPost);
    }

    @Override
    protected void onPostExecute(String jsonObject) {
        super.onPostExecute(jsonObject);
    }

    @Override
    protected void onCancelled(String jsonObject) {
        super.onCancelled(jsonObject);
        Toast.makeText(this.context, "Please Connect To A Network", Toast.LENGTH_SHORT).show();
        Log.i("AsyncTask Cancel", "Connection Timeout");
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

    }
}
