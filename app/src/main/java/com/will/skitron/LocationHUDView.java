package com.will.skitron;

import android.graphics.Color;

/**
 * Created by Aaron on 4/29/2014.
 */
public class LocationHUDView extends BaseHUDView
{

    private Text altitude, speed;
    private Text altitude_unit, speed_unit;

    public LocationHUDView()
    {
        super("Stats", Color.YELLOW);
        altitude = new Text(MAIN_X - MAIN_SIZE*5, MAIN_Y + 60, MAIN_SIZE, "N/A", Color.WHITE);
        altitude_unit = new Text(MAIN_X + MAIN_SIZE*5*5, MAIN_Y + 60, MAIN_SIZE, "ft", Color.WHITE);
        speed = new Text(MAIN_X, MAIN_Y, MAIN_SIZE, "0", Color.WHITE);
        speed_unit = new Text(MAIN_X + MAIN_SIZE*5*5, MAIN_Y, MAIN_SIZE, "mph", Color.WHITE);
    }

    public void update(String alt, String sp)
    {
        altitude.setText(alt);
        speed.setText(sp);
    }

    public String updateHML(String alt, String sp)
    {
        String ret = "<s u=1 i=1 b=0000 >";
        if(alt != altitude.getText())
        {
            altitude.setText(alt);
            ret += altitude.updateHML(2);
        }
        if(sp != speed.getText())
        {
            speed.setText(sp);
            ret += speed.updateHML(4);
        }
        return ret + "</s>";
    }

    @Override
    public String toHML()
    {
        String ret = "<s i=1 b=0000 >" + getTime().toHML() + getTitle().toHML() + altitude.toHML()
            + altitude_unit.toHML() + speed.toHML() + speed_unit.toHML() + "</s>";

        return ret;
    }
}
