package org.github.sipuada.plugins.android.audio.example.view;

import android.javax.sip.header.ContentTypeHeader;

public interface MainViewApi extends SipuadaViewApi {

    void showMessages(String remoteUsername, String remoteHost, String content, ContentTypeHeader contentTypeHeader);
}
