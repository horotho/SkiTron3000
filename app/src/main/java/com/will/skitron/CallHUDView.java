package com.will.skitron;

import android.graphics.Color;

/**
 * Created by Aaron on 4/29/2014.
 */
public class CallHUDView extends BaseHUDView
{

    private Text caller, number;

    public CallHUDView()
    {
        super("Incoming Call", Color.GREEN);
        caller = new Text(0, INFO_ONE_Y, TEXT_SIZE + 1, "CALLER", Color.WHITE);
        number = new Text(0, INFO_TWO_Y, TEXT_SIZE + 1, "NUMBER", Color.WHITE);
    }

    public void update(String callerName, String callerNumber)
    {
        caller.setText(callerName);
        number.setText(callerNumber);
    }

    @Override
    public String toHML()
    {
        String ret = "<s b=0000 >" + getTime().toHML() + getTitle().toHML() +
                    caller.toHML() + number.toHML() + "</s>";
        return ret;
    }
}
