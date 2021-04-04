package me.tsinling.aidl.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import me.tsinling.review.Compute2Impl;
import me.tsinling.review.ComputeImpl;
import me.tsinling.review.DataTypeImpl;
import me.tsinling.review.ICompute;
import me.tsinling.review.ICompute2;
import me.tsinling.review.IDataType;

public class BinderPoolService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        // fixme 此处因为实在一个项目中,仅仅将 BinderPoolService 的 process属性开启.作为另一个进程,
        //  所以为了保证 在提供服务的服务端进程中已经对 binder 的实现类进行配置,所以在这配置,可以看到
        //  MainActivity 中也有这段代码,但是因为两者处于两个进程,BinderPoolHelper虽然是单例模式,
        //  但在 多进程的情况下,单例并不安全.
        // 正常情况下,BinderPoolService 会在服务端应用进程中.在服务端应用进程中使用下面方式进行自定义配置即可.
        BinderPoolHelper.getInstance()
                .putBinder(ICompute.class, new ComputeImpl())
                .putBinder(ICompute2.class, new Compute2Impl())
                .putBinder(IDataType.class, new DataTypeImpl());
    }

    @Override
    public IBinder onBind(Intent intent) {
         return BinderPoolHelper.DEFAULT_IBINDER;
    }

}