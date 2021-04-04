package me.tsinling.aidl.client;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import me.tsinling.aidl.IBinderPool;

public class BinderPool {
//客户端通过标识符，获取相对应的Binder

    private static final String TAG = "BinderPool";
    private static final String BIND_POOL_SERVICE = "me.tsinling.aidl.BINDER_POOL_SERVICE";
    private  Context mContext;

    private IBinderPool mIBinderPool;

    private static volatile BinderPool sInstance;

    private CountDownLatch mConnectBinderPoolCountDownLatch;
    private String remoteServicePackageName;
    private Query mQuery;

    private static ConcurrentHashMap<Class, IInterface> sServices = new ConcurrentHashMap<Class, IInterface>();
    private BinderPool() {

    }

    /*单例模式，在整个app中只会产生一个对象*/

    private BinderPool(Context context) {
        this(context,context.getPackageName());

    }

    private BinderPool(Context context, String remoteServicePackageName) {
        mContext = context.getApplicationContext();
        // 这个值有可能与服务端的服务 Service 不是一个包名,此处获取的可能是客户端的应用包名,
        // 若是客户端与服务端应用包名不一致,请使用queryBinder方法前先用 setRemoteServicePackage进行配置
        this.remoteServicePackageName = remoteServicePackageName;
       // connnectBinderPoolService();
    }

   /* public static BinderPool getInstance(Context context) {

        synchronized (BinderPool.class) {
            if (sInstance == null) {
                sInstance = new BinderPool(context);
            }
        }
        return sInstance;
    }*/

    private static final class SingleTon{
        private static final BinderPool sInstance = new BinderPool();
    }

    private static BinderPool getInstance() {
        return SingleTon.sInstance;
    }

    public static void init(Context context) {
         init(context,context.getPackageName());
    }

    /**
     *  设置远程服务 Service 所在的应用包名.
     * @param remoteServicePackageName BinderPoolService 所在的应用 applicationId
     * @return BinderPool对象.意图实现链式编程
     */
    public static void init(Context context,String remoteServicePackageName) {
        getInstance().mContext = context.getApplicationContext();
        // 这个值有可能与服务端的服务 Service 不是一个包名,此处获取的可能是客户端的应用包名,
        // 若是客户端与服务端应用包名不一致,请使用queryBinder方法前先用 setRemoteServicePackage进行配置
        getInstance().remoteServicePackageName = remoteServicePackageName;
    }

    /**
     *  初始化后 异步绑定,无阻塞.
     * @param context
     * @param remoteServicePackageName
     */
    public static void initAsyncBind(Context context,String remoteServicePackageName) {
        init(context,remoteServicePackageName);
        getInstance().asyncBind(null);
    }

    /**
     *  初始化之后 进行绑定.  同步方法,会有阻塞
     * @param context
     * @param remoteServicePackageName
     */
    public static void initWithBind(Context context,String remoteServicePackageName) {
        init(context,remoteServicePackageName);
        getInstance().bind();
    }
    private ServiceConnectListener listener;
    public interface ServiceConnectListener {
         void onServiceConnected(Query mQuery) ;
    }

    private  boolean asyncBind(ServiceConnectListener listener ) {
        if (isNotInited()) return false;

        if (mQuery!=null &&  null!=listener){
            listener.onServiceConnected(mQuery);
            return true;
        }
        this.listener = listener;
        return bindRemoteService();
    }

    private boolean isNotInited() {
        return mContext==null || remoteServicePackageName == null;

    }

    /**
     *  与远程服务绑定,获取线程池匹配器
     * @return
     */
    private  Query bind() {
        if (isNotInited()) return null;
        //this.listener = null;
        if (mQuery!=null ){
            return mQuery;
        }
        connnectBinderPoolService();
        return mQuery;


    }

    /**
     *  获取远程服务所提供的服务类,此处将获取到的对象保存引用.
     *  使用该方法会造成阻塞,在第一次连接远程服务时会阻塞,所以使用该方法时,需要保证在线程中使用.
     * @param binderClazz
     * @param <S>
     * @return
     */
    public static <S extends IInterface> S queryBinder(Class<S> binderClazz ){
        Query mQuery = getInstance().bind();
        if (mQuery==null){
            return null;
        }
        if (null == binderClazz) {
            throw new IllegalArgumentException("binderClazz can not be null");
        }
        S service = (S) sServices.get(binderClazz);

        if (service == null){
            service = mQuery.queryBinder(binderClazz);
        }
        // 若是仍是获取不到,则有可能是服务端未对提供的服务进行提供支持,

        if (service!=null && service.asBinder().isBinderAlive()){
            sServices.put(binderClazz,service);
            return service;
        }

        return null;
    }

    public  static boolean asyncQueryBinder(ServiceConnectListener listener) {
        return getInstance().asyncBind(listener);
    }

   /* public static BinderPool getInstance(Context context,String remoteServicePackageName) {

        synchronized (BinderPool.class) {
            if (sInstance == null) {
                sInstance = new BinderPool(context,remoteServicePackageName);
            }
        }
        return sInstance;
    }*/


   /* public  BinderPool setRemoteServicePackage(String remoteServicePackageName) {
        this.remoteServicePackageName = remoteServicePackageName;
        return this;
    }*/

    private synchronized void connnectBinderPoolService() {

        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);
        bindRemoteService();
        try {
            mConnectBinderPoolCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
    }

    private boolean bindRemoteService() {
        Intent intent = new Intent();
        intent.setAction(BIND_POOL_SERVICE);
        intent.setPackage(remoteServicePackageName);
        //intent.setPackage(PACKAGE_NAME);
        // Intent intent = new Intent(mContext,BinderPoolService.class);
        return mContext.bindService(intent, mBinderPoolConnection, Context.BIND_AUTO_CREATE);
    }




    public static final class Query{
        private IBinderPool mIBinderPool;

        public Query(IBinderPool mIBinderPool) {
            this.mIBinderPool = mIBinderPool;
        }

        /**
         *  根据 aidl 文件的类,去服务进程查询获取对一个的Binder实现类
         * @param binderClazz aidl 文件编译出来的 java 类
         * @param <S>
         * @return
         */
        public <S extends IInterface> S queryBinder(Class<S > binderClazz ) {
            if (null == binderClazz){
                return null;
            }
            // if (!binderClazz.equals(IBinder.class) ){
            if (!IInterface.class.isAssignableFrom(binderClazz) ){
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



        public boolean isBinderAlive(){
            return  isBinderAlive(mIBinderPool);
        }

        private boolean isBinderAlive(IInterface mIBinder){
            return  mIBinder != null
                    && mIBinder.asBinder().isBinderAlive();
        }

        private IBinder queryBinder(String binderClazzName) {

            IBinder binder = null;
            if ( mIBinderPool != null) {

                try {
                    binder = mIBinderPool.queryBinder(binderClazzName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            return binder;

        }
    }

    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {

        @Override

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "BinderPool: onServiceConnected " );
             mIBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                // 为 Binder 设置一个死亡代理,若是 Binder 死亡将会受到通知.
                mIBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mQuery = new Query(mIBinderPool);


            if (listener!=null){
                listener.onServiceConnected(mQuery);
            }
            if (mConnectBinderPoolCountDownLatch!=null){
                mConnectBinderPoolCountDownLatch.countDown();
            }

        }

        @Override

        public void onServiceDisconnected(ComponentName name) {
           /* if (listener!=null){
                listener.onServiceDisconnected();
            }*/
            // todo 可以在这里重连,也可以在DeathRecipient.binderDied 中重连,
            //此处为 主线程,
        }

    };

    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {

        @Override

        public void binderDied() {

            Log.w("BinderPool", "binder died.");
            if (mIBinderPool!=null){
                mIBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);
                mIBinderPool = null;
            }
            mQuery = null;

            // todo : 这里可以重新绑定远程 service,此处为子线程(客户端的 Binder 线程池红)
            // 不可以访问 UI
            // bind

        }

    };

 


}


