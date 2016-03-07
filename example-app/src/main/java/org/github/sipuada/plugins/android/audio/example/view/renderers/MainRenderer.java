package org.github.sipuada.plugins.android.audio.example.view.renderers;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.guilhermesgb.marqueeto.LabelledMarqueeEditText;
import com.pedrogomez.renderers.Renderer;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.presenter.CallPresenter;
import org.github.sipuada.plugins.android.audio.example.presenter.MainPresenterApi;
import org.github.sipuada.plugins.android.audio.example.view.CallActivity;
import org.github.sipuada.plugins.android.audio.example.view.MainActivity;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainRenderer extends Renderer<SipuadaUserCredentials> {

    @Bind(R.id.sipuplug_andrdio_example_EntryUsernameAtAddress) TextView usernameAtPrimaryHost;
    @Bind(R.id.sipuplug_andrdio_example_RegisterButton) Button registerButton;
    @Bind(R.id.sipuplug_andrdio_example_RegisterOutput) TextView registerOutput;
    @Bind(R.id.sipuplug_andrdio_example_InviteButton) Button inviteButton;
    @Bind(R.id.sipuplug_andrdio_example_InviteUser) LabelledMarqueeEditText inviteUser;
    @Bind(R.id.sipuplug_andrdio_example_InviteOutput) TextView inviteOutput;
    @Bind(R.id.sipuplug_andrdio_example_CancelButton) Button cancelButton;

    private final MainPresenterApi presenter;
    private final MainActivity activity;

    public MainRenderer(MainPresenterApi presenter, MainActivity activity) {
        this.presenter = presenter;
        this.activity = activity;
    }

    @Override
    protected void setUpView(View rootView) {}

    @Override
    protected void hookListeners(View rootView) {}

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup parent) {
        View inflatedView = inflater.inflate(R.layout.item_main, parent, false);
        ButterKnife.bind(this, inflatedView);
        return inflatedView;
    }

    @Override
    public void render() {
        SipuadaUserCredentials userCredentials = getContent();
        final String username = userCredentials.getUsername();
        final String primaryHost = userCredentials.getPrimaryHost();
        usernameAtPrimaryHost.setText(String.format("%s@%s", username, primaryHost));
        if (!presenter.sipuadaServiceIsConnected()) {
            registerButton.setEnabled(false);
            String statusMessage = "Please wait...";
            registerOutput.setText(statusMessage);
            registerButton.setOnClickListener(null);
            return;
        }
        renderRegister(username, primaryHost);
        renderInvite(username, primaryHost);
    }

    private void renderRegister(final String username, final String primaryHost) {
        String statusMessage = "Waiting for your command...";
        registerOutput.setText(statusMessage);
        registerOutput.setSelected(true);
        registerButton.setEnabled(true);
        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String statusMessage = "Registering...";
                registerOutput.setText(statusMessage);
                registerButton.setEnabled(false);
                presenter.registerAddresses(username, primaryHost,
                    new MainPresenterApi.RegistrationCallback() {

                        @Override
                        public void onSuccess(final List<String> registeredContacts) {
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

                        @Override
                        public void onFailed(final String reason) {
                            registerOutput.setText(String.format("Failed: %s", reason));
                            registerOutput.setSelected(true);
                            registerButton.setEnabled(true);
                        }

                    }
                );
            }

        });
    }

    private void renderInvite(final String username, final String primaryHost) {
        String statusMessage = "Waiting for your command...";
        inviteOutput.setText(statusMessage);
        inviteOutput.setSelected(true);
        inviteUser.setEnabled(true);
        inviteButton.setEnabled(true);
        inviteButton.setVisibility(View.VISIBLE);
        inviteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String remoteUser = inviteUser.getText();
                if (remoteUser == null || remoteUser.trim().isEmpty() || !remoteUser.contains("@")) {
                    return;
                }
                String remoteUsername = remoteUser.split("@")[0];
                String remoteHost = remoteUser.split("@")[1];
                Intent intent = new Intent(activity.getApplicationContext(),
                        CallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(SipuadaApplication.KEY_CALL_ACTION,
                        CallPresenter.CallAction.MAKE_CALL);
                intent.putExtra(SipuadaApplication.KEY_USERNAME, username);
                intent.putExtra(SipuadaApplication.KEY_PRIMARY_HOST, primaryHost);
                intent.putExtra(SipuadaApplication.KEY_REMOTE_USERNAME, remoteUsername);
                intent.putExtra(SipuadaApplication.KEY_REMOTE_HOST, remoteHost);
                activity.startActivity(intent);
//                inviteOutput.setText(String.format("Inviting %s...", remoteUser));
//                inviteOutput.setSelected(true);
//                inviteUser.setEnabled(false);
//                inviteButton.setEnabled(false);
//                presenter.inviteUser(username, primaryHost, remoteUser,
//                    new MainPresenterApi.OutgoingCallInvitationCallback() {
//
//                        @Override
//                        public void onWaiting(final String callId) {
//                            inviteOutput.setText(String
//                                    .format("Waiting for answer from: %s", remoteUser));
//                            inviteOutput.setSelected(true);
//                            inviteButton.setVisibility(View.GONE);
//                            cancelButton.setEnabled(true);
//                            cancelButton.setVisibility(View.VISIBLE);
//                            cancelButton.setOnClickListener(new View.OnClickListener() {
//
//                                @Override
//                                public void onClick(View view) {
//                                    presenter.cancelInviteToUser(username, primaryHost, callId);
//                                }
//
//                            });
//                        }
//
//                        @Override
//                        public void onRinging(String callId) {
//                            inviteOutput.setText(String
//                                    .format("%s's softphone is ringing!", remoteUser));
//                            inviteOutput.setSelected(true);
//                        }
//
//                        @Override
//                        public void onDeclined() {
//                            inviteOutput.setText(String
//                                    .format("%s' declined your invite.", remoteUser));
//                            resetInvite();
//                        }
//
//                        @Override
//                        public void onAccepted(String callId) {
//                            inviteOutput.setText(String
//                                    .format("%s' accepted your invite.", remoteUser));
//                            resetInvite();
//                        }
//
//                        @Override
//                        public void onFailed(final String reason) {
//                            inviteOutput.setText(String
//                                    .format("Failed: %s", reason));
//                            resetInvite();
//                        }
//
//                        @Override
//                        public void onCanceled(final String reason) {
//                            inviteOutput.setText(String
//                                    .format("Canceled: %s", reason));
//                            resetInvite();
//                        }
//
//                    }
//                );
            }

        });
//        cancelButton.setEnabled(false);
//        cancelButton.setVisibility(View.GONE);
//        cancelButton.setOnClickListener(null);
    }

//    private void resetInvite() {
//        inviteOutput.setSelected(true);
//        inviteUser.setEnabled(true);
//        inviteButton.setEnabled(true);
//        inviteButton.setVisibility(View.VISIBLE);
//        cancelButton.setEnabled(false);
//        cancelButton.setVisibility(View.GONE);
//        cancelButton.setOnClickListener(null);
//    }

}
