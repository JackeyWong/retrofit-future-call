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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Response;

/**
 * Created by wangjie2013
 * on 18-1-26.
 */


class FuturePackableCall<T> extends AbsFutureCall<Packable<T>> {

    static final int CODE_INTERCEPT_PACK = 498;

    private static final String TAG = FuturePackableCall.class.getSimpleName();

    private final FutureResponseCall<Packable<T>> mResponseCall;

    private final Class<? extends Packable> mPackClz;

    private final List<PackableFilter> mFilters;

    FuturePackableCall(FutureResponseCall<Packable<T>> responseCall,
            Class<? extends Packable<T>> packClz, List<PackableFilter> filters) {
        this.mResponseCall = responseCall;
        this.mPackClz = packClz;
        this.mFilters = filters;
    }

    @NonNull
    @Override
    public FutureCall<Packable<T>> submit() {
        mResponseCall.submit();
        return this;
    }

    @NonNull
    @Override
    public FutureCall<Packable<T>> enqueue(final OnCallback<Packable<T>> callback) {
        checkIsExecuted(mResponseCall);
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        mResponseCall.enqueue(new OnPackCallback<>(callback, mFilters));
        return this;
    }

    @Override
    @Nullable
    public Packable<T> get(long timeout, TimeUnit unit, boolean throwIfTimeout)
            throws IOException, InterruptedException, TimeoutException {
        Response<Packable<T>> response = mResponseCall.get(timeout, unit, throwIfTimeout);
        if (response == null || response.code() == FutureResponseCall.CODE_FAIL_REQUEST) {
            return null;
        }
        return response.body();
    }

    @Override
    public boolean isExecuted() {
        return mResponseCall.isExecuted();
    }

    @Override
    public void cancel() {
        mResponseCall.cancel();
    }

    @Override
    public boolean isCanceled() {
        return mResponseCall.isCanceled();
    }

    @SuppressWarnings({"MethodDoesntCallSuperMethod", "unchecked"})
    @Override
    public Object clone() {
        return new FuturePackableCall(
                (FutureResponseCall<Packable>) mResponseCall.clone(),
                mPackClz, mFilters);
    }

    private static final class OnPackCallback<R> implements OnCallback<Response<Packable<R>>> {

        private final OnCallback<Packable<R>> mCallback;

        private final List<PackableFilter> mFilters;

        OnPackCallback(OnCallback<Packable<R>> callback,
                List<PackableFilter> filter) {
            mCallback = callback;
            mFilters = filter;
        }

        @Override
        public void onResponse(int code, String message,
                @NonNull Response<Packable<R>> result) {
            if (result.isSuccessful()) {
                final Packable<R> pack = result.body();
                if (doIntercept(pack)) {
                    mCallback.onResponse(CODE_INTERCEPT_PACK, "packable intercepted", null);
                    return;
                }
                mCallback.onResponse(result.code(), result.message(), pack);
            } else {
                mCallback.onError();
            }
        }

        @Override
        public void onError() {
            mCallback.onError();
        }

        @SuppressWarnings("unchecked")
        private boolean doIntercept(Packable pack) {
            if (pack == null) {
                return false;
            }
            PackableFilter[] filters =
                    mFilters != null ? mFilters.toArray(new PackableFilter[mFilters.size()]) : null;
            for (int i = 0; filters != null && i < filters.length; i++) {
                PackableFilter f = filters[i];
                if (f != null && f.onFilter(pack)) {
                    Log.d(TAG, "packable onFilter success: " + f);
                    return true;
                }
            }
            return false;
        }
    }
}
