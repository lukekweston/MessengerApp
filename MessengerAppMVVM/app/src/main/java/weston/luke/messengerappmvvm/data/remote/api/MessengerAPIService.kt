package weston.luke.messengerappmvvm.data.remote.api

import android.content.Context
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import weston.luke.messengerappmvvm.data.remote.request.*
import weston.luke.messengerappmvvm.data.remote.response.*
import weston.luke.messengerappmvvm.util.Constants
import java.io.File
import java.util.concurrent.TimeUnit


class MessengerAPIService(context: Context) {


    //Add a 100mb cache - good for getting full res images
    val cacheDirectory = File(context.cacheDir, "http-cache")
    val cacheSize = 100 * 1024 * 1024 // 100 MiB
    val cache = Cache(cacheDirectory, cacheSize.toLong())

    private val logging = HttpLoggingInterceptor()
    private val httpClient = OkHttpClient.Builder().apply {
        addInterceptor(
            Interceptor { chain ->
                val builder = chain.request().newBuilder()
                //todo Futrue, add api key?
                // builder.header("X-Api-key", Constants.apiKey)
                return@Interceptor chain.proceed(builder.build())
            }
        )
            .addNetworkInterceptor(CacheInterceptor())

        logging.level = HttpLoggingInterceptor.Level.BODY
        addNetworkInterceptor(logging)
    }
        //.addInterceptor(NetworkConnectionInterceptor(context))

        //Time out the api calls after 30 seconds of no response
        .callTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val httpClientCached = OkHttpClient.Builder().apply {
        addInterceptor(
            Interceptor { chain ->
                val builder = chain.request().newBuilder()
                //todo Futrue, add api key?
                // builder.header("X-Api-key", Constants.apiKey)
                return@Interceptor chain.proceed(builder.build())
            }
        )
            //Add the cache and the network Interceptor
            .cache(cache)
            .addNetworkInterceptor(CacheInterceptor())

        logging.level = HttpLoggingInterceptor.Level.BODY
        addNetworkInterceptor(logging)
    }
        //.addInterceptor(NetworkConnectionInterceptor(context))

        //Time out the api calls after 30 seconds of no response
        .callTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()


    private val api = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .client(httpClient)
        .build()
        .create(MessengerAPIInterface::class.java)

    private val apiCached = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .client(httpClientCached)
        .build()
        .create(MessengerAPIInterface::class.java)


    suspend fun loginUser(loginRequest: LoginRequest): LoginResponse {
        return api.loginUser(loginRequest)
    }

    suspend fun logoutUser(logoutRequest: LogoutRequest): SuccessResponse {
        return api.logoutUser(logoutRequest)
    }

    suspend fun checkFcmRegToken(fcmRegTokenCheckRequest: fcmRegTokenCheckRequest): SuccessResponse {
        return api.checkFcmRegToken(fcmRegTokenCheckRequest)
    }

    suspend fun getAllConversationsForUser(userId: Int): ConversationResponse {
        return api.getAllConversationsForUser(userId)
    }

    suspend fun getAllMessagesForUser(userId: Int): MessageResponseList {
        return api.getAllMessagesForUser(userId)
    }

    suspend fun sendMessage(messageRequest: MessageSendRequest): MessageResponse {
        return api.sendMessage(messageRequest)
    }

    suspend fun getLowResImageForMessage(messageId: Int): ImageResponse {
        return api.getLowResImageForMessage(messageId)
    }

    suspend fun sendFriendRequest(friendRequest: NewFriendRequest): FriendRequestResponse {
        return api.sendFriendRequest(friendRequest)
    }

    suspend fun getAllFriendsForUser(userId: Int): List<FriendResponse> {
        return api.getAllFriendsForUser(userId)
    }

    suspend fun updateFriendshipStatus(updateFriendshipStatusRequest: UpdateFriendshipStatusRequest): ConversationResponseItem {
        return api.updateFriendshipStatus(updateFriendshipStatusRequest)
    }

    //    this is the only endpoint that isnt cached, as these full sized images are not saved (only saved when the user does a save)
    suspend fun getFullResImageForMessage(messageId: Int): ImageResponse {
        return apiCached.getFullResImageForMessage(messageId)
    }


}