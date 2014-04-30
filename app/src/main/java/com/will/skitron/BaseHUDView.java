package com.will.skitron;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Aaron on 4/26/2014.
 */
public abstract class BaseHUDView
{

    private Text title, time;

    public static final int INFO_ONE_X = 10;
    public static final int INFO_ONE_Y = 60;

    public static final int INFO_TWO_X = 10;
    public static final int INFO_TWO_Y = 120;

    public static final int INFO_THREE_X = 10;
    public static final int INFO_THREE_Y = 180;

    public static final int TIME_X = 100;
    public static final int TIME_Y = 210;

    public static final int TITLE_X = 100;
    public static final int TITLE_Y = 20;

    public static final int MAIN_X = 80;
    public static final int MAIN_Y = 100;

    public static final int TITLE_SIZE = 3;
    public static final int TEXT_SIZE = 2;
    public static final int MAIN_SIZE = 4;

    public BaseHUDView(String titleText, int color)
    {
        title = new Text(TITLE_X, TITLE_Y, TITLE_SIZE, titleText, color);
        time = new Text(TIME_X, TIME_Y, TEXT_SIZE, "00:00:00", Color.WHITE);
    }

    public Text getTitle()
    {
        return title;
    }

    public Text getTime()
    {
        return time;
    }

    public abstract String toHML();

}
