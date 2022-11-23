package ece.course.reference;


import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class MainActivity extends AppCompatActivity {


    private BackgroundService mBackgroundService;
    private TextView tvState;
    private boolean isBound;
    private boolean isStarted;

    // Modified: permission request for Android 11
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final String TAG = "PERMISSION_TAG";

    private void requestPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try{
                Log.i(TAG, "requestPermission:try");

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(),null);
                intent.setData(uri);
            }
            catch (Exception e){
                Log.i(TAG, "requestPermission: catch", e);
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            }
        }
    }

    public boolean checkPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager();
        }
        return true;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isBound = false;
        isStarted = false;
        setContentView(R.layout.activity_main);
        tvState = (TextView) findViewById(R.id.tvState);
        Button btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isBound && (!isStarted)) {
                    mBackgroundService.startRun();
                    tvState.setText(R.string.state_running);
                    isStarted = true;
                }
            }
        });
        Button btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isBound && isStarted) {
                    mBackgroundService.stopRun();
                    tvState.setText(R.string.state_stopped);
                    isStarted = false;
                }
            }
        });

        Button btnPictureInMap = (Button) findViewById(R.id.btnReport);

        // Modified: add permission checking
        btnPictureInMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(checkPermission()){
                    Log.d(TAG, "onClick: Permission already granted...");
                }
                else{
                    Log.d(TAG, "onClick: Permission not granted, request...");
                    requestPermission();
                }
                Intent intent = new Intent(MainActivity.this, CustomMarkerClusteringDemoActivity.class);
                startActivity(intent);
            }
        });

//        Button btnReport = (Button) findViewById(R.id.btnReport);
//        btnReport.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
//                startActivity(intent);
//            }
//        });
/*
        Button btnPictureInMap = (Button) findViewById(R.id.btnPersonInMap);
        btnPictureInMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CustomMarkerClusteringDemoActivity.class);
                startActivity(intent);
            }
        });
*/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        super.onResume();
        if (!isBound) {
            Intent intent = new Intent(MainActivity.this, BackgroundService.class);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            mBackgroundService.stopRun();
            tvState.setText(R.string.state_stopped);
            unbindService(mServiceConnection);
            mBackgroundService = null;
            isStarted = false;
            isBound = false;
        }
    }

    public void onBackPressed() {
        if (isStarted) { }
        else
            finish();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BackgroundService.BackgroundBinder binder = (BackgroundService.BackgroundBinder) service;
            mBackgroundService = binder.getService();
            isBound = true;
            isStarted = mBackgroundService.getStarted();
            if (isStarted)
                tvState.setText(R.string.state_running);
            else
                tvState.setText(R.string.state_stopped);
        }
        public void onServiceDisconnected(ComponentName componentName) {
            mBackgroundService.stopRun();
            tvState.setText(R.string.state_stopped);
            isStarted = false;
            isBound = false;
        }
    };



}
