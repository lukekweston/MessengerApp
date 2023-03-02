package weston.luke.messengerappmvvm.repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import weston.luke.messengerappmvvm.data.database.dao.LoggedInUserDao
import weston.luke.messengerappmvvm.data.database.entities.LoggedInUser
import weston.luke.messengerappmvvm.data.remote.api.MessengerAPIService

class LoggedInUserRepository(private val loggedInUserDao: LoggedInUserDao, private val api: MessengerAPIService) {
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

}