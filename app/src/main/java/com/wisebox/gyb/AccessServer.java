package com.wisebox.gyb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;

/**
 *
 */
public class AccessServer implements Runnable {
    
    private static final int NO_SERVER_ERROR=1000;
    public static final String URL = "fill your own url";
    public final static String ADD = "/add";
    public final static String UPDATE = "/update";
    public final static String PING = "/ping";
    private static int connectionTimeout = 60000;
    private static int socketTimeout = 60000;
    private static HttpClient httpClient=new DefaultHttpClient();
    private static ExecutorService executorService=Executors.newCachedThreadPool();
    private static Handler handler = new Handler();
    private String strResult;
    private HttpPost httpPost;
    private HttpResponse httpResponse;
    private OnReceiveDataListener onReceiveDataListener;
    private int statusCode;

    /**
     */
    public AccessServer() {
        strResult = null;
        httpResponse = null;
        httpPost = new HttpPost();
    }
    
    /**
     * @param listener
     */
    public void setOnReceiveDataListener(OnReceiveDataListener listener) {
        onReceiveDataListener = listener;
    }

    /**
     * @throws UnsupportedEncodingException
     */
    public void iniRequest(String requestType, JSONObject jsonObject) throws UnsupportedEncodingException {
        httpPost.addHeader("Content-Type", "text/json");
        httpPost.addHeader("charset", "UTF-8");

        httpPost.addHeader("Cache-Control", "no-cache");
        HttpParams httpParameters = httpPost.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                connectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParameters, socketTimeout);
        httpPost.setParams(httpParameters);
        try {
            httpPost.setURI(new URI(URL + requestType));
            httpPost.setEntity(new StringEntity(jsonObject.toString(),
                    HTTP.UTF_8));
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        } 
    }
    
    public void init(String strRequest) {
        httpPost.addHeader("Content-Type", "text/json");
        httpPost.addHeader("charset", "UTF-8");

        httpPost.addHeader("Cache-Control", "no-cache");
        
        try {
            httpPost.setURI(new URI(strRequest));            
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        } 
    }

    /**
     */
    public void execute() {
        executorService.execute(this);
    }

    /**
     *
     * @return true is available else false
     */
    public static boolean checkNetState(Activity activity) {
        ConnectivityManager connManager = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    /**
     */
    @Override
    public void run() {
        httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            strResult = EntityUtils.toString(httpResponse.getEntity());
        } catch (ClientProtocolException e1) {
            strResult = null;
            e1.printStackTrace();
        } catch (IOException e1) {
            strResult = null;
            e1.printStackTrace();
        } finally {
            if (httpResponse != null) {
                statusCode = httpResponse.getStatusLine().getStatusCode();
            }
            else
            {
                statusCode=NO_SERVER_ERROR;
            }
            if(onReceiveDataListener!=null)
            {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onReceiveDataListener.onReceiveData(strResult, statusCode,httpPost.getURI().toString());
                    }
                });
            }
        }
    }

    /**
     *
     */
    public interface OnReceiveDataListener {
        /**
         * the callback function for receiving the result data
         * from post request, and further processing will be done here
         * @param strResult the result in string style.
         * @param StatusCode the status of the post
         */
        public abstract void onReceiveData(String strResult,int StatusCode,String strRequest);
    }

}