package com.will.skitron;

import android.graphics.Color;

/**
 * Created by Aaron on 4/26/2014.
 */
public class Text extends ScreenElement
{
    private String text;
    private int size, color;

    public Text(int x, int y, int size, String text, int color)
    {
        super(x, y);
        this.text = text;
        this.color = color;
        this.size = size;
    }

    public String toHML()
    {
        int red = (Color.red(color) / 8) << 11;
        int blue = (Color.blue(color) / 8);
        int green = (Color.green(color) / 8) << 6;

        if(green != 0)
        {
            green = green | (0x1 << 5);
        }

        int tempColor = red | blue | green;

        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(tempColor));
        while(sb.length() < 4)
        {
            sb.insert(0, '0'); // pad with leading zero if needed
        }

        String hex = sb.toString();

        String ret = "<t s=" + size + " c=" + hex + " x=" + getScreenX()
                + " y=" + getScreenY() + " d=\"" + getText() + "\" />";

        return ret;
    }

    public void setText(String text)
    {
        int size = text.length() * 5 * getSize();
        if(size > 250)
        {
            this.text = text.substring(0, text.length() - (size - 250)/(5 * getSize()));
        }
        else
        {
            this.text = text;
        }
    }

    public String getText()
    {
        return text;
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }


}
