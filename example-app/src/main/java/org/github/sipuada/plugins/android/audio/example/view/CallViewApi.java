package org.github.sipuada.plugins.android.audio.example.view;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;

public interface CallViewApi extends SipuadaViewApi {

    void showMakingCall(SipuadaCallData sipuadaCallData);

    void showCancelingCall(SipuadaCallData sipuadaCallData);

    void showFailingCall(SipuadaCallData sipuadaCallData);

    void showMakingCallAccepted(SipuadaCallData sipuadaCallData);

    void showMakingCallDeclined(SipuadaCallData sipuadaCallData);

    void showReceivingCall(SipuadaCallData sipuadaCallData);

    void showReceivingCallCanceled(SipuadaCallData sipuadaCallData);

    void showReceivingCallFailed(SipuadaCallData sipuadaCallData);

    void showReceivingCallAccept(SipuadaCallData sipuadaCallData);

    void showReceivingCallDecline(SipuadaCallData sipuadaCallData);

    void showCallInProgress(SipuadaCallData sipuadaCallData);

    void showCallFinished(SipuadaCallData sipuadaCallData);

    void dismissCall(SipuadaCallData sipuadaCallData);

}
