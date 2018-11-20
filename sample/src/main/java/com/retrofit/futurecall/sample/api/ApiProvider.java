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

package com.retrofit.futurecall.sample.api;

import android.text.format.DateFormat;

import java.util.Objects;

import com.google.gson.Gson;
import com.retrofit.futurecall.FutureCallAdapterFactory;
import com.retrofit.futurecall.PackableFilter;
import com.retrofit.futurecall.sample.proto.Pack;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by wangjie2013
 * on 18-1-26.
 */

public final class ApiProvider {

    private final PackableFilter<Pack> mFilter = new PackableFilter<Pack>() {
        @Override
        public boolean onFilter(Pack pack) {
            pack.time = DateFormat.format("MM/dd HH:mm:ss yyyy", pack.timestamp)
                                  .toString();
            return false;
        }
    };

    private Retrofit mRetrofit;

    public ApiProvider() {
        onInit();
    }

    protected void onInit() {
        FutureCallAdapterFactory<Pack> futureCall
                = FutureCallAdapterFactory.create(Pack.class).addFilter(mFilter);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(
                        new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl("http://www.mocky.io/v2/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCallAdapterFactory(futureCall)
                .client(client)
                .build();
    }

    public <T> T obtain(Class<T> api) {
        return Objects.requireNonNull(mRetrofit, "retrofit == null")
                      .create(api);
    }

}
