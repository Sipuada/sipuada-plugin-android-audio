package org.github.sipuada.plugins.android.audio.example.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.pedrogomez.renderers.ListAdapteeCollection;
import com.pedrogomez.renderers.RendererAdapter;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaService;
import org.github.sipuada.plugins.android.audio.example.view.renderers.UserOperationEntriesRenderedBuilder;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SipuadaActivity extends AppCompatActivity {

    @Bind(R.id.sipuada_plugins_andrd_audio_ex_user_operation_entries) ListView userOperationEntries;

    private RendererAdapter<String> userOperationEntriesAdapter;
    private SipuadaService mSipuadaService;
    private boolean mBoundToSipuadaService = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SipuadaService.SipuadaBinder binder = (SipuadaService.SipuadaBinder) service;
            mSipuadaService = binder.getService();
            mBoundToSipuadaService = true;
            userOperationEntriesAdapter.clear();
            userOperationEntriesAdapter.addAll(new ListAdapteeCollection<>(Arrays
                    .asList(SipuadaApplication.USERNAMES_FROM_DB)));
            userOperationEntriesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBoundToSipuadaService = false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sipuada);
        ButterKnife.bind(this);
        userOperationEntriesAdapter = new RendererAdapter<>(getLayoutInflater(),
                new UserOperationEntriesRenderedBuilder(this),
                new ListAdapteeCollection<>(Arrays.asList(new String[]{})));
        userOperationEntries.setAdapter(userOperationEntriesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SipuadaService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public SipuadaService getSipuadaService() {
        return mSipuadaService;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBoundToSipuadaService) {
            unbindService(mConnection);
            mBoundToSipuadaService = false;
        }
    }

}
