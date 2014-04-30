package com.will.skitron;

import android.graphics.Color;

/**
 * Created by Aaron on 4/29/2014.
 */
public class LocationHUDView extends BaseHUDView
{

    private Text altitude, speed;

    public LocationHUDView()
    {
        super("Stats", Color.YELLOW);
        altitude = new Text(MAIN_X, MAIN_Y + 60, MAIN_SIZE, "N/A", Color.WHITE);
        speed = new Text(MAIN_X, MAIN_Y, MAIN_SIZE, "0 mph", Color.WHITE);
    }

    public void update(String alt, String sp)
    {
        altitude.setText(alt);
        speed.setText(sp);
    }

    @Override
    public String toHML()
    {
        String ret = "<s b=0000 >" + getTime().toHML() + getTitle().toHML() + altitude.toHML()
            + speed.toHML() + "</s>";

        return ret;
    }
}
