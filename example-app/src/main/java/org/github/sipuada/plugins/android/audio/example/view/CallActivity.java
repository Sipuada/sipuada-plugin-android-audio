package org.github.sipuada.plugins.android.audio.example.view;

import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.guilhermesgb.marqueeto.LabelledMarqueeEditText;
import com.hannesdorfmann.mosby.mvp.viewstate.RestoreableViewState;
import com.joanzapata.iconify.IconDrawable;
import com.pedrogomez.renderers.ListAdapteeCollection;
import com.pedrogomez.renderers.RVRendererAdapter;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaCallData;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenter;
import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.renderers.CallRendererBuilder;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;

public class CallActivity extends SipuadaViewStateActivity<CallViewApi, CallPresenterApi>
        implements CallViewApi {

    @Bind(R.id.sipuada_plugin_android_example_IncomingCallsSummary) TextView callsSummary;
    @Bind(R.id.sipuada_plugin_android_example_RecyclerView) RecyclerView recyclerView;
    @Bind(R.id.sipuada_plugin_android_example_FloatingActionButton) FloatingActionButton floatingActionButton;
    @Bind(R.id.sipuada_plugin_android_example_FooterFrameLayout) FrameLayout footerFrame;
    @Bind(R.id.sipuada_plugin_android_example_LocalUserSpinner) Spinner localUserCaller;
    @Bind(R.id.sipuada_plugin_android_example_RemoteUserMarqueeto) LabelledMarqueeEditText remoteUserCallee;

    private RVRendererAdapter<CallViewState.SipuadaCall> adapter;

    private ToneGenerator toneGenerator;
    private Ringtone ringtone;
    private RingtoneHandler toneHandler;

    private static final int INITIALIZE_TONE_STACK = 0;
    private static final int PLAY_RINGINGTONE = 1;
    private static final int STOP_RINGINGTONE = 2;
    private static final int PLAY_RECEIVINGTONE = 3;
    private static final int STOP_RECEIVINGTONE = 4;
    private static final int PLAY_BUSYTONE = 5;
    private static final int PLAY_FAILEDTONE = 6;

    private final class RingtoneHandler extends Handler {

        public RingtoneHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            switch(message.what) {
                case INITIALIZE_TONE_STACK:
                    initializeToneStack();
                    break;
                case PLAY_RINGINGTONE:
                    doPlayRingingTone();
                    break;
                case STOP_RINGINGTONE:
                    doStopRingingTone();
                    break;
                case PLAY_RECEIVINGTONE:
                    doPlayReceivingTone();
                    break;
                case STOP_RECEIVINGTONE:
                    doStopReceivingTone();
                    break;
                case PLAY_BUSYTONE:
                    doPlayBusyTone();
                    break;
                case PLAY_FAILEDTONE:
                    doPlayFailedTone();
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        callsSummary.setEnabled(false);
        adapter = new RVRendererAdapter<>(getLayoutInflater(), new CallRendererBuilder(getPresenter()),
                new ListAdapteeCollection<>(Arrays.asList(new CallViewState.SipuadaCall[]{})));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setEnabled(false);

        IconDrawable iconDrawable = new IconDrawable(getApplicationContext(), "md-phone")
                .actionBarSize().colorRes(android.R.color.black);
        floatingActionButton.setImageDrawable(iconDrawable);
        floatingActionButton.setBackgroundColor(ContextCompat
                .getColor(getApplicationContext(), R.color.colorAccent));
        floatingActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (footerFrame.getVisibility() != View.VISIBLE) {
                    footerFrame.setVisibility(View.VISIBLE);
                } else {
                    if (remoteUserCallee.getText().contains("@")) {
                        SipuadaUserCredentials userCredentials = (SipuadaUserCredentials)
                                localUserCaller.getSelectedItem();
                        String username = userCredentials.getUsername();
                        String primaryHost = userCredentials.getPrimaryHost();
                        String remoteUsername = remoteUserCallee.getText().split("@")[0];
                        String remoteHost = remoteUserCallee.getText().split("@")[1];
                        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(SipuadaApplication.KEY_CALL_ACTION,
                                CallPresenter.SipuadaCallAction.MAKE_CALL);
                        intent.putExtra(SipuadaApplication.KEY_USERNAME, username);
                        intent.putExtra(SipuadaApplication.KEY_PRIMARY_HOST, primaryHost);
                        intent.putExtra(SipuadaApplication.KEY_REMOTE_USERNAME, remoteUsername);
                        intent.putExtra(SipuadaApplication.KEY_REMOTE_HOST, remoteHost);
                        startActivity(intent);
                        footerFrame.setVisibility(View.INVISIBLE);
                    } else {
                        remoteUserCallee.setText("192.168.130.207:5060");
                        footerFrame.setVisibility(View.INVISIBLE);
                    }
                }
            }

        });

        HandlerThread thread = new HandlerThread(CallActivity.class.getName(),
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper serviceLooper = thread.getLooper();
        toneHandler = new RingtoneHandler(serviceLooper);
        Message message = toneHandler.obtainMessage(INITIALIZE_TONE_STACK);
        toneHandler.sendMessage(message);
    }

    @Override
    public void updatePresenter(CallViewState callViewState) {
        getPresenter().updateState(callViewState);
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleCallActionIntent(intent);
    }

    @Override
    public void onNewViewStateInstance() {
        Intent intent = getIntent();
        handleCallActionIntent(intent);
    }

    @Override
    protected void onSipuadaServiceConnected() {
        getPresenter().fetchLocalUsersThenRefresh();
        callsSummary.setEnabled(true);
        recyclerView.setEnabled(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public RestoreableViewState createViewState() {
        return new CallViewState();
    }

    @Override
    protected void onSipuadaServiceDisconnected() {
        callsSummary.setEnabled(false);
        recyclerView.setEnabled(false);
        adapter.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CallPresenter createPresenter() {
        return new CallPresenter();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0
                || super.onKeyDown(keyCode, event);
    }

    @Override
    public void refreshUsersCredentialsList(List<SipuadaUserCredentials> usersCredentials) {
        localUserCaller.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                R.layout.item_caller_spinner, R.id.sipuada_plugin_android_example_EntryLocalUsernameAtAddress,
                usersCredentials));
    }

    private void handleCallActionIntent(Intent intent) {
        CallPresenter.SipuadaCallAction sipuadaCallAction = (CallPresenter.SipuadaCallAction) intent
                .getSerializableExtra(SipuadaApplication.KEY_CALL_ACTION);
        String callId = intent.getStringExtra(SipuadaApplication.KEY_CALL_ID);
        String username = intent.getStringExtra(SipuadaApplication.KEY_USERNAME);
        String primaryHost = intent.getStringExtra(SipuadaApplication.KEY_PRIMARY_HOST);
        String remoteUsername = intent.getStringExtra(SipuadaApplication.KEY_REMOTE_USERNAME);
        String remoteHost = intent.getStringExtra(SipuadaApplication.KEY_REMOTE_HOST);
        final SipuadaCallData sipuadaCallData =
                new SipuadaCallData(callId, username, primaryHost, remoteUsername, remoteHost);
        addSipuadaCall(sipuadaCallAction, sipuadaCallData);
        presenter.performAction(sipuadaCallAction, sipuadaCallData);
    }

    @Override
    public void showMakingCall(SipuadaCallData sipuadaCallData) {
        addSipuadaCall(CallPresenter.SipuadaCallAction.MAKE_CALL, sipuadaCallData);
    }

    @Override
    public void showMakingCallCancelable(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_CANCELABLE, sipuadaCallData, null);
    }

    @Override
    public void showCancelingCall(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_CANCEL, sipuadaCallData, null);
    }

    @Override
    public void showMakingCallCanceled(SipuadaCallData sipuadaCallData, String reason) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_CANCELED, sipuadaCallData, reason);
        stopRingingTone();
    }

    @Override
    public void showMakingCallFailed(SipuadaCallData sipuadaCallData, String reason) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_FAILED, sipuadaCallData, reason);
        stopRingingTone();
        playFailedTone();
    }

    @Override
    public void showMakingCallRinging(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_RINGING, sipuadaCallData, null);
        playRingingTone();
    }

    @Override
    public void showMakingCallDeclined(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_DECLINED, sipuadaCallData, null);
        stopRingingTone();
        playBusyTone();
    }

    @Override
    public void showReceivingCall(SipuadaCallData sipuadaCallData) {
        addSipuadaCall(CallPresenter.SipuadaCallAction.RECEIVE_CALL, sipuadaCallData);
        playReceivingTone();
    }

    @Override
    public void showReceivingCallCanceled(SipuadaCallData sipuadaCallData, String reason) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_CANCELED, sipuadaCallData, reason);
        stopReceivingTone();
    }

    @Override
    public void showReceivingCallFailed(SipuadaCallData sipuadaCallData, String reason) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_FAILED, sipuadaCallData, reason);
        stopReceivingTone();
        playFailedTone();
    }

    @Override
    public void showReceivingCallAccept(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_ACCEPT, sipuadaCallData, null);
        stopReceivingTone();
    }

    @Override
    public void showReceivingCallDecline(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_DECLINE, sipuadaCallData, null);
        stopReceivingTone();
    }

    @Override
    public void showCallInProgress(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_IN_PROGRESS, sipuadaCallData, null);
        stopReceivingTone();
        stopRingingTone();
    }

    @Override
    public void showCallFailed(SipuadaCallData sipuadaCallData, String reason) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_FAILED, sipuadaCallData, reason);
        playFailedTone();
    }

    @Override
    public void showCallFinished(SipuadaCallData sipuadaCallData) {
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_FINISHED, sipuadaCallData, null);
    }

    @Override
    public void dismissCall(SipuadaCallData sipuadaCallData) {
        closeSipuadaCall(sipuadaCallData);
    }

    private void addSipuadaCall(CallPresenter.SipuadaCallAction sipuadaCallAction,
                                SipuadaCallData sipuadaCallData) {
        CallViewState callsViewState = (CallViewState) getViewState();
        CallViewState.SipuadaCallState sipuadaCallState;
        switch (sipuadaCallAction) {
            case MAKE_CALL:
                sipuadaCallState = CallViewState.SipuadaCallState.CALL_MAKING;
                break;
            default:
            case RECEIVE_CALL:
                sipuadaCallState = CallViewState.SipuadaCallState.CALL_RECEIVING;
                break;
        }
        callsViewState.addOrModifySipuadaCall(sipuadaCallState, sipuadaCallData, null);
        refreshCallDataList(callsViewState);
    }

    private void setSipuadaCall(CallViewState.SipuadaCallState sipuadaCallState,
                                SipuadaCallData sipuadaCallData, String stateInformation) {
        CallViewState callsViewState = (CallViewState) getViewState();
        callsViewState.addOrModifySipuadaCall(sipuadaCallState, sipuadaCallData, stateInformation);
        refreshCallDataList(callsViewState);
    }

    private void closeSipuadaCall(SipuadaCallData sipuadaCallData) {
        CallViewState callsViewState = (CallViewState) getViewState();
        callsViewState.removeSipuadaCall(sipuadaCallData);
        refreshCallDataList(callsViewState);
    }

    private void refreshCallDataList(CallViewState callsViewState) {
        adapter.clear();
        int pendingOutgoingCallsNumber = 0, pendingIncomingCallsNumber = 0,
                establishedCallsNumber = 0, finishedCallsNumber = 0;
        for (CallViewState.SipuadaCall sipuadaCall : callsViewState.getSipuadaCallsInformation()) {
            switch (sipuadaCall.getCallState()) {
                case CALL_MAKING:
                case CALL_MAKING_CANCELABLE:
                case CALL_MAKING_RINGING:
                    pendingOutgoingCallsNumber++;
                    break;
                case CALL_RECEIVING:
                case CALL_RECEIVING_ACCEPT:
                case CALL_RECEIVING_DECLINE:
                    pendingIncomingCallsNumber++;
                    break;
                case CALL_IN_PROGRESS:
                    establishedCallsNumber++;
                    break;
                case CALL_MAKING_DECLINED:
                case CALL_MAKING_CANCEL:
                case CALL_MAKING_CANCELED:
                case CALL_MAKING_FAILED:
                case CALL_RECEIVING_CANCELED:
                case CALL_RECEIVING_FAILED:
                case CALL_FAILED:
                case CALL_FINISHED:
                    finishedCallsNumber++;
                    break;
            }
            adapter.add(sipuadaCall);
        }
        StringBuilder summary = new StringBuilder();
        if (establishedCallsNumber == 1) {
            summary.append(String.format("%d established call", establishedCallsNumber));
        } else if (establishedCallsNumber > 1) {
            summary.append(String.format("%d established calls", establishedCallsNumber));
        }
        if (pendingIncomingCallsNumber == 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d incoming call invite", pendingIncomingCallsNumber));
        } else if (pendingIncomingCallsNumber > 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d incoming call invites", pendingIncomingCallsNumber));
        }
        if (pendingOutgoingCallsNumber == 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d outgoing call invite", pendingOutgoingCallsNumber));
        } else if (pendingOutgoingCallsNumber > 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d outgoing call invites", pendingOutgoingCallsNumber));
        }
        if (finishedCallsNumber == 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d call finished", finishedCallsNumber));
        } else if (finishedCallsNumber > 1) {
            if (summary.length() != 0) {
                summary.append(", ");
            }
            summary.append(String.format("%d calls finished", finishedCallsNumber));
        }
        if (establishedCallsNumber == 0 && pendingIncomingCallsNumber == 0
                && pendingOutgoingCallsNumber == 0 && finishedCallsNumber == 0) {
            summary.append("Finishing...");
            finish();
        }
        else {
            summary.append("...");
        }
        callsSummary.setText(summary);
        adapter.notifyDataSetChanged();
    }

    public void initializeToneStack() {
        toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100); //or STREAM_DTMF?
        ringtone = RingtoneManager.getRingtone(getApplicationContext(),
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
    }

    public void playRingingTone() {
        Message message = toneHandler.obtainMessage(PLAY_RINGINGTONE);
        toneHandler.sendMessage(message);
    }

    public void stopRingingTone() {
        Message message = toneHandler.obtainMessage(STOP_RINGINGTONE);
        toneHandler.sendMessage(message);
    }

    public void playReceivingTone() {
        Message message = toneHandler.obtainMessage(PLAY_RECEIVINGTONE);
        toneHandler.sendMessage(message);
    }

    public void stopReceivingTone() {
        Message message = toneHandler.obtainMessage(STOP_RECEIVINGTONE);
        toneHandler.sendMessage(message);
    }

    public void playBusyTone() {
        Message message = toneHandler.obtainMessage(PLAY_BUSYTONE);
        toneHandler.sendMessage(message);
    }

    public void playFailedTone() {
        Message message = toneHandler.obtainMessage(PLAY_FAILEDTONE);
        toneHandler.sendMessage(message);
    }

    public void doPlayRingingTone() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK);
    }

    public void doStopRingingTone() {
        toneGenerator.stopTone();
    }

    public void doPlayReceivingTone() {
        ringtone.play();
    }

    public void doStopReceivingTone() {
        ringtone.stop();
    }

    public void doPlayBusyTone() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 3000);
    }

    public void doPlayFailedTone() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 2000);
    }

}
