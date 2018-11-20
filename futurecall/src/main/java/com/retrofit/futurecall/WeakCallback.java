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

import java.lang.ref.WeakReference;

/**
 * Created by wangjie2013
 * on 18-7-24.
 */


public class WeakCallback<T> implements OnCallback<T> {

    private final WeakReference<OnCallback<T>> mWeakRef;

    public WeakCallback(OnCallback<T> cb) {
        mWeakRef = new WeakReference<>(cb);
    }

    @Override
    public void onResponse(int code, String message, T result) {
        OnCallback<T> cb = mWeakRef.get();
        if (cb != null) {
            cb.onResponse(code, message, result);
        }
    }

    @Override
    public void onError() {
        OnCallback<T> cb = mWeakRef.get();
        if (cb != null) {
            cb.onError();
        }
    }
}
