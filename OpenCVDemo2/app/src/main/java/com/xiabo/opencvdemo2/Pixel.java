package com.xiabo.opencvdemo2;

public class Pixel {

    Vevent vevent = new Vevent();

    static int EV_SYN = 0;
    static int EV_ABS = 3;

    static int SYN_REPORT = 0;
    static int ABS_MT_SLOT = 47;
    static int ABS_MT_TOUCH_MAJOR = 48;
    static int ABS_MT_POSITION_X = 53;
    static int ABS_MT_POSITION_Y = 54;
    static int ABS_MT_TRACKING_ID = 57;
    static int ABS_MT_PRESSURE = 58;


    public void touch() {
//        vevent.sendevent("event1", EV_ABS, ABS_MT_SLOT, finger_index);
//        vevent.sendevent("event1", EV_ABS, ABS_MT_TRACKING_ID, finger_index);
//        vevent.sendevent("event1", EV_ABS, ABS_MT_POSITION_X, x);
//        vevent.sendevent("event1", EV_ABS, ABS_MT_POSITION_Y, y);
//        vevent.sendevent("event1", EV_SYN, SYN_REPORT, 0);

        vevent.sendevent("event2", EV_ABS, ABS_MT_SLOT, 0);
        vevent.sendevent("event2", EV_ABS, ABS_MT_TRACKING_ID, 3349);
        vevent.sendevent("event2", EV_ABS, ABS_MT_POSITION_X, 900);
        vevent.sendevent("event2", EV_ABS, ABS_MT_POSITION_Y, 1799);
        vevent.sendevent("event2", EV_ABS, ABS_MT_PRESSURE, 56);
        vevent.sendevent("event2", EV_SYN, SYN_REPORT, 0);
        vevent.sendevent("event2", EV_ABS, ABS_MT_PRESSURE, 61);
        vevent.sendevent("event2", EV_SYN, SYN_REPORT, 0);
        vevent.sendevent("event2", EV_ABS, ABS_MT_PRESSURE, 58);
        vevent.sendevent("event2", EV_SYN, SYN_REPORT, 0);
        vevent.sendevent("event2", EV_ABS, ABS_MT_TRACKING_ID, 4294967295L);
        vevent.sendevent("event2", EV_SYN, SYN_REPORT, 0);
    }

    public void release(int finger_index) {
        vevent.sendevent("event1", EV_ABS, ABS_MT_SLOT, finger_index);
        vevent.sendevent("event1", EV_ABS, ABS_MT_TRACKING_ID, -1);
        vevent.sendevent("event1", EV_SYN, SYN_REPORT, 0);
    }
}
