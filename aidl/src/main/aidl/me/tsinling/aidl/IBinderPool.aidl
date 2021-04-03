// IBinderPool.aidl
package me.tsinling.aidl;

//

interface IBinderPool {

    IBinder queryBinder(String binderClassName);
}