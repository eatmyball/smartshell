package com.wisebox.gyb.utils;

import java.text.MessageFormat;

public class ParamsBuildUtils {

    public static String bodyBuild(String actionName, String params) {
        String body =
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\n" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <tem:{0}>" +
                "         <!--Optional:-->" +
                "         <tem:strParameter>" +
                "         {1}" +
                "         </tem:strParameter>" +
                "      </tem:{2}>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";
        String buildStr = MessageFormat.format(body, actionName, params, actionName);
        return buildStr;
    }
}
