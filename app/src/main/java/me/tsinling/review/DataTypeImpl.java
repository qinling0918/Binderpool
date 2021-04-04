package me.tsinling.review;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * created by tsinling on: 2021/4/3 21:43
 * description:
 */
public  class DataTypeImpl extends IDataType.Stub {
    private static final String TAG = "DataTypeImpl";

    private CopyOnWriteArrayList<Person> people = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<Callback> callbacks = new RemoteCallbackList<>();

    public DataTypeImpl() {
        super();
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
}
