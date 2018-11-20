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

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by wangjie2013
 * on 18-9-28.
 */


public class LiveCallback<T> implements OnCallback<T> {

    private final WeakReference<Object> mOwnerRef;

    private final OnCallback<T> mCallback;

    /**
     * callback invoke, if activity is not finish or destroyed.
     * callback is weak!
     */
    public LiveCallback(@NonNull Activity act, OnCallback<T> cb) {
        this(cb, act);
    }

    /**
     * callback invoke, if fragment is not detached
     * callback is weak!
     */
    public LiveCallback(@NonNull Fragment fragment, OnCallback<T> cb) {
        this(cb, fragment);
    }

    /**
     * callback invoke, if view is shown
     * callback is weak!
     */
    public LiveCallback(@NonNull View view, OnCallback<T> cb) {
        this(cb, view);
    }

    private LiveCallback(OnCallback<T> cb, Object owner) {
        mCallback = new WeakCallback<>(cb);
        mOwnerRef = new WeakReference<>(owner);
    }

    @Override
    public void onResponse(int code, String message, T result) {
        if (!isOwnerLiving()) {
            return;
        }
        mCallback.onResponse(code, message, result);
    }

    @Override
    public void onError() {
        if (!isOwnerLiving()) {
            return;
        }
        mCallback.onError();
    }

    private boolean isOwnerLiving() {
        final Object owner = mOwnerRef.get();
        if (owner == null) {
            return false;
        }
        if (owner instanceof Activity) {
            Activity act = (Activity) owner;
            return isLiveActivity(act);
        } else if (owner instanceof Fragment) {
            Fragment f = (Fragment) owner;
            return f.getActivity() != null && !f.isDetached();
        } else if (owner instanceof View) {
            View view = (View) owner;
            return view.isShown() && view.hasWindowFocus();
        }
        return false;
    }

    private boolean isLiveActivity(@NonNull Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !act.isFinishing() && !act.isDestroyed();
        }
        return !act.isFinishing();
    }
}
