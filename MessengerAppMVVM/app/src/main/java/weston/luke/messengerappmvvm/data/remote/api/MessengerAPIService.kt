package weston.luke.messengerappmvvm.data.remote.api

import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import weston.luke.messengerappmvvm.data.remote.request.LoginRequest
import weston.luke.messengerappmvvm.data.remote.response.ConversationResponse
import weston.luke.messengerappmvvm.data.remote.response.LoginResponse
import weston.luke.messengerappmvvm.data.remote.response.UserResponse
import weston.luke.messengerappmvvm.util.Constants


class MessengerAPIService {

    private val api = Retrofit.Builder().baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
        .create(messengerAPIInterface::class.java)

    fun loginUser(loginRequest: LoginRequest): Single<LoginResponse>{
        return api.loginUser(loginRequest)
    }

    fun getAllConversationsForUser(userId: Int): Single<ConversationResponse>{
        return api.getAllConversationsForUser(userId)
    }
}