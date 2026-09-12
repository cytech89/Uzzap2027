package com.example.data.remote.firestore

import android.util.Log
import com.example.data.model.ChatroomEntity
import com.example.data.model.ContactCategory
import com.example.data.model.ContactEntity
import com.example.data.model.FriendshipState
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.RoomMessageEntity
import com.example.data.model.RoomRole
import com.example.data.model.UserPresence
import com.example.data.model.UserProfileEntity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class FirestoreSyncStatus(val label: String) {
    INITIALIZING("Connecting to Cloud..."),
    CONNECTED("Connected to Firestore"),
    SYNCING("Syncing with Cloud..."),
    OFFLINE_CACHE("Cloud Offline (Cached)"),
    ERROR("Cloud Sync Warning")
}

class UzzapFirestoreService(
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "UzzapFirestore"
        private const val USERS_COLLECTION = "users"
        private const val CHATROOMS_COLLECTION = "chatrooms"
        private const val ROOM_MESSAGES_SUBCOLLECTION = "messages"
        private const val CONVERSATIONS_COLLECTION = "conversations"
        private const val CONVO_MESSAGES_SUBCOLLECTION = "messages"
        private const val INBOX_SUBCOLLECTION = "inbox"
        private const val FRIEND_REQUESTS_COLLECTION = "friend_requests"
    }

    private val firestore: FirebaseFirestore by lazy {
        try {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            db.firestoreSettings = settings
            db
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firestore with persistent cache: ${e.message}", e)
            FirebaseFirestore.getInstance()
        }
    }

    private val _syncStatus = MutableStateFlow(FirestoreSyncStatus.INITIALIZING)
    val syncStatus: StateFlow<FirestoreSyncStatus> = _syncStatus.asStateFlow()

    private val activeListeners = mutableListOf<ListenerRegistration>()
    private var activeRoomListener: ListenerRegistration? = null
    private var inboxListener: ListenerRegistration? = null
    private var friendRequestsListener: ListenerRegistration? = null
    private var chatroomsListener: ListenerRegistration? = null

    init {
        checkConnectivity()
    }

    private fun checkConnectivity() {
        scope.launch(Dispatchers.IO) {
            try {
                // Quick ping document to verify connectivity
                firestore.collection(USERS_COLLECTION).document("_ping").get()
                    .addOnSuccessListener {
                        _syncStatus.value = FirestoreSyncStatus.CONNECTED
                    }
                    .addOnFailureListener {
                        _syncStatus.value = FirestoreSyncStatus.OFFLINE_CACHE
                    }
            } catch (e: Exception) {
                _syncStatus.value = FirestoreSyncStatus.OFFLINE_CACHE
            }
        }
    }

    // ==========================================
    // USER AUTHENTICATION & PROFILE SYNC
    // ==========================================

    suspend fun signUpWithFirestore(
        username: String,
        displayName: String,
        phoneNumber: String,
        password: String,
        avatarEmoji: String,
        statusMessage: String
    ): Result<UserProfileEntity> {
        val cleanUsername = username.trim().lowercase().removePrefix("@")
        if (cleanUsername.length < 3) {
            return Result.failure(IllegalArgumentException("Username must be at least 3 characters."))
        }
        if (!cleanUsername.matches(Regex("^[a-z0-9_.]+$"))) {
            return Result.failure(IllegalArgumentException("Username can only contain letters, numbers, underscores and dots."))
        }
        if (displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your display name."))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("PIN / Password must be at least 4 characters."))
        }

        return try {
            val docRef = firestore.collection(USERS_COLLECTION).document(cleanUsername)
            val existingDoc = try {
                docRef.get().await()
            } catch (e: Exception) {
                null
            }

            if (existingDoc != null && existingDoc.exists()) {
                return Result.failure(IllegalStateException("Username '@$cleanUsername' is already taken. Please choose another username or sign in."))
            }

            // Ensure Firebase Auth session
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                if (auth.currentUser == null) {
                    auth.signInAnonymously().await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Anonymous auth note: ${e.message}")
            }

            val finalStatusMsg = if (statusMessage.isBlank()) "Chatting on Uzzap \uD83D\uDCF1" else statusMessage.trim()
            val newProfile = UserProfileEntity(
                id = "me",
                username = cleanUsername,
                displayName = displayName.trim(),
                phoneNumber = phoneNumber.ifBlank { "+63 918 000 0000" }.trim(),
                status = UserPresence.ONLINE,
                statusMessage = finalStatusMsg,
                avatarEmoji = avatarEmoji.ifBlank { "\uD83D\uDE0A" },
                phoneVerified = true,
                vibrationEnabled = true
            )

            val userData = hashMapOf<String, Any>(
                "username" to cleanUsername,
                "displayName" to displayName.trim(),
                "phoneNumber" to phoneNumber.trim(),
                "password" to password,
                "status" to UserPresence.ONLINE.name,
                "statusMessage" to finalStatusMsg,
                "avatarEmoji" to newProfile.avatarEmoji,
                "phoneVerified" to true,
                "vibrationEnabled" to true,
                "createdAt" to System.currentTimeMillis()
            )

            docRef.set(userData, SetOptions.merge()).await()
            _syncStatus.value = FirestoreSyncStatus.CONNECTED
            Log.d(TAG, "User registered in Firestore: $cleanUsername")
            Result.success(newProfile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed signUpWithFirestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithFirestore(
        usernameOrPhone: String,
        password: String
    ): Result<UserProfileEntity> {
        val cleanInput = usernameOrPhone.trim().lowercase().removePrefix("@")
        if (cleanInput.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your username or mobile number."))
        }

        return try {
            // Ensure Firebase Auth session
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                if (auth.currentUser == null) {
                    auth.signInAnonymously().await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Anonymous auth note: ${e.message}")
            }

            // 1. Try fetching by username document ID
            val userDocRef = firestore.collection(USERS_COLLECTION).document(cleanInput)
            val docSnapshot = try {
                userDocRef.get().await()
            } catch (e: Exception) {
                null
            }

            var targetDoc = if (docSnapshot != null && docSnapshot.exists()) docSnapshot else null

            // 2. If not found by username, try querying by phoneNumber
            if (targetDoc == null) {
                val phoneQuery = try {
                    firestore.collection(USERS_COLLECTION)
                        .whereEqualTo("phoneNumber", usernameOrPhone.trim())
                        .limit(1)
                        .get().await()
                } catch (e: Exception) {
                    null
                }
                if (phoneQuery != null && !phoneQuery.isEmpty) {
                    targetDoc = phoneQuery.documents.first()
                }
            }

            if (targetDoc != null) {
                val storedPassword = targetDoc.getString("password")
                if (!storedPassword.isNullOrBlank() && password.isNotBlank() && storedPassword != password) {
                    return Result.failure(IllegalArgumentException("Incorrect password or PIN for this account."))
                }

                val username = targetDoc.getString("username") ?: cleanInput
                val displayName = targetDoc.getString("displayName") ?: username
                val phoneNumber = targetDoc.getString("phoneNumber") ?: "+63 918 555 1014"
                val statusMsg = targetDoc.getString("statusMessage") ?: "Chatting on Uzzap \uD83D\uDCF1"
                val avatarEmoji = targetDoc.getString("avatarEmoji") ?: "\uD83D\uDE0E"
                val phoneVerified = targetDoc.getBoolean("phoneVerified") ?: true
                val vibrationEnabled = targetDoc.getBoolean("vibrationEnabled") ?: true

                val profile = UserProfileEntity(
                    id = "me",
                    username = username,
                    displayName = displayName,
                    phoneNumber = phoneNumber,
                    status = UserPresence.ONLINE,
                    statusMessage = statusMsg,
                    avatarEmoji = avatarEmoji,
                    phoneVerified = phoneVerified,
                    vibrationEnabled = vibrationEnabled
                )

                // Update status in Firestore
                try {
                    targetDoc.reference.update(
                        mapOf(
                            "status" to UserPresence.ONLINE.name,
                            "lastSeen" to FieldValue.serverTimestamp()
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Could not update last seen: ${e.message}")
                }
                _syncStatus.value = FirestoreSyncStatus.CONNECTED
                Result.success(profile)
            } else {
                // Special check for demo default Juan Dela Cruz
                if (cleanInput == "juandelacruz" || cleanInput == "juan") {
                    val defaultProfile = UserProfileEntity(
                        id = "me",
                        username = "juandelacruz",
                        displayName = "Juan Dela Cruz",
                        phoneNumber = "+63 918 555 1014",
                        status = UserPresence.ONLINE,
                        statusMessage = "Chatting on Uzzap v1.0.14 \uD83D\uDCF1",
                        avatarEmoji = "\uD83D\uDE0E",
                        phoneVerified = true,
                        vibrationEnabled = true
                    )
                    syncUserProfile(defaultProfile)
                    Result.success(defaultProfile)
                } else {
                    Result.failure(IllegalStateException("No account found for '$usernameOrPhone'. Please check your credentials or switch to Sign Up."))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithFirestore exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun syncUserProfile(profile: UserProfileEntity) {
        scope.launch(Dispatchers.IO) {
            try {
                _syncStatus.value = FirestoreSyncStatus.SYNCING
                val userData = hashMapOf<String, Any>(
                    "username" to profile.username,
                    "displayName" to profile.displayName,
                    "phoneNumber" to profile.phoneNumber,
                    "status" to profile.status.name,
                    "statusMessage" to profile.statusMessage,
                    "avatarEmoji" to profile.avatarEmoji,
                    "lastSeen" to FieldValue.serverTimestamp(),
                    "phoneVerified" to profile.phoneVerified,
                    "vibrationEnabled" to profile.vibrationEnabled
                )

                firestore.collection(USERS_COLLECTION)
                    .document(profile.username)
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener {
                        _syncStatus.value = FirestoreSyncStatus.CONNECTED
                        Log.d(TAG, "User profile synced to Firestore: ${profile.username}")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Failed syncing user profile: ${e.message}")
                        _syncStatus.value = FirestoreSyncStatus.OFFLINE_CACHE
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Error in syncUserProfile: ${e.message}")
            }
        }
    }

    fun listenToUsersPresence(
        onPresenceChanged: (username: String, presence: UserPresence, statusMessage: String) -> Unit
    ) {
        try {
            val listener = firestore.collection(USERS_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenToUsersPresence error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        for (doc in snapshot.documentChanges) {
                            val username = doc.document.getString("username") ?: doc.document.id
                            val statusStr = doc.document.getString("status") ?: "ONLINE"
                            val statusMsg = doc.document.getString("statusMessage") ?: ""
                            val presence = try {
                                UserPresence.valueOf(statusStr)
                            } catch (e: Exception) {
                                UserPresence.ONLINE
                            }
                            onPresenceChanged(username, presence, statusMsg)
                        }
                    }
                }
            activeListeners.add(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Error setting up presence listener: ${e.message}")
        }
    }

    // ==========================================
    // CHATROOMS SYNC
    // ==========================================

    fun seedInitialRoomsIfEmpty(initialRooms: List<ChatroomEntity>) {
        scope.launch(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection(CHATROOMS_COLLECTION).limit(1).get().await()
                if (snapshot.isEmpty) {
                    Log.d(TAG, "Firestore chatrooms collection is empty. Seeding initial rooms...")
                    val batch = firestore.batch()
                    initialRooms.forEach { room ->
                        val docRef = firestore.collection(CHATROOMS_COLLECTION).document(room.id)
                        val data = hashMapOf<String, Any>(
                            "id" to room.id,
                            "name" to room.name,
                            "topic" to room.topic,
                            "category" to room.category,
                            "chatterCount" to room.chatterCount,
                            "createdAt" to System.currentTimeMillis()
                        )
                        batch.set(docRef, data, SetOptions.merge())
                    }
                    batch.commit().await()
                    Log.d(TAG, "Successfully seeded ${initialRooms.size} chatrooms to Firestore.")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not check/seed chatrooms: ${e.message}")
            }
        }
    }

    fun listenToChatrooms(onRoomsUpdated: (List<ChatroomEntity>) -> Unit) {
        chatroomsListener?.remove()
        try {
            chatroomsListener = firestore.collection(CHATROOMS_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenToChatrooms error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val rooms = snapshot.documents.mapNotNull { doc ->
                            val id = doc.getString("id") ?: doc.id
                            val name = doc.getString("name") ?: return@mapNotNull null
                            val topic = doc.getString("topic") ?: ""
                            val category = doc.getString("category") ?: "General"
                            val chatterCount = (doc.getLong("chatterCount") ?: 1L).toInt()
                            ChatroomEntity(
                                id = id,
                                name = name,
                                topic = topic,
                                category = category,
                                chatterCount = chatterCount,
                                isJoined = false,
                                userRole = RoomRole.MEMBER
                            )
                        }
                        if (rooms.isNotEmpty()) {
                            onRoomsUpdated(rooms)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error registering chatrooms listener: ${e.message}")
        }
    }

    fun createChatroom(room: ChatroomEntity) {
        scope.launch(Dispatchers.IO) {
            try {
                val data = hashMapOf<String, Any>(
                    "id" to room.id,
                    "name" to room.name,
                    "topic" to room.topic,
                    "category" to room.category,
                    "chatterCount" to room.chatterCount,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection(CHATROOMS_COLLECTION)
                    .document(room.id)
                    .set(data, SetOptions.merge())
                    .await()
                Log.d(TAG, "Created room in Firestore: ${room.name}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed creating room in Firestore: ${e.message}")
            }
        }
    }

    fun updateRoomChatterCount(roomId: String, delta: Int) {
        scope.launch(Dispatchers.IO) {
            try {
                firestore.collection(CHATROOMS_COLLECTION)
                    .document(roomId)
                    .update("chatterCount", FieldValue.increment(delta.toLong()))
            } catch (e: Exception) {
                Log.w(TAG, "Failed updating chatter count: ${e.message}")
            }
        }
    }

    fun sendRoomMessage(roomId: String, message: RoomMessageEntity) {
        scope.launch(Dispatchers.IO) {
            try {
                val data = hashMapOf<String, Any>(
                    "id" to message.id,
                    "roomId" to roomId,
                    "senderUsername" to message.senderUsername,
                    "senderRole" to message.senderRole.name,
                    "message" to message.message,
                    "timestamp" to message.timestamp,
                    "isSystem" to message.isSystem
                )
                firestore.collection(CHATROOMS_COLLECTION)
                    .document(roomId)
                    .collection(ROOM_MESSAGES_SUBCOLLECTION)
                    .document(message.id)
                    .set(data)
                    .await()
                Log.d(TAG, "Room message uploaded to Firestore: ${message.id}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed sending room message to Firestore: ${e.message}")
            }
        }
    }

    fun listenToRoomMessages(
        roomId: String,
        currentUsername: String,
        onNewMessage: (RoomMessageEntity) -> Unit
    ) {
        activeRoomListener?.remove()
        try {
            activeRoomListener = firestore.collection(CHATROOMS_COLLECTION)
                .document(roomId)
                .collection(ROOM_MESSAGES_SUBCOLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenToRoomMessages error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val msgId = doc.getString("id") ?: doc.id
                            val sender = doc.getString("senderUsername") ?: "unknown"
                            val roleStr = doc.getString("senderRole") ?: "MEMBER"
                            val text = doc.getString("message") ?: ""
                            val time = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val isSystem = doc.getBoolean("isSystem") ?: false
                            val role = try {
                                RoomRole.valueOf(roleStr)
                            } catch (e: Exception) {
                                RoomRole.MEMBER
                            }

                            val entity = RoomMessageEntity(
                                id = msgId,
                                roomId = roomId,
                                senderUsername = sender,
                                senderRole = role,
                                message = text,
                                timestamp = time,
                                isFromMe = (sender == currentUsername),
                                isSystem = isSystem
                            )
                            onNewMessage(entity)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to room messages: ${e.message}")
        }
    }

    fun stopListeningToRoomMessages() {
        activeRoomListener?.remove()
        activeRoomListener = null
    }

    // ==========================================
    // 1-ON-1 DIRECT MESSAGES & BUZZ
    // ==========================================

    fun sendDirectMessage(
        conversationId: String,
        recipientUsername: String,
        message: MessageEntity
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val msgData = hashMapOf<String, Any>(
                    "id" to message.id,
                    "conversationId" to conversationId,
                    "senderUsername" to message.senderUsername,
                    "senderDisplayName" to message.senderDisplayName,
                    "recipientUsername" to recipientUsername,
                    "type" to message.type.name,
                    "body" to message.body,
                    "timestamp" to message.timestamp,
                    "status" to message.status.name
                )
                message.replyToBody?.let { msgData["replyToBody"] = it }

                // 1. Write to conversation shared message history
                firestore.collection(CONVERSATIONS_COLLECTION)
                    .document(conversationId)
                    .collection(CONVO_MESSAGES_SUBCOLLECTION)
                    .document(message.id)
                    .set(msgData)

                // 2. Update conversation overview
                val convoData = hashMapOf<String, Any>(
                    "id" to conversationId,
                    "lastMessage" to message.body,
                    "lastTimestamp" to message.timestamp,
                    "lastSender" to message.senderUsername,
                    "participants" to listOf(message.senderUsername, recipientUsername)
                )
                firestore.collection(CONVERSATIONS_COLLECTION)
                    .document(conversationId)
                    .set(convoData, SetOptions.merge())

                // 3. Post to recipient's direct inbox for instant delivery across devices
                firestore.collection(USERS_COLLECTION)
                    .document(recipientUsername)
                    .collection(INBOX_SUBCOLLECTION)
                    .document(message.id)
                    .set(msgData)

                Log.d(TAG, "Sent direct message to $recipientUsername via Firestore")
            } catch (e: Exception) {
                Log.w(TAG, "Failed sending direct message to Firestore: ${e.message}")
            }
        }
    }

    fun listenToUserInbox(
        currentUsername: String,
        onIncomingMessage: (MessageEntity) -> Unit
    ) {
        inboxListener?.remove()
        try {
            inboxListener = firestore.collection(USERS_COLLECTION)
                .document(currentUsername)
                .collection(INBOX_SUBCOLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenToUserInbox error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        for (doc in snapshot.documents) {
                            val id = doc.getString("id") ?: doc.id
                            val convoId = doc.getString("conversationId") ?: "convo_${doc.getString("senderUsername")}"
                            val senderUser = doc.getString("senderUsername") ?: "uzzap_buddy"
                            val senderName = doc.getString("senderDisplayName") ?: senderUser
                            val typeStr = doc.getString("type") ?: "TEXT"
                            val body = doc.getString("body") ?: ""
                            val replyTo = doc.getString("replyToBody")
                            val time = doc.getLong("timestamp") ?: System.currentTimeMillis()

                            val msgType = try {
                                MessageType.valueOf(typeStr)
                            } catch (e: Exception) {
                                MessageType.TEXT
                            }

                            val incomingMsg = MessageEntity(
                                id = id,
                                conversationId = convoId,
                                senderUsername = senderUser,
                                senderDisplayName = senderName,
                                type = msgType,
                                body = body,
                                replyToBody = replyTo,
                                timestamp = time,
                                status = MessageDeliveryStatus.READ,
                                isFromMe = false
                            )
                            onIncomingMessage(incomingMsg)

                            // Clear processed inbox message document
                            doc.reference.delete()
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to inbox: ${e.message}")
        }
    }

    // ==========================================
    // FRIEND REQUESTS SYNC
    // ==========================================

    fun sendFriendRequest(fromUser: UserProfileEntity, toUsername: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val requestId = "${fromUser.username}_to_${toUsername}"
                val reqData = hashMapOf<String, Any>(
                    "id" to requestId,
                    "fromUsername" to fromUser.username,
                    "fromDisplayName" to fromUser.displayName,
                    "fromPhoneNumber" to fromUser.phoneNumber,
                    "toUsername" to toUsername,
                    "status" to "PENDING",
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection(FRIEND_REQUESTS_COLLECTION)
                    .document(requestId)
                    .set(reqData, SetOptions.merge())
                    .await()
                Log.d(TAG, "Sent friend request to $toUsername")
            } catch (e: Exception) {
                Log.w(TAG, "Failed sending friend request to Firestore: ${e.message}")
            }
        }
    }

    fun listenToFriendRequests(
        myUsername: String,
        onNewRequest: (ContactEntity) -> Unit
    ) {
        friendRequestsListener?.remove()
        try {
            friendRequestsListener = firestore.collection(FRIEND_REQUESTS_COLLECTION)
                .whereEqualTo("toUsername", myUsername)
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenToFriendRequests error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        for (doc in snapshot.documents) {
                            val fromUser = doc.getString("fromUsername") ?: continue
                            val fromName = doc.getString("fromDisplayName") ?: fromUser
                            val fromPhone = doc.getString("fromPhoneNumber") ?: ""

                            val contact = ContactEntity(
                                id = "contact_$fromUser",
                                username = fromUser,
                                displayName = fromName,
                                nickname = fromName.split(" ").firstOrNull() ?: fromName,
                                phoneNumber = fromPhone,
                                presence = UserPresence.ONLINE,
                                statusMessage = "Wants to connect on Uzzap \uD83D\uDCF1",
                                category = ContactCategory.BUDDIES,
                                friendshipState = FriendshipState.PENDING_INCOMING,
                                avatarEmoji = "\uD83D\uDE0A",
                                avatarBgColor = 0xFF3F51B5,
                                isFavorite = false,
                                lastSeen = "Online now"
                            )
                            onNewRequest(contact)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to friend requests: ${e.message}")
        }
    }

    fun submitReport(
        reporterUsername: String,
        target: String,
        reason: String,
        details: String
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val reportData = hashMapOf(
                    "reporter" to reporterUsername,
                    "target" to target,
                    "reason" to reason,
                    "details" to details,
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("ugc_reports").add(reportData)
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting report", e)
            }
        }
    }

    suspend fun deleteUserCloudData(username: String) {
        try {
            firestore.collection(USERS_COLLECTION).document(username).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user cloud data", e)
        }
    }

    fun cleanUp() {
        activeListeners.forEach { it.remove() }
        activeListeners.clear()
        activeRoomListener?.remove()
        inboxListener?.remove()
        friendRequestsListener?.remove()
        chatroomsListener?.remove()
    }
}
