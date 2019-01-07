package com.wisebox.gyb;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class OtherTransferActivity extends Activity implements OnClickListener {
    private EditText tbxBarcode;
    private Button btnNew;
    private List<String> lstGRFromLocationCode = new ArrayList<String>();
    private List<String> lstGIToLocationCode = new ArrayList<String>();
    private List<String> lstAllFromLocationCode = new ArrayList<String>();
    private List<String> lstAllFromLocationName = new ArrayList<String>();
    private List<String> lstAllToLocationCode = new ArrayList<String>();
    private List<String> lstAllToLocationName = new ArrayList<String>();
    private TextView tbxBillNo, tbxFromLocation, tbxToLocation, tbxTargetType,
            tbxTransferMode, tbxETA, tbxNote, tbxTotalGet, tbxTotalSet,
            tbxTotalLeft;
    private PlayRingThread playThread;
    public String m_BillNo = "", m_TargetType = "", m_FromLocationCode = "",
            m_State = "";
    private String strFromLocationCode, strToLocationCode;
    Button btnScan;
    private int REQUEST_CODE_SCAN = 299;
    private int REQUEST_CODE_SELECTCANCELREASON=399;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_transfer);

        GlobalInfo.lstOTDI = new ArrayList<OtherTaskDetailInfo>();
        View btnDeal = this.findViewById(R.id.ot_btn_Deal);
        btnDeal.setOnClickListener(this);
        setPlayThread(new PlayRingThread(this));
        tbxBarcode = (EditText) findViewById(R.id.ot_tbxBarcode);
        tbxBarcode.setOnKeyListener(onKey);
        btnNew = (Button) this.findViewById(R.id.ot_btn_NewTask);
        btnNew.setOnClickListener(this);

        Intent intent = getIntent();
        m_BillNo = intent.getStringExtra("billNo");

        btnScan = (Button) findViewById(R.id.pt_btn_Scan);
        btnScan.setOnClickListener(this);

        m_FromLocationCode = intent.getStringExtra("fromLocationCode");
        tbxBillNo = (TextView) this.findViewById(R.id.ot_BillNo);
        tbxFromLocation = (TextView) this.findViewById(R.id.ot_FromLocation);
        tbxToLocation = (TextView) this.findViewById(R.id.ot_ToLocation);
        tbxTargetType = (TextView) this.findViewById(R.id.ot_TargetType);
        tbxTargetType.setVisibility(View.GONE);
        tbxTransferMode = (TextView) this.findViewById(R.id.ot_TransferMode);
        tbxETA = (TextView) this.findViewById(R.id.ot_ETA);
        tbxNote = (TextView) this.findViewById(R.id.ot_Note);

        tbxTotalGet = (TextView) this.findViewById(R.id.ot_GRTotalQuantity);
        tbxTotalSet = (TextView) this.findViewById(R.id.ot_GITotalQuantity);
        tbxTotalLeft = (TextView) this.findViewById(R.id.ot_TotalOnHand);
        tbxTotalSet.setVisibility(View.GONE);
        tbxTotalLeft.setVisibility(View.GONE);

        tbxBillNo.setText("单据号：" + m_BillNo);

        Button btnCancel = (Button) findViewById(R.id.ot_btn_Cancel);
        btnCancel.setOnClickListener(this);

        if (LoadBillInfo()) {
            DealFromLocationColor();
            DealToLocationColor();
            if (m_FromLocationCode.length() > 0) {
                DealBarcode();
            }
        }
    }

    OnKeyListener onKey = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                DealBarcode();
                return true;
            }
            return false;
        }
    };

    private void DealFromLocationColor() {
        String strFromLocation = tbxFromLocation.getText().toString();

        SpannableString ss = new SpannableString(strFromLocation);
        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
            OtherTaskDetailInfo otdi = GlobalInfo.lstOTDI.get(i);
            if (otdi.m_strGRGI.equals("GR") && otdi.m_bArrived) {
                int nIndex = strFromLocation.indexOf(otdi.m_strDeptName);
                if (nIndex >= 0) {
                    ss.setSpan(new ForegroundColorSpan(Color.GREEN), nIndex,
                            nIndex + otdi.m_strDeptName.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        /*
         * for (int i = 0; i < lstGRFromLocationCode.size(); i++) { for (int j =
         * 0; j < lstAllFromLocationCode.size(); j++) { if
         * (lstGRFromLocationCode.get(i).toString()
         * .endsWith(lstAllFromLocationCode.get(j).toString())) { int nIndex =
         * strFromLocation.indexOf(lstAllFromLocationName .get(j).toString());
         * ss.setSpan(new ForegroundColorSpan(Color.GREEN), nIndex, nIndex +
         * lstAllFromLocationName.get(j).toString() .length(),
         * Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); break; } } }
         */
        tbxFromLocation.setText(ss);
    }

    private void DealToLocationColor() {
        String strToLocation = tbxToLocation.getText().toString();

        SpannableString ss = new SpannableString(strToLocation);
        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
            OtherTaskDetailInfo otdi = GlobalInfo.lstOTDI.get(i);
            if (otdi.m_strGRGI.equals("GI") && otdi.m_bArrived) {
                int nIndex = strToLocation.indexOf(otdi.m_strDeptName);
                if (nIndex >= 0) {
                    ss.setSpan(new ForegroundColorSpan(Color.GREEN), nIndex,
                            nIndex + otdi.m_strDeptName.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        /*
         * SpannableString ss = new SpannableString(strToLocation); for (int i =
         * 0; i < lstGIToLocationCode.size(); i++) { for (int j = 0; j <
         * lstAllToLocationCode.size(); j++) { if
         * (lstGIToLocationCode.get(i).toString()
         * .endsWith(lstAllToLocationCode.get(j).toString())) { int nIndex =
         * strToLocation.indexOf(lstAllToLocationName .get(j).toString());
         * ss.setSpan(new ForegroundColorSpan(Color.GREEN), nIndex, nIndex +
         * lstAllToLocationName.get(j).toString() .length(),
         * Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); break; } } }
         */
        tbxToLocation.setText(ss);
    }

    private boolean LoadBillInfo() {
        lstAllFromLocationCode = new ArrayList<String>();
        lstAllFromLocationName = new ArrayList<String>();
        lstAllToLocationCode = new ArrayList<String>();
        lstAllToLocationName = new ArrayList<String>();
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(m_BillNo);// 登陆人身份

        String strUploadData = sbUploadData.toString();

        String strURI = "http://" + GlobalInfo.m_ServerIP
                + "/appTask.aspx?Method=GetTask&Value=" + strUploadData;
        Toast.makeText(this, strURI, Toast.LENGTH_SHORT);
        HttpPost httpRequest = new HttpPost(strURI);

        String strResponse = "";
        try {
            new ArrayList<NameValuePair>();
            // 发出HTTP request, 取得HTTP response
            HttpResponse httpResponse = new DefaultHttpClient()
                    .execute(httpRequest);

            // 若状态码为200 ==> OK
            int nStatus = httpResponse.getStatusLine().getStatusCode();
            if (nStatus == 200) {
                strResponse = EntityUtils.toString(httpResponse.getEntity())
                        .trim();

                if (strResponse.startsWith("SUCCESS")) {
                    strResponse = strResponse.substring(
                            strResponse.indexOf("SUCCESS:") + 8,
                            strResponse.indexOf("SUCCESSEND"));

                    if (strResponse == null || strResponse.equals("")
                            || strResponse.length() == 0) {
                        finish();
                    }

                    String straTask[] = strResponse.split("\n");

                    String straDetail[] = straTask[0]
                            .split(GlobalInfo.m_SplitString);

                    tbxTransferMode.setText("模式:" + straDetail[1] + " 类型:"
                            + straDetail[2]);
                    // tbxTargetType.setText("类型:" + straDetail[2]);
                    tbxFromLocation.setText("起始：" + straDetail[3]);
                    String straDetail3[] = straDetail[3].split(",");
                    for (int i = 0; i < straDetail3.length; i++) {
                        lstAllFromLocationName.add(straDetail3[i]);
                        OtherTaskDetailInfo otdi = new OtherTaskDetailInfo();
                        otdi.m_strDeptName = straDetail3[i];
                        otdi.m_strGRGI = "GR";
                        otdi.m_bArrived = false;
                        GlobalInfo.lstOTDI.add(otdi);
                    }

                    tbxToLocation.setText("目的：" + straDetail[7]);
                    String straDetail4[] = straDetail[7].split(",");
                    for (int i = 0; i < straDetail4.length; i++) {
                        lstAllToLocationName.add(straDetail4[i]);
                        OtherTaskDetailInfo otdi = new OtherTaskDetailInfo();
                        otdi.m_strDeptName = straDetail4[i];
                        otdi.m_strGRGI = "GI";
                        otdi.m_bArrived = false;
                        GlobalInfo.lstOTDI.add(otdi);
                    }

                    tbxETA.setText("要求时间：" + straDetail[8]);
                    tbxNote.setText("备注：" + straDetail[13]);
                    tbxTotalGet.setText("收:" + straDetail[15] + " 发:"
                            + straDetail[16]);// + " 余:" + straDetail[17] +
                    // " 退:"
                    // + straDetail[18]);
                    // tbxTotalSet.setText("发:" + straDetail[16]);
                    // tbxTotalLeft.setText("余:" + straDetail[17]);
                    strFromLocationCode = straDetail[11];
                    String straDetail2[] = strFromLocationCode.split(",");
                    for (int i = 0; i < straDetail2.length; i++) {
                        lstAllFromLocationCode.add(straDetail2[i]);
                        GlobalInfo.lstOTDI.get(i).m_strDeptCode = straDetail2[i];
                    }

                    strToLocationCode = straDetail[12];
                    String straDetail5[] = strToLocationCode.split(",");
                    for (int i = 0; i < straDetail5.length; i++) {
                        lstAllToLocationCode.add(straDetail5[i]);
                        GlobalInfo.lstOTDI.get(straDetail2.length + i).m_strDeptCode = straDetail5[i];
                    }

                    m_TargetType = straDetail[2];
                    if (!m_TargetType.endsWith("标本"))
                        btnNew.setVisibility(View.GONE);

                    m_State = straDetail[14];

                    // 开始得到任务的所有明细
                    strURI = "http://" + GlobalInfo.m_ServerIP
                            + "/appTask.aspx?Method=GetOtherTaskDetail&Value="
                            + strUploadData + GlobalInfo.m_SplitString;
                    httpRequest = new HttpPost(strURI);
                    httpResponse = new DefaultHttpClient().execute(httpRequest);
                    nStatus = httpResponse.getStatusLine().getStatusCode();
                    if (nStatus == 200) {
                        strResponse = EntityUtils.toString(
                                httpResponse.getEntity()).trim();

                        if (strResponse.startsWith("SUCCESS")) {
                            strResponse = strResponse.substring(
                                    strResponse.indexOf("SUCCESS:") + 8,
                                    strResponse.indexOf("SUCCESSEND"));

                            if (strResponse == null || strResponse.equals("")
                                    || strResponse.length() == 0) {
                                return true;
                            }

                            String stra[] = strResponse
                                    .split(GlobalInfo.m_SplitString);
                            for (int i = 0; i < stra.length; i += 6) {
                                for (int j = 0; j < GlobalInfo.lstOTDI.size(); j++) {
                                    OtherTaskDetailInfo otdi = GlobalInfo.lstOTDI
                                            .get(j);
                                    if (otdi.m_strDeptCode.equals(stra[i + 3])) {
                                        otdi.m_bArrived = true;
                                        OtherTaskDetailInfoNode otdin = new OtherTaskDetailInfoNode();
                                        otdin.m_strItemCode = stra[i + 1];
                                        if (stra[i + 1].equals("BARCODEVALUE")) {
                                            otdin.m_strBarcode = stra[i + 5];
                                            otdin.m_nQuantity = 1;
                                        } else {
                                            try {
                                                otdin.m_nQuantity = Integer
                                                        .parseInt(stra[i + 5]);
                                            } catch (Exception exp) {
                                                otdin.m_nQuantity = 0;
                                            }
                                        }
                                        otdin.m_strOperatorID = stra[i + 4];
                                        otdi.m_lstOTDIN.add(otdin);
                                    }
                                }
                            }
                        }
                    }
                    return true;

                } else if (strResponse.startsWith("ERROR")) {
                    strResponse = strResponse.substring(
                            strResponse.indexOf("ERROR:") + 6,
                            strResponse.indexOf("ERROREND"));
                    new AlertDialog.Builder(this).setTitle("确认")
                            .setMessage(strResponse)
                            .setPositiveButton("确定", null).show();
                    return false;
                }
            } else {
                new AlertDialog.Builder(this).setTitle("确认").setMessage("错误")
                        .setPositiveButton("确定", null).show();
            }
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("获取任务过程出现异常" + e.getMessage())
                    .setPositiveButton("确定", null).show();
            return false;
        }

        return false;
    }

    @SuppressLint("ShowToast")
    private void GetOtherTaskGRDetail() {
        lstGRFromLocationCode = new ArrayList<String>();
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(m_BillNo);// 登陆人身份
        String strUploadData = sbUploadData.toString();

        String strURI = "http://" + GlobalInfo.m_ServerIP
                + "/appTask.aspx?Method=GetOtherTaskGRDetail&Value="
                + strUploadData;
        Toast.makeText(this, strURI, Toast.LENGTH_SHORT);
        HttpPost httpRequest = new HttpPost(strURI);

        String strResponse = "";
        try {
            new ArrayList<NameValuePair>();
            // 发出HTTP request, 取得HTTP response
            HttpResponse httpResponse = new DefaultHttpClient()
                    .execute(httpRequest);

            // 若状态码为200 ==> OK
            int nStatus = httpResponse.getStatusLine().getStatusCode();
            if (nStatus == 200) {
                strResponse = EntityUtils.toString(httpResponse.getEntity())
                        .trim();

                if (strResponse.startsWith("SUCCESS")) {
                    strResponse = strResponse.substring(
                            strResponse.indexOf("SUCCESS:") + 8,
                            strResponse.indexOf("SUCCESSEND"));
                    if (strResponse.length() == 0)
                        return;
                    String straDetail[] = strResponse
                            .split(GlobalInfo.m_SplitString);
                    for (int i = 0; i < straDetail.length; i++) {
                        if (straDetail[i].length() == 0)
                            continue;
                        lstGRFromLocationCode.add(straDetail[i]);
                        for (int j = 0; j < GlobalInfo.lstOTDI.size(); j++) {
                            if (GlobalInfo.lstOTDI.get(j).m_strDeptCode
                                    .equals(straDetail[i]))
                                GlobalInfo.lstOTDI.get(j).m_bArrived = true;
                        }
                    }

                    if (lstGRFromLocationCode.size() == lstAllFromLocationCode
                            .size() || !m_TargetType.endsWith("标本")) {
                    }
                } else if (strResponse.startsWith("ERROR")) {
                    strResponse = strResponse.substring(
                            strResponse.indexOf("ERROR:") + 6,
                            strResponse.indexOf("ERROREND"));
                    new AlertDialog.Builder(this).setTitle("确认")
                            .setMessage(strResponse)
                            .setPositiveButton("确定", null).show();
                }
            } else {
                new AlertDialog.Builder(this).setTitle("确认").setMessage("错误")
                        .setPositiveButton("确定", null).show();
            }
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("获取任务过程出现异常" + e.getMessage())
                    .setPositiveButton("确定", null).show();
            return;
        }
    }

    @SuppressLint("ShowToast")
    private void GetOtherTaskGIDetail() {
        lstGIToLocationCode = new ArrayList<String>();
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(m_BillNo);// 登陆人身份
        String strUploadData = sbUploadData.toString();

        String strURI = "http://" + GlobalInfo.m_ServerIP
                + "/appTask.aspx?Method=GetOtherTaskGIDetail&Value="
                + strUploadData;
        Toast.makeText(this, strURI, Toast.LENGTH_SHORT);
        HttpPost httpRequest = new HttpPost(strURI);

        String strResponse = "";
        try {
            new ArrayList<NameValuePair>();
            // 发出HTTP request, 取得HTTP response
            HttpResponse httpResponse = new DefaultHttpClient()
                    .execute(httpRequest);

            // 若状态码为200 ==> OK
            int nStatus = httpResponse.getStatusLine().getStatusCode();
            if (nStatus == 200) {
                strResponse = EntityUtils.toString(httpResponse.getEntity())
                        .trim();

                if (strResponse.startsWith("SUCCESS")) {
                    strResponse = strResponse.substring(
                            strResponse.indexOf("SUCCESS:") + 8,
                            strResponse.indexOf("SUCCESSEND"));

                    if (strResponse.length() == 0)
                        return;

                    String straDetail[] = strResponse
                            .split(GlobalInfo.m_SplitString);
                    for (int i = 0; i < straDetail.length; i++) {
                        if (straDetail[i].length() == 0)
                            continue;
                        lstGIToLocationCode.add(straDetail[i]);
                        for (int j = 0; j < GlobalInfo.lstOTDI.size(); j++) {
                            if (GlobalInfo.lstOTDI.get(j).m_strDeptCode
                                    .equals(straDetail[i]))
                                GlobalInfo.lstOTDI.get(j).m_bArrived = true;
                        }
                    }

                    /*
                     * if (lstGIToLocationCode.size() == lstAllToLocationCode
                     * .size()||!m_TargetType.endsWith("标本")) bCanFinish = true;
                     */
                } else if (strResponse.startsWith("ERROR")) {
                    strResponse = strResponse.substring(
                            strResponse.indexOf("ERROR:") + 6,
                            strResponse.indexOf("ERROREND"));
                    new AlertDialog.Builder(this).setTitle("确认")
                            .setMessage(strResponse)
                            .setPositiveButton("确定", null).show();
                }
            } else {
                new AlertDialog.Builder(this).setTitle("确认").setMessage("错误")
                        .setPositiveButton("确定", null).show();
            }
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("获取任务过程出现异常" + e.getMessage())
                    .setPositiveButton("确定", null).show();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.other_transfer, menu);
        return true;
    }

    private void DealBarcode() {
        String strBarcode = tbxBarcode.getText().toString().trim();
        tbxBarcode.setText("");

        if (strBarcode.contains(",") || strBarcode.length() == 0) {
            return;
        }

        if (strFromLocationCode.contains(strBarcode)) {
            // 对于扫描的是起点条码，先更新登陆人的最后到达科室
            // 在判断任务是否处于执行中的状态，如果不是执行中，则先把任务变为执行中
            // 再调出输入数量的界面
            GlobalInfo.UpdateLastLocation(strBarcode, strToLocationCode);
            if (m_State.equals("执行中")) {
            } else {
                StringBuilder sbUploadData = new StringBuilder("");
                sbUploadData.append(m_BillNo);// 单号
                sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
                sbUploadData.append(GlobalInfo.m_LoginAccount);// 人员
                sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
                sbUploadData.append(strBarcode);// 科室编码
                sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
                sbUploadData.append("START");// 开始该任务

                String strUploadData = sbUploadData.toString();
                String strURI = "http://" + GlobalInfo.m_ServerIP
                        + "/appTask.aspx?Method=UpdateTask&Value="
                        + strUploadData;
                HttpPost httpRequest = new HttpPost(strURI);
                String strResponse = "";
                try {
                    HttpResponse httpResponse = new DefaultHttpClient()
                            .execute(httpRequest);

                    // 若状态码为200 ==> OK
                    int nStatus = httpResponse.getStatusLine().getStatusCode();
                    if (nStatus == 200) {
                        strResponse = EntityUtils.toString(
                                httpResponse.getEntity()).trim();
                        if (strResponse.startsWith("SUCCESS")) {
                        } else if (strResponse.startsWith("ERROR")) {
                            strResponse = strResponse.substring(
                                    strResponse.indexOf("ERROR:") + 8,
                                    strResponse.indexOf("ERROREND"));
                            new AlertDialog.Builder(this).setTitle("确认")
                                    .setMessage(strResponse)
                                    .setPositiveButton("确定", null).show();
                            return;
                        }
                    }
                } catch (Exception e) {
                    new AlertDialog.Builder(this).setTitle("确认")
                            .setMessage("获取任务过程出现异常" + e.getMessage())
                            .setPositiveButton("确定", null).show();
                    return;
                }
            }

            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("deptcode", strBarcode);
            bundle.putString("grgi", "GR");
            bundle.putString("transferType", m_TargetType);
            bundle.putString("billno", m_BillNo);
            intent.putExtras(bundle);
            intent.setClass(OtherTransferActivity.this,
                    OtherTransferDetailActivity.class);
            startActivityForResult(intent, 2);
        } else if (strToLocationCode.contains(strBarcode)) {
            GlobalInfo.UpdateLastLocation(strBarcode, strToLocationCode);

            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("deptcode", strBarcode);
            bundle.putString("grgi", "GI");
            bundle.putString("transferType", m_TargetType);
            bundle.putString("billno", m_BillNo);
            intent.putExtras(bundle);

            intent.setClass(OtherTransferActivity.this,
                    OtherTransferDetailActivity.class);

            startActivityForResult(intent, 2);

            /*
             * if (!m_TargetType.equals("标本")) { Intent intent = new Intent();
             * Bundle bundle = new Bundle(); bundle.putString("deptcode",
             * strBarcode); bundle.putString("grgi", "GI");
             * bundle.putString("transferType", m_TargetType);
             * bundle.putString("billno", m_BillNo); intent.putExtras(bundle);
             *
             * intent.setClass(OtherTransferActivity.this,
             * OtherTransferDetailActivity.class);
             *
             * startActivityForResult(intent, 2); return; } else { //
             * 判断是否有条码，如果有条码，则必须扫描条码才能完工。否则可以直接完工 boolean bHaveBarcode = false;
             * bHaveBarcode=GlobalInfo.CheckBarcodeGRGIEqual(); if
             * (!bHaveBarcode) { Intent intent = new Intent();
             *
             * Bundle bundle = new Bundle(); bundle.putString("deptcode",
             * strBarcode); bundle.putString("grgi", "GI");
             * bundle.putString("transferType", m_TargetType);
             * bundle.putString("billno", m_BillNo); intent.putExtras(bundle);
             *
             * intent.setClass(OtherTransferActivity.this,
             * OtherTransferDetailActivity.class);
             *
             * startActivityForResult(intent, 3); } else { //
             * tbxToLocation.setTextColor(Color.GREEN); StringBuilder
             * sbUploadData = new StringBuilder("");
             * sbUploadData.append(m_BillNo);// 单号
             * sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
             * sbUploadData.append(GlobalInfo.m_LoginAccount);// 人员
             * sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
             * sbUploadData.append("");// 登陆人身份
             * sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
             * sbUploadData.append("END" + strBarcode);// 分隔符 //
             * sbUploadData.append(GlobalInfo.m_SplitString);//分隔符 //
             * sbUploadData.append(GlobalInfo.m_VerifyCode); String
             * strUploadData = sbUploadData.toString();
             *
             * String strURI = "http://" + GlobalInfo.m_ServerIP +
             * "/appTask.aspx?Method=UpdateTask&Value=" + strUploadData;
             * HttpPost httpRequest = new HttpPost(strURI);
             *
             * // new AlertDialog.Builder(this).setTitle("确认" //
             * ).setMessage(strURI).setPositiveButton("确定", null // ).show();
             * String strResponse = ""; try { new ArrayList<NameValuePair>();
             *
             * // httpRequest.setEntity(new // UrlEncodedFormEntity(params, //
             * HTTP.UTF_8)); // 发出HTTP request, 取得HTTP response HttpResponse
             * httpResponse = new DefaultHttpClient() .execute(httpRequest);
             *
             * // 若状态码为200 ==> OK int nStatus = httpResponse.getStatusLine()
             * .getStatusCode(); if (nStatus == 200) { strResponse =
             * EntityUtils.toString( httpResponse.getEntity()).trim(); if
             * (strResponse.startsWith("SUCCESS")) { if (playThread != null)
             * playThread.StopAlarmRing(); playThread = new
             * PlayRingThread(this); playThread.strType = "finish";
             * playThread.start(); new AlertDialog.Builder(this).setTitle("确认")
             * .setMessage("本任务已完工，请执行下一任务") .setPositiveButton("确定",
             * null).show(); finish(); return; } else if
             * (strResponse.startsWith("ERROR")) { strResponse =
             * strResponse.substring( strResponse.indexOf("ERROR:") + 8,
             * strResponse.indexOf("ERROREND")); new
             * AlertDialog.Builder(this).setTitle("确认") .setMessage(strResponse)
             * .setPositiveButton("确定", null).show(); } } } catch (Exception e)
             * { new AlertDialog.Builder(this).setTitle("确认")
             * .setMessage("获取任务过程出现异常" + e.getMessage())
             * .setPositiveButton("确定", null).show(); return; } } }
             */
        }
    }

    private void ChangeState(String strReason, String strState) {
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(m_BillNo);// 单号
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(GlobalInfo.m_LoginAccount);// 人员
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(strReason);// 登陆人身份
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(strState);// 分隔符
        // sbUploadData.append(GlobalInfo.m_SplitString);//分隔符
        // sbUploadData.append(GlobalInfo.m_VerifyCode);
        String strUploadData = sbUploadData.toString();

        String strURI = "http://" + GlobalInfo.m_ServerIP
                + "/appTask.aspx?Method=UpdateTask&Value=" + strUploadData;
        HttpPost httpRequest = new HttpPost(strURI);

        // new AlertDialog.Builder(this).setTitle("确认"
        // ).setMessage(strURI).setPositiveButton("确定", null ).show();
        String strResponse = "";
        try {
            new ArrayList<NameValuePair>();

            // httpRequest.setEntity(new UrlEncodedFormEntity(params,
            // HTTP.UTF_8));
            // 发出HTTP request, 取得HTTP response
            HttpResponse httpResponse = new DefaultHttpClient()
                    .execute(httpRequest);

            // 若状态码为200 ==> OK
            int nStatus = httpResponse.getStatusLine().getStatusCode();
            if (nStatus == 200) {
                strResponse = EntityUtils.toString(httpResponse.getEntity())
                        .trim();
                // new AlertDialog.Builder(this).setTitle("确认"
                // ).setMessage(strResponse).setPositiveButton("确定",
                // null ).show();
                if (strResponse.startsWith("SUCCESS")) {
                    new AlertDialog.Builder(this).setTitle("确认")
                            .setMessage("本任务已结束，请执行下一任务")
                            .setPositiveButton("确定", null).show();
                    finish();
                    return;
                } else if (strResponse.startsWith("ERROR")) {
                    strResponse = strResponse.substring(
                            strResponse.indexOf("ERROR:") + 6,
                            strResponse.indexOf("ERROREND"));
                    new AlertDialog.Builder(this).setTitle("确认")
                            .setMessage(strResponse)
                            .setPositiveButton("确定", null).show();
                }
            }
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("获取任务过程出现异常" + e.getMessage())
                    .setPositiveButton("确定", null).show();
            return;
        }
    }

    @SuppressLint("ShowToast")
    @Override
    /**
     * 当跳转的activity(被激活的activity)使用完毕,销毁的时候调用该方法
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == REQUEST_CODE_SCAN) {
                if (data != null) {
                    String content = data.getStringExtra("codedContent");
                    tbxBarcode.setText(content);
                    DealBarcode();
                }
            }
            else if(requestCode==REQUEST_CODE_SELECTCANCELREASON) {
                if (data != null) {
                    String name = data.getStringExtra("name");
                    if (name != null && name.length() > 0) {
                        if (requestCode == REQUEST_CODE_SELECTCANCELREASON) {// 返回了取消原因
                            ConfirmAcceptTaskActivity dlg = new ConfirmAcceptTaskActivity(
                                    this, "确认取消该任务吗？原因：" + name, "确认", "取消");
                            int nReturn = dlg.showDialog();
                            if (nReturn == 0) {
                                Toast.makeText(getApplicationContext(), "取消该任务",
                                        Toast.LENGTH_SHORT).show();
                                ChangeState(name, "CANCEL");
                            }
                        } else if (requestCode == 2) { // 返回了延迟原因
                            ConfirmAcceptTaskActivity dlg = new ConfirmAcceptTaskActivity(
                                    this, "确认延迟该任务吗？原因：" + name, "确认", "取消");
                            int nReturn = dlg.showDialog();
                            if (nReturn == 0) {
                                Toast.makeText(getApplicationContext(), "延迟该任务",
                                        Toast.LENGTH_SHORT).show();
                                ChangeState(name, "DELAY");
                            }
                        }
                    }
                }
            }
            else if (requestCode == 2) {
                String name = data.getStringExtra("result");
                String mode = data.getStringExtra("mode");
                if (name.equals("success")) {
                    // 判断是否至少去过一个科室，如果去过一个科室再判断每种物品的收的数量和发的数量是否一致，以及是否所有收的条码都已经发放
                    // 如果这样则可以结束任务
                    Integer nTotalGR = 0, nTotalGI = 0;
                    boolean bHaveOneDept = false, bCanFinish = false;
                    for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
                        if (GlobalInfo.lstOTDI.get(i).m_bArrived) {
                            bHaveOneDept = true;
                            break;
                        }
                    }
                    if (bHaveOneDept) {
                        Integer[] nGRQuantity = new Integer[6];
                        String[] strGRCode = new String[6];
                        Integer nMax = 0;
                        List<String> lstBarcode = new ArrayList<String>();

                        for (int i = 0; i < 6; i++) {
                            nGRQuantity[i] = 0;
                            strGRCode[i] = "";
                        }

                        // 得到每种收货标本种类的编码和总的收货数量
                        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
                            if (GlobalInfo.lstOTDI.get(i).m_strGRGI
                                    .equals("GR")) {
                                boolean bHave = false;
                                for (int k = 0; k < GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                        .size(); k++) {
                                    bHave = false;
                                    String strItemCode = GlobalInfo.lstOTDI
                                            .get(i).m_lstOTDIN.get(k).m_strItemCode;
                                    if (strItemCode.equals("BARCODEVALUE")) {
                                        lstBarcode
                                                .add(GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                                        .get(k).m_strBarcode);
                                        nTotalGR++;
                                        continue;
                                    }
                                    if (strItemCode.equals("RETURN")
                                            || strItemCode.equals("BARCODE")) {
                                        continue;
                                    }
                                    nTotalGR += GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                            .get(k).m_nQuantity;
                                    for (int j = 0; j < 6; j++) {
                                        if (strItemCode.equals(strGRCode[j])) {
                                            bHave = true;
                                            nGRQuantity[j] += GlobalInfo.lstOTDI
                                                    .get(i).m_lstOTDIN.get(k).m_nQuantity;
                                        }
                                    }
                                    if (!bHave) {
                                        if (strItemCode.equals("RETURN")
                                                || strItemCode
                                                .equals("BARCODE")
                                                || strItemCode
                                                .equals("BARCODEVALUE")) {
                                        } else {
                                            strGRCode[nMax] = GlobalInfo.lstOTDI
                                                    .get(i).m_lstOTDIN.get(k).m_strItemCode;
                                            nGRQuantity[nMax] = GlobalInfo.lstOTDI
                                                    .get(i).m_lstOTDIN.get(k).m_nQuantity;
                                            nMax++;
                                        }
                                    }
                                }
                            }
                        }

                        // 开始校验每种种类的标本收货数量与发货数量是否相等，以及收的条码是否已经发货
                        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
                            if (GlobalInfo.lstOTDI.get(i).m_strGRGI
                                    .equals("GI")) {
                                for (int k = 0; k < GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                        .size(); k++) {
                                    String strItemCode = GlobalInfo.lstOTDI
                                            .get(i).m_lstOTDIN.get(k).m_strItemCode;
                                    boolean bHave = false;
                                    if (strItemCode.equals("BARCODEVALUE")) {
                                        bHave = false;
                                        for (int j = 0; j < lstBarcode.size(); j++) {
                                            if (GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                                    .get(k).m_strBarcode
                                                    .equals(lstBarcode.get(j))) {
                                                bHave = true;
                                                lstBarcode.remove(j);
                                                break;
                                            }
                                        }
                                        nTotalGI++;
                                        //if (bHave)
                                        continue;
                                    }
                                    if (strItemCode.equals("RETURN")
                                            || strItemCode.equals("BARCODE")) {
                                        continue;
                                    }
                                    nTotalGI += GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                            .get(k).m_nQuantity;

                                    for (int j = 0; j < 6; j++) {
                                        if (strItemCode.equals(strGRCode[j])) {
                                            nGRQuantity[j] -= GlobalInfo.lstOTDI
                                                    .get(i).m_lstOTDIN.get(k).m_nQuantity;
                                        }
                                    }
                                }
                            }
                        }

                        bCanFinish = true;
                        // 判断是否所有收的标本数量与发的都相等
                        for (int i = 0; i < 6; i++) {
                            if (nGRQuantity[i] > 0) {
                                bCanFinish = false;
                                break;
                            }
                        }
                        if (lstBarcode.size() > 0)
                            bCanFinish = false;

                        if (bCanFinish && mode.equals("GI")) {
                            Finish("");
                            return;
                        }
                    }

                    tbxTotalGet.setText("收:" + nTotalGR + " 发:" + nTotalGI);
                    DealFromLocationColor();
                    DealToLocationColor();
                } else if (name.equals("osuccess")) {
                    finish();
                } else if (name.equals("ofailure")) {
                    LoadBillInfo();
                    GetOtherTaskGRDetail();
                    GetOtherTaskGIDetail();
                    DealFromLocationColor();
                    DealToLocationColor();
                }
            } else if (requestCode == 3) {
                String name = data.getStringExtra("result");

                /*
                 * new AlertDialog.Builder(this).setTitle("确认")
                 * .setMessage("12345678"+name) .setPositiveButton("确定",
                 * null).show();
                 */

                if (name.equals("success")) {
                    finish();
                }
            }
        }
    }

    private void Finish(String strReason) {
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(m_BillNo);// 单号
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(GlobalInfo.m_LoginAccount);// 人员
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(strReason);// 登陆人身份
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append("END");// 分隔符
        // sbUploadData.append(GlobalInfo.m_SplitString);//分隔符
        // sbUploadData.append(GlobalInfo.m_VerifyCode);
        String strUploadData = sbUploadData.toString();

        String strURI = "http://" + GlobalInfo.m_ServerIP
                + "/appTask.aspx?Method=UpdateTask&Value=" + strUploadData;
        HttpPost httpRequest = new HttpPost(strURI);

        // new AlertDialog.Builder(this).setTitle("确认"
        // ).setMessage(strURI).setPositiveButton("确定", null
        // ).show();
        String strResponse = "";
        try {
            new ArrayList<NameValuePair>();

            // httpRequest.setEntity(new
            // UrlEncodedFormEntity(params,
            // HTTP.UTF_8));
            // 发出HTTP request, 取得HTTP response
            HttpResponse httpResponse = new DefaultHttpClient()
                    .execute(httpRequest);

            // 若状态码为200 ==> OK
            int nStatus = httpResponse.getStatusLine().getStatusCode();
            if (nStatus == 200) {
                strResponse = EntityUtils.toString(httpResponse.getEntity())
                        .trim();
                if (strResponse.startsWith("SUCCESS")) {
                    PlayRingThread playThread = new PlayRingThread(this);
                    playThread.strType = "finish";
                    playThread.start();

                    finish();
                    return;
                } else if (strResponse.startsWith("ERROR")) {
                    strResponse = strResponse.substring(
                            strResponse.indexOf("ERROR:") + 6,
                            strResponse.indexOf("ERROREND"));
                    new AlertDialog.Builder(this).setTitle("确认")
                            .setMessage(strResponse)
                            .setPositiveButton("确定", null).show();
                }
            }
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("结束任务过程出现异常" + e.getMessage())
                    .setPositiveButton("确定", null).show();
            return;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ot_btn_Deal:
                DealBarcode();
                break;
            case R.id.ot_btn_Cancel:
                Intent intent = new Intent(this, SelectReasonActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("reasonType", "取消原因");
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_CODE_SELECTCANCELREASON);// 请求码
                break;
            case R.id.ot_btn_NewTask:
                Intent intent1 = new Intent(this, NewSampleTaskActivity.class);
                startActivity(intent1);
            case R.id.pt_btn_Scan:
                //替换为新的二维码构建工具
                startActivityForResult(new Intent(OtherTransferActivity.this, CaptureActivity.class), REQUEST_CODE_SCAN);
                break;
        }
    }

    public void onPause() {
        super.onPause();
    }

    public synchronized void onResume() {
        super.onResume();
        tbxBarcode.requestFocus();
    }

    public PlayRingThread getPlayThread() {
        return playThread;
    }

    public void setPlayThread(PlayRingThread playThread) {
        this.playThread = playThread;
    }
}
