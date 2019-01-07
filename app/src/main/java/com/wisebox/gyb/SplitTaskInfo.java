package com.wisebox.gyb;

public class SplitTaskInfo {
	public String m_TaskInfo;

	public SplitTaskInfo(String str) {
		m_TaskInfo = str;
	}

	public TaskInfo Split() {
		TaskInfo ti = new TaskInfo();
		if (m_TaskInfo.length() == 0)
			return null;
		String straDetail[] = m_TaskInfo.split(GlobalInfo.m_SplitString);

		ti.m_BillNo = straDetail[0];
		ti.m_BillType = straDetail[1];
		ti.m_TargetType = straDetail[2];
		ti.m_FromLocation = straDetail[3];
		ti.m_FromSickbed = new String(straDetail[4].getBytes());
		ti.m_PatientNo = straDetail[5];
		ti.m_PatientName = straDetail[6];
		ti.m_ToLocation = straDetail[7];
		ti.m_PatientBirthday = straDetail[8];
		String straString1[]=straDetail[9].split(",");
		if(straString1.length>0)
		ti.m_String1 = straString1[0];
		if(straString1.length>1)
			ti.m_String3=straDetail[9].replace(ti.m_String1+",", "");
		else
			ti.m_String3=ti.m_FromLocation;
		ti.m_EmergencyLevel = straDetail[10];
		ti.m_FromLocationCode = straDetail[11];
		ti.m_ToLocationCode = straDetail[12];
		ti.m_Note = straDetail[13];
		ti.m_State = straDetail[14];
		if(ti.m_TargetType.equals("病人")&&ti.m_State.contains(","))
		{
			String strTemp=ti.m_State;
			String stra[]=strTemp.split(",");
			ti.m_State=stra[0];
			if(stra.length>1)
			{
				for(int i=1;i<stra.length;i++)
				{
					String stra1[]=stra[i].split("--");
					if(stra1.length>0)
						ti.m_String6+=","+stra1[0];
				}
			}	
			if(ti.m_String6.startsWith(","))
				ti.m_String6=ti.m_String6.substring(1);
			if(ti.m_String6.equals(","))
				ti.m_String6="";
		}
		else			
			ti.m_State = straDetail[14];
		if( straDetail.length>16)
			ti.m_RelatedBillNo=straDetail[16];

		return ti;
	}
}
