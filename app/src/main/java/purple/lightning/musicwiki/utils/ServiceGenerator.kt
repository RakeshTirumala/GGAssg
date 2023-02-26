package purple.lightning.musicwiki.utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceGenerator {
    private var client = OkHttpClient
        .Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private var retrofit = Retrofit.Builder()
        .baseUrl("http://ws.audioscrobbler.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()


    fun <T>buildService(service: Class<T>):T{
        return retrofit.create(service)
    }

}