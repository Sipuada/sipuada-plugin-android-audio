package org.github.sipuada.plugins.android.audio.example.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.guilhermesgb.marqueeto.LabelledMarqueeEditText;

import org.github.sipuada.plugins.android.audio.example.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewUserCredentialsDialog extends AppCompatActivity {

    @Bind(R.id.sipuplug_andrdio_example_UsernameMarqueeto) LabelledMarqueeEditText usernameEditText;
    @Bind(R.id.sipuplug_andrdio_example_PrimaryHostMarqueeto) LabelledMarqueeEditText primaryHostEditText;
    @Bind(R.id.sipuplug_andrdio_example_PasswordMarqueeto) LabelledMarqueeEditText passwordEditText;
    @Bind(R.id.sipuplug_andrdio_example_SubmitButton) Button submitButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_new_user_credentials);
        ButterKnife.bind(this);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
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
                intent.putExtra("username", username);
                intent.putExtra("primaryHost", primaryHost);
                intent.putExtra("password", password);
                setResult(RESULT_OK, intent);
                finish();
            }

        });
    }

}
