package weston.luke.messengerappmvvm.ui.conversationsAndFriends.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import weston.luke.messengerappmvvm.databinding.DialogAddFriendBinding
import weston.luke.messengerappmvvm.databinding.FragmentFriendsBinding
import weston.luke.messengerappmvvm.ui.conversationsAndFriends.FriendAdapter
import weston.luke.messengerappmvvm.ui.conversationsAndFriends.FriendRequestAdapter
import weston.luke.messengerappmvvm.ui.conversationsAndFriends.viewModels.FriendsViewModel
import weston.luke.messengerappmvvm.ui.messages.activity.MessagesActivity
import weston.luke.messengerappmvvm.util.*

@AndroidEntryPoint
class FriendFragment : Fragment() {

    private lateinit var mBinding: FragmentFriendsBinding
    private lateinit var dialogAddFriend: AlertDialog

    private lateinit var friendRequestRecyclerView: RecyclerView
    private lateinit var friendRequestAdapter: FriendRequestAdapter

    private lateinit var friendRecyclerView: RecyclerView
    private lateinit var friendAdapter: FriendAdapter

    private val mViewModel: FriendsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        createDialogAddFriend()

        mBinding = FragmentFriendsBinding.inflate(inflater, container, false)
        mBinding.fab.setOnClickListener {
            dialogAddFriend.show()
        }


        //Observe the latest messages and the conversations for changes
        mViewModel.friendRequestResponse.observe(viewLifecycleOwner) { friendResponse ->
            if (friendResponse == null) {
                requireActivity().toast("Server error sending friend request, please try again")
            } else if (friendResponse!!.friendRequestSent) {
                requireActivity().toast("Friend request sent")
                dialogAddFriend.dismiss()
            } else if (friendResponse.alreadyFriends) {
                requireActivity().toast("You are already friends")
            } else if (friendResponse.friendRequestSent) {
                requireActivity().toast("You have already sent a friend request")
            } else if (friendResponse.usernameOrEmailNotFound) {
                requireActivity().toast("Username or email not found")
            }
        }



        friendRequestAdapter =
            FriendRequestAdapter(onFriendRequestAcceptClick = { friendId, friendUsername ->
                mViewModel.acceptFriendRequest(friendId, friendUsername)
            },
                onFriendRequestDeclineClick = { friendId, friendUsername ->
                    mViewModel.declineFriendRequest(friendId, friendUsername)
                })

        friendRequestRecyclerView = mBinding.recyclerViewFriendRequests
        friendRequestRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        friendRequestRecyclerView.adapter = friendRequestAdapter

        mViewModel.friendRequests.observe(viewLifecycleOwner) { friendRequests ->
            if (friendRequests.isNotEmpty()) {
                mBinding.tvFriendRequests.show()
                friendRequestAdapter.setData(friendRequests)
            } else {
                mBinding.tvFriendRequests.hide()
                friendRequestAdapter.setData(listOf())
            }
        }

        friendAdapter = FriendAdapter(onFriendMessageClick = { conversationId ->
            //Go to the messagesActivity for this conversation
            val intent = Intent(requireContext(), MessagesActivity::class.java)
            intent.putExtra(Constants.CONVERSATION_ID, conversationId)
            startActivity(intent)

        }, onRemoveFriend = { friendUserId, friendUsername ->
            Utils.createAlertDialog(
                context = requireContext(),
                title = "Remove friend",
                message = "Are you sure you would like to remove $friendUsername",
                positiveText = "Remove",
                onPositiveClick = { mViewModel.removeFriend(friendUserId, friendUsername) },
                onNegativeClick = {}
            ).show()

        })

        friendRecyclerView = mBinding.recyclerViewFriend
        friendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        friendRecyclerView.adapter = friendAdapter

        mViewModel.friends.observe(viewLifecycleOwner) { friends ->
            if (friends.isNotEmpty()) {
                mBinding.tvFriends.show()
                friendAdapter.setData(friends)
            } else {
                mBinding.tvFriends.hide()
                friendAdapter.setData(listOf())
            }
        }


        return mBinding.root
    }


    private fun createDialogAddFriend() {
        val binding = DialogAddFriendBinding.inflate(layoutInflater)
        val usernameOrEmail = binding.etInputAccount

        val dialogBuilder = AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setTitle("Add friend")
            .setPositiveButton("Submit", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        dialogAddFriend = dialogBuilder.create()
        dialogAddFriend.create()
        // Override the positive button click listener after the dialog is shown
        dialogAddFriend.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val input = usernameOrEmail.text.toString()
            if (input.isBlank()) {
                usernameOrEmail.error = "Text cannot be empty"
            } else {
                //Close the keyboard
                val currentFocus = dialogAddFriend.currentFocus
                if (currentFocus != null) {
                    // Get the InputMethodManager service
                    val inputMethodManager =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    // Hide the keyboard
                    inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
                }

                mViewModel.sendFriendRequest(input)

            }
        }
    }
}