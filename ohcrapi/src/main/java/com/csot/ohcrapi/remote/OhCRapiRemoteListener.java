package com.csot.ohcrapi.remote;

import com.android.volley.NetworkResponse;

/**
 * Description Created on 19-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public interface OhCRapiRemoteListener {

    void onOhCRapiStarted();

    void onOhCRapiFinished(NetworkResponse recognizedText);
}
