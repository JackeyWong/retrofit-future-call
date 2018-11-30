FutureCall
=
A retrofit 'call' adapter factory which you can request data 
synchronously and support for expanding common packaging protocol.

Get Started
=
Many times when we define a protocol, we need a common protocol 
and a business protocol, such as

```json
{
    "resultMessage":"success",
    "resultCode":0,
    "timestamp":1541417793,
    "result":{
        /* some business protocol */
    }
}
```

Use FutureCall you can get the business protocol directly.

Define common protocol wrapper
-
Common protocol must implement Packable, Such as

```java
public class Pack<T> implements Packable<T> {

    public long timestamp;

    public String time;

    String resultMessage;

    int resultCode;

    T result;

    @Override
    public T data() {
        return this.result;
    }

    @Override
    public int code() {
        return this.resultCode;
    }

    @Override
    public String message() {
        return this.resultMessage;
    }

}
```

Build Retrofit
-

```java
Retrofit retrofit = new Retrofit.Builder() 
    .baseUrl("http://your/host/")
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(GsonConverterFactory.create(new Gson()))
    .addCallAdapterFactory(FutureCallAdapterFactory.create(Pack.class))
    .build();
```

Define Retrofit Interface
-

```java
public interface IMocky {

    @GET("path")
    FutureCall<Pack<ResultData>> requestPackData();

    @GET("path")
    FutureCall<ResultData> requestResultData();
}
```

Request Data
-

You can request data synchronously or asynchronously.

```java
    IMocky api = mRetrofit.create(IMocky.class);
    // synchronously, need in background
    api.requestResultData().submit().get();
    // asynchronously
    OnCallback<ResultData> callback = new OnCallback<>{
        // ignore
    }
    api.requestResultData().enqueue(callback);
```
Is it very easy, to sample project see more detail.


Developers
=

**Jackey Wong** ([@JackeyWong](https://github.com/JackeyWong))


Thinks
=

The Sample Project use (https://www.mocky.io/) from [@julien_lafont](https://www.twitter.com/julien_lafont)
