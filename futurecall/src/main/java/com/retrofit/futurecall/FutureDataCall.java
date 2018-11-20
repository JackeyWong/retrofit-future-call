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

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by wangjie2013
 * on 18-1-26.
 */


class FutureDataCall<T> extends AbsFutureCall<T> {

    private final FuturePackableCall<T> mPackCall;

    FutureDataCall(FuturePackableCall<T> packCall) {
        checkIsExecuted(packCall);
        this.mPackCall = packCall;
    }

    @NonNull
    @Override
    public FutureCall<T> submit() {
        mPackCall.submit();
        return this;
    }

    @NonNull
    @Override
    public FutureCall<T> enqueue(final OnCallback<T> callback) {
        checkIsExecuted(mPackCall);
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        mPackCall.enqueue(new OnDataCallback<>(callback));
        return this;
    }

    @Override
    public T get(long timeout, TimeUnit unit, boolean throwIfTimeout)
            throws IOException, InterruptedException, TimeoutException {
        Packable<T> pack = mPackCall.get(timeout, unit, throwIfTimeout);
        if (pack == null) {
            return null;
        }
        return pack.data();
    }

    @Override
    public boolean isExecuted() {
        return mPackCall.isExecuted();
    }

    @Override
    public void cancel() {
        mPackCall.cancel();
    }

    @Override
    public boolean isCanceled() {
        return mPackCall.isCanceled();
    }

    @Override
    public Object clone() {
        return new FutureDataCall<>((FuturePackableCall<Object>) mPackCall.clone());
    }

    private static final class OnDataCallback<T> implements OnCallback<Packable<T>> {

        private final OnCallback<T> mCallback;

        OnDataCallback(OnCallback<T> callback) {
            mCallback = callback;
        }

        @Override
        public void onResponse(int code, String message, @NonNull Packable<T> result) {
            mCallback.onResponse(result.code(), result.message(), result.data());
        }

        @Override
        public void onError() {
            mCallback.onError();
        }

    }
}
