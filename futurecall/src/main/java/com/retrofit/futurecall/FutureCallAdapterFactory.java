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

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by wangjie2013
 * on 18-1-25.
 */


public class FutureCallAdapterFactory<P extends Packable> extends CallAdapter.Factory {

    private final Class<P> mWrapType;

    private List<PackableFilter<P>> mFilters = new ArrayList<>();

    private FutureCallAdapterFactory(Class<P> wrapType) {
        this.mWrapType = wrapType;
    }

    public static FutureCallAdapterFactory create() {
        return create(null);
    }

    public static <P extends Packable> FutureCallAdapterFactory<P> create(Class<P> wrap) {
        return new FutureCallAdapterFactory<P>(wrap);
    }

    public FutureCallAdapterFactory<P> addFilter(PackableFilter<P> filter) {
        if (filter == null) {
            throw new IllegalArgumentException("filter == null");
        }
        mFilters.add(filter);
        return this;
    }

    public void removeFilter(PackableFilter<P> filter) {
        if (filter == null) {
            return;
        }
        mFilters.remove(filter);
    }

    @Nullable
    @Override
    public CallAdapter<?, ?> get(@NonNull Type returnType, Annotation[] annotations,
            Retrofit retrofit) {
        final Class<?> rawReturnType = getRawType(returnType);
        if (rawReturnType == FutureCall.class) {
            if (!(returnType instanceof ParameterizedType)) {
                throw new IllegalStateException("return type must be parameterized.");
            }
            final Class<? extends Packable> wrapType = mWrapType;
            final Type resType = getParameterUpperBound(0, (ParameterizedType) returnType);
            final Class<?> rawResType = getRawType(resType);
            boolean isPack = false;
            boolean isPackData = false;
            boolean noWrap = false;
            Type type;
            if (wrapType == null) {
                type = resType;
                noWrap = true;
            } else if (rawResType == Response.class) {
                if (!(resType instanceof ParameterizedType)) {
                    throw new IllegalStateException("Response must be parameterized"
                            + " as Response<Foo> or Response<? extends Foo>");
                }
                type = getParameterUpperBound(0, (ParameterizedType) resType);
            } else if (rawResType == wrapType) {
                if (!(resType instanceof ParameterizedType)) {
                    throw new IllegalStateException(wrapType + " must be parameterized"
                            + " as " + wrapType + "<Foo>"
                            + " or " + wrapType + "<? extends Foo>");
                }
                type = resType;
                isPack = true;
            } else if (IData.class.isAssignableFrom(rawResType)) {
                type = ParameterizedTypeAdapter.create(wrapType, resType);
                isPackData = true;
            } else if (rawResType.isArray()
                    && IData.class.isAssignableFrom(rawResType.getComponentType())) {
                type = ParameterizedTypeAdapter.create(wrapType, resType);
                isPackData = true;
            } else if (Collection.class.isAssignableFrom(rawResType)
                    && resType instanceof ParameterizedType
                    && IData.class.isAssignableFrom(
                    getRawType(getParameterUpperBound(0, (ParameterizedType) resType)))) {
                type = ParameterizedTypeAdapter.create(wrapType, resType);
                isPackData = true;
            } else {
                type = resType;
                noWrap = true;
            }
            return new FutureCallAdapter<>(type, retrofit.callbackExecutor(), isPack,
                    isPackData, noWrap, wrapType, mFilters);
        }
        return null;
    }

    private static class FutureCallAdapter<R, P extends Packable>
            implements CallAdapter<R, FutureCall> {

        private final Type responseType;

        private final Executor mCallbackExecutor;

        private final boolean isPackData;

        private final boolean isPack;

        private final boolean noWrap;

        private final Class<? extends Packable> packClz;

        private List<PackableFilter<P>> mFilters;

        FutureCallAdapter(Type responseType, Executor callbackExecutor,
                boolean isPack,
                boolean isPackData,
                boolean noWrap,
                Class<? extends Packable> packClz,
                List<PackableFilter<P>> filters) {
            this.responseType = responseType;
            this.mCallbackExecutor = callbackExecutor;
            this.isPack = isPack;
            this.isPackData = isPackData;
            this.noWrap = noWrap;
            this.packClz = packClz;
            this.mFilters = filters;
        }

        @Override
        public Type responseType() {
            return this.responseType;
        }

        @SuppressWarnings("unchecked")
        @Override
        public FutureCall adapt(@NonNull Call<R> call) {
            FutureResponseCall<?> respCall = new FutureResponseCall<>(call, mCallbackExecutor);
            FutureCall future = respCall;
            if (isPack) {
                future = new FuturePackableCall(respCall, packClz, mFilters);
            } else if (isPackData) {
                FuturePackableCall<?> c = new FuturePackableCall(respCall, packClz, mFilters);
                future = new FutureDataCall(c);
            } else if (noWrap) {
                future = new FutureCallImpl(respCall);
            }
            return future;
        }
    }

    private static class ParameterizedTypeAdapter implements ParameterizedType {

        private final Type mRawType;

        private final Type[] mActualType;

        private ParameterizedTypeAdapter(Type rawType, Type... actualType) {
            this.mRawType = rawType;
            this.mActualType = actualType;
        }

        static ParameterizedType create(Type rawType, Type... actualType) {
            return new ParameterizedTypeAdapter(rawType, actualType);
        }

        @Override
        public Type[] getActualTypeArguments() {
            return this.mActualType;
        }

        @Override
        public Type getRawType() {
            return this.mRawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}


