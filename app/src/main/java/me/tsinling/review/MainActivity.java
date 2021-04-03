package me.tsinling.review;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import me.tsinling.aidl.client.BinderPool;
import me.tsinling.aidl.server.BinderPoolHelper;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 将提供服务的一方称为服务端, 使用服务的一端称为客户端

        // todo 这块代码只会出现在 客户端
        // 初始化,建议放到 application 中
        BinderPool.init(this);
        // 若是客户端与服务端不再一个应用内,则必须使用两参初始方法,用以提供服务端所在的包名.
       // BinderPool.init(this,"远程 service 所在应用包名");



        // todo 此处可以放在提供远程服务的一端.此例子代码出现在这里
        //  是因为在一个应用中,若是使用的时候,这快代码只会出现在服务端
        // 若是有新的 aidl 则在此处添加.
        // key 为 aidl 文件经过编译后生成的java 类文件,
        // value 则为 aidl 编译出后的 Stub 对应的实现类的实例.
        BinderPoolHelper.getInstance()
                .putBinder(ICompute.class,new ComputeImpl())
                .putBinder(ICompute2.class,new Compute2Impl())
                .putBinder(IDataType.class,new Compute2Impl());

    }

    private void doWork(int a,int b) throws RemoteException{

        ICompute compute = BinderPool.queryBinder(ICompute.class);
        Log.d(TAG, "doWork: add " + compute.add(a,b));
        ICompute2 compute2 = BinderPool.queryBinder(ICompute2.class);
        Log.d(TAG, "doWork: sub " + compute2.sub(a,b));
        IDataType dataType = BinderPool.queryBinder(IDataType.class);
        Log.d(TAG, "doWork: parcelableTypes " + dataType.parcelableTypes(new Person()));
        Log.d(TAG, "doWork: collectionTypes " + dataType.collectionTypes(new ArrayList<>(),new ArrayList<>(),new String[]{"1","2"}));
    }


    /**
     *  异步,在服务连接成功后 可以获取对应的 binder 实例.
     *  连接成功后 不再调用.会直接获取保存的Query 实例.
     *  建议使用 queryBinder方法,但是需要保证已经成功建立连接.
     * @param view
     */
    public void async(View view) {

        BinderPool.asyncQueryBinder(new BinderPool.ServiceConnectListener() {
            @Override
            public void onServiceConnected(BinderPool.Query mQuery) {

                try {
                    doWork(1,1);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });



    }

    /**
     * 同步绑定 Service,首次连接远程服务,会有阻塞. 所以建议在线程中使用.
     */
    public void sync(View view) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    doWork(9,6);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}