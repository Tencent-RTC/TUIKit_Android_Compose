package io.trtc.tuikit.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Badge
import io.trtc.tuikit.atomicx.basecomponent.config.AppBuilderConfig
import io.trtc.tuikit.atomicx.basecomponent.theme.Colors
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.basecomponent.utils.SetActivitySystemBarAppearance
import io.trtc.tuikit.atomicx.contactlist.ui.addfriendandgroup.AddContactAndGroupBottomSheet
import io.trtc.tuikit.atomicx.contactlist.ui.addnewchat.AddNewChatBottomSheet
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddType
import io.trtc.tuikit.atomicx.contactlist.viewmodels.ChatType
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.contact.ContactListStore
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationListStore
import io.trtc.tuikit.atomicxcore.api.login.LoginStatus
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.chat.chat.ChatActivity
import io.trtc.tuikit.chat.login.LoginActivity
import io.trtc.tuikit.chat.pages.ContactsPage
import io.trtc.tuikit.chat.pages.ConversationsPage
import io.trtc.tuikit.chat.search.SearchActivity
import io.trtc.tuikit.chat.widgets.PageHeader
import kotlinx.coroutines.flow.flowOf

data class TabItem(
    val title: Int,
    val normalIcon: Int,
    val selectedIcon: Int,
    val route: String
)

class MainActivity : BaseActivity() {
    var conversationListStore by mutableStateOf<ConversationListStore?>(null)
    var contactListStore by mutableStateOf<ContactListStore?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (LoginStore.shared.loginState.loginStatus.value == LoginStatus.UNLOGIN) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            return
        }

        conversationListStore = ConversationListStore.create()
        conversationListStore?.getConversationTotalUnreadCount()
        contactListStore = ContactListStore.create().apply {
            fetchGroupApplicationList(object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
            fetchFriendApplicationList(object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
        }
        val conversationListState = conversationListStore?.conversationListState
        val contactListState = contactListStore?.contactListState
        val messagesTab = TabItem(
            title = R.string.compose_demo_messages,
            normalIcon = R.drawable.app_main_nav_item_messages_normal_icon,
            selectedIcon = R.drawable.app_main_nav_item_messages_selected_icon,
            route = "Messages"
        )
        val contactsTab = TabItem(
            title = R.string.compose_demo_contacts,
            normalIcon = R.drawable.app_main_nav_item_contacts_normal_icon,
            selectedIcon = R.drawable.app_main_nav_item_contacts_selected_icon,
            route = "Contacts"
        )
        val settingsTab = TabItem(
            title = R.string.compose_demo_settings,
            normalIcon = R.drawable.app_main_nav_item_settings_normal_icon,
            selectedIcon = R.drawable.app_main_nav_item_settings_selected_icon,
            route = "Settings"
        )
        val tabList = listOf(messagesTab, contactsTab, settingsTab)

        setContent {
            val conversationUnreadCount by (conversationListState?.totalUnreadCount
                ?: flowOf(0)).collectAsState(0)
            val friendApplicationUnreadCount by (contactListState?.friendApplicationUnreadCount
                ?: flowOf(0)).collectAsState(0)
            val groupApplicationUnreadCount by (contactListState?.groupApplicationUnreadCount
                ?: flowOf(0)).collectAsState(0)
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryAsState()

            val colors = LocalTheme.current.colors

            SetActivitySystemBarAppearance()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colors.bgColorOperate)
                    .systemBarsPadding()
            ) {
                NavHost(
                    modifier = Modifier
                        .weight(1f)
                        .background(color = colors.bgColorOperate),
                    navController = navController,
                    startDestination = messagesTab.route
                ) {
                    animatedComposable(messagesTab.route) { MessagesScreen() }
                    animatedComposable(contactsTab.route) { ContactsScreen() }
                    animatedComposable(settingsTab.route) { SettingsScreenContent() }
                }
                Column() {
                    HorizontalDivider(color = colors.strokeColorPrimary)

                    Row(modifier = Modifier) {
                        tabList.forEach { tab ->
                            val selected = currentRoute.value?.destination?.route == tab.route

                            NavigationBarItem(
                                icon = {
                                    Box(
                                        Modifier
                                            .width(46.dp)
                                            .height(28.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(if (selected) tab.selectedIcon else tab.normalIcon),
                                            contentDescription = null
                                        )
                                        if (tab.route == "Messages" || tab.route == "Contacts") {
                                            var unreadCount = 0
                                            if (tab.route == "Messages") {
                                                unreadCount = conversationUnreadCount.toInt()
                                            } else {
                                                unreadCount =
                                                    friendApplicationUnreadCount + groupApplicationUnreadCount
                                            }
                                            if (unreadCount > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .offset(x = 4.dp)
                                                        .wrapContentSize()
                                                ) {
                                                    Badge(text = if (unreadCount > 99) "99+" else unreadCount.toString())
                                                }
                                            }
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        stringResource(tab.title),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.W500
                                    )
                                },
                                selected = selected,
                                colors = NavigationBarItemColors(
                                    selectedIconColor = colors.textColorLink,
                                    selectedTextColor = colors.textColorLink,
                                    selectedIndicatorColor = Colors.Transparent,
                                    unselectedIconColor = colors.textColorSecondary,
                                    unselectedTextColor = colors.textColorSecondary,
                                    disabledIconColor = colors.textColorLink,
                                    disabledTextColor = colors.textColorLink,
                                ),
                                onClick = {
                                    navController.navigate(tab.route) {
                                        launchSingleTop = true
                                        popUpTo(0) { saveState = true }
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}


@Composable
private fun MessagesScreen() {
    val activity = LocalActivity.current
    val context = LocalContext.current
    var showAddChatSheet by remember { mutableStateOf(false) }
    var chatType by remember { mutableStateOf(ChatType.SINGLE) }

    ConversationsPage(
        onConversationClick = {
            activity?.startActivity(
                Intent(activity, ChatActivity::class.java).apply {
                    putExtra("conversationID", it.conversationID)
                }
            )
        },
        onSearchClick = {
            SearchActivity.start(context)
        }
    ) {
        if (AppBuilderConfig.enableCreateConversation) {
            AddMoreButton(
                menuItems = createChatMenuItems(
                    onAddC2CChatClick = {
                        chatType = ChatType.SINGLE
                        showAddChatSheet = true
                    },
                    onAddGroupChatClick = {
                        chatType = ChatType.GROUP
                        showAddChatSheet = true
                    },
                )
            )
        }
    }

    if (showAddChatSheet) {
        AddNewChatBottomSheet(
            chatType = chatType,
            onDismiss = { showAddChatSheet = false },
            onCreateChat = { conversationId ->
                showAddChatSheet = false
                activity?.startActivity(
                    Intent(activity, ChatActivity::class.java).apply {
                        putExtra("conversationID", conversationId)
                    }
                )
            }
        )
    }
}

@Composable
private fun ContactsScreen() {
    var showAddContactSheet by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf(AddType.CONTACT) }
    val context = LocalContext.current
    val activity = LocalActivity.current
    ContactsPage(onGroupClick = {
        activity?.startActivity(
            Intent(activity, ChatActivity::class.java).apply {
                putExtra("conversationID", "group_${it.contactID}")
            }
        )
    }, onContactClick = {
        activity?.startActivity(
            Intent(activity, ChatActivity::class.java).apply {
                putExtra("conversationID", "c2c_${it.contactID}")
            }
        )
    }) {
        AddMoreButton(
            menuItems = createAddContactMenuItems(
                onAddFriendClick = {
                    addType = AddType.CONTACT
                    showAddContactSheet = true
                },
                onAddGroupClick = {
                    addType = AddType.GROUP
                    showAddContactSheet = true
                }
            )
        )
    }

    if (showAddContactSheet) {
        AddContactAndGroupBottomSheet(
            addType = addType,
            onDismiss = { showAddContactSheet = false }
        )
    }
}

@Composable
private fun SettingsScreenContent() {
    Column(
        modifier = Modifier
    ) {
        PageHeader(stringResource(R.string.compose_demo_settings))
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SettingsScreen()
        }
    }
}

private fun NavGraphBuilder.animatedComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
        popEnterTransition = {
            EnterTransition.None
        },
        popExitTransition = {
            ExitTransition.None
        },
        content = content
    )
}