package com.wisebox.gyb.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

public class XmlParseUtil {
    public static String getSoapResult(String input, String actionName) {
        String result = "";
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(input));
            String tagName = actionName + "Result";
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG) {
                    String tag = parser.getName();
                    if(tag.equals(tagName)) {
                        result = parser.nextText();                    }
                }
                eventType = parser.next();
            }
        }catch (XmlPullParserException exception) {
            exception.printStackTrace();
        }catch (IOException exception) {
            exception.printStackTrace();
        }
        return result;
    }
}
