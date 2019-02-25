package com.wisebox.gyb.utils.gsonObj;

import android.text.TextUtils;

import java.util.ArrayList;

public class UserInfoJson {
   private String Flag;
   private String Message;
   private Table dsData;
    public String getFlag() {
        return Flag;
    }

    public String getMessage() {
        return Message;
    }

    public Table getDsData() {
        return dsData;
    }

    public class Table{
       private ArrayList<UserInfoBean> Table1 = new ArrayList<UserInfoBean>();

        public ArrayList<UserInfoBean> getTable1() {
            return Table1;
        }

        public class UserInfoBean {
           private String Account;
           private String Name;
           private String DeptCode;
           private String DeptName;
           private String RoleCode;
           private String RoleName;
           private String HospitalCode;

           public String getAccount() {
               return Account;
           }

           public String getName() {
               return Name;
           }

           public String getDeptCode() {
               return DeptCode;
           }

           public String getDeptName() {
               return DeptName;
           }

           public String getRoleCode() {
               return RoleCode;
           }

           public String getRoleName() {
               return RoleName;
           }

           public String getHospitalCode() {
               if((!TextUtils.isEmpty(DeptCode))&&DeptCode.length() > 5) {
                   HospitalCode = DeptCode.substring(0, 5);
               }else {
                   HospitalCode = "";
               }
               return HospitalCode;
           }
       }
   }
}
