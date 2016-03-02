package org.github.sipuada.plugins.android.audio.example.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.joanzapata.iconify.IconDrawable;
import com.pedrogomez.renderers.ListAdapteeCollection;
import com.pedrogomez.renderers.RVRendererAdapter;

import org.github.sipuada.plugins.android.audio.example.R;
import org.github.sipuada.plugins.android.audio.example.presenter.SipuadaService;
import org.github.sipuada.plugins.android.audio.example.model.SipuadaUserCredentials;
import org.github.sipuada.plugins.android.audio.example.view.renderers.UserOperationEntriesRenderedBuilder;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SipuadaActivity extends AppCompatActivity {

    private static final int REQUEST_NEW_USER_CREDENTIALS = 1;

    @Bind(R.id.sipuplug_andrdio_example_AppToolbar) Toolbar appToolbar;
    @Bind(R.id.sipuplug_andrdio_example_FloatingActionButton) FloatingActionButton floatingActionButton;
    @Bind(R.id.sipuplug_andrdio_example_RecyclerView) RecyclerView recyclerView;
    @Bind(R.id.sipuplug_andrdio_example_EmptyTextView) TextView emptyView;

    private RVRendererAdapter<SipuadaUserCredentials> adapter;
    private SipuadaService mSipuadaService;
    private boolean mBoundToSipuadaService = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SipuadaService.SipuadaBinder binder = (SipuadaService.SipuadaBinder) service;
            mSipuadaService = binder.getService();
            mBoundToSipuadaService = true;
            adapter.clear();
            List<SipuadaUserCredentials> usersCredentials = new Select()
                    .from(SipuadaUserCredentials.class).execute();
            adapter.addAll(new ListAdapteeCollection<>(usersCredentials));
            if (usersCredentials.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
            IconDrawable iconDrawable = new IconDrawable(getApplicationContext(), "md-add")
                    .actionBarSize().colorRes(android.R.color.black);
            floatingActionButton.setImageDrawable(iconDrawable);
            floatingActionButton.setEnabled(true);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), NewUserCredentialsDialog.class);
                    startActivityForResult(intent, REQUEST_NEW_USER_CREDENTIALS);
                }

            });
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
        setSupportActionBar(appToolbar);
        appToolbar.setTitle(getTitle());
        appToolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
        adapter = new RVRendererAdapter<>(getLayoutInflater(),
                new UserOperationEntriesRenderedBuilder(this),
                new ListAdapteeCollection<>(Arrays.asList(new SipuadaUserCredentials[]{})));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        floatingActionButton.setEnabled(false);
        floatingActionButton.setOnClickListener(null);
        Intent intent = new Intent(this, SipuadaService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_USER_CREDENTIALS
                && resultCode == RESULT_OK) {
            String username = data.getStringExtra("username");
            String primaryHost = data.getStringExtra("primaryHost");
            String password = data.getStringExtra("password");
            mSipuadaService.createSipuada(new SipuadaUserCredentials(username, primaryHost, password));
            adapter.clear();
            List<SipuadaUserCredentials> usersCredentials = new Select()
                    .from(SipuadaUserCredentials.class).execute();
            adapter.addAll(new ListAdapteeCollection<>(usersCredentials));
            adapter.notifyDataSetChanged();
        }
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
