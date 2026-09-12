package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserPresence
import com.example.ui.components.AddContactDialog
import com.example.ui.components.CreateRoomDialog
import com.example.ui.components.PresenceSelectorDialog
import com.example.ui.components.UzzapTopHeader
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatsScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoomDetailScreen
import com.example.ui.screens.RoomsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.UzzapOrange
import com.example.ui.theme.UzzapOrangeContainer
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.UzzapViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                UzzapApp()
            }
        }
    }
}

@Composable
fun UzzapApp(
    viewModel: UzzapViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeConversationId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val activeRoomId by viewModel.activeRoomId.collectAsStateWithLifecycle()

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val pendingRequests by viewModel.pendingRequests.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val chatrooms by viewModel.chatrooms.collectAsStateWithLifecycle()

    val activeMessages by viewModel.activeConversationMessages.collectAsStateWithLifecycle()
    val activeRoomMessages by viewModel.activeRoomMessages.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val buddyFilter by viewModel.buddyCategoryFilter.collectAsStateWithLifecycle()
    val roomFilter by viewModel.roomCategoryFilter.collectAsStateWithLifecycle()

    val isAddContactDialogOpen by viewModel.isAddContactDialogOpen.collectAsStateWithLifecycle()
    val isCreateRoomDialogOpen by viewModel.isCreateRoomDialogOpen.collectAsStateWithLifecycle()
    val isPresenceMenuOpen by viewModel.isPresenceMenuOpen.collectAsStateWithLifecycle()
    val buzzTrigger by viewModel.buzzShakeTrigger.collectAsStateWithLifecycle()

    val activeConvo = conversations.firstOrNull { it.id == activeConversationId }
    val activeRoom = chatrooms.firstOrNull { it.id == activeRoomId }

    // Intercept back navigation when in detail screens
    BackHandler(enabled = activeConversationId != null || activeRoomId != null) {
        if (activeConversationId != null) {
            viewModel.closeConversation()
        } else if (activeRoomId != null) {
            viewModel.closeRoom()
        }
    }

    val totalUnread = conversations.sumOf { it.unreadCount }
    val pendingRequestsCount = pendingRequests.size

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (activeConversationId == null && activeRoomId == null) {
                UzzapTopHeader(
                    profile = profile,
                    onPresenceClick = { viewModel.setPresenceMenuOpen(true) }
                )
            }
        },
        bottomBar = {
            if (activeConversationId == null && activeRoomId == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    // 1. Buddies
                    NavigationBarItem(
                        selected = currentTab == MainTab.BUDDIES,
                        onClick = { viewModel.setTab(MainTab.BUDDIES) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (pendingRequestsCount > 0) {
                                        Badge(containerColor = UzzapOrange) {
                                            Text("$pendingRequestsCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == MainTab.BUDDIES) Icons.Filled.Group else Icons.Outlined.Group,
                                    contentDescription = "Buddies"
                                )
                            }
                        },
                        label = { Text("Buddies", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UzzapOrange,
                            selectedTextColor = UzzapOrange,
                            indicatorColor = UzzapOrange.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_buddies")
                    )

                    // 2. Chats
                    NavigationBarItem(
                        selected = currentTab == MainTab.CHATS,
                        onClick = { viewModel.setTab(MainTab.CHATS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (totalUnread > 0) {
                                        Badge(containerColor = UzzapOrange) {
                                            Text("$totalUnread")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == MainTab.CHATS) Icons.Filled.Chat else Icons.Outlined.Chat,
                                    contentDescription = "Chats"
                                )
                            }
                        },
                        label = { Text("Chats", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UzzapOrange,
                            selectedTextColor = UzzapOrange,
                            indicatorColor = UzzapOrange.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_chats")
                    )

                    // 3. Rooms
                    NavigationBarItem(
                        selected = currentTab == MainTab.ROOMS,
                        onClick = { viewModel.setTab(MainTab.ROOMS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.ROOMS) Icons.Filled.Tag else Icons.Outlined.Tag,
                                contentDescription = "Rooms"
                            )
                        },
                        label = { Text("Rooms", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UzzapOrange,
                            selectedTextColor = UzzapOrange,
                            indicatorColor = UzzapOrange.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_rooms")
                    )

                    // 4. Profile
                    NavigationBarItem(
                        selected = currentTab == MainTab.PROFILE,
                        onClick = { viewModel.setTab(MainTab.PROFILE) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UzzapOrange,
                            selectedTextColor = UzzapOrange,
                            indicatorColor = UzzapOrange.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                activeConversationId != null -> {
                    ChatDetailScreen(
                        conversation = activeConvo,
                        messages = activeMessages,
                        onBack = { viewModel.closeConversation() },
                        onSendMessage = { text, replyTo -> viewModel.sendMessage(text, replyTo) },
                        onSendBuzz = { viewModel.sendBuzz() },
                        buzzTrigger = buzzTrigger
                    )
                }
                activeRoomId != null -> {
                    RoomDetailScreen(
                        room = activeRoom,
                        messages = activeRoomMessages,
                        onBack = { viewModel.closeRoom() },
                        onSendMessage = { text -> viewModel.sendRoomMessage(text) }
                    )
                }
                else -> {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TabTransition"
                    ) { tab ->
                        when (tab) {
                            MainTab.BUDDIES -> {
                                FriendsScreen(
                                    contacts = contacts,
                                    pendingRequests = pendingRequests,
                                    searchQuery = searchQuery,
                                    selectedCategory = buddyFilter,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onCategoryChange = { viewModel.setBuddyCategoryFilter(it) },
                                    onContactClick = { contact ->
                                        viewModel.startConversationWithContact(contact)
                                    },
                                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                    onAcceptRequest = { viewModel.acceptFriendRequest(it) },
                                    onDeclineRequest = { viewModel.declineFriendRequest(it) },
                                    onAddContactClick = { viewModel.setAddContactDialogOpen(true) }
                                )
                            }
                            MainTab.CHATS -> {
                                ChatsScreen(
                                    conversations = conversations,
                                    onConversationClick = { convoId ->
                                        viewModel.openConversation(convoId)
                                    },
                                    onStartNewChat = {
                                        viewModel.setTab(MainTab.BUDDIES)
                                    }
                                )
                            }
                            MainTab.ROOMS -> {
                                RoomsScreen(
                                    rooms = chatrooms,
                                    selectedCategory = roomFilter,
                                    onCategoryChange = { viewModel.setRoomCategoryFilter(it) },
                                    onRoomClick = { room ->
                                        viewModel.openRoom(room.id)
                                    },
                                    onToggleJoin = { roomId, join ->
                                        viewModel.joinOrLeaveRoom(roomId, join)
                                    },
                                    onCreateRoomClick = {
                                        viewModel.setCreateRoomDialogOpen(true)
                                    }
                                )
                            }
                            MainTab.PROFILE -> {
                                ProfileScreen(
                                    profile = profile,
                                    totalBuddies = contacts.size,
                                    totalChats = conversations.size,
                                    totalRooms = chatrooms.count { it.isJoined },
                                    onEditPresenceClick = { viewModel.setPresenceMenuOpen(true) },
                                    onToggleVibration = { viewModel.updateVibrationSetting(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (isPresenceMenuOpen) {
        PresenceSelectorDialog(
            currentPresence = profile?.status ?: UserPresence.ONLINE,
            currentMessage = profile?.statusMessage ?: "",
            onDismiss = { viewModel.setPresenceMenuOpen(false) },
            onSave = { presence, message ->
                viewModel.updatePresence(presence, message)
            }
        )
    }

    if (isAddContactDialogOpen) {
        AddContactDialog(
            onDismiss = { viewModel.setAddContactDialogOpen(false) },
            onAdd = { username, displayName, phone, category ->
                viewModel.addContact(username, displayName, phone, category)
            }
        )
    }

    if (isCreateRoomDialogOpen) {
        CreateRoomDialog(
            onDismiss = { viewModel.setCreateRoomDialogOpen(false) },
            onCreate = { name, topic, category ->
                viewModel.createChatroom(name, topic, category)
            }
        )
    }
}

// Preserve backwards-compatible Greeting composable for Robolectric screenshot test
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
