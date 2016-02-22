package org.github.sipuada;

import org.github.sipuada.Constants.RequestMethod;

import android.javax.sdp.SessionDescription;

public interface SipuadaPlugin {

	/*
     * Generates offer to go along
     * a session-creating request of given method.
     */
    SessionDescription generateOffer(String callId, RequestMethod method);
 
    /*
     * Generates an answer to an offer to go along a response
     * to a session-creating request of given method.
     */
    SessionDescription generateAnswer(String callId, RequestMethod method, SessionDescription offer);
 
    /*
     * Perform session setup since offer/answer sent alongside
     * a call invitation request/response with given callId
     * was established successfully (or is expected to).
     * The UserAgent is passed along so that the plugin can ask
     * it to perform session modification requests in the future.
     */
    boolean performSessionSetup(String callId, UserAgent userAgent);
 
    /*
     * Perform session termination since offer/answer sent alongside
     * a call invitation request/response with given callId could not
     * establish a call or established a call that was recently finished.
     */
    boolean performSessionTermination(String callId);;
}
