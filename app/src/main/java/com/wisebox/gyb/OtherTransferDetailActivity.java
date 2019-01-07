package com.wisebox.gyb;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OtherTransferDetailActivity extends Activity implements
        OnClickListener {
    private EditText tbxBarcode;
    public String m_DeptCode = "";// 针对科室的编码
    public String m_TransferType = "";// 运送类型：标本、文件、物品、其他
    public String m_BillNo = "";// 单号
    public String m_GRGI = "";// 收物品还是发物品的标记：GR、GI
    private PlayRingThread playThread;
    private Integer nDeptIndex = -1;// 当前科室的数据在List中的Index。
    private String OldString1 = "", OldString2 = "", OldString3 = "",
            OldString4 = "", OldString5 = "", OldString6 = "",
            OldStringReturn = "";
    private List<String> lstCode = new ArrayList<String>();// 具体种类的编码
    private List<String> lstBarcode = new ArrayList<String>();// 具体种类的名称
    private TextView tbxSample1, tbxSample2, tbxSample3, tbxSample4,
            tbxSample5, tbxSample6, tbxBarcodeQuantity, lblReturn, lblBarcode;
    private EditText tbxQuantity1, tbxQuantity2, tbxQuantity3, tbxQuantity4,
            tbxQuantity5, tbxQuantity6, tbxReturn;
    Button btnScan;
    private int REQUEST_CODE_SCAN = 299;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_transfer_detail);

        setPlayThread(new PlayRingThread(this));

        InitControl();// 根据控件ID得到各控件
        InitValue();// 从传入的参数中解析出各全局值
        GetSampleType();// 得到具体种类的编码、名称，并显示/隐藏各控件
        GetSampleQuantity();// 得到各具体种类的数量并显示
    }

    private void InitControl() {
        tbxBarcode = (EditText) findViewById(R.id.otd_tbxBarcode);
        // tbxBarcode.setMovementMethod(ScrollingMovementMethod.getInstance());
        tbxBarcode.setOnKeyListener(onKey);
        /*
         * tbxBarcode .setOnEditorActionListener(new
         * TextView.OnEditorActionListener() { public boolean
         * onEditorAction(TextView v, int actionId, KeyEvent event) { if
         * (actionId == EditorInfo.IME_ACTION_SEND || (event != null &&
         * event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) { DealBarcode();
         * return true; } return false; } });
         */
        View btnDeal = this.findViewById(R.id.otd_btn_Deal);
        btnDeal.setOnClickListener(this);
        View btnSave = this.findViewById(R.id.otd_btn_Save);
        btnSave.setOnClickListener(this);

        tbxSample1 = (TextView) this.findViewById(R.id.otd_tbxSample1);
        tbxSample2 = (TextView) this.findViewById(R.id.otd_tbxSample2);
        tbxSample3 = (TextView) this.findViewById(R.id.otd_tbxSample3);
        tbxSample4 = (TextView) this.findViewById(R.id.otd_tbxSample4);
        tbxSample5 = (TextView) this.findViewById(R.id.otd_tbxSample5);
        tbxSample6 = (TextView) this.findViewById(R.id.otd_tbxSample6);
        lblReturn = (TextView) this.findViewById(R.id.otd_lblReturn);
        lblBarcode = (TextView) this.findViewById(R.id.otd_tbxSampleBarcode);
        tbxQuantity1 = (EditText) this.findViewById(R.id.otd_tbxQuantity1);
        tbxQuantity2 = (EditText) this.findViewById(R.id.otd_tbxQuantity2);
        tbxQuantity3 = (EditText) this.findViewById(R.id.otd_tbxQuantity3);
        tbxQuantity4 = (EditText) this.findViewById(R.id.otd_tbxQuantity4);
        tbxQuantity5 = (EditText) this.findViewById(R.id.otd_tbxQuantity5);
        tbxQuantity6 = (EditText) this.findViewById(R.id.otd_tbxQuantity6);
        tbxReturn = (EditText) this.findViewById(R.id.otd_tbxReturn);

        tbxBarcode.setInputType(InputType.TYPE_NULL);
        // tbxQuantity1.setInputType(InputType.TYPE_NULL);
        // tbxQuantity2.setInputType(InputType.TYPE_NULL);
        // tbxQuantity3.setInputType(InputType.TYPE_NULL);
        // tbxQuantity4.setInputType(InputType.TYPE_NULL);
        // tbxQuantity5.setInputType(InputType.TYPE_NULL);
        // tbxQuantity6.setInputType(InputType.TYPE_NULL);
        // tbxReturn.setInputType(InputType.TYPE_NULL);

        tbxBarcodeQuantity = (TextView) this
                .findViewById(R.id.otd_tbxBarcodeQuantity);
        tbxQuantity1.addTextChangedListener(textWatcher1);
        tbxQuantity2.addTextChangedListener(textWatcher2);
        tbxQuantity3.addTextChangedListener(textWatcher3);
        tbxQuantity4.addTextChangedListener(textWatcher4);
        tbxQuantity5.addTextChangedListener(textWatcher5);
        tbxQuantity6.addTextChangedListener(textWatcher6);
        tbxReturn.addTextChangedListener(textWatcherReturn);
        tbxSample1.setVisibility(View.GONE);
        tbxSample2.setVisibility(View.GONE);
        tbxSample3.setVisibility(View.GONE);
        tbxSample4.setVisibility(View.GONE);
        tbxSample5.setVisibility(View.GONE);
        tbxSample6.setVisibility(View.GONE);
        lblReturn.setVisibility(View.GONE);
        tbxQuantity1.setVisibility(View.GONE);
        tbxQuantity2.setVisibility(View.GONE);
        tbxQuantity3.setVisibility(View.GONE);
        tbxQuantity4.setVisibility(View.GONE);
        tbxQuantity5.setVisibility(View.GONE);
        tbxQuantity6.setVisibility(View.GONE);
        tbxReturn.setVisibility(View.GONE);

        btnScan = (Button) findViewById(R.id.pt_btn_Scan);
        btnScan.setOnClickListener(this);
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

    private TextWatcher textWatcher1 = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            if (s != null)
                OldString1 = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && s.length() > 3 && OldString1 != null)
                tbxQuantity1.setText(OldString1);
        }
    };

    private TextWatcher textWatcher2 = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            if (s != null)
                OldString2 = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && s.length() > 3 && OldString2 != null)
                tbxQuantity2.setText(OldString2);
        }
    };

    private TextWatcher textWatcher3 = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            if (s != null)
                OldString3 = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && s.length() > 3 && OldString3 != null)
                tbxQuantity3.setText(OldString3);
        }
    };

    private TextWatcher textWatcher4 = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            if (s != null)
                OldString4 = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && s.length() > 3 && OldString4 != null)
                tbxQuantity4.setText(OldString4);
        }
    };

    private TextWatcher textWatcher5 = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            if (s != null)
                OldString5 = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && s.length() > 3 && OldString5 != null)
                tbxQuantity5.setText(OldString5);
        }
    };

    private TextWatcher textWatcher6 = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            if (s != null)
                OldString6 = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && s.length() > 3 && OldString6 != null)
                tbxQuantity6.setText(OldString6);
        }
    };

    private TextWatcher textWatcherReturn = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            if (s != null)
                OldStringReturn = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && s.length() > 3 && OldStringReturn != null)
                tbxReturn.setText(OldStringReturn);
        }
    };

    private void InitValue() {
        Intent intent = getIntent();
        m_DeptCode = intent.getStringExtra("deptcode");
        m_GRGI = intent.getStringExtra("grgi");
        m_TransferType = intent.getStringExtra("transferType");
        m_BillNo = intent.getStringExtra("billno");
    }

    private void GetSampleType() {
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(GlobalInfo.m_DeptCode);// 医院
        String strUploadData = sbUploadData.toString();

        String strURI = "";
        if (m_TransferType.equals("标本"))
            strURI = "http://" + GlobalInfo.m_ServerIP
                    + "/appSysCode.aspx?Method=GetSample&Value="
                    + strUploadData;
        else if (m_TransferType.equals("物品"))
            strURI = "http://" + GlobalInfo.m_ServerIP
                    + "/appSysCode.aspx?Method=GetMateriel&Value="
                    + strUploadData;
        else if (m_TransferType.equals("药品"))
            strURI = "http://" + GlobalInfo.m_ServerIP
                    + "/appSysCode.aspx?Method=GetDrug&Value=" + strUploadData;
        else if (m_TransferType.equals("文件"))
            strURI = "http://" + GlobalInfo.m_ServerIP
                    + "/appSysCode.aspx?Method=GetDocuments&Value="
                    + strUploadData;

        HttpPost httpRequest = new HttpPost(strURI);

        String strResponse = "";
        try {
            HttpResponse httpResponse = new DefaultHttpClient()
                    .execute(httpRequest);
            int nStatus = httpResponse.getStatusLine().getStatusCode();

            if (nStatus == 200) {
                strResponse = EntityUtils.toString(httpResponse.getEntity())
                        .trim();
                if (strResponse.startsWith("SUCCESS")) {
                    strResponse = strResponse.substring(
                            strResponse.indexOf("SUCCESS:") + 8,
                            strResponse.indexOf("SUCCESSEND"));
                    String straDetail[] = strResponse
                            .split(GlobalInfo.m_SplitString);

                    if (straDetail.length >= 1) {
                        tbxSample1.setText(straDetail[0]);
                        tbxSample1.setVisibility(View.VISIBLE);
                        tbxQuantity1.setVisibility(View.VISIBLE);
                        lstCode.add(straDetail[1]);
                    }
                    if (straDetail.length >= 3) {
                        tbxSample2.setText(straDetail[2]);
                        tbxSample2.setVisibility(View.VISIBLE);
                        tbxQuantity2.setVisibility(View.VISIBLE);
                        lstCode.add(straDetail[3]);
                    }
                    if (straDetail.length >= 5) {
                        tbxSample3.setText(straDetail[4]);
                        tbxSample3.setVisibility(View.VISIBLE);
                        tbxQuantity3.setVisibility(View.VISIBLE);
                        lstCode.add(straDetail[5]);
                    }
                    if (straDetail.length >= 7) {
                        tbxSample4.setText(straDetail[6]);
                        tbxSample4.setVisibility(View.VISIBLE);
                        tbxQuantity4.setVisibility(View.VISIBLE);
                        lstCode.add(straDetail[7]);
                    }
                    if (straDetail.length >= 9) {
                        tbxSample5.setText(straDetail[8]);
                        tbxSample5.setVisibility(View.VISIBLE);
                        tbxQuantity5.setVisibility(View.VISIBLE);
                        lstCode.add(straDetail[9]);
                    }
                    if (straDetail.length >= 11) {
                        tbxSample6.setText(straDetail[10]);
                        tbxSample6.setVisibility(View.VISIBLE);
                        tbxQuantity6.setVisibility(View.VISIBLE);
                        lstCode.add(straDetail[11]);
                    }
                    // 非标本运送放下时可以有退回
                    if (!m_TransferType.equals("标本") && m_GRGI.equals("GI")) {
                        lblReturn.setVisibility(View.VISIBLE);
                        tbxReturn.setVisibility(View.VISIBLE);
                    }
                    // 只有标本和药品可以扫条码
                    if (!m_TransferType.equals("标本")
                            && !m_TransferType.equals("药品")) {
                        tbxBarcode.setVisibility(View.GONE);
                        tbxBarcodeQuantity.setVisibility(View.GONE);
                        lblBarcode.setVisibility(View.GONE);
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
                    .setMessage("获取种类出现异常" + e.getMessage())
                    .setPositiveButton("确定", null).show();
            return;
        }
    }

    private void GetSampleQuantity() {
        Integer[] nTotalGR = new Integer[6];
        Integer[] nTotalGI = new Integer[6];
        Integer[] nCurrentQuantity = new Integer[6];
        Integer nReturn = 0, nBarcode = 0;
        for (int i = 0; i < 6; i++) {
            nTotalGR[i] = 0;
            nTotalGI[i] = 0;
            nCurrentQuantity[i] = 0;
        }
        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
            OtherTaskDetailInfo otdi = GlobalInfo.lstOTDI.get(i);
            if (otdi.m_strDeptCode.equals(m_DeptCode)) {
                // 得到这个部门的数据在List中的位置
                nDeptIndex = i;
                for (int j = 0; j < otdi.m_lstOTDIN.size(); j++) {
                    if (otdi.m_lstOTDIN.get(j).m_strItemCode.equals("RETURN")) {
                        // 在这个科室之前输入的退回的数量
                        nReturn = otdi.m_lstOTDIN.get(j).m_nQuantity;
                        continue;
                    } else if (otdi.m_lstOTDIN.get(j).m_strItemCode
                            .equals("BARCODEVALUE")) {
                        // 累计得到总的条码数量
                        nBarcode++;
                        continue;
                    }
                    if (otdi.m_strGRGI.equals("GR")) {
                        // 得到不同种类标本的收货总数以及在本部门的收货数
                        for (int k = 0; k < lstCode.size(); k++) {
                            if (lstCode.get(k).equals(
                                    otdi.m_lstOTDIN.get(j).m_strItemCode)) {
                                nTotalGR[k] += otdi.m_lstOTDIN.get(j).m_nQuantity;
                                nCurrentQuantity[k] = otdi.m_lstOTDIN.get(j).m_nQuantity;
                            }
                        }
                    } else {
                        // 得到不同种类标本的发货总数以及在本部门的发货数
                        for (int k = 0; k < lstCode.size(); k++) {
                            if (lstCode.get(k).equals(
                                    otdi.m_lstOTDIN.get(j).m_strItemCode)) {
                                nTotalGI[k] += otdi.m_lstOTDIN.get(j).m_nQuantity;
                                nCurrentQuantity[k] = otdi.m_lstOTDIN.get(j).m_nQuantity;
                            }
                        }
                    }
                }
            } else {
                for (int j = 0; j < otdi.m_lstOTDIN.size(); j++) {
                    if (otdi.m_strGRGI.equals("GR")) {
                        // 累加其它部门的收货总数，得到不同种类标本的收货总数
                        for (int k = 0; k < lstCode.size(); k++) {
                            if (lstCode.get(k).equals(
                                    otdi.m_lstOTDIN.get(j).m_strItemCode)) {
                                nTotalGR[k] += otdi.m_lstOTDIN.get(j).m_nQuantity;
                            }
                        }
                    } else {
                        // 累加其它部门的发货总数，得到不同种类标本的发货总数
                        for (int k = 0; k < lstCode.size(); k++) {
                            if (lstCode.get(k).equals(
                                    otdi.m_lstOTDIN.get(j).m_strItemCode)) {
                                nTotalGI[k] += otdi.m_lstOTDIN.get(j).m_nQuantity;
                            }
                        }
                    }
                }
            }
        }

        // 填写各个数量
        tbxReturn.setText(Integer.toString(nReturn));
        tbxBarcodeQuantity.setText(Integer.toString(nBarcode));
        if (m_GRGI.equals("GR")) {
            if (nCurrentQuantity[0] > 0)
                tbxQuantity1.setText(Integer.toString(nCurrentQuantity[0]));
            if (nCurrentQuantity[1] > 0)
                tbxQuantity2.setText(Integer.toString(nCurrentQuantity[1]));
            if (nCurrentQuantity[2] > 0)
                tbxQuantity3.setText(Integer.toString(nCurrentQuantity[2]));
            if (nCurrentQuantity[3] > 0)
                tbxQuantity4.setText(Integer.toString(nCurrentQuantity[3]));
            if (nCurrentQuantity[4] > 0)
                tbxQuantity5.setText(Integer.toString(nCurrentQuantity[4]));
            if (nCurrentQuantity[5] > 0)
                tbxQuantity6.setText(Integer.toString(nCurrentQuantity[5]));
        } else {
            tbxBarcodeQuantity.setText(Integer.toString(CalNeedToGIBarcode()));
            tbxSample1.setText(tbxSample1.getText() + " 余:"
                    + (nTotalGR[0] - nTotalGI[0] + nCurrentQuantity[0]));
            tbxSample2.setText(tbxSample2.getText() + " 余:"
                    + (nTotalGR[1] - nTotalGI[1] + nCurrentQuantity[1]));
            tbxSample3.setText(tbxSample3.getText() + " 余:"
                    + (nTotalGR[2] - nTotalGI[2] + nCurrentQuantity[2]));
            tbxSample4.setText(tbxSample4.getText() + " 余:"
                    + (nTotalGR[3] - nTotalGI[3] + nCurrentQuantity[3]));
            tbxSample5.setText(tbxSample5.getText() + " 余:"
                    + (nTotalGR[4] - nTotalGI[4] + nCurrentQuantity[4]));
            tbxSample6.setText(tbxSample6.getText() + " 余:"
                    + (nTotalGR[5] - nTotalGI[5] + nCurrentQuantity[5]));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.other_transfer_detail, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        Vibrator vibrator;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // long[] pattern = { 100, 400, 100, 400 }; // 停止 开启 停止 开启
        vibrator.vibrate(200);
        switch (v.getId()) {
            case R.id.otd_btn_Deal:
                DealBarcode();
                break;
            case R.id.otd_btn_Save:
                // 保存当前科室的明细
                Save();
                break;
            case R.id.pt_btn_Scan:
                //替换为新的二维码构建工具
                startActivityForResult(new Intent(OtherTransferDetailActivity.this, CaptureActivity.class), REQUEST_CODE_SCAN);
                break;
        }
    }

    private void DealBarcode() {
        // 只有标本和药品能处理条码
        if (!m_TransferType.equals("标本") && !m_TransferType.equals("药品"))
            return;

        String strBarcode = tbxBarcode.getText().toString().trim();
        tbxBarcode.setText("");
        if (strBarcode == null || strBarcode.equals(""))
            return;

        // 条码不能以医院编码开始，如新华医院的条码不能以03023开始
        if (strBarcode.startsWith(GlobalInfo.m_VerifyCode)) {
            Toast.makeText(this, "只能扫描医院的条码", Toast.LENGTH_SHORT);
            return;
        }

        Toast.makeText(this, "扫描了条码：" + strBarcode, Toast.LENGTH_SHORT);
        // 收货时，判断是否在已收条码中存在该条码，如果存在则不允许再次扫描
        // 否则通过这个循环能得到在这个部门已经扫描的条码总数
        int nBarcodeQuantity = 0;
        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
            if (GlobalInfo.lstOTDI.get(i).m_strGRGI.equals(m_GRGI)) {
                for (int j = 0; j < GlobalInfo.lstOTDI.get(i).m_lstOTDIN.size(); j++) {
                    if (GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_strItemCode
                            .equals("BARCODEVALUE")) {
                        if (GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_strBarcode
                                .equals(strBarcode)) {
                            new AlertDialog.Builder(this).setTitle("确认")
                                    .setMessage("不能重复扫描条码")
                                    .setPositiveButton("确定", null).show();
                            return;
                        }
                        if (GlobalInfo.lstOTDI.get(i).m_strDeptCode
                                .equals(m_DeptCode))
                            nBarcodeQuantity++;
                    }
                }
            }
        }

        // 如果条码没有扫描，则添加到数据列表中
        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
            if (GlobalInfo.lstOTDI.get(i).m_strGRGI.equals(m_GRGI)) {
                if (GlobalInfo.lstOTDI.get(i).m_strDeptCode.equals(m_DeptCode)) {
                    OtherTaskDetailInfoNode otdin = new OtherTaskDetailInfoNode();
                    otdin.m_nQuantity = 1;
                    otdin.m_strBarcode = strBarcode;
                    otdin.m_strItemCode = "BARCODEVALUE";
                    otdin.m_strOperatorID = GlobalInfo.m_LoginAccount;
                    GlobalInfo.lstOTDI.get(i).m_lstOTDIN.add(otdin);
                }
            }
        }

        int nB = 0;
        try {
            nB = CalNeedToGIBarcode();
        } catch (Exception exp) {
            new AlertDialog.Builder(this).setTitle("确认")
                    .setMessage("计算条码数量出现异常:" + exp.getMessage())
                    .setPositiveButton("确定", null).show();
        }
        // 显示条码总数量
        if (m_GRGI.equals("GR"))
            tbxBarcodeQuantity.setText(Integer.toString(nBarcodeQuantity + 1));
        else
            tbxBarcodeQuantity.setText(Integer.toString(nB));
    }

    private int CalNeedToGIBarcode() {
        List<String> lstTemp = new ArrayList<String>();
        // new
        // AlertDialog.Builder(this).setTitle("确认").setMessage("lstOTDI"+GlobalInfo.lstOTDI.size()).setPositiveButton("确定",
        // null).show();
        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
            // new
            // AlertDialog.Builder(this).setTitle("确认").setMessage("m_lstOTDIN"+GlobalInfo.lstOTDI.get(i).m_lstOTDIN.size()).setPositiveButton("确定",
            // null).show();
            if (GlobalInfo.lstOTDI.get(i) == null
                    && GlobalInfo.lstOTDI.get(i).m_lstOTDIN == null)
                continue;
            for (int j = 0; j < GlobalInfo.lstOTDI.get(i).m_lstOTDIN.size(); j++) {
                if (GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_strItemCode
                        .equals("BARCODEVALUE")) {
                    if (GlobalInfo.lstOTDI.get(i).m_strGRGI.equals("GR")) {
                        lstTemp.add(GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_strBarcode);
                    } else {
                        for (int k = 0; k < lstTemp.size(); k++) {
                            if (lstTemp
                                    .get(k)
                                    .equals(GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                            .get(j).m_strBarcode)) {
                                lstTemp.remove(k);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return lstTemp.size();
    }

    private void SaveGR() {

        Integer nReturn = 0, nBarcode = 0;
        // 退回数量
        try {
            nReturn = Integer.parseInt(tbxReturn.getText().toString());
        } catch (Exception exp) {
        }
        // 条码总数量
        try {
            nBarcode = Integer
                    .parseInt(tbxBarcodeQuantity.getText().toString());
        } catch (Exception exp) {
        }

        // 判断全局数据中是否有本科室的数据，如果有则设置Index值；如果没有则新生成一个节点数据添加到List中。
        boolean bHave = false;
        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
            if (GlobalInfo.lstOTDI.get(i).m_strDeptCode.equals(m_DeptCode)) {
                GlobalInfo.lstOTDI.get(i).m_bArrived = true;
                nDeptIndex = i;
                bHave = true;
                break;
            }
        }
        if (!bHave) {
            OtherTaskDetailInfo otdi = new OtherTaskDetailInfo();
            otdi.m_bArrived = true;
            otdi.m_strDeptCode = m_DeptCode;
            otdi.m_strDeptName = "";
            otdi.m_strGRGI = m_GRGI;
            GlobalInfo.lstOTDI.add(otdi);
            nDeptIndex = GlobalInfo.lstOTDI.size() - 1;
        }

        // 判断本节点的数据中是否有退回节点，如果有则重置退回数量；如果没有则添加退回节点
        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
            if (GlobalInfo.lstOTDI.get(i).m_strDeptCode.equals(m_DeptCode)) {
                bHave = false;
                for (int j = 0; j < GlobalInfo.lstOTDI.get(i).m_lstOTDIN.size(); j++) {
                    if (GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_strItemCode
                            .equals("RETURN")) {
                        bHave = true;
                        GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_nQuantity = nReturn;
                        break;
                    }
                }
                if (!bHave) {
                    OtherTaskDetailInfoNode otdin = new OtherTaskDetailInfoNode();
                    otdin.m_nQuantity = nReturn;
                    otdin.m_strItemCode = "RETURN";
                    otdin.m_strOperatorID = GlobalInfo.m_LoginAccount;
                    GlobalInfo.lstOTDI.get(i).m_lstOTDIN.add(otdin);
                }
            }
        }

        // 判断本节点的数据中是否有条码数量节点，如果有则重置条码数量；如果没有则添加条码数量节点
        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
            if (GlobalInfo.lstOTDI.get(i).m_strDeptCode.equals(m_DeptCode)) {
                bHave = false;
                for (int j = 0; j < GlobalInfo.lstOTDI.get(i).m_lstOTDIN.size(); j++) {
                    if (GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_strItemCode
                            .equals("BARCODE")) {
                        bHave = true;
                        GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_nQuantity = nBarcode;
                        break;
                    }
                }
                if (!bHave) {
                    OtherTaskDetailInfoNode otdin = new OtherTaskDetailInfoNode();
                    otdin.m_nQuantity = nBarcode;
                    otdin.m_strItemCode = "BARCODE";
                    otdin.m_strOperatorID = GlobalInfo.m_LoginAccount;
                    GlobalInfo.lstOTDI.get(i).m_lstOTDIN.add(otdin);
                }
            }
        }

        // 对每种标本，如果在当前科室的数据中不存在，则添加，有则更改
        // 对于收货，直接更改或设置数量；
        // 对于发货，需要判断在本科室输入的发货数量，是否大于 总收货数量-其他科室的总发货数量
        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
            if (GlobalInfo.lstOTDI.get(i).m_strDeptCode.equals(m_DeptCode)) {
                for (int k = 0; k < lstCode.size(); k++) {
                    bHave = false;
                    int nQuantity = 0;
                    if (k == 0) {
                        try {
                            nQuantity = Integer.parseInt(tbxQuantity1.getText()
                                    .toString());
                        } catch (Exception exp) {
                        }
                        if (m_GRGI.equals("GI")) {
                            int nIndex1 = tbxSample1.getText().toString()
                                    .indexOf(":");
                            if (nIndex1 > 0) {
                                try {
                                    int n1 = Integer.parseInt(tbxSample1
                                            .getText().toString()
                                            .substring(nIndex1 + 1));

                                    if (nQuantity > n1) {
                                        new AlertDialog.Builder(this)
                                                .setTitle("确认")
                                                .setMessage("数量不能超出")
                                                .setPositiveButton("确定", null)
                                                .show();
                                        tbxQuantity1.requestFocus();
                                        return;
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    } else if (k == 1) {
                        try {
                            nQuantity = Integer.parseInt(tbxQuantity2.getText()
                                    .toString());
                        } catch (Exception exp) {
                        }
                        if (m_GRGI.equals("GI")) {
                            int nIndex1 = tbxSample2.getText()
                                    .toString().indexOf(":");
                            if (nIndex1 > 0) {
                                try {
                                    int n1 = Integer
                                            .parseInt(tbxSample2
                                                    .getText()
                                                    .toString()
                                                    .substring(
                                                            nIndex1 + 1));

                                    if (nQuantity > n1) {
                                        new AlertDialog.Builder(this)
                                                .setTitle("确认")
                                                .setMessage("数量不能超出")
                                                .setPositiveButton(
                                                        "确定", null)
                                                .show();
                                        tbxQuantity2.requestFocus();
                                        return;
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                    } else if (k == 2) {
                        try {
                            nQuantity = Integer.parseInt(tbxQuantity3.getText()
                                    .toString());
                        } catch (Exception exp) {
                        }
                        if (m_GRGI.equals("GI")) {
                            int nIndex1 = tbxSample3.getText()
                                    .toString().indexOf(":");
                            if (nIndex1 > 0) {
                                try {
                                    int n1 = Integer
                                            .parseInt(tbxSample3
                                                    .getText()
                                                    .toString()
                                                    .substring(
                                                            nIndex1 + 1));

                                    if (nQuantity > n1) {
                                        new AlertDialog.Builder(this)
                                                .setTitle("确认")
                                                .setMessage("数量不能超出")
                                                .setPositiveButton(
                                                        "确定", null)
                                                .show();
                                        tbxQuantity3.requestFocus();
                                        return;
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                    } else if (k == 3) {
                        try {
                            nQuantity = Integer.parseInt(tbxQuantity4.getText()
                                    .toString());
                        } catch (Exception exp) {
                        }
                        if (m_GRGI.equals("GI")) {
                            int nIndex1 = tbxSample4.getText()
                                    .toString().indexOf(":");
                            if (nIndex1 > 0) {
                                try {
                                    int n1 = Integer
                                            .parseInt(tbxSample4
                                                    .getText()
                                                    .toString()
                                                    .substring(
                                                            nIndex1 + 1));

                                    if (nQuantity > n1) {
                                        new AlertDialog.Builder(this)
                                                .setTitle("确认")
                                                .setMessage("数量不能超出")
                                                .setPositiveButton(
                                                        "确定", null)
                                                .show();
                                        tbxQuantity4.requestFocus();
                                        return;
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                    } else if (k == 4) {
                        try {
                            nQuantity = Integer.parseInt(tbxQuantity5.getText()
                                    .toString());
                        } catch (Exception exp) {
                        }
                        if (m_GRGI.equals("GI")) {
                            int nIndex1 = tbxSample5.getText()
                                    .toString().indexOf(":");
                            if (nIndex1 > 0) {
                                try {
                                    int n1 = Integer
                                            .parseInt(tbxSample5
                                                    .getText()
                                                    .toString()
                                                    .substring(
                                                            nIndex1 + 1));

                                    if (nQuantity > n1) {
                                        new AlertDialog.Builder(this)
                                                .setTitle("确认")
                                                .setMessage("数量不能超出")
                                                .setPositiveButton(
                                                        "确定", null)
                                                .show();
                                        tbxQuantity5.requestFocus();
                                        return;
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                    } else if (k == 5) {
                        try {
                            nQuantity = Integer.parseInt(tbxQuantity6.getText()
                                    .toString());
                        } catch (Exception exp) {
                        }
                        if (m_GRGI.equals("GI")) {
                            int nIndex1 = tbxSample6.getText()
                                    .toString().indexOf(":");
                            if (nIndex1 > 0) {
                                try {
                                    int n1 = Integer
                                            .parseInt(tbxSample6
                                                    .getText()
                                                    .toString()
                                                    .substring(
                                                            nIndex1 + 1));

                                    if (nQuantity > n1) {
                                        new AlertDialog.Builder(this)
                                                .setTitle("确认")
                                                .setMessage("数量不能超出")
                                                .setPositiveButton(
                                                        "确定", null)
                                                .show();
                                        tbxQuantity6.requestFocus();
                                        return;
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                    }

                    String strCode = lstCode.get(k);
                    for (int j = 0; j < GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                            .size(); j++) {
                        if (GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_strItemCode
                                .equals(lstCode.get(k))) {
                            bHave = true;
                            if (k == 0) {

                                if (m_GRGI.equals("GR"))
                                    GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_nQuantity = nQuantity;
                                else {
                                    int nIndex1 = tbxSample1.getText()
                                            .toString().indexOf(":");
                                    if (nIndex1 > 0) {
                                        try {
                                            int n1 = Integer
                                                    .parseInt(tbxSample1
                                                            .getText()
                                                            .toString()
                                                            .substring(
                                                                    nIndex1 + 1));

                                            if (nQuantity > n1) {
                                                new AlertDialog.Builder(this)
                                                        .setTitle("确认")
                                                        .setMessage("数量不能超出")
                                                        .setPositiveButton(
                                                                "确定", null)
                                                        .show();
                                                tbxQuantity1.requestFocus();
                                                return;
                                            }
                                            GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                                    .get(j).m_nQuantity = nQuantity;
                                        } catch (Exception e) {

                                        }
                                    }
                                }
                            } else if (k == 1) {

                                if (m_GRGI.equals("GR"))
                                    GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_nQuantity = nQuantity;
                                else {
                                    int nIndex1 = tbxSample2.getText()
                                            .toString().indexOf(":");
                                    if (nIndex1 > 0) {
                                        try {
                                            int n1 = Integer
                                                    .parseInt(tbxSample2
                                                            .getText()
                                                            .toString()
                                                            .substring(
                                                                    nIndex1 + 1));

                                            if (nQuantity > n1) {
                                                new AlertDialog.Builder(this)
                                                        .setTitle("确认")
                                                        .setMessage("数量不能超出")
                                                        .setPositiveButton(
                                                                "确定", null)
                                                        .show();
                                                tbxQuantity2.requestFocus();
                                                return;
                                            }
                                            GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                                    .get(j).m_nQuantity = nQuantity;
                                        } catch (Exception e) {

                                        }
                                    }
                                }
                            } else if (k == 2) {

                                if (m_GRGI.equals("GR"))
                                    GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_nQuantity = nQuantity;
                                else {
                                    int nIndex1 = tbxSample3.getText()
                                            .toString().indexOf(":");
                                    if (nIndex1 > 0) {
                                        try {
                                            int n1 = Integer
                                                    .parseInt(tbxSample3
                                                            .getText()
                                                            .toString()
                                                            .substring(
                                                                    nIndex1 + 1));

                                            if (nQuantity > n1) {
                                                new AlertDialog.Builder(this)
                                                        .setTitle("确认")
                                                        .setMessage("数量不能超出")
                                                        .setPositiveButton(
                                                                "确定", null)
                                                        .show();
                                                tbxQuantity3.requestFocus();
                                                return;
                                            }
                                            GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                                    .get(j).m_nQuantity = nQuantity;
                                        } catch (Exception e) {

                                        }
                                    }
                                }
                            } else if (k == 3) {

                                if (m_GRGI.equals("GR"))
                                    GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_nQuantity = nQuantity;
                                else {
                                    int nIndex1 = tbxSample4.getText()
                                            .toString().indexOf(":");
                                    if (nIndex1 > 0) {
                                        try {
                                            int n1 = Integer
                                                    .parseInt(tbxSample4
                                                            .getText()
                                                            .toString()
                                                            .substring(
                                                                    nIndex1 + 1));

                                            if (nQuantity > n1) {
                                                new AlertDialog.Builder(this)
                                                        .setTitle("确认")
                                                        .setMessage("数量不能超出")
                                                        .setPositiveButton(
                                                                "确定", null)
                                                        .show();
                                                tbxQuantity4.requestFocus();
                                                return;
                                            }
                                            GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                                    .get(j).m_nQuantity = nQuantity;
                                        } catch (Exception e) {

                                        }
                                    }
                                }
                            } else if (k == 4) {

                                if (m_GRGI.equals("GR"))
                                    GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_nQuantity = nQuantity;
                                else {
                                    int nIndex1 = tbxSample5.getText()
                                            .toString().indexOf(":");
                                    if (nIndex1 > 0) {
                                        try {
                                            int n1 = Integer
                                                    .parseInt(tbxSample5
                                                            .getText()
                                                            .toString()
                                                            .substring(
                                                                    nIndex1 + 1));

                                            if (nQuantity > n1) {
                                                new AlertDialog.Builder(this)
                                                        .setTitle("确认")
                                                        .setMessage("数量不能超出")
                                                        .setPositiveButton(
                                                                "确定", null)
                                                        .show();
                                                tbxQuantity5.requestFocus();
                                                return;
                                            }
                                            GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                                    .get(j).m_nQuantity = nQuantity;
                                        } catch (Exception e) {

                                        }
                                    }
                                }
                            } else if (k == 5) {

                                if (m_GRGI.equals("GR"))
                                    GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_nQuantity = nQuantity;
                                else {
                                    int nIndex1 = tbxSample6.getText()
                                            .toString().indexOf(":");
                                    if (nIndex1 > 0) {
                                        try {
                                            int n1 = Integer
                                                    .parseInt(tbxSample6
                                                            .getText()
                                                            .toString()
                                                            .substring(
                                                                    nIndex1 + 1));

                                            if (nQuantity > n1) {
                                                new AlertDialog.Builder(this)
                                                        .setTitle("确认")
                                                        .setMessage("数量不能超出")
                                                        .setPositiveButton(
                                                                "确定", null)
                                                        .show();
                                                tbxQuantity6.requestFocus();
                                                return;
                                            }
                                            GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                                    .get(j).m_nQuantity = nQuantity;
                                        } catch (Exception e) {

                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!bHave) {
                        OtherTaskDetailInfoNode otdin = new OtherTaskDetailInfoNode();
                        otdin.m_nQuantity = nQuantity;
                        otdin.m_strItemCode = strCode;
                        otdin.m_strOperatorID = GlobalInfo.m_LoginAccount;
                        GlobalInfo.lstOTDI.get(i).m_lstOTDIN.add(otdin);
                    }
                }
            }
        }

        // 尝试发送数据
        String strBillNo = m_BillNo, strDeptName = m_DeptCode, strAccount = GlobalInfo.m_LoginAccount, strMode = m_GRGI;
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(strBillNo);// 单号
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(strDeptName);// 科室编码
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(strAccount);// 登陆人身份
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(strMode);// 收货还是发货
        StringBuilder sbUploadDataHead = new StringBuilder(
                sbUploadData.toString());

        // 拼接除具体条码值之外的数据
        // 由于服务端会先删除这个科室除具体条码值之外的所有数据，所以拼接所有的数据即可，无需考虑是新增还是更改
        for (int i = 0; i < GlobalInfo.lstOTDI.size(); i++) {
            if (GlobalInfo.lstOTDI.get(i).m_strDeptCode.equals(m_DeptCode)) {
                for (int j = 0; j < GlobalInfo.lstOTDI.get(i).m_lstOTDIN.size(); j++) {
                    if (GlobalInfo.lstOTDI.get(i).m_lstOTDIN.get(j).m_strItemCode
                            .equals("BARCODEVALUE")) {
                    } else {
                        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
                        sbUploadData
                                .append(GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                        .get(j).m_strItemCode);
                        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
                        sbUploadData
                                .append(GlobalInfo.lstOTDI.get(i).m_lstOTDIN
                                        .get(j).m_nQuantity);
                    }
                }
            }
        }

        String strUploadData = sbUploadData.toString();
        String strURI = "http://" + GlobalInfo.m_ServerIP
                + "/appTask.aspx?Method=UpdateOtherTaskDetail&Value="
                + URLEncoder.encode(strUploadData);
        HttpPost httpRequest = new HttpPost(strURI);
        boolean bSuccess = false;
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
                    strResponse = strResponse.substring(
                            strResponse.indexOf("SUCCESS:") + 8,
                            strResponse.indexOf("SUCCESSEND"));
                    bSuccess = true;

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
                    .setMessage("保存入库过程出现异常" + e.getMessage())
                    .setPositiveButton("确定", null).show();
        }

        // 如果上传失败，则保存到临时命令文件中
        if (!bSuccess) {
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

        // 对于条码，服务器同样会判断该条码在该科室是否已经保存，所以全部上传即可
        // 每10个上传一次。如果上传失败则保存到临时命令文件中
        lstBarcode = new ArrayList<String>();
        for (int i = 0; i < GlobalInfo.lstOTDI.get(nDeptIndex).m_lstOTDIN
                .size(); i++) {
            if (GlobalInfo.lstOTDI.get(nDeptIndex).m_lstOTDIN.get(i).m_strItemCode
                    .equals("BARCODEVALUE")) {
                lstBarcode.add(GlobalInfo.lstOTDI.get(nDeptIndex).m_lstOTDIN
                        .get(i).m_strBarcode);
            }
        }
        if (lstBarcode != null) {
            int nTimes = lstBarcode.size() / 10;
            if (lstBarcode.size() % 10 != 0)
                nTimes = nTimes + 1;

            for (int j = 0; j < nTimes; j++) {
                strURI = "";
                boolean bHaveException = false;
                StringBuilder sb = sbUploadDataHead;
                for (int i = 0; i < 10; i++) {
                    if (j * 10 + i >= lstBarcode.size())
                        break;
                    sb.append(GlobalInfo.m_SplitString);// 分隔符
                    sb.append("BARCODEVALUE");
                    sb.append(GlobalInfo.m_SplitString);// 分隔符
                    sb.append(lstBarcode.get(j * 10 + i));
                }
                strResponse = "";
                try {
                    strURI = "http://" + GlobalInfo.m_ServerIP
                            + "/appTask.aspx?Method=SaveBarcode&Value="
                            + URLEncoder.encode(sb.toString());

                    httpRequest = new HttpPost(strURI);
                    HttpResponse httpResponse = new DefaultHttpClient()
                            .execute(httpRequest);

                    // 若状态码为200 ==> OK
                    int nStatus = httpResponse.getStatusLine().getStatusCode();
                    if (nStatus == 200) {
                        strResponse = EntityUtils.toString(
                                httpResponse.getEntity()).trim();
                        if (strResponse.startsWith("SUCCESS")) {
                            continue;
                        } else if (strResponse.startsWith("ERROR")) {
                            strResponse = strResponse.substring(
                                    strResponse.indexOf("ERROR:") + 6,
                                    strResponse.indexOf("ERROREND"));
                            new AlertDialog.Builder(this).setTitle("确认")
                                    .setMessage(strResponse)
                                    .setPositiveButton("确定", null).show();
                            bHaveException = true;
                        }
                    } else {
                        new AlertDialog.Builder(this).setTitle("确认")
                                .setMessage("错误").setPositiveButton("确定", null)
                                .show();
                        bHaveException = true;
                    }
                } catch (Exception e) {
                    bHaveException = true;
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
        }
        // 直接返回到任务明细界面，在那个界面来处理是否往服务器发送结束该任务的命令
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT);
        Intent intent = new Intent();
        intent.putExtra("result", "success");// 放入返回值
        intent.putExtra("mode", m_GRGI);// 放入返回值
        setResult(0, intent);// 放入回传的值,并添加一个Code,方便区分返回的数据
        finish();
    }

    private void Finish(String strReason) {
        StringBuilder sbUploadData = new StringBuilder("");
        sbUploadData.append(m_BillNo);// 单号
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(GlobalInfo.m_LoginAccount);// 人员
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append(strReason);// 登陆人身份
        sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
        sbUploadData.append("END");
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
                    PlayRingThread playThread = new PlayRingThread(
                            OtherTransferDetailActivity.this);
                    playThread.strType = "finish";
                    playThread.start();

                    Intent intent = new Intent();
                    if (m_TransferType.equals("标本"))
                        intent.putExtra("result", "success");// 放入返回值
                    else
                        intent.putExtra("result", "osuccess");// 放入返回值
                    setResult(0, intent);// 放入回传的值,并添加一个Code,方便区分返回的数据
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
            } else if (requestCode == 1) {// 返回了取消原因
                String name = data.getStringExtra("name");
                if (name != null && name.length() > 0) {

                    ConfirmAcceptTaskActivity dlg = new ConfirmAcceptTaskActivity(
                            this, "确认完成该任务吗？非正常完工原因：" + name, "确认", "取消");
                    int nReturn = dlg.showDialog();
                    if (nReturn == 0) {
                        Toast.makeText(getApplicationContext(), "非正常完工该任务",
                                Toast.LENGTH_SHORT).show();

                        Finish(name);
                    }
                }
            }
        }
    }

    private void Save() {
        // 点击完成按钮，处理各种数量。尝试往服务器发送数量和条码，如果成功则继续否则保存到临时文件中。
        // 关闭本界面，回到任务明细界面，在任务明细界面判断收发总数是否相等，以及所有收的条码是否都已经在发的时候扫描
        // 如果满足这些条件，则结束该任务，往服务器发送命令
        // 否则停留在任务明细界面
        SaveGR();
    }

    public void onStop() {
        super.onStop();
        super.onDestroy();
    }

    public synchronized void onResume() {
        super.onResume();
    }

    public PlayRingThread getPlayThread() {
        return playThread;
    }

    public void setPlayThread(PlayRingThread playThread) {
        this.playThread = playThread;
    }
}
