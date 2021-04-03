package me.tsinling.review;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import me.tsinling.aidl.BinderPool;
import me.tsinling.aidl.ICompute;
import me.tsinling.aidl.ICompute2;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    private void doWork() {


        BinderPool binderPool = BinderPool.getInstance(this);
        ICompute compute = binderPool.queryBinder(ICompute.class);

        Log.d(TAG, "first pid "+android.os.Process.myPid());

        try {
            Log.d(TAG, "doWork: add " + compute.add(1,2));
            Log.d(TAG, "doWork add pid "+android.os.Process.myPid());
            Log.d(TAG, "doWork: visit ICompute2");
            ICompute2 compute2 = binderPool.queryBinder(ICompute2.class);
            Log.d(TAG, "doWork: sub " + compute2.sub(11,2));
            Log.d(TAG, "doWork sub pid "+android.os.Process.myPid());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void aidl(View view) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                doWork();
            }
        }.start();
    }
}