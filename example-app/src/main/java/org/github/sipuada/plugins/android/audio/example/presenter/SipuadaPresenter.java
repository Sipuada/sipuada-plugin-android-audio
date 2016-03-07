package org.github.sipuada.plugins.android.audio.example.presenter;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import org.github.sipuada.plugins.android.audio.example.view.SipuadaViewApi;

public abstract class SipuadaPresenter<V extends SipuadaViewApi> extends MvpBasePresenter<V>
        implements SipuadaPresenterApi<V> {

    protected SipuadaService sipuadaService;
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SipuadaService.SipuadaBinder binder = (SipuadaService.SipuadaBinder) service;
            sipuadaService = binder.getService();
            sipuadaService.registerSipuadaPresenter(SipuadaPresenter.this);
            doUponServiceConnected();
            if (isViewAttached()) {
                //noinspection ConstantConditions
                getView().sipuadaServiceConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            sipuadaService = null;
            doUponServiceDisconnected();
            if (isViewAttached()) {
                //noinspection ConstantConditions
                getView().sipuadaServiceDisconnected();
            }
        }

    };

    @Override
    public boolean sipuadaServiceIsConnected() {
        return sipuadaService != null;
    }

    @Override
    public void bindToSipuadaService() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().doBindToSipuadaService(connection);
        }
    }

    @Override
    public void unbindFromSipuadaService() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().doUnbindFromSipuadaService(connection);
        }
    }

    protected abstract void doUponServiceConnected();

    protected abstract void doUponServiceDisconnected();

}
