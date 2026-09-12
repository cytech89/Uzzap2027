package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UzzapDatabase
import com.example.data.model.ChatroomEntity
import com.example.data.model.ContactCategory
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.RoomMessageEntity
import com.example.data.model.UserPresence
import com.example.data.model.UserProfileEntity
import com.example.data.repository.UzzapRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    BUDDIES("Buddies"),
    CHATS("Chats"),
    ROOMS("Rooms"),
    PROFILE("Profile")
}

@OptIn(ExperimentalCoroutinesApi::class)
class UzzapViewModel(application: Application) : AndroidViewModel(application) {
    private val database = UzzapDatabase.getDatabase(application, viewModelScope)
    val repository = UzzapRepository(database, viewModelScope)

    // Current navigation state
    private val _currentTab = MutableStateFlow(MainTab.BUDDIES)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _activeRoomId = MutableStateFlow<String?>(null)
    val activeRoomId: StateFlow<String?> = _activeRoomId.asStateFlow()

    // Filters and Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _buddyCategoryFilter = MutableStateFlow("All")
    val buddyCategoryFilter: StateFlow<String> = _buddyCategoryFilter.asStateFlow()

    private val _roomCategoryFilter = MutableStateFlow("All")
    val roomCategoryFilter: StateFlow<String> = _roomCategoryFilter.asStateFlow()

    // UI Dialogs
    private val _isAddContactDialogOpen = MutableStateFlow(false)
    val isAddContactDialogOpen: StateFlow<Boolean> = _isAddContactDialogOpen.asStateFlow()

    private val _isCreateRoomDialogOpen = MutableStateFlow(false)
    val isCreateRoomDialogOpen: StateFlow<Boolean> = _isCreateRoomDialogOpen.asStateFlow()

    private val _isPresenceMenuOpen = MutableStateFlow(false)
    val isPresenceMenuOpen: StateFlow<Boolean> = _isPresenceMenuOpen.asStateFlow()

    private val _buzzShakeTrigger = MutableStateFlow(0)
    val buzzShakeTrigger: StateFlow<Int> = _buzzShakeTrigger.asStateFlow()

    // Persistent Flows from Room
    val profile: StateFlow<UserProfileEntity?> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val contacts: StateFlow<List<ContactEntity>> = repository.contactsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRequests: StateFlow<List<ContactEntity>> = repository.pendingRequestsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<ConversationEntity>> = repository.conversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatrooms: StateFlow<List<ChatroomEntity>> = repository.chatroomsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active conversation messages
    val activeConversationMessages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getMessagesForConversation(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active room messages
    val activeRoomMessages: StateFlow<List<RoomMessageEntity>> = _activeRoomId
        .flatMapLatest { id ->
            if (id != null) repository.getRoomMessages(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Collect buzz events to trigger vibration/screen shake
        viewModelScope.launch {
            repository.buzzEvents.collect {
                _buzzShakeTrigger.value += 1
            }
        }
    }

    // Navigation Actions
    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun openConversation(conversationId: String) {
        _activeConversationId.value = conversationId
        viewModelScope.launch {
            repository.markConversationRead(conversationId)
        }
    }

    fun startConversationWithContact(contact: ContactEntity) {
        viewModelScope.launch {
            val convoId = repository.startOrGetConversation(contact)
            _activeConversationId.value = convoId
            _currentTab.value = MainTab.CHATS
        }
    }

    fun closeConversation() {
        _activeConversationId.value = null
    }

    fun openRoom(roomId: String) {
        _activeRoomId.value = roomId
    }

    fun closeRoom() {
        _activeRoomId.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setBuddyCategoryFilter(filter: String) {
        _buddyCategoryFilter.value = filter
    }

    fun setRoomCategoryFilter(filter: String) {
        _roomCategoryFilter.value = filter
    }

    // Dialog Controls
    fun setAddContactDialogOpen(open: Boolean) {
        _isAddContactDialogOpen.value = open
    }

    fun setCreateRoomDialogOpen(open: Boolean) {
        _isCreateRoomDialogOpen.value = open
    }

    fun setPresenceMenuOpen(open: Boolean) {
        _isPresenceMenuOpen.value = open
    }

    // Operational Actions
    fun updatePresence(status: UserPresence, message: String) {
        viewModelScope.launch {
            repository.updatePresence(status, message)
            _isPresenceMenuOpen.value = false
        }
    }

    fun updateVibrationSetting(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateVibration(enabled)
        }
    }

    fun toggleFavorite(contactId: String) {
        viewModelScope.launch {
            repository.toggleFavoriteContact(contactId)
        }
    }

    fun acceptFriendRequest(contactId: String) {
        viewModelScope.launch {
            repository.acceptFriendRequest(contactId)
        }
    }

    fun declineFriendRequest(contactId: String) {
        viewModelScope.launch {
            repository.declineFriendRequest(contactId)
        }
    }

    fun addContact(username: String, displayName: String, phone: String, category: ContactCategory) {
        viewModelScope.launch {
            repository.addContact(username, displayName, phone, category)
            _isAddContactDialogOpen.value = false
        }
    }

    fun sendMessage(text: String, replyTo: String? = null) {
        val convoId = _activeConversationId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(convoId, text, replyTo)
        }
    }

    fun sendBuzz() {
        val convoId = _activeConversationId.value ?: return
        viewModelScope.launch {
            repository.sendBuzz(convoId)
        }
    }

    fun joinOrLeaveRoom(roomId: String, join: Boolean) {
        viewModelScope.launch {
            repository.joinOrLeaveRoom(roomId, join)
        }
    }

    fun sendRoomMessage(text: String) {
        val roomId = _activeRoomId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendRoomMessage(roomId, text)
        }
    }

    fun createChatroom(name: String, topic: String, category: String) {
        viewModelScope.launch {
            repository.createChatroom(name, topic, category)
            _isCreateRoomDialogOpen.value = false
        }
    }
}
