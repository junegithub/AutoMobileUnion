package com.yt.car.union.util;

public class EventData {
    public static final int EVENT_LOGIN = 1;

    public int eventType;
    public Object data;

    public EventData(int eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
    }
}
