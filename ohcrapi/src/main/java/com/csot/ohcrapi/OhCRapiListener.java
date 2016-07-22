package com.csot.ohcrapi;

/**
 * Description Created on 19-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public interface OhCRapiListener {

    void onOhCRapiStarted();

//    void onOhCRapiFinished(Bitmap bitmap, String recognizedText);

    void onOhCRapiFinished(String recognizedText);
}
