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

/**
 * Created by wangjie2013
 * on 18-1-25.
 */


public interface FutureCall<T> extends Cloneable {

    @NonNull
    FutureCall<T> submit();

    /**
     * @param callback response callback.
     * @return this FutureCall.
     */
    @NonNull
    FutureCall<T> enqueue(OnCallback<T> callback);

    /**
     * This method is blocked! Until the result is obtained.
     *
     * @return success data,other null, if failure.
     */
    @Nullable
    T get() throws IOException, InterruptedException;

    /**
     * This method is blocked! Until the result is obtained or timed out.
     * If the timeout does not throw an exception, it returns null
     *
     * @return success data,other null, if failure.
     */
    @Nullable
    T get(long timeout, TimeUnit unit) throws IOException, InterruptedException;

    /**
     * This method is blocked! Until the result is obtained or timed out.
     *
     * @param throwIfTimeout true if set, when timeout throw TimeoutException
     * @return success data,other null, if failure.
     */
    @Nullable
    T get(long timeout, TimeUnit unit, boolean throwIfTimeout)
            throws IOException, InterruptedException, TimeoutException;

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    Object clone() throws CloneNotSupportedException;

}
