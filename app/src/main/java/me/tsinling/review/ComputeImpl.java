package me.tsinling.review;

import android.os.RemoteException;
import android.util.Log;

/**
 * created by tsinling on: 2021/4/3 21:43
 * description:
 */
public  class ComputeImpl extends ICompute.Stub {

    public ComputeImpl() {
        super();
    }

    @Override
    public int add(int a, int b) throws RemoteException {
        Log.d("ComputeImpl", "add pid "+android.os.Process.myPid());
        return a+b;
    }
}
