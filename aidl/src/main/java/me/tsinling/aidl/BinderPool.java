package me.tsinling.aidl;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

public class BinderPool {
//客户端通过标识符，获取相对应的Binder

    private static final String TAG = "BinderPool";
    private static final String BIND_POOL_SERVICE = "me.tsinling.aidl.BinderPoolService";
    private static Context mContext;

    private IBinderPool mIBinderPool;

    private static volatile BinderPool sInstance;

    private CountDownLatch mConnectBinderPoolCountDownLatch;
    private String remoteServicePackageName;

    /*单例模式，在整个app中只会产生一个对象*/

    private BinderPool(Context context) {

        mContext = context.getApplicationContext();
        // 这个值有可能与服务端的服务 Service 不是一个包名,此处获取的可能是客户端的应用包名,
        // 若是客户端与服务端应用包名不一致,请使用queryBinder方法前先用 setRemoteServicePackage进行配置
        this.remoteServicePackageName = mContext.getPackageName();
        connnectBinderPoolService();

    }

    public static BinderPool getInstance(Context context) {

        synchronized (BinderPool.class) {

            if (sInstance == null) {
                sInstance = new BinderPool(context);
            }
        }

        return sInstance;

    }

    /**
     *  设置远程服务 Service 所在的应用包名.
     * @param remoteServicePackageName BinderPoolService 所在的应用 applicationId
     * @return BinderPool对象.意图实现链式编程
     */
    public  BinderPool setRemoteServicePackage(String remoteServicePackageName) {
        this.remoteServicePackageName = remoteServicePackageName;
        return this;
    }

    private synchronized void connnectBinderPoolService() {

        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);

        Intent intent = new Intent();
        intent.setAction(BIND_POOL_SERVICE);
        intent.setPackage(remoteServicePackageName);
        //intent.setPackage(PACKAGE_NAME);
       // Intent intent = new Intent(mContext,BinderPoolService.class);
        mContext.bindService(intent, mBinderPoolConnection, Context.BIND_AUTO_CREATE);

        try {
            mConnectBinderPoolCountDownLatch.await();
        } catch (InterruptedException e) {

            e.printStackTrace();

        }
        // IBinderPool iBinderPool = queryBinder(IBinderPool.class);
    }


    /**
     *  根据 aidl 文件的类,去服务进程查询获取对一个的Binder实现类
     * @param binderClazz aidl 文件编译出来的 java 类
     * @param <S>
     * @return
     */
    public <S extends android.os.IInterface> S queryBinder(Class<S> binderClazz ) {
        if (null == binderClazz){
            return null;
        }
       // if (!binderClazz.equals(IBinder.class) ){
        if (!android.os.IInterface.class.isAssignableFrom(binderClazz) ){
            // 输入的类的类型必须是 IBinder的子类
            return null;
        }
        binderClazz.getDeclaredClasses();
        String binderClazzName = binderClazz.getName();
       //  android.os.Binder
        Class<?> stubClazz = null;
        try {
            stubClazz = Class.forName(binderClazzName.concat("$Stub"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (stubClazz == null){
            return null;
        }
        Method method = null;
        try {
            method = stubClazz.getDeclaredMethod("asInterface", IBinder.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
                return null;
        }

        IBinder mIBinder = queryBinder(binderClazzName);
        if (mIBinder == null){
            return null;
        }
        S obj = null;
        try {
            obj = (S)method.invoke(null, mIBinder);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /*因为客户端没法收到从Service发送过来的Binder，利用该方法来执行Binder的方法*/

    private IBinder queryBinder(String binderClazzName) {

        IBinder binder = null;

        if (mIBinderPool != null) {
            try {
                binder = mIBinderPool.queryBinder(binderClazzName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return binder;

    }

    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {

        @Override

        public void onServiceConnected(ComponentName name, IBinder service) {

            mIBinderPool = IBinderPool.Stub.asInterface(service);

            try {

                mIBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);

            } catch (RemoteException e) {

                e.printStackTrace();

            }

            mConnectBinderPoolCountDownLatch.countDown();

        }

        @Override

        public void onServiceDisconnected(ComponentName name) {

        }

    };

    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {

        @Override

        public void binderDied() {

            Log.w("BinderPool", "binder died.");

            mIBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);

            mIBinderPool = null;

        }

    };

 


}


