package com.wisebox.gyb;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.ksoap2.serialization.SoapObject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class PatientActivity extends Activity implements OnClickListener {
    private EditText tbxBarcode;
    private Button btnDeal, btnCancel, btnDelay, btnWait, btnUpdate, btnScan;
    private TextView tbxBillNo, tbxFromLocation, tbxToLocation, tbxPatientName;
    private TextView tbxSickBed, tbxPatientNumber, tbxTargetType,
            tbxTransferMode;
    private TextView tbxEmergencyLevel, tbxTransferEquipment, tbxETA, tbxNote, tbxOriginBillNo;
    private PlayRingThread playThread;
    private String m_BillNo = "", m_FromLocationCode = "", m_State = "";
    private String m_PatientNo = "", m_RealPatientNo = "", m_strPatientNo = "";
    private String strFromLocationCode = "", strToLocationCode = "", strPatientNumber = "";
    private ArrayList<String> straScannedOriginBillNo;
    private int REQUEST_CODE_SCAN = 299;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        straScannedOriginBillNo = new ArrayList<String>();
        InitControl();
        InitBill();
    }

    private void InitControl() {
        tbxBarcode = (EditText) findViewById(R.id.pt_tbxBarcode);
        tbxBarcode.setMovementMethod(ScrollingMovementMethod.getInstance());
        tbxBarcode.setOnKeyListener(onKey);

        btnDeal = (Button) this.findViewById(R.id.pt_btn_Deal);
        btnDeal.setOnClickListener(this);
        btnDeal.requestFocus();

        btnCancel = (Button) this.findViewById(R.id.pt_btn_Cancel);
        btnCancel.setOnClickListener(this);

        btnWait = (Button) this.findViewById(R.id.pt_btn_Wait);
        btnWait.setOnClickListener(this);

        btnScan = (Button) this.findViewById(R.id.pt_btn_Scan);
        btnScan.setOnClickListener(this);

        btnDelay = (Button) this.findViewById(R.id.pt_btn_Delay);
        btnDelay.setOnClickListener(this);

        btnUpdate = (Button) this.findViewById(R.id.pt_btn_Update);
        btnUpdate.setOnClickListener(this);

        tbxBillNo = (TextView) this.findViewById(R.id.pt_BillNo);
        tbxFromLocation = (TextView) this.findViewById(R.id.pt_FromLocation);
        tbxToLocation = (TextView) this.findViewById(R.id.pt_ToLocation);
        tbxPatientName = (TextView) this.findViewById(R.id.pt_PatientName);
        tbxSickBed = (TextView) this.findViewById(R.id.pt_SickBed);
        tbxPatientNumber = (TextView) this.findViewById(R.id.pt_PatientNumber);
        tbxTargetType = (TextView) this.findViewById(R.id.pt_TargetType);
        tbxTransferMode = (TextView) this.findViewById(R.id.pt_TransferMode);
        tbxEmergencyLevel = (TextView) this
                .findViewById(R.id.pt_EmergencyLevel);
        tbxTransferEquipment = (TextView) this
                .findViewById(R.id.pt_TransferEquipment);
        tbxETA = (TextView) this.findViewById(R.id.pt_ETA);
        tbxNote = (TextView) this.findViewById(R.id.pt_Note);
        tbxOriginBillNo = (TextView) findViewById(R.id.pt_OriginBillNo);

        // 暂时限定只有03023才能进行更新
        // 以后需要根据在网页端设定的医院的配置信息来确定是否能更新，而不是写死
        if (!GlobalInfo.m_LoginAccount.startsWith("03023"))
            btnUpdate.setVisibility(View.GONE);

        playThread = new PlayRingThread(this);
    }

    private void InitBill() {
        try {
            // 得到任务列表Activity传过来的订单号和起点Code
            Intent intent = getIntent();
            m_BillNo = intent.getStringExtra("billNo");
            m_FromLocationCode = intent.getStringExtra("fromLocationCode");
            intent.getStringExtra("autostart");

            if (m_BillNo.length() > 0) {
                for (int i = 0; i < GlobalInfo.lstWaitingTask.size(); i++) {
                    if (m_BillNo.equals(GlobalInfo.lstWaitingTask.get(i)
                            .toString())) {
                        btnWait.setText("结束等待");
                        break;
                    }
                }
            }

            tbxBillNo.setText("单据号:" + m_BillNo);

            StringBuilder sbUploadData = new StringBuilder("");
            sbUploadData.append(m_BillNo);// 登陆人身份

            String strUploadData = sbUploadData.toString();

            String strURI = "http://" + GlobalInfo.m_ServerIP
                    + "/appTask.aspx?Method=GetTask&Value=" + strUploadData;
            HttpPost httpRequest = new HttpPost(strURI);

            String strResponse = "";

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

                    if (strResponse.length() == 0) {
                        finish();
                    }

                    String straDetail[] = strResponse
                            .split(GlobalInfo.m_SplitString);

                    String straString1[] = straDetail[9].split(",");
                    String m_String1 = straString1[0], m_String3;
                    if (straString1.length > 1)
                        m_String3 = straDetail[9].replace(m_String1 + ",", "");
                    else
                        m_String3 = straDetail[3];

                    tbxTransferMode.setText("模式：" + straDetail[1]);
                    tbxTargetType.setText("类型：" + straDetail[2] + " 模式："
                            + straDetail[1]);
                    tbxFromLocation.setText("起始：" + straDetail[3]);
                    tbxSickBed.setText("病床：" + straDetail[4]);
                    m_strPatientNo = straDetail[5];
                    tbxPatientNumber.setText("病人编码：" + straDetail[5]);
                    m_PatientNo = straDetail[5];
                    tbxPatientName.setText("姓名：" + straDetail[6] + " 病床："
                            + straDetail[4]);
                    tbxToLocation.setText("目的：" + straDetail[7]);
                    tbxETA.setText("要求时间：" + straDetail[8]);

                    tbxTransferEquipment.setText("设备：" + m_String1);
                    tbxEmergencyLevel.setText("紧急程度：" + straDetail[10] + " 设备："
                            + straDetail[9]);

                    tbxOriginBillNo.setText("原单号:" + straDetail[16]);
                    tbxOriginBillNo.setTextColor(Color.rgb(255, 0, 0));

                    strFromLocationCode = straDetail[11];
                    strToLocationCode = straDetail[12];

                    String strTemp = straDetail[14], m_String6 = "";
                    String stra[] = strTemp.split(",");
                    m_State = stra[0];
                    if (stra.length > 1) {
                        for (int i = 1; i < stra.length; i++) {
                            String stra1[] = stra[i].split("--");
                            m_String6 += "," + stra1[0];
                        }
                    }
                    if (m_String6.startsWith(","))
                        m_String6 = m_String6.substring(1);

                    tbxNote.setText("备注：" + straDetail[13] + " " + m_String6);
                    if (m_State.equals("执行中")) {
                        tbxFromLocation.setTextColor(Color.GREEN);
                    }
                    m_RealPatientNo = straDetail[15];
                    if (m_RealPatientNo.toUpperCase().equals("NULL"))
                        m_RealPatientNo = "";
                    tbxPatientNumber.setText("病人住院号：" + m_PatientNo + "\n手环编号："
                            + strPatientNumber);

                    if (m_PatientNo.length() > 0
                            && m_RealPatientNo.length() > 0
                            && m_PatientNo.equals(m_RealPatientNo)) {
                        tbxPatientNumber.setTextColor(Color.GREEN);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.patient, menu);
        return true;
    }

    private void StartTask(String strBarcode) {
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(m_BillNo);// 单号
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(GlobalInfo.m_LoginAccount);// 人员
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(strBarcode);// 登陆人身份
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append("START");// 分隔符
        String strUploadData = sbUploadData.toString();

        String strURI = "http://" + GlobalInfo.m_ServerIP
                + "/appTask.aspx?Method=UpdateTask&Value=" + strUploadData;
        HttpPost httpRequest = new HttpPost(strURI);

        String strResponse = "";
        try {
            new ArrayList<NameValuePair>();

            HttpResponse httpResponse = new DefaultHttpClient()
                    .execute(httpRequest);

            // 若状态码为200 ==> OK
            int nStatus = httpResponse.getStatusLine().getStatusCode();
            if (nStatus == 200) {
                strResponse = EntityUtils.toString(httpResponse.getEntity())
                        .trim();
                if (strResponse.startsWith("SUCCESS")) {
                    if (playThread != null)
                        playThread.StopAlarmRing();
                    playThread = new PlayRingThread(this);
                    playThread.strType = "start";
                    playThread.start();

                    m_State = "执行中";
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

    private void UpdatePatientInfo() {
        if (m_strPatientNo.length() == 0) {
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("无病人住院号，无法更新").setPositiveButton("确定", null)
                    .show();
            return;
        }
        try {
            String strURI = "http://" + GlobalInfo.m_ServerIP
                    + "/appTask.aspx?Method=GetPatientInfo&Value="
                    + m_strPatientNo + GlobalInfo.m_SplitString + m_BillNo;

            HttpPost httpRequest = new HttpPost(strURI);

            String strResponse = "";

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
                    if (strResponse.equals("UPDATED")) {
                        InitBill();

                        if (playThread != null)
                            playThread.StopAlarmRing();
                        playThread = new PlayRingThread(this);
                        playThread.strType = "update";
                        playThread.start();

                        tbxPatientNumber.setTextColor(Color.RED);
                    }
                } else if (strResponse.startsWith("ERROR")) {
                    strResponse = strResponse.substring(
                            strResponse.indexOf("ERROR:") + 6,
                            strResponse.indexOf("ERROREND"));
                    new AlertDialog.Builder(this).setTitle("确认")
                            .setMessage("处理任务更新过程出现错误:" + strResponse)
                            .setPositiveButton("确定", null).show();
                }
            }
        } catch (IOException exp) {
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("处理任务更新过程出现异常:网络连接存在异常")
                    .setPositiveButton("确定", null).show();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().contains("illegal character")) {
                new AlertDialog.Builder(this).setTitle("确认")
                        .setMessage("处理任务更新过程出现异常:无效的腕带编码格式")
                        .setPositiveButton("确定", null).show();
            } else {
                new AlertDialog.Builder(this).setTitle("确认")
                        .setMessage("处理任务更新过程出现异常:未知异常.详细信息:" + e.getMessage())
                        .setPositiveButton("确定", null).show();
            }
        }
    }

    private void SaveScannedOriginBillNo(final String strOriginBillNo) {
        //通过工具类调用WebService接口
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        final String strCurrent = simpleDateFormat.format(date);

        HashMap<String, String> para = new HashMap<String, String>() {
            {
                OriginBillNoScanRecord rec = new OriginBillNoScanRecord();
                rec.setBillNo(m_BillNo);
                rec.setOriginBillNo(strOriginBillNo);
                rec.setScannedAt(strCurrent);
                rec.setScannedByCode(GlobalInfo.m_LoginAccount);
                rec.setScannedByName(GlobalInfo.m_PersonName);
                Gson gson = new Gson();
                put("strParameter", gson.toJson(rec));
            }
        };
        WebServiceUtils.callWebService(WebServiceUtils.wsForPhoneURL, "SaveOriginBillNoScanLog", para, new WebServiceUtils.WebServiceCallBack() {
            //WebService接口返回的数据回调到这个方法中
            @Override
            public void callBack(SoapObject result) {
                //关闭进度条
                if (result != null) {
                    String str = result.getProperty(0).toString();
                    if (str.contains("\"Flag\":\"S\"")) {
                        Toast.makeText(PatientActivity.this, "保存单号扫描信息成功", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PatientActivity.this, "获取WebService数据错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void DealBarcode() {
        String strBarcode = tbxBarcode.getText().toString().trim();
        tbxBarcode.setText("");

        if (strBarcode.length() == 0)
            return;

        if (strBarcode.length() < 5) {
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("条码长度不能小于5位").setPositiveButton("确定", null)
                    .show();
            return;
        }

        // strBarcode=strBarcode.replace("|","");
        if (strBarcode.startsWith(GlobalInfo.m_LoginAccount.substring(0, 5))) {
        } else {
            if (strBarcode.length() > 18)
                strBarcode = strBarcode.substring(10, 18);
            /*
             * else if (strBarcode.length() > (GlobalInfo.m_nSplitLeft +
             * GlobalInfo.m_nSplitRight)) strBarcode =
             * strBarcode.substring(GlobalInfo.m_nSplitLeft, strBarcode.length()
             * - GlobalInfo.m_nSplitRight);
             */
        }
        strBarcode = strBarcode.replace("REG", "");
        strBarcode = strBarcode.replace("WB|", "");

        // 如果是起点条码则设置最后一个访问地点并置任务为执行中的状态
        if (strFromLocationCode.contains(strBarcode)) {
            GlobalInfo.UpdateLastLocation(strBarcode, strToLocationCode);
            tbxFromLocation.setTextColor(Color.GREEN);
            StartTask(strBarcode);
        }
        // 如果是终点条码
        else if (strToLocationCode.contains(strBarcode)) {
            // 如果任务不是执行中的状态则给出提示并直接返回
            if (!m_State.equals("执行中")) {
                new AlertDialog.Builder(this).setTitle("确认")
                        .setMessage("只有执行中的任务才能扫描完工")
                        .setPositiveButton("确定", null).show();
                return;
            }
            // 否则记录最后一个访问地点并置任务为完成状态
            GlobalInfo.UpdateLastLocation(strBarcode, strToLocationCode);
            tbxToLocation.setTextColor(Color.GREEN);
            if (playThread != null)
                playThread.StopAlarmRing();
            playThread = new PlayRingThread(this);
            playThread.strType = "finish";
            playThread.start();

            StringBuilder sbUploadData = new StringBuilder("");
            sbUploadData.append(m_BillNo);// 单号
            sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
            sbUploadData.append(GlobalInfo.m_LoginAccount);// 人员
            sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
            sbUploadData.append(strPatientNumber);// 登陆人身份
            sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
            sbUploadData.append("END" + strBarcode);// 分隔符
            String strUploadData = sbUploadData.toString();

            String strURI = "http://" + GlobalInfo.m_ServerIP
                    + "/appTask.aspx?Method=UpdateTask&Value=" + strUploadData;
            HttpPost httpRequest = new HttpPost(strURI);

            String strResponse = "";
            boolean bHaveException = false;
            try {
                // 发出HTTP request, 取得HTTP response
                HttpResponse httpResponse = new DefaultHttpClient()
                        .execute(httpRequest);

                // 若状态码为200 ==> OK
                int nStatus = httpResponse.getStatusLine().getStatusCode();
                if (nStatus == 200) {
                    strResponse = EntityUtils
                            .toString(httpResponse.getEntity()).trim();

                    if (strResponse.startsWith("SUCCESS")) {
                        new AlertDialog.Builder(this).setTitle("确认")
                                .setMessage("本任务已完工，请执行下一任务")
                                .setPositiveButton("确定", null).show();
                        Intent intent = new Intent();
                        intent.putExtra("name", "success");// 放入返回值
                        setResult(0, intent);// 放入回传的值,并添加一个Code,方便区分返回的数据

                        finish();
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
                bHaveException = true;
            }
            // 如果在完成任务的过程中出现了异常，则把这个命令记录到文件中
            if (bHaveException) {
                FileService fs = new FileService(this);
                try {
                    fs.append(GlobalInfo.m_TmpFileName, strURI
                            + GlobalInfo.m_CommandSplitString);
                } catch (Exception ee) {
                    new AlertDialog.Builder(this).setTitle("确认")
                            .setMessage(ee.getMessage())
                            .setPositiveButton("确定", null).show();
                }
                Intent intent = new Intent();
                intent.putExtra("name", "failure");// 放入返回值
                setResult(0, intent);// 放入回传的值,并添加一个Code,方便区分返回的数据
                finish();
            }
        }
        // 如果扫描了不是起点或终点的其它医院条码，则不允许
        else if (strBarcode.startsWith(GlobalInfo.m_VerifyCode)) {
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("不能扫描医院的条码").setPositiveButton("确定", null)
                    .show();
            return;
        }
        // 所有其余的条码都认为是病人腕带条码或者原单号条码
        else {
            if (strBarcode.length() < 5) {
                new AlertDialog.Builder(this).setTitle("确认")
                        .setMessage("条码长度不能小于8位").setPositiveButton("确定", null)
                        .show();
                return;
            }
            //对于余杭医院，要处理原单号
            boolean bDealed = false;
            if (GlobalInfo.m_VerifyCode.equals("03024")) {
                String str = tbxOriginBillNo.getText().toString();

                if (strBarcode.startsWith("100") && strBarcode.length() == 10) {
                    //if (strBarcode.startsWith("100") && strBarcode.length() == 10) {
                    boolean bHave = false;
                    for (int i = 0; i < straScannedOriginBillNo.size(); i++) {
                        if (straScannedOriginBillNo.get(i).equals(str)) {
                            bDealed = true;
                            bHave = true;
                            break;
                        }
                    }
                    if (!bHave) {
                        //判断原单号是否包含扫描的单号，如果包含了，则加入到已扫描的单号列表中，并处理颜色
                        if (str.contains(strBarcode)) {
                            bDealed = true;
                            straScannedOriginBillNo.add(strBarcode);
                            SaveScannedOriginBillNo(strBarcode);
                        }
                    }
                }
                SpannableString ss = new SpannableString(str);
                for (int i = 0; i < straScannedOriginBillNo.size(); i++) {
                    int nIndex = str.indexOf(straScannedOriginBillNo.get(i));
                    if (nIndex >= 0) {
                        ss.setSpan(new ForegroundColorSpan(Color.GREEN), nIndex,
                                nIndex + straScannedOriginBillNo.get(i).length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                tbxOriginBillNo.setText(ss);
            }

            if (!bDealed) {
                // 得到扫描病人腕带代表的住院号
                String strScannedPatientNo = strBarcode;//.substring(0, 7);
                if (m_strPatientNo.length() > 0) {
                    if (!strBarcode.equals(m_strPatientNo)) {
                        //UpdatePatientInfo();
                        new AlertDialog.Builder(this).setTitle("确认")
                                .setMessage("条码与病人编号不一致，请检查").setPositiveButton("确定", null)
                                .show();
                        return;
                    } else {
                        StartTask(strFromLocationCode);
                        tbxPatientNumber.setTextColor(Color.GREEN);
                        //return;
                    }
                } else {
                    strPatientNumber = strBarcode;
                    // tbxPatientNumber.setText("病人编码：" + strBarcode);
                    tbxPatientNumber.setText("病人住院号：" + m_PatientNo + "\n手环编号："
                            + strPatientNumber);
                    if (m_PatientNo.equals(strBarcode)) {
                        StartTask(strFromLocationCode);
                        tbxPatientNumber.setTextColor(Color.GREEN);
                    } else
                        tbxPatientNumber.setTextColor(Color.RED);
                    RecordPatientNo(strBarcode);
                    // }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, SelectReasonActivity.class);
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.pt_btn_Deal:
                DealBarcode();
                break;
            case R.id.pt_btn_Update:
                UpdatePatientInfo();
                break;
            case R.id.pt_btn_Cancel:
                bundle.putString("reasonType", "取消原因");
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);// 请求码
                break;
            case R.id.pt_btn_Delay:
                bundle.putString("reasonType", "延迟原因");
                intent.putExtras(bundle);
                startActivityForResult(intent, 2);// 请求码
                break;
            case R.id.pt_btn_Scan:
                startActivityForResult(new Intent(PatientActivity.this, CaptureActivity.class), REQUEST_CODE_SCAN);

                break;
            case R.id.pt_btn_Wait:
                Button btnWait = (Button) this.findViewById(R.id.pt_btn_Wait);
                if (btnWait.getText().equals("开始等待")) {
                    StartWait();
                    btnWait.setText("结束等待");
                    GlobalInfo.lstWaitingTask.add(m_BillNo);
                } else {
                    EndWait();
                    btnWait.setText("开始等待");
                    for (int i = GlobalInfo.lstWaitingTask.size() - 1; i >= 0; i--) {
                        if (m_BillNo.equals(GlobalInfo.lstWaitingTask.get(i)
                                .toString())) {
                            GlobalInfo.lstWaitingTask.remove(i);
                            break;
                        }
                    }
                }
                break;
        }
    }

    private void RecordPatientNo(String strPatientNo) {
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(m_BillNo);// 单号
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(strPatientNo);// 人员

        String strUploadData = sbUploadData.toString();

        String strURI = "http://" + GlobalInfo.m_ServerIP
                + "/appTask.aspx?Method=RecordPatientNo&Value=" + strUploadData;
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
                if (strResponse.startsWith("SUCCESS")) {
                    Toast.makeText(this, "记录病人编码成功", Toast.LENGTH_LONG);
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

    private void StartWait() {
        boolean bHaveException = false;
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(GlobalInfo.m_LoginAccount);// 单号
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(m_BillNo);// 人员

        String strUploadData = sbUploadData.toString();

        String strURI = "http://" + GlobalInfo.m_ServerIP
                + "/appTask.aspx?Method=StartWait&Value=" + strUploadData;
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
                if (strResponse.startsWith("SUCCESS")) {
                    Toast.makeText(this, "开始等待", Toast.LENGTH_LONG);
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
            bHaveException = true;
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("获取任务过程出现异常" + e.getMessage())
                    .setPositiveButton("确定", null).show();
            return;
        }
        if (bHaveException) {
            FileService fs = new FileService(this);
            try {
                fs.append(GlobalInfo.m_TmpFileName, strURI
                        + GlobalInfo.m_CommandSplitString);
            } catch (Exception ee) {
                new AlertDialog.Builder(this).setTitle("确认")
                        .setMessage(ee.getMessage())
                        .setPositiveButton("确定", null).show();
            }
        }
    }

    private void EndWait() {
        boolean bHaveException = false;
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(GlobalInfo.m_LoginAccount);// 单号
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(m_BillNo);// 人员

        String strUploadData = sbUploadData.toString();

        String strURI = "http://" + GlobalInfo.m_ServerIP
                + "/appTask.aspx?Method=EndWait&Value=" + strUploadData;
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
                if (strResponse.startsWith("SUCCESS")) {
                    Toast.makeText(this, "结束等待", Toast.LENGTH_LONG);
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
            bHaveException = true;
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("结束等待过程出现异常" + e.getMessage())
                    .setPositiveButton("确定", null).show();
            return;
        }
        if (bHaveException) {
            FileService fs = new FileService(this);
            try {
                fs.append(GlobalInfo.m_TmpFileName, strURI
                        + GlobalInfo.m_CommandSplitString);
            } catch (Exception ee) {
                new AlertDialog.Builder(this).setTitle("确认")
                        .setMessage(ee.getMessage())
                        .setPositiveButton("确定", null).show();
            }
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

    @Override
    /**
     * 当跳转的activity(被激活的activity)使用完毕,销毁的时候调用该方法
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN) {
            if (data != null) {
                String content = data.getStringExtra("codedContent");
                tbxBarcode.setText(content);
                DealBarcode();
            }
        } else {
            if (data != null) {
                String name = data.getStringExtra("name");
                if (name != null && name.length() > 0) {
                    if (requestCode == 1) {// 返回了取消原因
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
    }
}
