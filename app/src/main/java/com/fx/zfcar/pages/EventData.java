package com.fx.zfcar.pages;

public class EventData {
    public static final int EVENT_LOGIN = 1;
    public static final int EVENT_CAR_DETAIL = 2;
    public static final int EVENT_LABEL_DETAIL = 3;

    public static final int EVENT_WXPAY_SUCCESS = 4;
    public static final int EVENT_WXPAY_FAIL = 5;
    public static final int EVENT_WXPAY_CANCEL = 6;

    public int eventType;
    public Object data;

    public EventData(int eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
    }
}
