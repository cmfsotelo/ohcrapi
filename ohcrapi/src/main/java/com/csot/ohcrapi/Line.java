package com.csot.ohcrapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Description Created on 29-07-2016.
 *
 * @author <a href="mailto:csotelo@cetelem.pt">csotelo</a>
 * @version $Revision : 1 $
 */
public class Line {
    Collection<Word> words;
    int maxHeight;
    int minTop;

    public Line(JSONObject object) throws JSONException {
        words = new ArrayList<>();
        JSONArray wordsJson = object.getJSONArray("Words");
        for (int i = 0; i < wordsJson.length(); i++) {
            words.add(new Word((JSONObject) wordsJson.get(i)));
        }
        maxHeight = object.getInt("MaxHeight");
        minTop = object.getInt("MinTop");
    }

    public Line(Collection<Word> words, int maxHeight, int minTop) {
        this.words = words;
        this.maxHeight = maxHeight;
        this.minTop = minTop;
    }

    public Collection<Word> getWords() {
        return words;
    }

    public void setWords(Collection<Word> words) {
        this.words = words;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMinTop() {
        return minTop;
    }

    public void setMinTop(int minTop) {
        this.minTop = minTop;
    }
}
