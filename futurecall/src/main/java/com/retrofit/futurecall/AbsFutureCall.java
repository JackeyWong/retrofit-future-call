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

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;

/**
 * Created by wangjie2013
 * on 18-1-25.
 */


abstract class AbsFutureCall<T> implements FutureCall<T> {

    static void checkIsExecuted(FutureCall<?> call) {
        if (call == null) {
            throw new IllegalStateException("FutureCall is null");
        }
        if (call.isExecuted()) {
            throw new IllegalStateException("Already executed.");
        }
    }

    static void checkIsExecuted(Call<?> call) {
        if (call == null) {
            throw new IllegalStateException("Call is null");
        }
        if (call.isExecuted()) {
            throw new IllegalStateException("Already executed.");
        }
    }

    @Override
    public T get() throws IOException, InterruptedException {
        return get(-1, TimeUnit.MILLISECONDS);
    }

    @Override
    public T get(long timeout, TimeUnit unit)
            throws IOException, InterruptedException {
        try {
            return get(timeout, unit, false);
        } catch (TimeoutException e) {
            throw new IllegalStateException("not reach", e);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("is abstract");
    }
}
