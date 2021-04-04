// IDataType.aidl
package me.tsinling.review;
import me.tsinling.review.Person;
import me.tsinling.review.Callback;
// Declare any non-default types here with import statements

interface IDataType {
    /**
     * 基本数据类型.
     */
    int basicTypes(int anInt, long aLong, boolean aBoolean,
        float aFloat,double aDouble, char achar,byte aByte,
          String aString,CharSequence aCharSequence);


  /**
     * 实现Parcelable的类 ,值传递 ,需要 import
     */
    void parcelableTypes(in Person person);

      /**
      * 其他AIDL定义的AIDL接口,传递的是引用 ,需要 import
      */
   // void aidlTypes(in Callback callback);
    void registerListener(in Callback callback);
    void unregisterListener(in Callback callback);

   /**
    * 集合类. 元素为基本数据类型以及 实现Parcelable的类
    List，Map内的元素必须是AIDL支持CharSequence, 	的类型；
    */
       boolean collectionTypes(in List<String> list, in List<Person> pList,in String[] arr);

    // AIDL中的定向 tag 表示了在跨进程通信中数据的流向，其中 in 表示数据只能由客户端流向服务端，
    // out 表示数据只能由服务端流向客户端，而 inout 则表示数据可在服务端与客户端之间双向流通。
    //其中，数据流向是针对在客户端中的那个传入方法的对象而言的。in 为定向 tag 的话
    //表现为服务端将会接收到一个那个对象的完整数据，但是客户端的那个对象不会因为服务端对传参的修改
    //而发生变动；out 的话表现为服务端将会接收到那个对象的参数为空的对象，但是在服务端对接收到的
    //空对象有任何修改之后客户端将会同步变动；inout 为定向 tag 的情况下，服务端将会接收到客户端
    //传来对象的完整信息，并且客户端将会同步服务端对该对象的任何变动

}