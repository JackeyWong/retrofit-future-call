/*
 * Copyright 2016 Jie Wang.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.retrofit.futurecall;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by wangjie2013
 * on 18-1-25.
 */


class FutureResponseCall<R> extends AbsFutureCall<Response<R>> {

    static final int CODE_FAIL_REQUEST = 499;

    private static final String TAG = FutureResponseCall.class.getSimpleName();

    private final Executor mCallbackExecutor;

    private final Call<R> mCall;

    private final Object mLock = new Object();

    private volatile Response<?> mResult;

    FutureResponseCall(Call<R> call, Executor callbackExecutor) {
        if (call == null) {
            throw new NullPointerException("call is null");
        }
        this.mCall = call;
        this.mCallbackExecutor = callbackExecutor;
    }

    private void submitIfNeed() {
        if (mCall.isExecuted()) {
            return;
        }
        submit();
    }

    @NonNull
    @Override
    public FutureCall<Response<R>> submit() {
        final Call<R> call = mCall.isExecuted() ? mCall.clone() : mCall;
        //noinspection unchecked
        call.enqueue(new CallCallback(null));
        return this;
    }

    @NonNull
    @Override
    public FutureCall<Response<R>> enqueue(OnCallback<Response<R>> callback) {
        checkIsExecuted(mCall);
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        mCall.enqueue(new CallCallback<>(callback));
        return this;
    }

    @Override
    @Nullable
    public Response<R> get(long timeout, TimeUnit unit, boolean throwIfTimeout)
            throws IOException, InterruptedException, TimeoutException {
        if (mResult != null) {
            return (Response<R>) mResult;
        }
        submitIfNeed();
        final long waitTime = timeout > 0 ? unit.toMillis(timeout) : -1;
        final Response<R> ret;
        boolean isTimeOut = false;
        try {
            synchronized (mLock) {
                while (unInterrupted() && mResult == null && !isTimeOut) {
                    if (waitTime < 0) {
                        mLock.wait();
                    } else {
                        mLock.wait(waitTime);
                        isTimeOut = true;
                    }
                }
                ret = (Response<R>) mResult;
                mLock.notifyAll();
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
        if (throwIfTimeout && isTimeOut && ret == null) {
            throw new TimeoutException(String.format("timeout: %s, %s", timeout, unit));
        }
        return ret;
    }

    private boolean unInterrupted() {
        return !Thread.currentThread().isInterrupted();
    }

    @Override
    public boolean isExecuted() {
        return mCall.isExecuted();
    }

    @Override
    public void cancel() {
        mCall.cancel();
    }

    @Override
    public boolean isCanceled() {
        return mCall.isCanceled();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Object clone() {
        return new FutureResponseCall<>(mCall.clone(), mCallbackExecutor);
    }

    private void execInCallbackExecutor(Runnable task) {
        final Executor e = mCallbackExecutor;
        if (e != null) {
            e.execute(task);
        } else {
            task.run();
        }
    }

    static final class NoContentResponseBody extends ResponseBody {

        private final MediaType contentType;

        private final long contentLength;

        private final Throwable cause;

        NoContentResponseBody(Throwable t) {
            this(null, -1, t);
        }

        NoContentResponseBody(MediaType contentType, long contentLength, Throwable t) {
            this.contentType = contentType;
            this.contentLength = contentLength;
            this.cause = t;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public BufferedSource source() {
            throw new IllegalStateException("Cannot read raw response body of a converted body.");
        }

        @Override
        public String toString() {
            return "NoContentResponseBody{" +
                    "contentType=" + contentType +
                    ", contentLength=" + contentLength +
                    ", cause=" + cause +
                    '}';
        }
    }

    private class CallCallback<R> implements Callback<R> {

        private final OnCallback<Response<R>> nOnCallback;

        CallCallback(OnCallback<Response<R>> onCallback) {
            this.nOnCallback = onCallback;
        }

        @Override
        public void onResponse(Call<R> call, final Response<R> response) {
            synchronized (mLock) {
                mResult = response;
                mLock.notifyAll();
            }
            if (nOnCallback != null) {
                execInCallbackExecutor(new Runnable() {
                    @Override
                    public void run() {
                        nOnCallback.onResponse(response.code(),
                                response.message(),
                                response);
                    }
                });
            }
        }

        @Override
        public void onFailure(Call<R> call, Throwable t) {
            Log.w(TAG, "response onFailure: " + t.toString());
            synchronized (mLock) {
                mResult = Response.error(CODE_FAIL_REQUEST, new NoContentResponseBody(t));
                mLock.notifyAll();
            }
            if (nOnCallback != null) {
                execInCallbackExecutor(new Runnable() {
                    @Override
                    public void run() {
                        nOnCallback.onError();
                    }
                });
            }
        }
    }
}
