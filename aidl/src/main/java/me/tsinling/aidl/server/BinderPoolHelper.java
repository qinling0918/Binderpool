package me.tsinling.aidl.server;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import java.util.HashMap;

import me.tsinling.aidl.IBinderPool;

/**
 * created by tsinling on: 2021/4/3 00:00
 * description: Binder 连接池帮助类.
 */
public class BinderPoolHelper {
    private final  HashMap<Class<? extends IInterface>, IBinder> mIBinders = new HashMap<>();
    public static final  IBinder DEFAULT_IBINDER = new BinderPoolImpl();

    private BinderPoolHelper() {
        // 此处将 Binder连接池的对象 默认赋值,其实没有意义.
        mIBinders.put(IBinderPool.class,DEFAULT_IBINDER);
        // todo  在这里添加 Binder 对象实现类.注意 key 值与 aidl 文件名保持一致,方便客户端通过
        // 相同的aidl 文件名来查询获取对应的 实现类.
        // todo 也可以使用 putBinder()方法在类外扩展,此处是实现默认 Binder 类时使用.

       // mIBinders.put(ICompute.class,new ComputeImpl());
       // mIBinders.put(ICompute2.class,new Compute2Impl());
    }
    private static class SingleTon {
        private static final BinderPoolHelper mBinderPoolImpl = new BinderPoolHelper();
    }
    public static BinderPoolHelper getInstance(){
        return SingleTon.mBinderPoolImpl;
    }
    private static IBinder queryBinder(String binderClazzName){
        if (null == binderClazzName){
            // fixme 提示对应值不能为空. throw 出异常
            return null;
        }
        try {
            return getInstance().mIBinders.get(Class.forName(binderClazzName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *  提供出去 ,灵活扩展
     * @param binderClazz Binder 类  需要与 aidl 文件一致
     * @param binder 服务端aidl对应的实现类.
     * @return Binder 连接池注帮助类.
     */

    public  BinderPoolHelper putBinder(Class<? extends IInterface> binderClazz, IBinder binder){
      //  BinderPoolHelper helper = getInstance();
        if (binder == null || binderClazz==null){
            // fixme 提示对应值不能为空. throw 出异常
            return this;
        }
        mIBinders.put(binderClazz,binder);
        return this;
    }

    /**
     *  Binder 连接池的默认实现类.
     */
  private static class BinderPoolImpl extends IBinderPool.Stub {

        private BinderPoolImpl() {
            super();
        }

        @Override
        public IBinder queryBinder(String binderClazzName) throws RemoteException {
            return BinderPoolHelper.queryBinder(binderClazzName);
        }
    }



}