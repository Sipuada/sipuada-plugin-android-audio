package org.github.sipuada.plugins.android.audio.example.presenter;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.SipuadaViewApi;

import java.util.List;

public abstract class SipuadaPresenter<V extends SipuadaViewApi> extends MvpBasePresenter<V>
        implements SipuadaPresenterApi<V> {

    protected SipuadaService mSipuadaService;
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SipuadaService.SipuadaBinder binder = (SipuadaService.SipuadaBinder) service;
            mSipuadaService = binder.getService();
            mSipuadaService.registerSipuadaPresenter(SipuadaPresenter.this);
            if (isViewAttached()) {
                doUponServiceConnected();
                //noinspection ConstantConditions
                getView().sipuadaServiceConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mSipuadaService = null;
            if (isViewAttached()) {
                doUponServiceDisconnected();
                //noinspection ConstantConditions
                getView().sipuadaServiceDisconnected();
            }
        }

    };

    @Override
    public boolean sipuadaServiceIsConnected() {
        return mSipuadaService != null;
    }

    @Override
    public void bindToSipuadaService() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().doBindToSipuadaService(mConnection);
        }
    }

    @Override
    public void unbindFromSipuadaService() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().doUnbindFromSipuadaService(mConnection);
        }
    }

    protected abstract void doUponServiceConnected();

    protected abstract void doUponServiceDisconnected();

}
