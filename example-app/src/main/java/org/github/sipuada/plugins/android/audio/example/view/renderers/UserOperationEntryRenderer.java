package org.github.sipuada.plugins.android.audio.example.view.renderers;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pedrogomez.renderers.Renderer;

import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaService;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaActivity;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaApplication;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserOperationEntryRenderer extends Renderer<SipuadaUserCredentials> {

    @Bind(R.id.sipuplug_andrdio_example_EntryButton) Button userOperationEntryButton;
    @Bind(R.id.sipuplug_andrdio_example_EntryTextView) TextView userOperationEntryOutput;

    private final SipuadaActivity activity;

    public UserOperationEntryRenderer(SipuadaActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void setUpView(View rootView) {}

    @Override
    protected void hookListeners(View rootView) {}

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup parent) {
        View inflatedView = inflater.inflate(R.layout.item_user_operation_entry, parent, false);
        ButterKnife.bind(this, inflatedView);
        return inflatedView;
    }

    @Override
    public void render() {
        SipuadaUserCredentials userCredentials = getContent();
        final String username = userCredentials.getUsername();
        final String primaryHost = userCredentials.getPrimaryHost();
        final SipuadaService sipuadaService = activity.getSipuadaService();
        if (sipuadaService == null) {
            userOperationEntryButton.setEnabled(false);
            String statusMessage = "Binding to SipuadaService...";
            userOperationEntryOutput.setText(statusMessage);
            userOperationEntryButton.setOnClickListener(null);
            return;
        }
        userOperationEntryButton.setText(String.format("REGISTER: {%s}", username));
        String statusMessage = "Waiting for your command...";
        userOperationEntryOutput.setText(statusMessage);
        userOperationEntryOutput.setSelected(true);
        userOperationEntryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
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
                                output.append("Registered contacts: ");
                                if (registeredContacts.isEmpty()) {
                                    output.append("none");
                                } else {
                                    output.append(registeredContacts.get(0));
                                }
                                for (int i = 1; i < registeredContacts.size(); i++) {
                                    output.append(", ");
                                    output.append(registeredContacts.get(i));
                                }
                                output.append(".");
                                userOperationEntryOutput.setText(output.toString());
                                userOperationEntryOutput.setSelected(true);
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
                                userOperationEntryOutput.setText(String.format("Failed: %s", reason));
                                userOperationEntryOutput.setSelected(true);
                            }

                        });
                    }

                });
            }

        });
    }

}
