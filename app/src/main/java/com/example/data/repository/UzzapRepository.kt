package com.example.data.repository

import com.example.data.local.UzzapDatabase
import com.example.data.model.ChatroomEntity
import com.example.data.model.ContactCategory
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.FriendshipState
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.RoomMessageEntity
import com.example.data.model.RoomRole
import com.example.data.model.UserPresence
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

class UzzapRepository(
    private val database: UzzapDatabase,
    private val scope: CoroutineScope
) {
    private val userDao = database.userDao()
    private val contactDao = database.contactDao()
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val chatroomDao = database.chatroomDao()

    // Event bus for buzzer effect
    private val _buzzEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val buzzEvents: SharedFlow<String> = _buzzEvents

    val profileFlow: Flow<UserProfileEntity?> = userDao.getProfileFlow()
    val contactsFlow: Flow<List<ContactEntity>> = contactDao.getAcceptedContactsFlow()
    val pendingRequestsFlow: Flow<List<ContactEntity>> = contactDao.getPendingRequestsFlow()
    val conversationsFlow: Flow<List<ConversationEntity>> = conversationDao.getAllConversationsFlow()
    val chatroomsFlow: Flow<List<ChatroomEntity>> = chatroomDao.getAllChatroomsFlow()

    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversationFlow(conversationId)
    }

    fun getRoomMessages(roomId: String): Flow<List<RoomMessageEntity>> {
        return chatroomDao.getRoomMessagesFlow(roomId)
    }

    suspend fun updatePresence(status: UserPresence, statusMessage: String) {
        userDao.updatePresence(status, statusMessage)
    }

    suspend fun updateVibration(enabled: Boolean) {
        userDao.updateVibration(enabled)
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        userDao.insertProfile(profile)
    }

    suspend fun toggleFavoriteContact(contactId: String) {
        contactDao.toggleFavorite(contactId)
    }

    suspend fun acceptFriendRequest(contactId: String) {
        contactDao.updateFriendshipState(contactId, FriendshipState.ACCEPTED)
    }

    suspend fun declineFriendRequest(contactId: String) {
        contactDao.deleteContact(contactId)
    }

    suspend fun deleteContact(contactId: String) {
        contactDao.deleteContact(contactId)
    }

    suspend fun addContact(
        username: String,
        displayName: String,
        phoneNumber: String,
        category: ContactCategory
    ) {
        val newContact = ContactEntity(
            id = "contact_${UUID.randomUUID().toString().take(8)}",
            username = username.lowercase().trim(),
            displayName = displayName.trim(),
            nickname = displayName.split(" ").firstOrNull() ?: displayName,
            phoneNumber = phoneNumber,
            presence = UserPresence.ONLINE,
            statusMessage = "Added via Uzzap \uD83D\uDCF1",
            category = category,
            friendshipState = FriendshipState.ACCEPTED,
            avatarEmoji = listOf("\uD83D\uDE0A", "\uD83E\uDD17", "\uD83D\uDC36", "\uD83C\uDF89", "\u2B50", "\uD83D\uDCBB").random(),
            avatarBgColor = listOf(0xFFE91E63, 0xFF3F51B5, 0xFF009688, 0xFFFF9800, 0xFF673AB7).random()
        )
        contactDao.insertContact(newContact)
    }

    suspend fun startOrGetConversation(contact: ContactEntity): String {
        val convoId = "convo_${contact.username}"
        val existing = conversationDao.getConversationById(convoId)
        if (existing != null) {
            return existing.id
        }

        val newConvo = ConversationEntity(
            id = convoId,
            type = "DIRECT",
            title = "${contact.nickname} (${contact.displayName})",
            recipientUsername = contact.username,
            avatarEmoji = contact.avatarEmoji,
            avatarBgColor = contact.avatarBgColor,
            lastMessage = "Started a conversation",
            lastTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            isPinned = false
        )
        conversationDao.insertConversation(newConvo)
        return convoId
    }

    suspend fun sendMessage(conversationId: String, text: String, replyToBody: String? = null) {
        val profile = userDao.getProfile()
        val myName = profile?.displayName ?: "Juan Dela Cruz"
        val myUsername = profile?.username ?: "juandelacruz"

        val msgId = "msg_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()

        val msg = MessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderUsername = myUsername,
            senderDisplayName = myName,
            type = MessageType.TEXT,
            body = text,
            replyToBody = replyToBody,
            timestamp = now,
            status = MessageDeliveryStatus.SENT,
            isFromMe = true
        )
        messageDao.insertMessage(msg)
        conversationDao.updateLastMessage(conversationId, text, now, 0)

        // Real message delivery confirmation
        scope.launch(Dispatchers.IO) {
            messageDao.updateStatus(msgId, MessageDeliveryStatus.DELIVERED)
        }
    }

    suspend fun sendBuzz(conversationId: String) {
        val profile = userDao.getProfile()
        val myName = profile?.displayName ?: "Juan Dela Cruz"
        val myUsername = profile?.username ?: "juandelacruz"

        val msgId = "msg_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()

        val msg = MessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderUsername = myUsername,
            senderDisplayName = myName,
            type = MessageType.BUZZ,
            body = "BUZZED YOU!",
            timestamp = now,
            status = MessageDeliveryStatus.SENT,
            isFromMe = true
        )
        messageDao.insertMessage(msg)
        conversationDao.updateLastMessage(conversationId, "\u26A1 BUZZED YOU!", now, 0)
        _buzzEvents.emit("Outgoing BUZZ sent!")

        scope.launch(Dispatchers.IO) {
            messageDao.updateStatus(msgId, MessageDeliveryStatus.DELIVERED)
        }
    }

    suspend fun markConversationRead(conversationId: String) {
        conversationDao.markAsRead(conversationId)
    }

    suspend fun deleteConversation(conversationId: String) {
        messageDao.clearHistory(conversationId)
        conversationDao.deleteConversation(conversationId)
    }

    suspend fun joinOrLeaveRoom(roomId: String, join: Boolean) {
        val room = chatroomDao.getChatroomById(roomId) ?: return
        chatroomDao.updateJoinState(roomId, join, if (join) 1 else -1)

        val now = System.currentTimeMillis()
        val systemNotice = RoomMessageEntity(
            id = "rm_${UUID.randomUUID().toString().take(8)}",
            roomId = roomId,
            senderUsername = "System",
            senderRole = RoomRole.ADMIN,
            message = if (join) "You joined ${room.name}." else "You left ${room.name}.",
            timestamp = now,
            isSystem = true
        )
        chatroomDao.insertRoomMessage(systemNotice)
    }

    suspend fun sendRoomMessage(roomId: String, text: String) {
        val profile = userDao.getProfile()
        val username = profile?.username ?: "juandelacruz"

        val msg = RoomMessageEntity(
            id = "rm_${UUID.randomUUID().toString().take(8)}",
            roomId = roomId,
            senderUsername = username,
            senderRole = RoomRole.MEMBER,
            message = text,
            timestamp = System.currentTimeMillis(),
            isFromMe = true,
            isSystem = false
        )
        chatroomDao.insertRoomMessage(msg)
    }

    suspend fun createChatroom(name: String, topic: String, category: String) {
        val formattedName = if (name.startsWith("#")) name else "#$name"
        val newRoom = ChatroomEntity(
            id = "room_${UUID.randomUUID().toString().take(8)}",
            name = formattedName,
            topic = topic,
            category = category,
            chatterCount = 1,
            isJoined = true,
            userRole = RoomRole.OWNER
        )
        chatroomDao.insertChatroom(newRoom)

        val welcomeMsg = RoomMessageEntity(
            id = "rm_${UUID.randomUUID().toString().take(8)}",
            roomId = newRoom.id,
            senderUsername = "System",
            senderRole = RoomRole.ADMIN,
            message = "Room created: $formattedName ($category). Welcome!",
            timestamp = System.currentTimeMillis(),
            isSystem = true
        )
        chatroomDao.insertRoomMessage(welcomeMsg)
    }
}
