package com.csot.ohcrapi;

import android.graphics.Rect;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description Created on 29-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public class Word {
    String word;
    Rect rectangle;

    public Word(JSONObject object) throws JSONException {
        word = object.getString("WordText");
        int left = object.getInt("Left");
        int top = object.getInt("Top");
        int right = left + object.getInt("Width");
        int bottom = top + object.getInt("Height");
        rectangle = new Rect(left, top, right, bottom);
    }

    public Word(String word, Rect rectangle) {
        this.word = word;
        this.rectangle = rectangle;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Rect getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rect rectangle) {
        this.rectangle = rectangle;
    }
}
