package org.github.sipuada.android.client.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.github.sipuada.android.client.R;
import org.github.sipuada.android.client.utils.SipuadaLog;
import org.github.sipuada.Sipuada;
import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugin.android.audio.AndroidAudioSipuadaPlugin;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.et_user) EditText etUser;
    @Bind(R.id.et_domain) EditText etDomain;
    @Bind(R.id.et_password) EditText etPassword;
    @Bind(R.id.et_contact) EditText etContact;
    @Bind(R.id.et_contact_domain) EditText etContactDomain;
    @Bind(R.id.bt_call) Button btCall;

    private Sipuada sipuada;
    private String localIpAddress;
    private Boolean isRegistered = false;
    private AndroidAudioSipuadaPlugin androidAudioPlugin;

    @OnClick(R.id.bt_call)
    public void call() {
        sipuada.inviteToCall(etContact.getText().toString(), etContactDomain.getText().toString(),
                new SipuadaApi.CallInvitationCallback() {
                    @Override
                    public void onWaitingForCallInvitationAnswer(String callId) {

                    }

                    @Override
                    public void onCallInvitationRinging(String callId) {

                    }

                    @Override
                    public void onCallInvitationDeclined(String reason) {

                    }
                });
    }

    @OnClick(R.id.bt_register)
    public void register() {
        if (sipuada == null) {
            sipuada = new Sipuada(new SipuadaApi.SipuadaListener() {
                @Override
                public boolean onCallInvitationArrived(String s) {
                    return false;
                }

                @Override
                public void onCallInvitationCanceled(String s, String s1) {

                }

                @Override
                public void onCallInvitationFailed(String s, String s1) {

                }

                @Override
                public void onCallEstablished(String s) {

                }

                @Override
                public void onCallFinished(String s) {

                }
            }, etUser.getText().toString(), etDomain.getText().toString(),etPassword.getText().toString(),localIpAddress + ":55500/TCP");

            Thread thread = new Thread() {
                public void run() {
                    try {
                        sipuada.register(new SipuadaApi.RegistrationCallback() {
                            @Override
                            public void onRegistrationSuccess(
                                    List<String> registeredContacts) {
                                isRegistered = true;
                                updateCallButtonState();
                                SipuadaLog.info("Successfully Registered");
                            }

                            @Override
                            public void onRegistrationRenewed() {
                                Log.v("TESTE", " Registration Renewed");
                            }

                            @Override
                            public void onRegistrationFailed(String reason) {
                                SipuadaLog.error("Failure to register, reason: " + reason);
                            }
                        });
                    } catch (Exception e) {
                        SipuadaLog.error("Failure to register", e);
                    }
                }
            };
            thread.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        localIpAddress = getIPAddress(true);
        ButterKnife.bind(this);
        updateCallButtonState();
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface netWorkInterface : interfaces) {
                List<InetAddress> addressList = Collections.list(netWorkInterface.getInetAddresses());
                for (InetAddress address : addressList) {
                    if (!address.isLoopbackAddress()) {
                        String stringAddress = address.getHostAddress();
                        boolean isIPv4 = stringAddress.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return stringAddress;
                        } else {
                            if (!isIPv4) {
                                int delim = stringAddress.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? stringAddress.toUpperCase() : stringAddress.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            SipuadaLog.error("Failure to get IP Address", ex);
        }
        SipuadaLog.error("Failure to get IP Address");
        return "";
    }

    public void updateCallButtonState() {
        if (isRegistered) {
            btCall.setEnabled(true);
        } else {
            btCall.setEnabled(false);
        }
    }
}
