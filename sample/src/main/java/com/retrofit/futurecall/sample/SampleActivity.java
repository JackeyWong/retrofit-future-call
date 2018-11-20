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

package com.retrofit.futurecall.sample;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import com.retrofit.futurecall.FutureCall;
import com.retrofit.futurecall.LiveCallback;
import com.retrofit.futurecall.OnCallback;
import com.retrofit.futurecall.sample.api.ApiProvider;
import com.retrofit.futurecall.sample.api.IMocky;
import com.retrofit.futurecall.sample.proto.Pack;
import com.retrofit.futurecall.sample.proto.ResultData;

/**
 * Created by wangjie2013
 * on 18-1-26.
 */
@SuppressLint("SetTextI18n")
public class SampleActivity extends AppCompatActivity {

    private IMocky mMockyApi;

    private TextView mResultText;

    private final OnCallback<Pack<ResultData>> mPackOnCallback
            = new OnCallback<Pack<ResultData>>() {
        @Override
        public void onResponse(int code, String message,
                Pack<ResultData> result) {
            mResultText.setText("requestPackData : \n" + result);
        }

        @Override
        public void onError() {
            mResultText.setText("requestPackData error");
        }
    };

    private final OnCallback<ResultData> mResultOnCallback =
            new OnCallback<ResultData>() {
                @Override
                public void onResponse(int code, String message, ResultData result) {
                    mResultText.setText("requestResultData : \n" + result);
                }

                @Override
                public void onError() {
                    mResultText.setText("requestResultData error");
                }
            };

    private static void asyncRequest(final IMocky mocky,
            OnCallback<ResultData> callback) {
        final WeakReference<OnCallback<ResultData>> ref = new WeakReference<>(callback);
        new AsyncTask<Void, Void, ResultData>() {

            @Override
            protected ResultData doInBackground(Void... voids) {
                try {
                    FutureCall<ResultData> future = mocky.requestResultData().submit();
                    return future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(ResultData resultData) {
                super.onPostExecute(resultData);
                OnCallback<ResultData> cb = ref.get();
                if (cb == null) {
                    return;
                }
                if (resultData != null) {
                    cb.onResponse(0, null, resultData);
                } else {
                    cb.onError();
                }
            }
        }.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        mResultText = findViewById(R.id.tv_result);
        mMockyApi = new ApiProvider().obtain(IMocky.class);
    }

    public void onButtonClick(View view) throws CloneNotSupportedException {
        switch (view.getId()) {
            case R.id.btn_request_full_data:
                mMockyApi.requestPackData()
                         .enqueue(new LiveCallback<>(this, mPackOnCallback));
                break;
            case R.id.btn_request_result:
                asyncRequest(mMockyApi, mResultOnCallback);
                break;
            default:
                break;
        }
    }
}
