package com.wisebox.gyb;

public class SplitStudentInfo {
	public String m_StudentInfo;

	public SplitStudentInfo(String str) {
		m_StudentInfo = str;
	}

	public StudentInfo Split() {
		StudentInfo ti = new StudentInfo();
		if (m_StudentInfo.length() == 0)
			return null;
		String straDetail[] = m_StudentInfo.split(GlobalInfo.m_SplitString);
		//ti.m_TrainNo = straDetail[0];
		//ti.m_PersonNo = straDetail[1];
		//ti.m_PersonName = straDetail[2];
		//ti.m_Score = straDetail[3];

		return ti;
	}
}
