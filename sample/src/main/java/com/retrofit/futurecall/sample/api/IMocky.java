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

import com.retrofit.futurecall.FutureCall;
import com.retrofit.futurecall.sample.proto.Pack;
import com.retrofit.futurecall.sample.proto.ResultData;

import retrofit2.http.GET;

/**
 * Created by wangjie2013
 * on 18-1-26.
 */

public interface IMocky {

    @GET("5be02b9f32000011006494bb")
    FutureCall<Pack<ResultData>> requestPackData();

    @GET("5be02b9f32000011006494bb")
    FutureCall<ResultData> requestResultData();
}
