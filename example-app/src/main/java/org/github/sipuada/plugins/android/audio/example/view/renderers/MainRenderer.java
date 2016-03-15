package org.github.sipuada.plugins.android.audio.example.view.renderers;

import android.content.Intent;
import android.javax.sdp.SessionDescription;
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
import org.github.sipuada.plugins.android.audio.example.view.CredentialsActivity;
import org.github.sipuada.plugins.android.audio.example.view.MainActivity;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainRenderer extends Renderer<SipuadaUserCredentials> {

    @Bind(R.id.sipuada_plugin_android_example_EntryUsernameAtAddress) TextView user;
    @Bind(R.id.sipuada_plugin_android_example_RegisterButton) Button registerButton;
    @Bind(R.id.sipuada_plugin_android_example_RegisterOutput) TextView registerOutput;
    @Bind(R.id.sipuada_plugin_android_example_InviteButton) Button inviteButton;
    @Bind(R.id.sipuada_plugin_android_example_InviteUser) LabelledMarqueeEditText inviteUser;
    @Bind(R.id.sipuada_plugin_android_example_CancelButton) Button cancelButton;
    @Bind(R.id.sipuada_plugin_android_example_OptionsButton) Button optionsButton;
    @Bind(R.id.sipuada_plugin_android_example_OptionsUser) LabelledMarqueeEditText optionsUser;
    @Bind(R.id.sipuada_plugin_android_example_OptionsOutput) TextView optionsOutput;

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
        final SipuadaUserCredentials userCredentials = getContent();
        final String username = userCredentials.getUsername();
        final String primaryHost = userCredentials.getPrimaryHost();
        user.setText(String.format("%s@%s", username, primaryHost));
        user.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(activity.getApplicationContext(), CredentialsActivity.class);
                intent.putExtra(SipuadaApplication.KEY_USER_CREDENTIALS, userCredentials);
                activity.startActivityForResult(intent, MainActivity.REQUEST_UPDATE_USER_CREDENTIALS);
                return true;
            }

        });
        if (!presenter.sipuadaServiceIsConnected()) {
            registerButton.setEnabled(false);
            String statusMessage = "Please wait...";
            registerOutput.setText(statusMessage);
            registerButton.setOnClickListener(null);
            return;
        }
        renderRegister(username, primaryHost);
        renderInvite(username, primaryHost);
        renderOptions(username, primaryHost);
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
                Intent intent = new Intent(activity.getApplicationContext(), CallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(SipuadaApplication.KEY_CALL_ACTION,
                        CallPresenter.CallAction.MAKE_CALL);
                intent.putExtra(SipuadaApplication.KEY_USERNAME, username);
                intent.putExtra(SipuadaApplication.KEY_PRIMARY_HOST, primaryHost);
                intent.putExtra(SipuadaApplication.KEY_REMOTE_USERNAME, remoteUsername);
                intent.putExtra(SipuadaApplication.KEY_REMOTE_HOST, remoteHost);
                activity.startActivity(intent);

            }

        });
    }

    private void renderOptions(final String username, final String primaryHost) {
        optionsUser.setEnabled(true);
        optionsButton.setEnabled(true);
        optionsButton.setVisibility(View.VISIBLE);
        optionsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String remoteUser = optionsUser.getText();
                if (remoteUser == null || remoteUser.trim().isEmpty() || !remoteUser.contains("@")) {
                    return;
                }
                String remoteUsername = remoteUser.split("@")[0];
                String remoteHost = remoteUser.split("@")[1];

                String statusMessage = "Querying options...";
                registerOutput.setText(statusMessage);
                registerButton.setEnabled(false);

                // TODO - working - BEGIN
                presenter.queryingOptions(username, primaryHost, remoteUsername, remoteHost, new MainPresenterApi.OptionsQueryingCallback() {

                    @Override
                    public void onOptionsQueryingSuccess(String callId, SessionDescription content) {
                        StringBuilder output = new StringBuilder();
                        if (null == content) {
                            output.append("No session description");
                        } else {
                            output.append("Session description found.");
                        }

                        optionsOutput.setText(output.toString());
                        optionsOutput.setSelected(true);
                        optionsButton.setEnabled(true);
                    }

                    @Override
                    public void onOptionsQueryingFailed(String reason) {
                        optionsOutput.setText(String.format("Failed: %s", reason));
                        optionsOutput.setSelected(true);
                        optionsButton.setEnabled(true);
                    }
                });
                // TODO - working - END

            }

        });



    }

}
