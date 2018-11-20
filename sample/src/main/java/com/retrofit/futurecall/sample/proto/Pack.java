/*
 * Copyright 2016 Jie Wang.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 *  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *  either express or implied. See the License for the specific language governing permissions
 *  and limitations under the License.
 */

package com.retrofit.futurecall.sample.proto;

import com.retrofit.futurecall.Packable;

/**
 * Created by wangjie2013
 * on 18-1-26.
 */

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

    @Override
    public String toString() {
        return "\nPack{" +
                "\nmessage='" + message() + '\'' +
                "\n, code=" + code() +
                "\n, time=" + time +
                "\n, data=" + data() +
                "\n}";
    }
}
