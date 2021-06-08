package py.com.opentech.drawerwithbottomnavigation.api;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiFactory {

    public static String BASE_URL = "https://proxglobal.com/api/oodles-book/";

    private static OkHttpClient client_noauth;

    public static ApiService createGatewayJson(Context context) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true);

        okHttpBuilder.addInterceptor(loggingInterceptor);

        okHttpBuilder.addInterceptor(chain -> {
            Request newRequest = chain.request().newBuilder()
//                    .addHeader("APIKey", SharedPrefsUtils.getStringPreference(context, Constants.SECRET_KEY))
                    .addHeader("Content-Type", "application/json")
                    .build();
            return chain.proceed(newRequest);
        });
        client_noauth = okHttpBuilder.build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).client(client_noauth)
                .build();
        return retrofit.create(ApiService.class);
    }

    public static ApiService createGatewayMultipart(Context context) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true);

        okHttpBuilder.addInterceptor(loggingInterceptor);


        okHttpBuilder.addInterceptor(chain -> {
            Request newRequest = chain.request().newBuilder()
//                    .addHeader("APIKey", SharedPrefsUtils.getStringPreference(context, Constants.SECRET_KEY))
                    .addHeader("Content-Type", "application/json")
                    .build();
            return chain.proceed(newRequest);
        });
        client_noauth = okHttpBuilder.build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).client(client_noauth)
                .build();
        return retrofit.create(ApiService.class);
    }


}
