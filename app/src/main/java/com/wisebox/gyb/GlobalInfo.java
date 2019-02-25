package com.wisebox.gyb;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.os.StrictMode;
import android.widget.TextView;

public class GlobalInfo {
	// static public String m_ServerIP = "192.168.1.104/htm";
	// static public boolean b_SupportBluetooth = false;
	//PROD
//	static public String m_ServerIP = "3b53f1-0.sh.1251226507.clb.myqcloud.com/htmprd";// "129.100.250.99/htm";

	//TEST
	static public String m_ServerIP = "115.159.188.190/htmwebtest";//
	final static public String NEW_API_URL = "http://115.159.188.190/htmwstest/" ;
	//static public String m_ServerIP = "123.206.111.21/htmprd";
	static public String m_barcodePrefix = "barcode:";
	static public String m_LoginAccount;
	static public String m_PropID;
	static public String m_RoleCode;
	static public String m_RoleName;
	static public String m_PersonName;
	static public String m_DeptName;
	static public String m_DeptID;
	static public String m_DeptCode = "03023";
	static public String m_HospitalCode;
	static public String m_VerifyCode;
	static public String m_PhoneNumber = "18618618601";
	static public String m_SplitString = "fgf";
	static public String m_CommandSplitString = "commandfgf";
	static public String m_OnOffDutyState = "未上班";
	static public String m_OnOffLineState = "在线";
	static public String m_TmpFileName = "Liontowntobesend.txt";
	static public String m_UserInfoFileName = "Liontownuserinfo.txt";
	static public String m_AdministratorBarcode = "LIONTOWN";
	static public String m_APKID = "48";
	static public String m_APKURL = "http://3b53f1-0.sh.1251226507.clb.myqcloud.com/htmprd/LiontownHTM.apk";
	// static public String m_APKURL =
	// "http://apk.r1.market.hiapk.com/data/upload/2015/04_21/9/com.tencent.qqradio_092505.apk";
	static public int m_nSplitLeft = 3;
	static public int m_nSplitRight = 3;
	static public List<String> lstWaitingTask = new ArrayList<String>();
	static public List<OtherTaskDetailInfo> lstOTDI = new ArrayList<OtherTaskDetailInfo>();

	static public Boolean CheckBarcodeGRGIEqual() {
		Boolean bEqual = false;
		List<String> lstGRBarcode = new ArrayList<String>();
		List<String> lstGIBarcode = new ArrayList<String>();
		for (int i = 0; i < lstOTDI.size(); i++) {
			OtherTaskDetailInfo otdi = lstOTDI.get(i);
			if (otdi.m_strGRGI.equals("GR")) {
				for (int j = 0; j < otdi.m_lstOTDIN.size(); j++) {
					if (otdi.m_lstOTDIN.get(j).m_strItemCode
							.equals("BARCODEVALUE")) {
						lstGRBarcode.add(otdi.m_lstOTDIN.get(j).m_strBarcode);
					}
				}
			} else {
				for (int j = 0; j < otdi.m_lstOTDIN.size(); j++) {
					if (otdi.m_lstOTDIN.get(j).m_strItemCode
							.equals("BARCODEVALUE")) {
						lstGIBarcode.add(otdi.m_lstOTDIN.get(j).m_strBarcode);
					}
				}
			}
		}
		if (lstGRBarcode.size() == lstGIBarcode.size()) {
			Boolean bHave = false;
			for (int i = 0; i < lstGRBarcode.size(); i++) {
				bHave = false;
				for (int j = 0; j < lstGIBarcode.size(); j++) {
					if (lstGRBarcode.get(i).equals(lstGIBarcode.get(j))) {
						bHave = true;
						break;
					}
				}
			}
			if (bHave) {
				for (int i = 0; i < lstGIBarcode.size(); i++) {
					bHave = false;
					for (int j = 0; j < lstGRBarcode.size(); j++) {
						if (lstGIBarcode.get(i).equals(lstGRBarcode.get(j))) {
							bHave = true;
							break;
						}
					}
				}
			}
			bEqual = bHave;
		}
		return bEqual;
	}

	static public void AuthenicateAccessInternet() {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
				.build());

	}

	public static void setEditTextReadOnly(TextView view, boolean bReadOnly) {
		if (view instanceof android.widget.EditText) {
			view.setCursorVisible(bReadOnly); // 设置输入框中的光标不可见
			view.setFocusable(bReadOnly); // 无焦点
			view.setFocusableInTouchMode(bReadOnly); // 触摸时也得不到焦点
		}
	}

	// 扫描打卡。上班或下班时用
	// 如果打卡成功，再改变显示的人员上班/未上班的状态
	public static void UpdateLastLocation(String strLocationCode,
			String strTaskToLocationCode) {
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(GlobalInfo.m_LoginAccount);// 工号
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(strLocationCode);// 上班或下班的标识
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(strTaskToLocationCode);// 上班或下班的标识

		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP
				+ "/appTask.aspx?Method=UpdateLastLocation&Value="
				+ strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		String strResponse = "";
		try {
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);
		} catch (Exception e) {
			return;
		}
	}

	// 扫描打卡。上班或下班时用
	// 如果打卡成功，再改变显示的人员上班/未上班的状态
	public static void GetBarcodeSplitInfo() {
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(GlobalInfo.m_LoginAccount);// 工号

		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP
				+ "/appTask.aspx?Method=GetBarcodeSplitInfo&Value="
				+ strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		String strResponse = "";
		try {
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);
			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				String strTemp = EntityUtils.toString(httpResponse.getEntity())
						.trim();
				if (strTemp.startsWith("SUCCESS")) {
					strTemp = strTemp.substring(
							strTemp.indexOf("SUCCESS:") + 8,
							strTemp.indexOf("SUCCESSEND"));

					String straDetail[] = strTemp
							.split(GlobalInfo.m_CommandSplitString);

					if (straDetail.length > 0) {
						try {
							m_nSplitLeft = Integer.parseInt(straDetail[0]);
						} catch (Exception exp) {

						}
					}
					if (straDetail.length > 1) {
						try {
							m_nSplitRight = Integer.parseInt(straDetail[1]);
						} catch (Exception exp) {

						}
					}
				} else {

				}
			}
		} catch (Exception e) {
			return;
		}
	}
}
