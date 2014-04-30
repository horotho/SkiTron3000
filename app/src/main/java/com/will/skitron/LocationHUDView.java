package com.will.skitron;

import android.graphics.Color;

/**
 * Created by Aaron on 4/29/2014.
 */
public class LocationHUDView extends BaseHUDView
{

    private Text altitude, speed, latitude, longitude;

    public LocationHUDView()
    {
        super("Stats", Color.YELLOW);
        altitude = new Text(INFO_ONE_X, INFO_ONE_Y, TEXT_SIZE, "N/A", Color.WHITE);
        speed = new Text(MAIN_X, MAIN_Y, MAIN_SIZE, "0 mph", Color.WHITE);
        latitude = new Text(INFO_TWO_X, INFO_TWO_Y, TEXT_SIZE, "N/A", Color.WHITE);
        longitude = new Text(INFO_THREE_X, INFO_THREE_Y, TEXT_SIZE, "N/A", Color.WHITE);
    }

    public void update(String alt, String sp, String lat, String lon)
    {
        altitude.setText(alt);
        speed.setText(sp);
        latitude.setText(lat);
        longitude.setText(lon);
    }

    @Override
    public String toHML()
    {
        String ret = "<s b=0000 >" + getTime().toHML() + getTitle().toHML() + altitude.toHML()
            + speed.toHML() + latitude.toHML() + longitude.toHML() + "</s>";

        return ret;
    }
}
