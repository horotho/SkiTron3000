package com.will.skitron;

import android.graphics.Color;

/**
 * Created by Aaron on 4/29/2014.
 */
public class MusicHUDView extends BaseHUDView
{
    private Text artist, song;

    public MusicHUDView()
    {
        super("Music", Color.BLUE);
        artist = new Text(INFO_TWO_X + 20, INFO_TWO_Y, TEXT_SIZE, "N/A", Color.rgb(58, 165, 181));
        song = new Text(INFO_ONE_X + 10, INFO_ONE_Y, TEXT_SIZE, "N/A", Color.rgb(58, 165, 181));
    }

    @Override
    public String toHML()
    {
        String ret = "<s b=0000 >" + getTime().toHML() + getTitle().toHML() +
                     artist.toHML() + song.toHML() + "</s>";
        return ret;
    }

    public void update(String artistName, String songName)
    {
        artist.setText(artistName);
        song.setText(songName);
    }
}
