package com.wisebox.gyb;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class TaskListAdapter extends BaseAdapter {

    private Context context;
    private List<TaskInfo> lstTask;
    private File cache;
    private LayoutInflater mInflater;

    // 自己定义的构造函数
    public TaskListAdapter(Context context, List<TaskInfo> contacts) {
        this.context = context;
        this.lstTask = contacts;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return lstTask.size();
    }

    @Override
    public Object getItem(int i) {
        return lstTask.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view;
        if (convertView != null) {
            view=convertView;
        } else {
            view = mInflater.inflate(R.layout.tasklistview, null);
        }

        ImageView imgImage = (ImageView) view.findViewById(R.id.ItemImage);
        TextView lblTitle = (TextView) view.findViewById(R.id.ItemTitle);
        TextView lblText = (TextView) view.findViewById(R.id.ItemText);
        TextView lblBillNo = (TextView) view.findViewById(R.id.ItemBillNo);

        TaskInfo task = lstTask.get(i);
        if(task.m_TargetType.equals("病人"))
            imgImage.setImageDrawable(context.getResources().getDrawable(R.drawable.bingren));
        else if (task.m_TargetType.equals("标本"))
            imgImage.setImageDrawable(context.getResources().getDrawable(R.drawable.biaoben));
        else if (task.m_TargetType.equals("物品"))
            imgImage.setImageDrawable(context.getResources().getDrawable(R.drawable.wupin));
        else if (task.m_TargetType.equals("文件"))
            imgImage.setImageDrawable(context.getResources().getDrawable(R.drawable.wenjian));

        String strTitle = task.m_FromLocation + " " + task.m_FromSickbed + "床->";
        if (task.m_String6.length() > 0) {
            strTitle += task.m_String6 + "  " + task.m_String1;
        } else {
            strTitle += task.m_ToLocation + "  " + task.m_String1;
        }
        lblTitle.setText(strTitle);

        String strText=task.m_TargetType + " " + task.m_PatientName + " "
                // + lstTaskInfo.get(i).m_String1 + " "
                + task.m_BillType + " " + task.m_EmergencyLevel + " "
                + task.m_PatientBirthday + " " + task.m_Note + " "
                + task.m_State;
        lblText.setText(strText);

        lblBillNo.setText(task.m_BillNo);
        lblTitle.setTextColor(Color.rgb(0,0,0));
        lblText.setTextColor(Color.rgb(0,0,0));
        lblBillNo.setTextColor(Color.rgb(0,0,0));

        if(task.m_BillType.equals("即时"))
            view.setBackgroundColor(Color.rgb(255,164,209));
        return view;
    }
}
