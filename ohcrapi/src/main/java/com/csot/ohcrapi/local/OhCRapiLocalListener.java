package com.csot.ohcrapi.local;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Description Created on 19-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public interface OhCRapiLocalListener extends TessBaseAPI.ProgressNotifier{

    void onOhCRapiStarted();

    void onOhCRapiFinished(String recognizedText);
}
