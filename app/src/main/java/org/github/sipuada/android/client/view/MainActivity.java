package org.github.sipuada.android.client.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.github.sipuada.Sipuada;
import org.github.sipuada.SipuadaApi;
import org.github.sipuada.android.client.R;
import org.github.sipuada.android.client.utils.SipuadaLog;
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
    private Context mContext;

    @OnClick(R.id.bt_call)
    public void call() {
        SipuadaLog.info("Calling: " + etContact.getText().toString() + ", " + etContactDomain.getText().toString());
        sipuada.inviteToCall(etContact.getText().toString(), etContactDomain.getText().toString(),
                new SipuadaApi.CallInvitationCallback() {
                    @Override
                    public void onWaitingForCallInvitationAnswer(String callId) {
                        SipuadaLog.info("onWaitingForCallInvitationAnswer");
                    }

                    @Override
                    public void onCallInvitationRinging(String callId) {
                        SipuadaLog.info("onCallInvitationRinging");
                    }

                    @Override
                    public void onCallInvitationDeclined(String reason) {
                        SipuadaLog.info("onCallInvitationDeclined");
                    }
                });
    }

    @OnClick(R.id.bt_register)
    public void register() {
        SipuadaLog.info("Register: " + etUser.getText().toString()+"(" + localIpAddress + ")" + ", " + etDomain.getText().toString());

        sipuada = null;
        sipuada = new Sipuada(new SipuadaApi.SipuadaListener() {
            @Override
            public boolean onCallInvitationArrived(String s) {
                SipuadaLog.info("onCallInvitationArrived");
                final String callId = s;

                ((Activity)mContext).runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("Call incoming")
                                .setCancelable(false)
                                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        sipuada.acceptCallInvitation(callId);
                                        dialog.dismiss();
                                    }
                                })

                                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        sipuada.declineCallInvitation(callId);
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

                return false;
            }

            @Override
            public void onCallInvitationCanceled(String s, String s1) {
                SipuadaLog.info("onCallInvitationCanceled");
            }

            @Override
            public void onCallInvitationFailed(String s, String s1) {
                SipuadaLog.info("onCallInvitationFailed");
                SipuadaLog.error("Call failed. Reason: " + s);
            }

            @Override
            public void onCallEstablished(String s) {
                SipuadaLog.info("onCallEstablished");
            }

            @Override
            public void onCallFinished(String s) {
                SipuadaLog.info("onCallFinished");
            }

            @Override
            public void onCallFailure(String reason, String callId) {
                SipuadaLog.info("onCallFailed");
            }

        }, etUser.getText().toString(), etDomain.getText().toString(), etPassword.getText().toString(),localIpAddress + ":55500/TCP");

        androidAudioPlugin = new AndroidAudioSipuadaPlugin(etUser.getText().toString(),localIpAddress, this);
        boolean pluginRegister = sipuada.registerPlugin(androidAudioPlugin);
        SipuadaLog.info("Plugin register: " + pluginRegister);

        Thread thread = new Thread() {
            public void run() {
                try {
                    sipuada.registerAddresses(new SipuadaApi.RegistrationCallback() {
                        @Override
                        public void onRegistrationSuccess(
                                List<String> registeredContacts) {
//                                isRegistered = true;
//                                updateCallButtonState();
                            SipuadaLog.info("Successfully Registered");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        localIpAddress = getIPAddress(true);
        ButterKnife.bind(this);
        mContext = this;
        setupSipuada();
//        updateCallButtonState();
    }

    private void setupSipuada() {
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
}
