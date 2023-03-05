package weston.luke.messengerappmvvm.util

import android.annotation.SuppressLint
import weston.luke.messengerappmvvm.data.remote.request.LogoutRequest
import weston.luke.messengerappmvvm.repository.ConversationRepository
import weston.luke.messengerappmvvm.repository.LoggedInUserRepository
import weston.luke.messengerappmvvm.repository.MessageRepository
import java.time.format.DateTimeFormatter
import java.util.*


object Utils {
    @SuppressLint("ConstantLocale")
    val formatDayMonthHourMin: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM HH:mm", Locale.getDefault())

    @SuppressLint("ConstantLocale")
    val formatHourMin: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    @SuppressLint("ConstantLocale")
    val formatDayMonth: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())


    //Accessible everywhere method to logout the currently logged in user
    suspend fun logoutUser(
        loggedInUserRepository: LoggedInUserRepository,
        conversationRepository: ConversationRepository,
        messageRepository: MessageRepository,
        loggedInUserId: Int,
        loggedInUserName: String
    ) {


        //Delete the users fcm_reg_token from server
        loggedInUserRepository.logoutUser(
            LogoutRequest(
                userId = loggedInUserId,
                userName = loggedInUserName
            )
        )

        //Delete users data locally
        loggedInUserRepository.deleteUserFromLocalDatabase()
        conversationRepository.deleteConversationData()
        messageRepository.deleteAllMessages()


    }
}