package ece.course.reference;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class ReportActivity extends Activity {
    private ArrayAdapter<String> mReports;
    private boolean  isBound;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);
        mReports = new ArrayAdapter<String>(this, R.layout.report_line);
        ListView lvReport = (ListView) findViewById(R.id.lvReport);
        lvReport.setAdapter(mReports);
        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mkFolder("abc");
            }
        });
    }


    public void onStart() {
        super.onStart();
        if (!isBound) {
            Intent intent = new Intent(this, BackgroundService.class);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void onStop() {
        super.onStop();
        if (isBound)
            unbindService(mServiceConnection);
        finish();
    }

    public int mkFolder(String folderName){ // make a folder under Environment.DIRECTORY_DCIM
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)){
            return 0;
        }
        if ( Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {

            return 0;
        }

        if ( ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if ( ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            }
        }
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),folderName);
        int result = 0;
        if (folder.exists()) {

            result = 2; // folder exist
            saveToFile();
        }else{
            try {
                if (folder.mkdir()) {

                    result = 1; // folder created
                } else {

                    result = 0; // creat folder fails
                }
            }catch (Exception ecp){
                ecp.printStackTrace();
            }
        }
        return result;
    }


    private void saveToFile() {

        File target = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/abc/Test.txt");
        try {
            FileWriter fileWriter = new FileWriter(target);
            int length = mReports.getCount();
            for (int i = length - 1; i >= 0; i--)
                fileWriter.write(mReports.getItem(i) + "\n");
            fileWriter.flush();
            fileWriter.close();
            Toast.makeText(getBaseContext(), "Report Saved!!", Toast.LENGTH_LONG).show();
        }
        catch (IOException ioException) {
            Toast.makeText(getBaseContext(), "Failed To Save Report...", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveToFile();
                    // permission was granted
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BackgroundService.BackgroundBinder binder = (BackgroundService.BackgroundBinder) service;
            BackgroundService backgroundService = binder.getService();
            ArrayList<String> reports = backgroundService.getReport();
            for (String report : reports)
                mReports.add(report);
            isBound = true;
        }
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };



}
