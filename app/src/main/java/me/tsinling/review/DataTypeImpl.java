package me.tsinling.review;

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
    private CopyOnWriteArrayList<Callback> callbacks = new CopyOnWriteArrayList<>();

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

        if (!callbacks.isEmpty()){
            for (Callback callback:callbacks) {
                callback.callback("person register succeed");
            }
        }
    }

    @Override
    public void registerListener(Callback callback) throws RemoteException {
        if (callbacks.contains(callback) || callback==null){
            Log.d(TAG, callback==null
                    ?"registerListener:  callback is null"
                    :"registerListener:  listener already exists ");
            return;
        }
        callbacks.add(callback);
        Log.d(TAG, "registerListener: listener.size: " + callbacks.size());
    }

    @Override
    public void unregisterListener(Callback callback) throws RemoteException {
        if (callbacks.contains(callback)){
            callbacks.remove(callback);
            Log.d(TAG, "unregisterListener: unregister listener succeed " );
        }
        Log.d(TAG, "unregisterListener: listener.size: " + callbacks.size());

    }


    @Override
    public boolean collectionTypes(List<String> list, List<Person> pList, String[] arr) throws RemoteException {
        return list== null || list.isEmpty();
    }
}
