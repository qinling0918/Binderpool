package me.tsinling.review;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * created by tsinling on: 2021/4/3 21:43
 * description: 测试Aidl支持的数据类型.
 */
public  class DataTypeImpl extends IDataType.Stub {
    private static final String TAG = "DataTypeImpl";

    private CopyOnWriteArrayList<Person> people = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<Callback> callbacks = new RemoteCallbackList<>();

    private Context mContext;
    public DataTypeImpl() {
        super();
    }
    // 会有上下文的构造,会执行权限校验.
    public DataTypeImpl(Context mContext) {
        super();
        this.mContext = mContext;
    }

    @Override
    public int basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, char achar, byte aByte, String aString, CharSequence aCharSequence) throws RemoteException {
        return anInt;
    }

    @Override
    public void parcelableTypes(Person person) throws RemoteException {
        if (people.contains(person) || person==null){
            Log.d(TAG, person==null
                    ?"parcelableTypes:  callback is null"
                    :"parcelableTypes:  person already exists ");
            return ;
        }

        people.add(person);

        final int N = callbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
            Callback callback = callbacks.getBroadcastItem(i);
            if (callback!=null){
                callback.callback("person register succeed");
            }
        }
        // fixme 此处有回调给客户端的 callback, 所以要保证 客户端在接收到回调时,执行的不是耗时操作,
        //  或者在线程中执行耗时操作.

        callbacks.finishBroadcast();

    }

    @Override
    public void registerListener(Callback callback) throws RemoteException {
        callbacks.register(callback);
        final int N = callbacks.beginBroadcast();
        callbacks.finishBroadcast();
        Log.d(TAG, "registerListener: listener.size: " + N);
    }

    @Override
    public void unregisterListener(Callback callback) throws RemoteException {
        callbacks.unregister(callback);
        final int N = callbacks.beginBroadcast();
        callbacks.finishBroadcast();
        Log.d(TAG, "unregisterListener: listener.size: " + N);

    }


    @Override
    public boolean collectionTypes(List<String> list, List<Person> pList, String[] arr) throws RemoteException {
        return list== null || list.isEmpty();
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (permissionIsNotOk()) return false;
        return super.onTransact(code, data, reply, flags);
    }

    /**
     *  权限校验. 判断调用方的权限,以及包名是否 "me.tsinling"开头
     *  一个应用想要远程调用服务中的方法,需要使用自定义的权限,包名好需要以"me.tsinling" 开始
     *
     *  此处上下文一直为 null ,所以该方法不会执行.
     * @return
     */
    private boolean permissionIsNotOk() {
        if (mContext!=null){
            int check = mContext.checkCallingOrSelfPermission("me.tsinling.aidl.BINDER_POOL_SERVICE");
            if (check == PackageManager.PERMISSION_DENIED){
                return true;
            }
            String  packageName = null;
            String[] packages = mContext.getPackageManager().getPackagesForUid(getCallingUid());
            if (packages!=null && packages.length>0){
                packageName = packages[0];
            }
            if (!packageName.startsWith("me.tsinling")){
                return true;
            }
        }
        return false;
    }
}
