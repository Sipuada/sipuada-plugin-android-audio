package org.github.sipuada.plugins.android.audio.example.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.github.sipuada.Sipuada;
import org.github.sipuada.SipuadaApi;
import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaService;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaUserBinding;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SipuadaActivity extends AppCompatActivity {

    private static final String TAG = SipuadaActivity.class.toString();

    @Bind(R.id.sipuada_plugins_andrd_audio_ex_register_button_for_bruno) Button brunoRegisterButton;
    @Bind(R.id.sipuada_plugins_andrd_audio_ex_register_output_for_bruno) TextView brunoRegisterOutput;
    @Bind(R.id.sipuada_plugins_andrd_audio_ex_register_button_for_xibaca) Button xibacaRegisterButton;
    @Bind(R.id.sipuada_plugins_andrd_audio_ex_register_output_for_xibaca) TextView xibacaRegisterOutput;

    private String mBrunoUsername = "bruno";
    private String mXibacaUsername = "xibaca";
    private String mPrimaryHost = "192.168.130.207:5060";

    private SipuadaService mSipuadaService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SipuadaService.SipuadaBinder binder = (SipuadaService.SipuadaBinder) service;
            mSipuadaService = binder.getService();
            mBound = true;

            mSipuadaService.initialize(createUserBindings());

            brunoRegisterButton.setEnabled(true);
            brunoRegisterButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Sipuada sipuada = mSipuadaService.getSipuada(mBrunoUsername, mPrimaryHost);
                    sipuada.registerAddresses(new SipuadaApi.RegistrationCallback() {

                        @Override
                        public void onRegistrationSuccess(final List<String> registeredContacts) {
                            Log.d(TAG, String.format("[onRegistrationSuccess; registeredContacts:{%s}]",
                                    registeredContacts));
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    StringBuilder output = new StringBuilder();
                                    output.append("Success. Registered contacts: ");
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
                                    brunoRegisterOutput.setText(output.toString());
                                    brunoRegisterOutput.setSelected(true);
                                }

                            });
                        }

                        @Override
                        public void onRegistrationFailed(final String reason) {
                            Log.d(TAG, String.format("[onRegistrationFailed; reason:{%s}]", reason));
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    brunoRegisterOutput.setText(String.format("Failed: %s", reason));
                                    brunoRegisterOutput.setSelected(true);
                                }

                            });
                        }

                    });
                }

            });
//            xibacaRegisterButton.setEnabled(true);
//            xibacaRegisterButton.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View view) {
//                    Sipuada sipuada = mSipuadaService.getSipuada(mXibacaUsername, mPrimaryHost);
//                    sipuada.registerAddresses(new SipuadaApi.RegistrationCallback() {
//
//                        @Override
//                        public void onRegistrationSuccess(final List<String> registeredContacts) {
//                            Log.d(TAG, String.format("[onRegistrationSuccess; registeredContacts:{%s}]",
//                                    registeredContacts));
//                            runOnUiThread(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    StringBuilder output = new StringBuilder();
//                                    output.append("Success. Registered contacts: ");
//                                    if (registeredContacts.isEmpty()) {
//                                        output.append("none");
//                                    } else {
//                                        output.append(registeredContacts.get(0));
//                                    }
//                                    for (int i = 1; i < registeredContacts.size(); i++) {
//                                        output.append(", ");
//                                        output.append(registeredContacts.get(i));
//                                    }
//                                    output.append(".");
//                                    xibacaRegisterOutput.setText(output.toString());
//                                    xibacaRegisterOutput.setSelected(true);
//                                }
//
//                            });
//                        }
//
//                        @Override
//                        public void onRegistrationFailed(final String reason) {
//                            Log.d(TAG, String.format("[onRegistrationFailed; reason:{%s}]", reason));
//                            runOnUiThread(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    xibacaRegisterOutput.setText(String.format("Failed: %s", reason));
//                                    xibacaRegisterOutput.setSelected(true);
//                                }
//
//                            });
//                        }
//
//                    });
//                }
//
//            });
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
        }

    };
    private boolean mBound = false;
    private boolean isBusy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sipuada);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SipuadaService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        brunoRegisterButton.setEnabled(false);
        brunoRegisterButton.setOnClickListener(null);
        xibacaRegisterButton.setEnabled(false);
        xibacaRegisterButton.setOnClickListener(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private List<SipuadaUserBinding> createUserBindings() {
        List<SipuadaUserBinding> usersBindings = new LinkedList<>();
        SipuadaApi.SipuadaListener listener = new SipuadaApi.SipuadaListener() {

            @Override
            public boolean onCallInvitationArrived(String callId) {
                Log.d(TAG, String.format("[onCallInvitationArrived; callId:{%s}]", callId));
                return isBusy;
            }

            @Override
            public void onCallInvitationCanceled(String reason, String callId) {
                Log.d(TAG, String.format("[onCallInvitationCanceled; reason:{%s}, callId:{%s}]", reason, callId));
            }

            @Override
            public void onCallInvitationFailed(String reason, String callId) {
                Log.d(TAG, String.format("[onCallInvitationFailed; reason:{%s}, callId:{%s}]", reason, callId));
            }

            @Override
            public void onCallEstablished(String callId) {
                Log.d(TAG, String.format("[onCallEstablished; callId:{%s}]", callId));
            }

            @Override
            public void onCallFinished(String callId) {
                Log.d(TAG, String.format("[onCallFinished; callId:{%s}]", callId));
            }

            @Override
            public void onCallFailure(String reason, String callId) {
                Log.d(TAG, String.format("[onCallFailure; reason:{%s}, callId:{%s}]", reason, callId));
            }

        };
        usersBindings.add(new SipuadaUserBinding(mBrunoUsername, mPrimaryHost,
                mBrunoUsername, listener, getLocalAddresses()));
//        usersBindings.add(new SipuadaUserBinding(mXibacaUsername, mPrimaryHost,
//                mXibacaUsername, listener, getLocalAddresses()));
        return usersBindings;
    }

    private String[] getLocalAddresses() {
        List<String> localAdresses = new LinkedList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                return new String[0];
            }
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
                    for (InterfaceAddress interfaceAddress : addresses) {
                        InetAddress inetAddress = interfaceAddress.getAddress();
                        if (inetAddress instanceof Inet4Address) {
                            String localIpAddress = inetAddress.getHostAddress();
                            int localPort = 50000 + (new Random()).nextInt(10000);
                            localAdresses.add(String.format("%s:%s/TCP", localIpAddress, localPort));
                        }
                    }
                }
            }
        } catch (SocketException ioException) {
            return new String[0];
        }
        return localAdresses.toArray(new String[localAdresses.size()]);
    }

}
