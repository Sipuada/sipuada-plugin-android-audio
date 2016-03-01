package org.github.sipuada.plugins.android.audio.example.view;

import android.app.Application;
import android.content.Intent;

import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaService;

public class SipuadaApplication extends Application {

    public static final String TAG = "SipuadaApplication";
//    private static final String KEY_PREFIX = "sipuada_plugins_andrd_audio_ex_";
//    private static final String CURRENT_USERS_BINDINGS = KEY_PREFIX + "current_users_bindings";

    public static final String PRIMARY_HOST_FROM_DB = "192.168.130.207:5060";
    public static final String[] USERNAMES_FROM_DB = {"bruno", "xibaca"};
    public static boolean CURRENTLY_BUSY_FROM_DB = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, SipuadaService.class);
        startService(intent);
    }

}
