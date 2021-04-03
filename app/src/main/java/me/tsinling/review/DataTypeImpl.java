package me.tsinling.review;

import android.os.RemoteException;

import java.util.List;

/**
 * created by tsinling on: 2021/4/3 21:43
 * description:
 */
public  class DataTypeImpl extends IDataType.Stub {

    public DataTypeImpl() {
        super();
    }


    @Override
    public int basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, char achar, byte aByte, String aString, CharSequence aCharSequence) throws RemoteException {
        return anInt;
    }

    @Override
    public Person parcelableTypes(Person person) throws RemoteException {
        person.setName(person.getName()+"123");
        return person;
    }

    @Override
    public void aidlTypes(Callback callback) throws RemoteException {
        callback.callback("123456");
    }

    @Override
    public boolean collectionTypes(List<String> list, List<Person> pList, String[] arr) throws RemoteException {
        return list== null || list.isEmpty();
    }
}
