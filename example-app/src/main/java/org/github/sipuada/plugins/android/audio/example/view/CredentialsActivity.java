package org.github.sipuada.plugins.android.audio.example.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.guilhermesgb.marqueeto.LabelledMarqueeEditText;
import com.hannesdorfmann.mosby.MosbyActivity;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;

import butterknife.Bind;

public class CredentialsActivity extends MosbyActivity {

    @Bind(R.id.sipuada_plugin_android_example_ActionTitle) TextView actionTitle;
    @Bind(R.id.sipuada_plugin_android_example_UsernameMarqueeto) LabelledMarqueeEditText usernameEditText;
    @Bind(R.id.sipuada_plugin_android_example_PrimaryHostMarqueeto) LabelledMarqueeEditText primaryHostEditText;
    @Bind(R.id.sipuada_plugin_android_example_PasswordMarqueeto) LabelledMarqueeEditText passwordEditText;
    @Bind(R.id.sipuada_plugin_android_example_SubmitButton) Button submitButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        Intent intent = getIntent();
        String actionTitleMessage = "Enter new user Credentials";
        actionTitle.setText(actionTitleMessage);
        SipuadaUserCredentials oldUserCredentials = null;
        if (intent != null) {
            oldUserCredentials = intent
                    .getParcelableExtra(SipuadaApplication.KEY_USER_CREDENTIALS);
            if (oldUserCredentials != null) {
                actionTitleMessage = "Update user Credentials";
                actionTitle.setText(actionTitleMessage);
                usernameEditText.setText(oldUserCredentials.getUsername());
                primaryHostEditText.setText(oldUserCredentials.getPrimaryHost());
                passwordEditText.setText(oldUserCredentials.getPassword());
            }
        }
        actionTitle.setVisibility(View.VISIBLE);
        final SipuadaUserCredentials realOldUserCredentials = oldUserCredentials;
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String username = usernameEditText.getText();
                String primaryHost = primaryHostEditText.getText();
                String password = passwordEditText.getText();
                if ((username == null || username.trim().isEmpty())
                        || (primaryHost == null || primaryHost.trim().isEmpty())
                        || (password == null || password.trim().isEmpty())) {

                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(SipuadaApplication.KEY_USERNAME, username);
                intent.putExtra(SipuadaApplication.KEY_PRIMARY_HOST, primaryHost);
                intent.putExtra(SipuadaApplication.KEY_PASSWORD, password);
                if (realOldUserCredentials != null) {
                    intent.putExtra(SipuadaApplication.KEY_USER_CREDENTIALS, realOldUserCredentials);
                }
                setResult(RESULT_OK, intent);
                finish();
            }

        });
    }

}
