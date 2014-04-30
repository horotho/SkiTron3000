package com.will.skitron;

/**
 * Created by Aaron on 4/26/2014.
 */
public abstract class ScreenElement
{

    private int screenX, screenY;

    public ScreenElement(int screenX, int screenY)
    {
        this.screenX = screenX;
        this.screenY = screenY;
    }

    public abstract String toHML();

    public int getScreenX()
    {
        return screenX;
    }

    public void setScreenX(int screenX)
    {
        this.screenX = screenX;
    }

    public int getScreenY()
    {
        return screenY;
    }

    public void setScreenY(int screenY)
    {
        this.screenY = screenY;
    }



}
