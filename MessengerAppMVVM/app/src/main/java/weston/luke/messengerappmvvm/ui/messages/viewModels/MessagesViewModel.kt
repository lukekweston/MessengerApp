package weston.luke.messengerappmvvm.ui.messages

import android.content.Context
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import weston.luke.messengerappmvvm.data.database.entities.Conversation
import weston.luke.messengerappmvvm.data.database.entities.Message
import weston.luke.messengerappmvvm.data.database.entities.MessageStatus
import weston.luke.messengerappmvvm.repository.ConversationRepository
import weston.luke.messengerappmvvm.repository.LoggedInUserRepository
import weston.luke.messengerappmvvm.repository.MessageRepository
import weston.luke.messengerappmvvm.util.ImageUtils
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel
    @Inject constructor(
    loggedInUserRepository: LoggedInUserRepository,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {


    private val _toastMessageToDisplay =
        MutableLiveData<String>()

    val toastMessageToDisplay: LiveData<String>
        get() = _toastMessageToDisplay

    val loggedInUser = loggedInUserRepository.loggedInUser

    val messages = MutableLiveData<List<Message>>()

    val loggedInUserAndMessages = MediatorLiveData<Pair<List<Message>, Int>>()

    init {
        loggedInUserAndMessages.addSource(messages) {
            loggedInUserAndMessages.value = Pair(it, loggedInUser.value?.userId ?: 0)
        }
        loggedInUserAndMessages.addSource(loggedInUser) {
            loggedInUserAndMessages.value = Pair(messages.value.orEmpty(), it!!.userId)
        }
    }


    fun getConversation(conversationId: Int): Flow<Conversation> {
        return conversationRepository.getConversation(conversationId)
    }

    fun loadData(conversationId: Int, context: Context) {
        viewModelScope.launch {
            messageRepository.getAllMessagesForAConversation(conversationId).collect { it ->
                val messagesThatNeedImageDownloaded =
                    it.filter { message -> message.status == MessageStatus.IMAGE_READY_TO_GET_FROM_API }
                if (messagesThatNeedImageDownloaded.isNotEmpty()) {
                    downloadLowResImageForImages(messagesThatNeedImageDownloaded,context)
                }
                messages.value = it
            }
        }
    }


    private suspend fun downloadLowResImageForImages(messages: List<Message>, context: Context) {
        for (message in messages) {
            messageRepository.getLowResImageForMessage(message, context)
        }
    }

    fun sendMessage(textMessage: String, conversationId: Int) {
        viewModelScope.launch {
            val message = Message(
                conversationId = conversationId,
                message = textMessage,
                userId = loggedInUser.value!!.userId,
                userName = loggedInUser.value!!.userName,
                status = MessageStatus.CREATED,
                timeSent = LocalDateTime.now(),
            )
            val success = messageRepository.sendMessageText(message)

            if (!success) {
                _toastMessageToDisplay.value = "Error contacting the server"
            }
        }
    }

    //Best practise would be to store the uri for the images in the database rather than the path
    // todo change this in the future
    //Todo update this in the future to use a coroutine that lasts longer than the viewmodel life time
    //also see if the lifetime is a problem - viewmodel lasts longer than activity, this could be finr
    fun sendImage(
        conversationId: Int,
        imageBase64StringFullRes: String,
        fullResImagePath: String,
        context: Context
    ) {
        viewModelScope.launch {
            val message = Message(
                conversationId = conversationId,
                message = "",
                userId = loggedInUser.value!!.userId,
                userName = loggedInUser.value!!.userName,
                status = MessageStatus.CREATED,
                timeSent = LocalDateTime.now(),
                pathToSavedHighRes = fullResImagePath
            )
            //Save the initial data, this will update the ui
            val messageId = messageRepository.insertMessage(message)

            //Compress the image and then save it and return the directory
            val imageBase64StringLowRes = ImageUtils.resizeImage(imageBase64StringFullRes)
            val pathToLowResImage = ImageUtils.saveImage(context, imageBase64StringLowRes, false)

            message.pathToSavedLowRes = pathToLowResImage
            message.id = messageId

            //Update the message to link it with the low res image
            messageRepository.updateMessage(message)

            //Send the image to the server
            messageRepository.sendMessageImage(
                messageId,
                message,
                imageBase64StringFullRes,
                imageBase64StringLowRes
            )
        }
    }
}


