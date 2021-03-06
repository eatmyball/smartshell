package com.wisebox.gyb;

public class TaskInfo {
	public String m_BillNo;
	public String m_BillType;
	public String m_TargetType;
	public String m_FromLocation;
	public String m_FromLocationCode;
	public String m_FromSickbed = "";
	public String m_ToLocation = "";
	public String m_ToLocationCode = "";
	public String m_ToSickbed = "";
	public String m_Target = "";
	public String m_PatientNo = "";
	public String m_PatientName = "";
	public String m_PatientSex = "";
	public String m_PatientOld = "";
	public String m_PatientBirthday = "";
	public String m_Tools = "";
	public String m_Operator = "";
	public String m_State = "";
	public String m_Note = "";
	public String m_TypeinBy = "";
	public String m_TypeinAt = "";
	public String m_AssignBy = "";
	public String m_AssignAt = "";
	public String m_ReassignBy = "";
	public String m_ReassignAt = "";
	public String m_OldOperator = "";
	public String m_AssignAlertBefore = "";
	public String m_ExecuteAlertBefore = "";
	public String m_ExecuteBy = "";
	public String m_ExecuteStart = "";
	public String m_ExecuteEnd = "";
	public String m_RelatedBillNo = "";
	public String m_EmergencyLevel = "";
	public String m_StandardLength = "";
	public String m_DelayReason = "";
	public String m_DelayBy = "";
	public String m_DelayAt = "";
	public String m_RestartBy = "";
	public String m_RestartAt = "";
	public String m_CancelReason = "";
	public String m_CancelBy = "";
	public String m_CancelAt = "";
	public String m_DelegateReason = "";
	public String m_DelegateBy = "";
	public String m_DelegateAt = "";
	public String m_CREATER = "";
	public String m_CREATEDATE = "";
	public String m_MODIFIERID = "";
	public String m_MODIFYDATE = "";
	public String m_String1 = "";
	public String m_String2 = "";
	public String m_String3 = "";
	public String m_String4 = "";
	public String m_String5 = "";
	public String m_String6 = "";
	public String m_String7 = "";
	public String m_String8 = "";
	public String m_String9 = "";
	public String m_String10 = "";
	public String m_String11 = "";
	public String m_String12 = "";
	public String m_String13 = "";
	public String m_String14 = "";
	public String m_String15 = "";
	public String m_String16 = "";
	public String m_String17 = "";
	public String m_String18 = "";
	public String m_String19 = "";
	public String m_String20 = "";

	public static TaskInfo Copy(TaskInfo to) {
		TaskInfo tn = new TaskInfo();
		tn.m_AssignAlertBefore = to.m_AssignAlertBefore;
		tn.m_AssignAt = to.m_AssignAt;
		tn.m_AssignBy = to.m_AssignBy;
		tn.m_BillNo = to.m_BillNo;
		tn.m_BillType = to.m_BillType;
		tn.m_CancelAt = to.m_CancelAt;
		tn.m_CancelBy = to.m_CancelBy;
		tn.m_CancelReason = to.m_CancelReason;
		tn.m_CREATEDATE = to.m_CREATEDATE;
		tn.m_CREATER = to.m_CREATER;
		tn.m_DelayAt = to.m_DelayAt;
		tn.m_DelayBy = to.m_DelayBy;
		tn.m_DelayReason = to.m_DelayReason;
		tn.m_DelegateAt = to.m_DelegateAt;
		tn.m_DelegateBy = to.m_DelegateBy;
		tn.m_DelegateReason = to.m_DelegateReason;
		tn.m_EmergencyLevel = to.m_EmergencyLevel;
		tn.m_ExecuteAlertBefore = to.m_ExecuteAlertBefore;
		tn.m_ExecuteBy = to.m_ExecuteBy;
		tn.m_ExecuteEnd = to.m_ExecuteEnd;
		tn.m_ExecuteStart = to.m_ExecuteStart;
		tn.m_FromLocation = to.m_FromLocation;
		tn.m_FromLocationCode = to.m_FromLocationCode;
		tn.m_FromSickbed = to.m_FromSickbed;
		tn.m_MODIFIERID = to.m_MODIFIERID;
		tn.m_MODIFYDATE = to.m_MODIFYDATE;
		tn.m_Note = to.m_Note;
		tn.m_OldOperator = to.m_OldOperator;
		tn.m_Operator = to.m_Operator;
		tn.m_PatientBirthday = to.m_PatientBirthday;
		tn.m_PatientName = to.m_PatientName;
		tn.m_PatientNo = to.m_PatientNo;
		tn.m_PatientOld = to.m_PatientOld;
		tn.m_PatientSex = to.m_PatientSex;
		tn.m_ReassignAt = to.m_ReassignAt;
		tn.m_ReassignBy = to.m_ReassignBy;
		tn.m_RelatedBillNo = to.m_RelatedBillNo;
		tn.m_RestartAt = to.m_RestartAt;
		tn.m_RestartBy = to.m_RestartAt;
		tn.m_StandardLength = to.m_StandardLength;
		tn.m_State = to.m_State;
		tn.m_String1 = to.m_String1;
		tn.m_String2 = to.m_String2;
		tn.m_String3 = to.m_String3;
		tn.m_String4 = to.m_String4;
		tn.m_String5 = to.m_String5;
		tn.m_String6 = to.m_String6;
		tn.m_String7 = to.m_String7;
		tn.m_String8 = to.m_String8;
		tn.m_String9 = to.m_String9;
		tn.m_String10 = to.m_String10;
		tn.m_String11 = to.m_String11;
		tn.m_String12 = to.m_String12;
		tn.m_String13 = to.m_String13;
		tn.m_String14 = to.m_String14;
		tn.m_String15 = to.m_String15;
		tn.m_String16 = to.m_String16;
		tn.m_String17 = to.m_String17;
		tn.m_String18 = to.m_String18;
		tn.m_String19 = to.m_String19;
		tn.m_String20 = to.m_String20;
		tn.m_Target = to.m_Target;
		tn.m_TargetType = to.m_TargetType;
		tn.m_ToLocation = to.m_ToLocation;
		tn.m_ToLocationCode = to.m_ToLocationCode;
		tn.m_Tools = to.m_Tools;
		tn.m_ToSickbed = to.m_ToSickbed;
		tn.m_TypeinAt = to.m_TypeinAt;
		tn.m_TypeinBy = to.m_TypeinBy;

		return tn;
	}
}
