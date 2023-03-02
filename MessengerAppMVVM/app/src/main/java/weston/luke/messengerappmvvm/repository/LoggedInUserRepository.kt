package weston.luke.messengerappmvvm.repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import weston.luke.messengerappmvvm.data.database.dao.LoggedInUserDao
import weston.luke.messengerappmvvm.data.database.entities.LoggedInUser
import weston.luke.messengerappmvvm.data.remote.api.MessengerAPIService
import weston.luke.messengerappmvvm.data.remote.request.LoginRequest
import weston.luke.messengerappmvvm.data.remote.response.LoginResponse

class LoggedInUserRepository(private val loggedInUserDao: LoggedInUserDao, private val apiService: MessengerAPIService) {
    @WorkerThread
    suspend fun loginUser(user: LoggedInUser){
        loggedInUserDao.loginUser(user)
    }

    @WorkerThread
    suspend fun logoutUser(){
        loggedInUserDao.logoutUser()
    }

    val loggedInUser: Flow<LoggedInUser?> = loggedInUserDao.getLoggedInUser()

    @WorkerThread
    suspend fun awaitGettingLoggedInUser() : LoggedInUser? {
        return loggedInUserDao.awaitGetLoggedInUser()
    }

    suspend fun loginUser(loginRequest: LoginRequest): LoginResponse {
        return apiService.loginUser(loginRequest)
    }

}