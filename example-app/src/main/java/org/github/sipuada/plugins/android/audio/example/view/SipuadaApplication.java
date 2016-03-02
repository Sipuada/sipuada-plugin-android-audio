package org.github.sipuada.plugins.android.audio.example.view;

import android.content.Intent;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialModule;

import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaService;

public class SipuadaApplication extends com.activeandroid.app.Application {

    public static final String TAG = SipuadaApplication.class.toString();

    public static boolean CURRENTLY_BUSY_FROM_DB = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Iconify.with(new MaterialModule());
        Intent intent = new Intent(this, SipuadaService.class);
        startService(intent);
    }

}
