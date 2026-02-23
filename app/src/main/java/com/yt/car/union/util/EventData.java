package com.yt.car.union.util;

public class EventData {
    public static final int EVENT_LOGIN = 1;
    public static final int EVENT_CAR_DETAIL = 2;
    public static final int EVENT_LABEL_DETAIL = 3;

    public int eventType;
    public Object data;

    public EventData(int eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
    }
}
