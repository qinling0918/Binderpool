package me.tsinling.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BinderPoolService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        Log.d("BinderPoolService"," onBind: iBinder ==null"+ (BinderPoolHelper.DEFAULT_IBINDER == null));
         return BinderPoolHelper.DEFAULT_IBINDER;
    }

    /*继承IBinderPool接口，重写方法*/

   /* public static class BinderPoolImpl extends IBinderPool.Stub {
        private final static HashMap<String,IBinder> mIBinders = new HashMap<>();


        private BinderPoolImpl() {
            super();
        }
        private static class SingleTon {
            private static final BinderPoolImpl mBinderPoolImpl = new BinderPoolImpl();
        }
        public static BinderPoolImpl getInstance(){
            return SingleTon.mBinderPoolImpl;
        }



        @Override
        public IBinder queryBinder(String binderClazzName) throws RemoteException {
            if (null == binderClazzName){
                return null;
            }

            return mIBinders.get(binderClazzName);
        }
    }*/
}