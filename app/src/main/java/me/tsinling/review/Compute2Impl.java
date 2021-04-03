package me.tsinling.review;

import android.os.RemoteException;
import android.util.Log;

/**
 * created by tsinling on: 2021/4/3 21:43
 * description:
 */
public  class Compute2Impl extends ICompute2.Stub {

    public Compute2Impl() {
        super();
    }

    @Override
    public int sub(int a, int b) throws RemoteException {
        Log.d("BinderPoolHelper", "sub pid "+android.os.Process.myPid());
        return a-b;
    }
}
