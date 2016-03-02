package org.github.sipuada.plugins.android.audio.example.view.renderers;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.guilhermesgb.marqueeto.LabelledMarqueeEditText;
import com.pedrogomez.renderers.Renderer;

import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaService;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaActivity;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserOperationsEntryRenderer extends Renderer<SipuadaUserCredentials> {

    @Bind(R.id.sipuplug_andrdio_example_EntryUsernameAtAddress) TextView usernameAtPrimaryHost;
    @Bind(R.id.sipuplug_andrdio_example_RegisterButton) Button registerButton;
    @Bind(R.id.sipuplug_andrdio_example_RegisterOutput) TextView registerOutput;
    @Bind(R.id.sipuplug_andrdio_example_InviteButton) Button inviteButton;
    @Bind(R.id.sipuplug_andrdio_example_InviteUser) LabelledMarqueeEditText inviteUser;
    @Bind(R.id.sipuplug_andrdio_example_InviteOutput) TextView inviteOutput;

    private final SipuadaActivity activity;

    public UserOperationsEntryRenderer(SipuadaActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void setUpView(View rootView) {}

    @Override
    protected void hookListeners(View rootView) {}

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup parent) {
        View inflatedView = inflater.inflate(R.layout.item_user_operations_entry, parent, false);
        ButterKnife.bind(this, inflatedView);
        return inflatedView;
    }

    @Override
    public void render() {
        SipuadaUserCredentials userCredentials = getContent();
        final String username = userCredentials.getUsername();
        final String primaryHost = userCredentials.getPrimaryHost();
        usernameAtPrimaryHost.setText(String.format("%s@%s", username, primaryHost));
        final SipuadaService sipuadaService = activity.getSipuadaService();
        if (sipuadaService == null) {
            registerButton.setEnabled(false);
            String statusMessage = "Binding to SipuadaService...";
            registerOutput.setText(statusMessage);
            registerButton.setOnClickListener(null);
            return;
        }
        renderRegister(sipuadaService, username, primaryHost);
        renderInvite(sipuadaService, username, primaryHost);
    }

    private void renderRegister(final SipuadaService sipuadaService, final String username,
                                final String primaryHost) {
        String statusMessage = "Waiting for your command...";
        registerOutput.setText(statusMessage);
        registerOutput.setSelected(true);
        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String statusMessage = "Please wait...";
                registerOutput.setText(statusMessage);
                registerButton.setEnabled(false);
                sipuadaService.registerAddresses(username, primaryHost,
                    new SipuadaApi.RegistrationCallback() {

                        @Override
                        public void onRegistrationSuccess(final List<String> registeredContacts) {
                            Log.d(SipuadaApplication.TAG,
                                    String.format("[onRegistrationSuccess; registeredContacts:{%s}]",
                                            registeredContacts));
                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    StringBuilder output = new StringBuilder();
                                    if (registeredContacts.isEmpty()) {
                                        output.append("No registered contacts");
                                    } else {
                                        output.append(registeredContacts.get(0));
                                    }
                                    for (int i = 1; i < registeredContacts.size(); i++) {
                                        output.append(", ");
                                        output.append(registeredContacts.get(i));
                                    }
                                    output.append(".");
                                    registerOutput.setText(output.toString());
                                    registerOutput.setSelected(true);
                                    registerButton.setEnabled(true);
                                }

                            });
                        }

                        @Override
                        public void onRegistrationFailed(final String reason) {
                            Log.d(SipuadaApplication.TAG,
                                    String.format("[onRegistrationFailed; reason:{%s}]", reason));
                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    registerOutput.setText(String.format("Failed: %s", reason));
                                    registerOutput.setSelected(true);
                                    registerButton.setEnabled(false);
                                }

                            });
                        }

                    }
                );
            }

        });
    }

    private void renderInvite(final SipuadaService sipuadaService, final String username,
                                final String primaryHost) {
        String statusMessage = "Waiting for your command...";
        inviteOutput.setText(statusMessage);
        inviteOutput.setSelected(true);
        inviteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String statusMessage = "Please wait...";
                inviteOutput.setText(statusMessage);
                inviteButton.setEnabled(false);
                final String remoteUser = inviteUser.getText();
                sipuadaService.inviteAddresses(username, primaryHost, remoteUser,
                    new SipuadaApi.CallInvitationCallback() {

                        @Override
                        public void onWaitingForCallInvitationAnswer(String callId) {
                            Log.d(SipuadaApplication.TAG, String
                                    .format("[onWaitingForCallInvitationAnswer; callId:{%s}]", callId));
                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    inviteOutput.setText(String
                                            .format("Waiting for answer from: %s", remoteUser));
                                    inviteOutput.setSelected(true);
                                    inviteButton.setEnabled(true);
                                }

                            });
                        }

                        @Override
                        public void onCallInvitationRinging(String callId) {
                            Log.d(SipuadaApplication.TAG,
                                    String.format("[onCallInvitationRinging; callId:{%s}]", callId));
                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    inviteOutput.setText(String
                                            .format("%s's softphone is ringing!", remoteUser));
                                    inviteOutput.setSelected(true);
                                    inviteButton.setEnabled(true);
                                }

                            });
                        }

                        @Override
                        public void onCallInvitationDeclined(String callId) {
                            Log.d(SipuadaApplication.TAG,
                                    String.format("[onCallInvitationDeclined; callId:{%s}]", callId));
                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    inviteOutput.setText(String
                                            .format("%s' declined your invite.", remoteUser));
                                    inviteOutput.setSelected(true);
                                    inviteButton.setEnabled(true);
                                }

                            });
                        }
                    }

                );
            }

        });
    }

}
