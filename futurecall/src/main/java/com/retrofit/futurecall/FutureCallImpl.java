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

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Response;

/**
 * Created by wangjie2013
 * on 18-1-27.
 */


class FutureCallImpl<T> extends AbsFutureCall<T> {

    private final FutureResponseCall<T> mDelegate;

    FutureCallImpl(FutureResponseCall<T> responseCall) {
        this.mDelegate = responseCall;
    }

    @NonNull
    @Override
    public FutureCall<T> submit() {
        mDelegate.submit();
        return this;
    }

    @NonNull
    @Override
    public FutureCall<T> enqueue(final OnCallback<T> callback) {
        checkIsExecuted(mDelegate);
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        mDelegate.enqueue(new OnCallback<Response<T>>() {
            @Override
            public void onResponse(int code, String message, Response<T> result) {
                callback.onResponse(code, message, result.body());
            }

            @Override
            public void onError() {
                callback.onError();
            }
        });
        return this;
    }

    @Nullable
    @Override
    public T get(long timeout, TimeUnit unit, boolean throwIfTimeout)
            throws IOException, InterruptedException, TimeoutException {
        Response<T> resp = mDelegate.get(timeout, unit, throwIfTimeout);
        return resp != null ? resp.body() : null;
    }

    @Override
    public boolean isExecuted() {
        return mDelegate.isExecuted();
    }

    @Override
    public void cancel() {
        mDelegate.cancel();
    }

    @Override
    public boolean isCanceled() {
        return mDelegate.isCanceled();
    }

    @Override
    public Object clone() {
        return new FutureCallImpl(mDelegate);
    }
}