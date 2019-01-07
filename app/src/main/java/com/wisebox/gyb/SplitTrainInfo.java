package com.wisebox.gyb;

public class SplitTrainInfo {
	public String m_TrainInfo;

	public SplitTrainInfo(String str) {
		m_TrainInfo = str;
	}

	public TrainInfo Split() {
		TrainInfo ti = new TrainInfo();
		if (m_TrainInfo.length() == 0)
			return null;
		String straDetail[] = m_TrainInfo.split(GlobalInfo.m_SplitString);

		ti.m_TrainNo = straDetail[0];
		ti.m_TrainName = straDetail[1];
		ti.m_CourseNo = straDetail[2];
		ti.m_CourseName = straDetail[3];
		ti.m_CourseType = straDetail[4];
		ti.m_ScoreType = straDetail[5];
		ti.m_IsOnBoardTrain = straDetail[6];
		ti.m_TeacherNo = straDetail[7];
		ti.m_TeacherName = straDetail[8];
		ti.m_Location = straDetail[9];
		ti.m_TotalStudentsNo = straDetail[10];
		ti.m_AttendStudentsNo = straDetail[11];
		ti.m_Note = straDetail[12];
		ti.m_State = straDetail[13];
		ti.m_DeptNo = straDetail[14];
		ti.m_DeptName = straDetail[15];

		return ti;
	}
}
