package com.example.uhfproject.utils.retrofit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.uhfproject.model.HttpResult;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;

//LiveDataCallAdapter是为了将原来的Call形式转为LiveData，来避免请求后收到回复时UI已经切换导致的空指针异常
public class LiveDataCallAdapter<T> implements CallAdapter<T, LiveData<T>> {
    private Type responseType;

    public LiveDataCallAdapter(Type responseType) {
        this.responseType = responseType;
    }

    @NonNull
    @Override
    public Type responseType() {
        return responseType;
    }

    @NonNull
    @Override
    public LiveData<T> adapt(@NonNull Call<T> call) {
        return new LiveData<T>() {
            private AtomicBoolean started = new AtomicBoolean(false);

            @Override
            protected void onActive() {
                super.onActive();
                if (started.compareAndSet(false, true)) {
                    //原始Call的处理
                    call.enqueue(new Callback<T>() {
                        @Override
                        public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                            postValue(response.body());
                        }

                        @Override
                        public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                            HttpResult<T> result = new HttpResult<>();
                            result.setCode(-1);
                            result.setMsg(t.getMessage());
                            result.setData(null);
                            postValue((T) result);
                        }
                    });
                }
            }
        };
    }
}
