package com.csot.ohcrapi.remote;

import com.csot.ohcrapi.Line;

import java.util.Collection;

/**
 * Description Created on 19-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public interface OhCRapiRemoteListener {

    void onOhCRapiStarted();

    /**
     * Method used by the remote OhCRapi system to return results when the param isOverlayRequired equals false.
     *
     * @param recognizedText recognized text
     */
    void onOhCRapiFinished(String recognizedText);

    /**
     * Method used by the remote OhCRapi system to return results when the param isOverlayRequired equals true.
     *
     * @param lines contains the Text Overlay lines structure.
     */
    void onOhCRapiFinished(Collection<Line> lines);


    /**
     * Method used by the remote OhCRapi system to return error responses
     *
     * @param e contains the exception.
     */
    void onOhCRapiFinished(Exception e);
}
