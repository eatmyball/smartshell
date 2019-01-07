package com.wisebox.gyb;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.StrictMode;

public class CommonFunctions {
	static public String m_ServerURL="http://3b53f1-0.sh.1251226507.clb.myqcloud.com/htmprd/";
	static public void AccessInternet()
	{
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads().detectDiskWrites().detectNetwork()
		.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		.detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
		.build());

	}
	
	static public String AccessServer(String strURI)
	{
		HttpPost httpRequest = new HttpPost(strURI);

		String strResponse = "";
		try {
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				strResponse = EntityUtils.toString(httpResponse.getEntity())
						.trim();
				return strResponse;				
			} else {
				return "ERROR:"+nStatus+"ERROREND";
			}
		} catch (Exception e) {
			return "ERROR:"+e.getMessage()+"ERROREND";			
		}
	}
}
