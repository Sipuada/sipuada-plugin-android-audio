package org.github.sipuada.plugins.android.audio.example.view;

import android.content.Intent;
import android.media.*;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private Ringtone mRingtone;
    private ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 100);

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

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mRingtone = RingtoneManager.getRingtone(getApplicationContext(), notification);

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
                                CallPresenter.CallAction.MAKE_CALL);
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
        CallPresenter.CallAction sipuadaCallAction = (CallPresenter.CallAction) intent
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
        playRingingTone();
        addSipuadaCall(CallPresenter.CallAction.MAKE_CALL, sipuadaCallData);
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
        stopRingingTone();
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_CANCELED, sipuadaCallData, reason);
    }

    @Override
    public void showMakingCallFailed(SipuadaCallData sipuadaCallData, String reason) {
        playBusyTone();
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_FAILED, sipuadaCallData, reason);
    }

    @Override
    public void showMakingCallRinging(SipuadaCallData sipuadaCallData) {
        playRingingTone();
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_RINGING, sipuadaCallData, null);
    }

    @Override
    public void showMakingCallDeclined(SipuadaCallData sipuadaCallData) {
        playBusyTone();
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_MAKING_DECLINED, sipuadaCallData, null);
    }

    @Override
    public void showReceivingCall(SipuadaCallData sipuadaCallData) {
        playReceivingCallTone();
        addSipuadaCall(CallPresenter.CallAction.RECEIVE_CALL, sipuadaCallData);
    }

    @Override
    public void showReceivingCallCanceled(SipuadaCallData sipuadaCallData, String reason) {
        stopReceivingCallTone();
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_CANCELED, sipuadaCallData, reason);
    }

    @Override
    public void showReceivingCallFailed(SipuadaCallData sipuadaCallData, String reason) {
        stopReceivingCallTone();
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_FAILED, sipuadaCallData, reason);
    }

    @Override
    public void showReceivingCallAccept(SipuadaCallData sipuadaCallData) {
        stopReceivingCallTone();
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_ACCEPT, sipuadaCallData, null);
    }

    @Override
    public void showReceivingCallDecline(SipuadaCallData sipuadaCallData) {
        stopReceivingCallTone();
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_RECEIVING_DECLINE, sipuadaCallData, null);
    }

    @Override
    public void showCallInProgress(SipuadaCallData sipuadaCallData) {
        stopReceivingCallTone();
        stopRingingTone();
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_IN_PROGRESS, sipuadaCallData, null);
    }

    @Override
    public void showCallFinished(SipuadaCallData sipuadaCallData) {
        stopReceivingCallTone();
        stopRingingTone();
        setSipuadaCall(CallViewState.SipuadaCallState.CALL_FINISHED, sipuadaCallData, null);
    }

    @Override
    public void dismissCall(SipuadaCallData sipuadaCallData) {
        closeSipuadaCall(sipuadaCallData);
    }

    private void addSipuadaCall(CallPresenter.CallAction sipuadaCallAction,
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
        for (int i = 0; i < callsViewState.getSipuadaCallsCount(); i++) {
            CallViewState.SipuadaCall sipuadaCall = callsViewState.getSipuadaCall(i);
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

    public void playRingingTone() {
        final Thread toneGeneratorThread = new Thread() {
            @Override
            public void run() {
                if (toneGenerator == null) {
                    toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100);
                }
                Log.v("JFL DEBUG", "STARTING TONE");
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK);
            }
        };
        try {
            toneGeneratorThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playReceivingCallTone() {
        if (mRingtone == null ) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        }

        mRingtone.play();
    }

    public void stopReceivingCallTone() {
        if (mRingtone != null) {
            mRingtone.stop();
        }
    }

    public void stopRingingTone() {
        if (toneGenerator != null) {
            final Thread toneGeneratorThread = new Thread() {
                @Override
                public void run() {
                    try {
                        toneGenerator.stopTone();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            try {
                toneGeneratorThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void playBusyTone() {
        final Thread toneGeneratorThread = new Thread() {
            @Override
            public void run() {
                if (toneGenerator == null) {
                    toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100);
                }
                try {
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 2000);
                } catch (Exception ignored) {}
            }
        };
        try {
            toneGeneratorThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
