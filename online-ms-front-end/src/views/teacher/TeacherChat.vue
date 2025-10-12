<template>
  <div class="chat-container">
    <!-- 左侧聊天类型切换栏 -->
    <div class="chat-sidebar neumorphism-card"
         style="margin-left: 0; margin-right: 0; padding-left: 15px; padding-right: 10px;">
      <div class="sidebar-header">
        <h3 class="sidebar-title">
          <font-awesome-icon :icon="['fas', 'comments']" class="title-icon"/>
          聊天
        </h3>
      </div>

      <div class="chat-type-tabs">
        <button
            class="chat-type-btn"
            :class="{ active: currentChatType === chatTypes.FRIEND }"
            @click="switchChatType(chatTypes.FRIEND)"
        >
          <font-awesome-icon :icon="['fas', 'user-friends']" class="tab-icon"/>
          <span class="tab-text">好友</span>
          <div class="tab-indicator"></div>
        </button>

        <button
            class="chat-type-btn"
            :class="{ active: currentChatType === chatTypes.GROUP }"
            @click="switchChatType(chatTypes.GROUP)"
        >
          <font-awesome-icon :icon="['fas', 'users']" class="tab-icon"/>
          <span class="tab-text">群聊</span>
          <div class="tab-indicator"></div>
        </button>

        <button
            class="chat-type-btn"
            :class="{ active: currentChatType === chatTypes.AI }"
            @click="switchChatType(chatTypes.AI)"
        >
          <font-awesome-icon :icon="['fas', 'robot']" class="tab-icon"/>
          <span class="tab-text">AI助手</span>
          <div class="tab-indicator"></div>
        </button>
      </div>
    </div>

    <!-- 中间对话列表栏 -->
    <div class="conversation-list neumorphism-card"
         style="margin-left: 0;margin-right: 0;padding-left: 0;padding-right: 0;">
      <div class="list-header">
        <h4 class="list-title">
          {{
            currentChatType === chatTypes.FRIEND ? '好友列表' :
                currentChatType === chatTypes.GROUP ? '群聊列表' : 'Recent Chats'
          }}
        </h4>
        <div class="list-actions">
          <button class="action-btn" v-if="currentChatType === chatTypes.FRIEND">
            <font-awesome-icon :icon="['fas', 'user-plus']"/>
          </button>
          <button class="action-btn" v-if="currentChatType === chatTypes.GROUP">
            <font-awesome-icon :icon="['fas', 'plus']"/>
          </button>
          <button class="action-btn create-new-btn" v-if="currentChatType === chatTypes.AI" @click="createNewAIChat">
            <font-awesome-icon :icon="['fas', 'plus']"/>
            <span class="btn-text">New Chat</span>
          </button>
        </div>
      </div>

      <div class="conversation-items">
        <div
            v-for="conversation in currentConversationList"
            :key="conversation.id"
            class="conversation-item"
            :class="{ active: currentConversation?.id === conversation.id }"
            @click="selectConversation(conversation)"
        >
          <div class="conversation-avatar">
            <img v-if="conversation.avatar" :src="conversation.avatar" :alt="conversation.name"/>
            <div v-else class="avatar-placeholder">
              <font-awesome-icon
                  :icon="currentChatType === chatTypes.AI ? ['fas', 'robot'] :
                       currentChatType === chatTypes.GROUP ? ['fas', 'users'] : ['fas', 'user']"
              />
            </div>
            <div v-if="conversation.online" class="online-indicator"></div>
          </div>

          <div class="conversation-info">
            <div class="conversation-header">
              <h5 class="conversation-name">{{ conversation.name }}</h5>
              <div class="conversation-actions">
                <span class="conversation-time">{{ conversation.lastTime }}</span>
                <button
                    v-if="currentChatType === chatTypes.AI && conversation.conversationId"
                    class="delete-conversation-btn"
                    :class="{ deleting: isDeletingConversation && conversationToDelete?.id === conversation.id }"
                    @click.stop="confirmDeleteConversation(conversation)"
                    :disabled="isDeletingConversation"
                    title="删除会话"
                >
                  <font-awesome-icon
                      v-if="isDeletingConversation && conversationToDelete?.id === conversation.id"
                      :icon="['fas', 'spinner']"
                      spin
                  />
                  <font-awesome-icon v-else :icon="['fas', 'trash']"/>
                </button>
              </div>
            </div>
            <div class="conversation-preview">
              <p class="last-message">{{ conversation.lastMessage }}</p>
              <div class="conversation-meta">
                <span v-if="conversation.memberCount" class="member-count">
                  <font-awesome-icon :icon="['fas', 'users']"/>
                  {{ conversation.memberCount }}
                </span>
                <span v-if="conversation.unreadCount > 0" class="unread-badge">
                  {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天内容区域 -->
    <div class="chat-content neumorphism-card" style="padding: 12px;">
      <div v-if="!currentConversation" class="empty-chat">
        <div class="empty-icon">
          <font-awesome-icon :icon="['fas', 'comments']"/>
        </div>
        <h3 class="empty-title">选择一个对话开始聊天</h3>
        <p class="empty-description">从左侧选择好友、群聊或AI助手开始对话</p>
      </div>

      <div v-else class="chat-active">
        <!-- 专注模式退出按钮 -->
        <div v-if="isFocusMode" class="focus-mode-exit">
          <button class="exit-focus-btn" @click="exitFocusMode" title="退出专注对话">
            <font-awesome-icon :icon="['fas', 'times']" class="exit-icon"/>
            <span>退出专注对话</span>
          </button>
        </div>
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="chat-info">
            <div class="chat-avatar">
              <img v-if="currentConversation.avatar" :src="currentConversation.avatar" :alt="currentConversation.name"/>
              <div v-else class="avatar-placeholder">
                <font-awesome-icon
                    :icon="currentChatType === chatTypes.AI ? ['fas', 'robot'] :
                         currentChatType === chatTypes.GROUP ? ['fas', 'users'] : ['fas', 'user']"
                />
              </div>
              <div v-if="currentConversation.online" class="online-indicator"></div>
            </div>
            <div class="chat-details">
              <h4 class="chat-name" :title="currentConversation.name">{{ truncatedConversationName }}</h4>
              <p class="chat-status">
                <span v-if="currentChatType === chatTypes.FRIEND">
                  {{ currentConversation.online ? '在线' : '离线' }}
                </span>
                <span v-else-if="currentChatType === chatTypes.GROUP">
                  {{ currentConversation.memberCount }} 名成员
                </span>
                <span v-else>
                  AI助手 - 随时为您服务
                </span>
              </p>
            </div>
          </div>
          <div class="chat-actions">
            <button class="header-action-btn" v-if="currentChatType === chatTypes.FRIEND">
              <font-awesome-icon :icon="['fas', 'phone']"/>
            </button>
            <button class="header-action-btn" v-if="currentChatType === chatTypes.FRIEND">
              <font-awesome-icon :icon="['fas', 'video']"/>
            </button>
            <div class="header-action-dropdown">
              <button class="header-action-btn" @click="toggleModeMenu" ref="modeMenuButton">
                <font-awesome-icon :icon="['fas', 'ellipsis-v']"/>
              </button>
              <!-- 模式选择菜单 -->
              <div v-if="showModeMenu" class="mode-menu neumorphism-raised" ref="modeMenu">
                <div class="mode-menu-header">
                  <span>对话模式</span>
                </div>
                <div class="mode-menu-options">
                  <button class="mode-option" @click="enterFullscreenMode">
                    <font-awesome-icon :icon="['fas', 'expand']" class="mode-icon"/>
                    <div class="mode-info">
                      <span class="mode-title">全屏对话</span>
                      <span class="mode-desc">浏览器全屏显示</span>
                    </div>
                  </button>
                  <button class="mode-option" @click="enterFocusMode">
                    <font-awesome-icon :icon="['fas', 'eye']" class="mode-icon"/>
                    <div class="mode-info">
                      <span class="mode-title">专注对话</span>
                      <span class="mode-desc">隐藏无关界面</span>
                    </div>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-container">
          <div class="messages-list">
            <div
                v-for="message in currentMessages"
                :key="message.id"
                class="message-item"
                :class="{
                  'sender-message': currentChatType === chatTypes.AI ? message.receiverId === 'ai_assistant' : message.isOwn,
                  'receiver-message': currentChatType === chatTypes.AI ? message.receiverId !== 'ai_assistant' : !message.isOwn
                }"
            >
              <div class="conversation-avatar" v-if="currentChatType === chatTypes.AI ? message.receiverId !== 'ai_assistant' : !message.isOwn">
                <img v-if="message.senderAvatar" :src="message.senderAvatar" :alt="message.senderName"
                     class="avatar-img"/>
                <div v-else class="avatar-placeholder">
                  <font-awesome-icon :icon="currentChatType === chatTypes.AI ? ['fas', 'robot'] : ['fas', 'user']"/>
                </div>
              </div>

              <div class="message-content">
                <div class="message-header" v-if="currentChatType === chatTypes.AI ? message.receiverId !== 'ai_assistant' : !message.isOwn">
                  <span class="sender-name">{{ message.senderName }}</span>
                  <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                </div>
                <div class="message-bubble">
                  <markdownRender v-if="currentChatType === chatTypes.AI && message.receiverId !== 'ai_assistant'" :content="message.content" class="message-text" />
                  <p v-else class="message-text">{{ message.content }}</p>
                  <div v-if="message.isStreaming" class="streaming-indicator">
                    <span class="dot"></span>
                    <span class="dot"></span>
                    <span class="dot"></span>
                  </div>
                </div>
                <div class="message-time" v-if="currentChatType === chatTypes.AI ? message.receiverId === 'ai_assistant' : message.isOwn">
                  {{ formatTime(message.timestamp) }}
                </div>
              </div>


            </div>
          </div>
        </div>

        <!-- 消息输入区域 -->
        <div class="message-input-area">
          <!-- 表情选择器 -->
          <div v-if="showEmojiPicker" class="emoji-picker neumorphism-raised">
            <div class="emoji-header">
              <span>选择表情</span>
              <button @click="closeEmojiPicker" class="close-btn">
                <i class="fas fa-times"></i>
              </button>
            </div>
            <div class="emoji-grid">
              <button
                  v-for="emoji in emojiList"
                  :key="emoji"
                  @click="insertEmoji(emoji)"
                  class="emoji-item"
                  :title="emoji"
              >
                {{ emoji }}
              </button>
            </div>
          </div>
          <div class="input-toolbar">
            <button
                class="toolbar-btn neumorphism-raised"
                title="表情"
                @click="toggleEmojiPicker"
                :class="{ active: showEmojiPicker }"
            >
              <font-awesome-icon :icon="['fas', 'smile']"/>
            </button>
            <button class="toolbar-btn">
              <font-awesome-icon :icon="['fas', 'paperclip']"/>
            </button>
            <button class="toolbar-btn">
              <font-awesome-icon :icon="['fas', 'image']"/>
            </button>
          </div>

          <div class="input-container">
            <textarea
                v-model="messageInput"
                @keydown="handleInputKeydown"
                @click="closeEmojiPicker"
                placeholder="输入消息..."
                class="message-textarea"
                rows="1"
                @keypress="handleKeyPress"
            ></textarea>
            <button
                class="send-btn"
                :disabled="!messageInput.trim()"
                @click="sendMessage"
            >
              <font-awesome-icon :icon="['fas', 'paper-plane']"/>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, onUnmounted, nextTick} from 'vue'
import {createWebSocket} from '@/utils/websocket'
import {getCurrentUserId, parseTokenPayload} from '@/api/index'
import {getFriendList, getHistoryList, getHistoryDetail, getTeacherProfile} from '@/api/teacher/teacherAPI.js'
import {fetchConversationList, fetchMessagesForConversation, deleteConversationById,getAIChatResponseObject} from '@/api/teacher/TeacherAIChatAPI.js'
import {ElMessage, ElMessageBox} from 'element-plus'
import markdownRender from '@/components/markdownRender/Index.vue'

// 聊天类型
const chatTypes = {
  FRIEND: 'friend',
  GROUP: 'group',
  AI: 'ai'
}

// 当前选中的聊天类型
const currentChatType = ref(chatTypes.FRIEND)

// 当前选中的对话
const currentConversation = ref(null)

// 消息输入
const messageInput = ref('')

// WebSocket相关变量
let socket = null
const connected = ref(false)
const error = ref('')
const currentUserId = ref('')

// 聊天历史记录
const chatHistory = ref([])

// 接收到的消息列表
const receivedMessages = ref([])

// 当前选中的聊天对象
const selectedChat = ref(null)

// 当前选中聊天的消息列表
const selectedChatMessages = ref([])

// 未读消息计数
const unreadCounts = ref({})

// 当前用户头像
const currentUserAvatar = ref('')

// AI聊天相关状态
const isAILoading = ref(false)
const aiConversationLoading = ref(false)
const currentAIConversationId = ref(null)

// 删除会话相关状态
const isDeletingConversation = ref(false)
const conversationToDelete = ref(null)

// 表情相关数据
const showEmojiPicker = ref(false)
const emojiList = ref([
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇',
  '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚',
  '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩',
  '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣',
  '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬',
  '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗',
  '🤔', '🤭', '🤫', '🤥', '😶', '😐', '😑', '😬', '🙄', '😯',
  '😦', '😧', '😮', '😲', '🥱', '😴', '🤤', '😪', '😵', '🤐',
  '🥴', '🤢', '🤮', '🤧', '😷', '🤒', '🤕', '🤑', '🤠', '😈',
  '👿', '👹', '👺', '🤡', '💩', '👻', '💀', '☠️', '👽', '👾'
])


// 模式选择菜单相关
const showModeMenu = ref(false)
const isFocusMode = ref(false)
const isFullscreenMode = ref(false)
const modeMenuButton = ref(null)
const modeMenu = ref(null)

// 计算属性：截断对话标题
const truncatedConversationName = computed(() => {
  if (!currentConversation.value || !currentConversation.value.name) {
    return ''
  }

  const name = currentConversation.value.name
  // 计算字符串的字节长度（中文字符占2个字节，英文字符占1个字节）
  let byteLength = 0
  let truncatedName = ''

  for (let i = 0; i < name.length; i++) {
    const char = name.charAt(i)
    // 判断是否为中文字符（Unicode范围）
    const isChinese = /[\u4e00-\u9fa5]/.test(char)
    const charByteLength = isChinese ? 2 : 1

    if (byteLength + charByteLength <= 10) {
      byteLength += charByteLength
      truncatedName += char
    } else {
      break
    }
  }

  // 如果原始名称长度超过截断后的长度，添加省略号
  return truncatedName.length < name.length ? truncatedName + '...' : truncatedName
})

// 好友列表 - friendsList
const friends = ref([
  {
    id: '1',
    name: '张同学',
    avatar: null,
    lastMessage: '你好，有什么问题吗？',
    lastTime: '10:30',
    unreadCount: 2,
    online: true
  },
  {
    id: '2',
    name: '李老师',
    avatar: null,
    lastMessage: '作业已经批改完成',
    lastTime: '昨天',
    unreadCount: 0,
    online: false
  },
  {
    id: '3',
    name: '王同学',
    avatar: null,
    lastMessage: '明天一起去图书馆吗？',
    lastTime: '15:20',
    unreadCount: 1,
    online: true
  }
])

// 加载好友列表
const loadFriendsList = async () => {
  try {
    const response = await getFriendList()

    // 检查响应结构 - 支持两种格式：直接数组或包含data字段的对象
    let friendsData = []

    if (Array.isArray(response)) {
      // 直接返回数组的情况
      friendsData = response
    } else if (response && response.success && response.data && Array.isArray(response.data)) {
      // 包含success和data字段的对象结构
      friendsData = response.data
    } else {
      console.error('好友列表数据格式错误:', response)
      ElMessage.error('好友列表数据格式错误')
      return
    }

    // 将后端数据映射为前端需要的格式
    friends.value = friendsData.map(friend => ({
      id: friend.friendId,
      name: friend.remark || friend.userName,
      avatar: friend.avatarUrl || `https://via.placeholder.com/40/4CAF50/FFFFFF?text=${friend.userName.charAt(0)}`,
      lastMessage: '点击开始聊天',
      lastTime: '刚刚',
      online: true, // 默认在线状态，后续可根据实际需求调整
      role: friend.role,
      remark: friend.remark,
      unreadCount: unreadCounts.value[friend.friendId] || 0
    }))

    console.log('好友列表加载成功:', friends.value)
    ElMessage.success(`成功加载 ${friends.value.length} 个好友`)
  } catch (error) {
    console.error('加载好友列表失败:', error)
    ElMessage.error('加载好友列表失败: ' + error.message)
  }
}

// 加载聊天历史记录
const loadChatHistory = async () => {
  try {
    const response = await getHistoryList()

    // 检查响应结构 - 支持两种格式：直接数组或包含data字段的对象
    let historyData = []

    if (Array.isArray(response)) {
      // 直接返回数组的情况
      historyData = response
    } else if (response && response.success && response.data && Array.isArray(response.data)) {
      // 包含success和data字段的对象结构
      historyData = response.data
    } else {
      console.error('聊天历史数据格式错误:', response)
      ElMessage.error('聊天历史数据格式错误')
      return
    }

    // 将后端数据映射为前端需要的格式
    chatHistory.value = historyData.map(chat => {
      // 处理好友信息
      const friendInfo = chat.friendInfo || {}
      const chatName = friendInfo.remark || friendInfo.userName || chat.chatName || chat.name || '未知聊天'
      const avatarUrl = friendInfo.avatarUrl || chat.avatarUrl || chat.avatar
      const friendId = friendInfo.friendId

      return {
        id: chat.conversationId || chat.chatId || chat.id,
        name: chatName,
        avatar: avatarUrl || `https://via.placeholder.com/40/4CAF50/FFFFFF?text=${chatName.charAt(0)}`,
        type: chat.chatType || chat.type || 'friend',
        lastMessage: chat.lastMessageContent || chat.lastMessage || '暂无消息',
        lastMessageTime: chat.timestamp ? new Date(chat.timestamp).getTime() : (chat.lastMessageTime || Date.now()),
        unreadCount: friendId ? (unreadCounts.value[friendId] || chat.unreadCount || 0) : (chat.unreadCount || 0),
        // 保存原始好友信息，用于后续聊天
        friendInfo: friendInfo
      }
    }).sort((a, b) => b.lastMessageTime - a.lastMessageTime) // 按最后消息时间倒序排列

    console.log('聊天历史加载成功:', chatHistory.value)
    ElMessage.success(`成功加载 ${chatHistory.value.length} 条聊天记录`)
  } catch (error) {
    console.error('加载聊天历史失败:', error)
    ElMessage.error('加载聊天历史失败: ' + error.message)
  }
}

// 定义消息处理逻辑
const handleSocketMessage = (data) => {
  try {
    // 解析消息数据
    let messageData
    try {
      messageData = JSON.parse(data)
    } catch {
      messageData = {content: data, timestamp: Date.now()}
    }

    // 添加时间戳
    if (!messageData.timestamp) {
      messageData.timestamp = Date.now()
    }

    // 添加到消息列表
    receivedMessages.value.push(messageData)

    // 如果当前有选中的聊天，也添加到选中聊天的消息列表
    if (currentConversation.value && messageData.senderId === currentConversation.value.id) {
      // 根据senderId查找对应好友的头像
      const friend = friends.value.find(f => f.id === messageData.senderId)
      const senderAvatar = messageData.senderAvatar || messageData.avatarUrl || (friend ? friend.avatar : null)

      const newMessage = {
        id: messageData.id || Date.now(),
        content: messageData.content,
        senderId: messageData.senderId,
        senderName: messageData.senderName || '好友',
        senderAvatar: senderAvatar,
        timestamp: messageData.timestamp,
        isOwn: false
      }
      currentMessages.value.push(newMessage)

      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    } else {
      // 如果不是当前聊天对象的消息，增加未读计数
      const senderId = messageData.senderId
      if (senderId && senderId !== currentUserId.value) {
        updateUnreadCount(senderId, 1)
      }
    }

    // 更新聊天历史记录
    updateChatHistory(messageData)

    // 显示通知
    // ElMessage.info(`收到新消息: ${messageData.content}`)
  } catch (err) {
    console.error('处理消息时出错:', err)
    error.value = '消息处理失败'
  }
}

// 更新未读消息计数
const updateUnreadCount = (senderId, increment) => {
  if (unreadCounts.value[senderId]) {
    unreadCounts.value[senderId] += increment
  } else {
    unreadCounts.value[senderId] = increment
  }
}

// 更新聊天历史记录
const updateChatHistory = (messageData) => {
  const chatIndex = chatHistory.value.findIndex(chat => chat.id === messageData.senderId)
  if (chatIndex !== -1) {
    chatHistory.value[chatIndex].lastMessage = messageData.content
    chatHistory.value[chatIndex].lastMessageTime = messageData.timestamp

    // 重新按最后消息时间倒序排列
    chatHistory.value.sort((a, b) => b.lastMessageTime - a.lastMessageTime)
  }
}

// 滚动到底部
const scrollToBottom = () => {
  const messagesContainer = document.querySelector('.messages-list')
  if (messagesContainer) {
    messagesContainer.scrollTop = messagesContainer.scrollHeight
  }
}


// 群聊列表
const groups = ref([
  {
    id: 'g1',
    name: 'Vue.js学习小组',
    avatar: null,
    lastMessage: '大家今天的作业完成了吗？',
    lastTime: '16:45',
    unreadCount: 5,
    memberCount: 15
  },
  {
    id: 'g2',
    name: '前端开发交流群',
    avatar: null,
    lastMessage: '分享一个很棒的CSS技巧',
    lastTime: '14:20',
    unreadCount: 0,
    memberCount: 28
  }
])

// AI对话列表
const aiChats = ref([
  {
    id: 'ai1',
    name: 'AI学习助手对话 #1',
    avatar: null,
    lastMessage: '我可以帮你解答学习问题',
    lastTime: '刚刚',
    unreadCount: 0,
    type: 'learning',
    createdAt: '2024-01-15 10:00:00'
  },
  {
    id: 'ai2',
    name: 'AI编程助手对话 #2',
    avatar: null,
    lastMessage: '有什么编程问题需要帮助吗？',
    lastTime: '09:15',
    unreadCount: 0,
    type: 'coding',
    createdAt: '2024-01-15 09:15:00'
  },
  {
    id: 'ai3',
    name: 'AI数学助手对话 #3',
    avatar: null,
    lastMessage: '需要帮助解决数学问题吗？',
    lastTime: '昨天',
    unreadCount: 0,
    type: 'math',
    createdAt: '2024-01-14 16:30:00'
  }
])

// 当前对话消息
const currentMessages = ref([
  {
    id: '1',
    senderId: '2',
    senderName: '李老师',
    content: '你好，有什么问题需要帮助吗？',
    timestamp: '2024-01-15 10:25:00',
    type: 'text',
    isOwn: false
  },
  {
    id: '2',
    senderId: 'me',
    senderName: '我',
    content: '老师，我想问一下关于Vue组件的问题',
    timestamp: '2024-01-15 10:26:00',
    type: 'text',
    isOwn: true
  },
  {
    id: '3',
    senderId: '2',
    senderName: '李老师',
    content: '好的，你具体想了解哪方面的内容？',
    timestamp: '2024-01-15 10:27:00',
    type: 'text',
    isOwn: false
  }
])

// 计算当前显示的对话列表
const currentConversationList = computed(() => {
  switch (currentChatType.value) {
    case chatTypes.FRIEND:
      return friends.value
    case chatTypes.GROUP:
      return groups.value
    case chatTypes.AI:
      return aiChats.value
    default:
      return []
  }
})

// 切换聊天类型
const switchChatType = async (type) => {
  currentChatType.value = type
  currentConversation.value = null
  currentMessages.value = []

  // 如果切换到AI聊天类型，加载AI会话列表
  if (type === chatTypes.AI) {
    await loadAIConversationList()
  }
}

// 选择对话
const selectConversation = async (conversation) => {
  currentConversation.value = conversation
  selectedChat.value = conversation

  // 清空当前消息列表
  currentMessages.value = []

  // 清除未读计数
  if (unreadCounts.value[conversation.id]) {
    unreadCounts.value[conversation.id] = 0
  }

  // 根据聊天类型加载不同的消息
  if (currentChatType.value === chatTypes.AI) {
    // 对于AI聊天，使用conversationId加载消息
    const conversationId = conversation.conversationId || conversation.id
    console.log('选择AI会话，conversationId:', conversationId)
    await loadAIMessages(conversationId)
  } else {
    // 对于好友和群聊，使用原有的加载方式
    await loadMessages(conversation.id)
  }
}

// 加载消息
const loadMessages = async (conversationId) => {
  try {
    if (currentChatType.value === chatTypes.FRIEND) {
      // 加载好友聊天记录
      const response = await getHistoryDetail(conversationId)

      let messagesData = []
      if (Array.isArray(response)) {
        messagesData = response
      } else if (response && response.data && Array.isArray(response.data)) {
        messagesData = response.data
      }

      // 将后端数据映射为前端需要的格式
      let mappedMessages = messagesData.map(msg => {
        // 根据senderId查找对应好友的头像
        let senderAvatar = null
        if (msg.senderId !== currentUserId.value) {
          const friend = friends.value.find(f => f.id === msg.senderId)
          senderAvatar = msg.senderAvatar || msg.avatarUrl || (friend ? friend.avatar : null)
        }

        return {
          id: msg.id || Date.now(),
          senderId: msg.senderId,
          senderName: msg.senderName || (msg.senderId === currentUserId.value ? '我' : '好友'),
          senderAvatar: senderAvatar,
          content: msg.content,
          timestamp: msg.timestamp || new Date().toISOString(),
          type: 'text',
          isOwn: msg.senderId === currentUserId.value,
          // 保存原始时间戳用于排序
          originalTimestamp: msg.timestamp ? new Date(msg.timestamp).getTime() : Date.now()
        }
      })

      // 对消息按时间戳进行正序排序（从早到晚）
      mappedMessages.sort((a, b) => a.originalTimestamp - b.originalTimestamp)

      // 移除临时的排序字段
      currentMessages.value = mappedMessages.map(msg => {
        const {originalTimestamp, ...messageWithoutSort} = msg
        return messageWithoutSort
      })

      console.log('聊天消息加载成功:', currentMessages.value)
    } else {
      // AI助手或群聊的消息加载逻辑
      console.log('加载对话消息:', conversationId)
    }

    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败: ' + error.message)
  }
}

// 创建新的AI对话
const createNewAIChat = () => {
  const newChatId = 'ai' + (Date.now())
  const newChat = {
    id: newChatId,
    conversationId: null, // 新会话暂时没有conversationId，将在第一次发送消息时由后端生成
    name: `AI助手对话 #${aiChats.value.length + 1}`,
    avatar: null,
    lastMessage: '开始新的对话...',
    lastTime: '刚刚',
    unreadCount: 0,
    type: 'general',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }

  aiChats.value.unshift(newChat)
  currentAIConversationId.value = null // 重置AI会话ID，创建新对话
  selectConversation(newChat)
  console.log('创建新AI会话:', newChat)
  ElMessage.success('已创建新的AI对话')
}

// 加载AI会话列表
const loadAIConversationList = async () => {
  try {
    aiConversationLoading.value = true
    const conversationList = await fetchConversationList()

    // 检查响应结构 - 支持两种格式：直接数组或包含data字段的对象
    let conversationData = []

    if (Array.isArray(conversationList)) {
      // 直接返回数组的情况
      conversationData = conversationList
    } else if (conversationList && conversationList.success && conversationList.data && Array.isArray(conversationList.data)) {
      // 包含success和data字段的对象结构
      conversationData = conversationList.data
    } else if (conversationList && conversationList.data && Array.isArray(conversationList.data)) {
      // 只包含data字段的对象结构
      conversationData = conversationList.data
    } else {
      console.warn('AI会话列表数据格式未知，使用空数组:', conversationList)
      conversationData = []
    }

    // 将后端返回的AI会话数据映射为前端格式
    aiChats.value = conversationData.map((conv, index) => {
      // 确保conversationId字段被正确解析和存储
      const conversationId = conv.conversationId || conv.id || conv.chatId

      return {
        id: conversationId, // 核心标识符，用于后续会话管理
        conversationId: conversationId, // 明确存储conversationId字段
        name: conv.title || conv.name || `AI助手对话 #${index + 1}`,
        avatar: null,
        lastMessage: conv.summary || conv.lastMessage || '点击开始对话',
        lastTime: formatTime(conv.updatedAt || conv.lastMessageTime || new Date()),
        unreadCount: 0,
        type: 'ai',
        createdAt: conv.createdAt || new Date().toISOString(),
        updatedAt: conv.updatedAt || new Date().toISOString()
      }
    }).sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))

    console.log('AI会话列表加载成功:', aiChats.value)
    console.log('已解析的conversationId列表:', aiChats.value.map(chat => chat.conversationId))
    ElMessage.success(`成功加载 ${aiChats.value.length} 个AI会话`)
  } catch (error) {
    console.error('加载AI会话列表失败:', error)
    ElMessage.error('加载AI会话列表失败: ' + error.message)
    // 发生错误时保持默认的AI会话列表
  } finally {
    aiConversationLoading.value = false
  }
}

// 加载AI会话消息
const loadAIMessages = async (conversationId) => {
  try {
    isAILoading.value = true
    currentMessages.value = []

    // 检查是否为有效的conversationId（不是临时创建的新会话）
    if (conversationId && !conversationId.toString().includes(Date.now().toString().slice(-6))) {
      console.log('加载AI会话消息，conversationId:', conversationId)

      // 使用fetchMessagesForConversation加载历史AI会话消息
      const fetchedMessages = await fetchMessagesForConversation({
        conversationId: conversationId,
        page: 0,
        size: 50 // 加载最近50条消息
      })

      // 检查响应结构
      let messagesData = []
      if (Array.isArray(fetchedMessages)) {
        messagesData = fetchedMessages
      } else if (fetchedMessages && fetchedMessages.data && Array.isArray(fetchedMessages.data)) {
        messagesData = fetchedMessages.data
      } else if (fetchedMessages && fetchedMessages.content && Array.isArray(fetchedMessages.content)) {
        // 分页响应格式
        messagesData = fetchedMessages.content
      }

      // 将后端消息数据映射为前端格式
      currentMessages.value = messagesData.map(msg => {
        // 根据receiverId判断消息是否为用户发送的
        // 当receiverId为"ai_assistant"时，表示用户发送的消息，显示在右侧
        // 当receiverId不是"ai_assistant"时，表示接收到的消息，显示在左侧
        const isUserMessage = msg.receiverId === 'ai_assistant'

        return {
          id: msg.id || Date.now(),
          content: msg.content || msg.message || '',
          senderId: isUserMessage ? getCurrentUserId() : 'ai',
          senderName: isUserMessage ? '我' : 'AI助手',
          senderAvatar: isUserMessage ? currentUserAvatar.value : null,
          timestamp: msg.timestamp || msg.createdAt || new Date().toLocaleString(),
          type: 'text',
          isOwn: isUserMessage,
          isStreaming: false,
          receiverId: msg.receiverId // 保留原始receiverId用于调试
        }
      }).sort((a, b) => {
        // 按时间戳排序，确保消息顺序正确
        const timeA = new Date(a.timestamp).getTime()
        const timeB = new Date(b.timestamp).getTime()
        return timeA - timeB
      })

      currentAIConversationId.value = conversationId
      console.log('AI会话消息加载成功，消息数量:', currentMessages.value.length)
    } else {
      // 新建AI会话，显示欢迎消息
      currentMessages.value = [{
        id: 'welcome',
        content: '你好！我是AI助手，有什么可以帮助你的吗？',
        senderId: 'ai',
        senderName: 'AI助手',
        senderAvatar: null,
        timestamp: new Date().toLocaleString(),
        type: 'text',
        isOwn: false,
        isStreaming: false
      }]

      currentAIConversationId.value = null
      console.log('创建新AI会话，显示欢迎消息')
    }
  } catch (error) {
    console.error('加载AI消息失败:', error)
    ElMessage.error('加载AI消息失败: ' + error.message)
    currentMessages.value = [{
      id: 'error',
      content: '加载历史消息失败，请稍后重试',
      senderId: 'ai',
      senderName: 'AI助手',
      timestamp: new Date().toLocaleString(),
      type: 'text',
      isOwn: false,
      isStreaming: false
    }]
  } finally {
    isAILoading.value = false
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// 发送消息
const sendMessage = async () => {
  if (!messageInput.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  if (!currentConversation.value) {
    ElMessage.warning('请先选择一个对话')
    return
  }

  const senderId = getCurrentUserId()
  if (!senderId) {
    ElMessage.error('无法获取用户信息，请重新登录')
    return
  }

  // AI聊天处理逻辑
  if (currentChatType.value === chatTypes.AI) {
    try {
      const userMessage = messageInput.value.trim()

      // 创建用户消息显示（右侧显示，receiverId为ai_assistant）
      const userLocalMessage = {
        id: Date.now(),
        content: userMessage,
        senderId: senderId,
        senderName: '我',
        senderAvatar: currentUserAvatar.value,
        timestamp: new Date().toLocaleString(),
        type: 'text',
        isOwn: true,
        receiverId: 'ai_assistant' // 用户发送给AI的消息，receiverId为ai_assistant
      }

      // 添加用户消息到列表
      currentMessages.value.push(userLocalMessage)

      // 清空输入框
      messageInput.value = ''

      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })

      // 创建AI回复的占位消息（左侧显示，receiverId不是ai_assistant）
      const aiPlaceholderMessage = {
        id: 'ai-' + Date.now(),
        content: '',
        senderId: 'ai',
        senderName: 'AI助手',
        senderAvatar: null,
        timestamp: new Date().toLocaleString(),
        type: 'text',
        isOwn: false,
        isStreaming: true,
        receiverId: senderId // AI回复给用户的消息，receiverId为用户ID
      }

      currentMessages.value.push(aiPlaceholderMessage)

      // 调用AI聊天API
      await getAIChatResponseObject(
          {
            question: userMessage,
            conversationId: currentAIConversationId.value
          },
          // onData回调 - 处理流式响应
          (data) => {
            const lastMessage = currentMessages.value[currentMessages.value.length - 1]
            if (lastMessage && lastMessage.id === aiPlaceholderMessage.id) {
              lastMessage.content += data.content
              nextTick(() => {
                scrollToBottom()
              })
            }
          },
          // onComplete回调 - 流结束
          () => {
            const lastMessage = currentMessages.value[currentMessages.value.length - 1]
            if (lastMessage && lastMessage.id === aiPlaceholderMessage.id) {
              lastMessage.isStreaming = false
            }
            console.log('AI回复完成')
          },
          // onError回调 - 错误处理
          (error) => {
            console.error('AI聊天错误:', error)
            const lastMessage = currentMessages.value[currentMessages.value.length - 1]
            if (lastMessage && lastMessage.id === aiPlaceholderMessage.id) {
              lastMessage.content = '抱歉，AI服务暂时不可用，请稍后重试。'
              lastMessage.isStreaming = false
            }
            ElMessage.error('AI聊天服务暂时不可用')
          }
      )

    } catch (error) {
      console.error('发送AI消息失败:', error)
      ElMessage.error('发送AI消息失败: ' + error.message)
    }
    return
  }

  // 普通聊天（好友/群聊）处理逻辑
  if (socket && socket.readyState === WebSocket.OPEN) {
    try {
      // 根据Java ChatMessage格式构建消息数据
      const messageData = {
        senderId: senderId,
        receiverId: currentConversation.value?.type === 'friend' || currentChatType.value === chatTypes.FRIEND ? currentConversation.value.id : null,
        groupId: currentConversation.value?.type === 'group' || currentChatType.value === chatTypes.GROUP ? currentConversation.value.id : null,
        messageType: currentChatType.value === chatTypes.GROUP ? 'GROUP' : 'PRIVATE',
        contentType: 'TEXT',
        content: messageInput.value.trim(),
        timestamp: new Date().toISOString(),
        status: 'SENT',
        tag: currentChatType.value === chatTypes.FRIEND ? 'friend' : null
      }

      socket.send(JSON.stringify(messageData))

      // 创建本地消息显示
      const localMessage = {
        id: Date.now(),
        content: messageInput.value.trim(),
        senderId: senderId,
        senderName: '我',
        timestamp: new Date().toLocaleString(),
        type: 'text',
        isOwn: true
      }

      // 添加到消息列表
      currentMessages.value.push(localMessage)

      // 更新聊天记录中的最后消息
      const chatIndex = chatHistory.value.findIndex(chat => chat.id === currentConversation.value.id)
      if (chatIndex !== -1) {
        chatHistory.value[chatIndex].lastMessage = messageInput.value.trim()
        chatHistory.value[chatIndex].lastMessageTime = Date.now()

        // 重新按最后消息时间倒序排列
        chatHistory.value.sort((a, b) => b.lastMessageTime - a.lastMessageTime)
      }

      // 更新当前对话列表中的最后消息
      if (currentChatType.value === chatTypes.FRIEND) {
        const friendIndex = friends.value.findIndex(friend => friend.id === currentConversation.value.id)
        if (friendIndex !== -1) {
          friends.value[friendIndex].lastMessage = messageInput.value.trim()
          friends.value[friendIndex].lastTime = '刚刚'
        }
      }

      // 清空输入框
      messageInput.value = ''

      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })

      ElMessage.success('消息发送成功')
    } catch (err) {
      error.value = '发送消息失败'
      console.error('发送消息时出错:', err)
      ElMessage.error('发送消息失败: ' + err.message)
    }
  } else {
    error.value = '无法发送消息，WebSocket 未连接'
    console.error('无法发送消息，WebSocket 未连接')
    ElMessage.error('WebSocket未连接，无法发送消息')
  }
}

// 处理Enter键发送
const handleKeyPress = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''

  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    return ''
  }

  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 24小时内
    // 显示具体时间（小时:分钟）
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${hours}:${minutes}`
  } else if (diff < 604800000) { // 7天内
    // 显示星期几和具体时间
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const weekday = weekdays[date.getDay()]
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${weekday} ${hours}:${minutes}`
  } else {
    // 显示完整日期和时间
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${month}/${day} ${hours}:${minutes}`
  }
}

// 建立WebSocket连接
const connectWebSocket = () => {
  try {
    socket = createWebSocket(handleSocketMessage)

    if (socket) {
      socket.onopen = () => {
        connected.value = true
        error.value = ''
        console.log('WebSocket 连接已成功建立')
        ElMessage.success('实时聊天服务已连接')
      }

      socket.onclose = () => {
        connected.value = false
        console.log('WebSocket 连接已关闭')
        ElMessage.warning('实时聊天服务已断开')
      }

      socket.onerror = (err) => {
        connected.value = false
        error.value = 'WebSocket连接错误'
        console.error('WebSocket 发生错误:', err)
        ElMessage.error('实时聊天服务连接失败')
      }
    }
  } catch (err) {
    error.value = '连接失败: ' + err.message
    console.error('创建WebSocket连接失败:', err)
    ElMessage.error('WebSocket连接失败: ' + err.message)
  }
}

// 处理输入键盘事件
const handleInputKeydown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

// 表情相关方法
const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const insertEmoji = (emoji) => {
  messageInput.value += emoji
  showEmojiPicker.value = false
}

const closeEmojiPicker = () => {
  showEmojiPicker.value = false
}

// 模式切换相关方法
const toggleModeMenu = () => {
  showModeMenu.value = !showModeMenu.value
}

// 进入全屏模式
const enterFullscreenMode = async () => {
  showModeMenu.value = false
  try {
    if (document.documentElement.requestFullscreen) {
      await document.documentElement.requestFullscreen()
    } else if (document.documentElement.webkitRequestFullscreen) {
      await document.documentElement.webkitRequestFullscreen()
    } else if (document.documentElement.msRequestFullscreen) {
      await document.documentElement.msRequestFullscreen()
    }
    isFullscreenMode.value = true
    ElMessage.success('已进入全屏对话模式')
  } catch (error) {
    console.error('进入全屏失败:', error)
    ElMessage.error('无法进入全屏模式')
  }
}

// 进入专注模式
const enterFocusMode = () => {
  showModeMenu.value = false
  isFocusMode.value = true

  // 隐藏页面其他元素
  const chatContainer = document.querySelector('.chat-container')
  if (chatContainer) {
    chatContainer.classList.add('focus-mode')
  }

  // 隐藏导航栏和侧边栏
  const navbar = document.querySelector('.navbar, .nav-bar, .header')
  const sidebar = document.querySelector('.sidebar, .side-nav, .menu')
  const layout = document.querySelector('.layout, .main-layout')

  if (navbar) {
    navbar.style.display = 'none'
    navbar.setAttribute('data-hidden-by-focus', 'true')
  }
  if (sidebar) {
    sidebar.style.display = 'none'
    sidebar.setAttribute('data-hidden-by-focus', 'true')
  }
  if (layout) {
    layout.style.padding = '0'
    layout.setAttribute('data-modified-by-focus', 'true')
  }

  ElMessage.success('已进入专注对话模式')
}

// 退出专注模式
const exitFocusMode = () => {
  isFocusMode.value = false

  // 恢复页面其他元素
  const chatContainer = document.querySelector('.chat-container')
  if (chatContainer) {
    chatContainer.classList.remove('focus-mode')
  }

  // 恢复导航栏和侧边栏
  const hiddenElements = document.querySelectorAll('[data-hidden-by-focus="true"]')
  hiddenElements.forEach(element => {
    element.style.display = ''
    element.removeAttribute('data-hidden-by-focus')
  })

  const modifiedElements = document.querySelectorAll('[data-modified-by-focus="true"]')
  modifiedElements.forEach(element => {
    element.style.padding = ''
    element.removeAttribute('data-modified-by-focus')
  })

  ElMessage.success('已退出专注对话模式')
}

// 监听全屏状态变化
const handleFullscreenChange = () => {
  const isFullscreen = !!(document.fullscreenElement || document.webkitFullscreenElement || document.msFullscreenElement)
  if (!isFullscreen && isFullscreenMode.value) {
    isFullscreenMode.value = false
    ElMessage.info('已退出全屏模式')
  }
}

// 点击外部关闭菜单
const handleClickOutside = (event) => {
  if (showModeMenu.value && modeMenu.value && !modeMenu.value.contains(event.target) && !modeMenuButton.value.contains(event.target)) {
    showModeMenu.value = false
  }
  if (showEmojiPicker.value) {
    const emojiPicker = document.querySelector('.emoji-picker')
    if (emojiPicker && !emojiPicker.contains(event.target)) {
      closeEmojiPicker()
    }
  }
}

// 确认删除会话
const confirmDeleteConversation = (conversation) => {
  conversationToDelete.value = conversation

  // 使用Element Plus的确认对话框
  ElMessageBox.confirm(
      `确定要删除会话 "${conversation.name}" 吗？此操作不可撤销。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
        customClass: 'delete-conversation-dialog'
      }
  ).then(() => {
    deleteConversation(conversation)
  }).catch(() => {
    conversationToDelete.value = null
    ElMessage.info('已取消删除')
  })
}

// 删除会话
const deleteConversation = async (conversation) => {
  if (!conversation.conversationId) {
    ElMessage.error('无法删除：会话ID不存在')
    return
  }

  try {
    isDeletingConversation.value = true

    // 调用API删除会话
    const result = await deleteConversationById(conversation.conversationId)

    if (result && result.code === 200) {
      // 从AI会话列表中移除
      const index = aiChats.value.findIndex(chat => chat.id === conversation.id)
      if (index !== -1) {
        aiChats.value.splice(index, 1)
      }

      // 如果删除的是当前选中的会话，清空当前会话
      if (currentConversation.value && currentConversation.value.id === conversation.id) {
        currentConversation.value = null
        currentMessages.value = []
        currentAIConversationId.value = null
      }

      ElMessage.success('会话已成功删除')
    } else {
      ElMessage.error(result?.message || '删除失败，请稍后重试')
    }
  } catch (error) {
    console.error('删除会话失败:', error)
    ElMessage.error('删除失败：' + (error.message || '网络错误'))
  } finally {
    isDeletingConversation.value = false
    conversationToDelete.value = null
  }
}

onMounted(async () => {
  // 初始化当前用户ID
  currentUserId.value = getCurrentUserId() || 'current_user'

  // 获取当前用户头像
  try {
    // 从token中获取用户标识符
    const userInfo = parseTokenPayload()
    if (userInfo && userInfo.sub) {
      getTeacherProfile(userInfo.sub,
          (data) => {
            if (data && data.avatarUrl) {
              currentUserAvatar.value = data.avatarUrl
            }
          },
          (error) => {
            console.warn('获取用户头像失败:', error)
          }
      )
    }
  } catch (error) {
    console.warn('解析用户信息失败:', error)
  }

  // 建立WebSocket连接
  connectWebSocket()

  // 加载所有类型的聊天数据
  await Promise.all([
    loadFriendsList(),
    loadChatHistory(),
    loadAIConversationList() // 添加AI会话列表加载
  ])

  // 添加事件监听器
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  document.addEventListener('webkitfullscreenchange', handleFullscreenChange)
  document.addEventListener('msfullscreenchange', handleFullscreenChange)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  // 移除事件监听器
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  document.removeEventListener('webkitfullscreenchange', handleFullscreenChange)
  document.removeEventListener('msfullscreenchange', handleFullscreenChange)
  document.removeEventListener('click', handleClickOutside)

  // 如果在专注模式下，退出时恢复页面
  if (isFocusMode.value) {
    exitFocusMode()
  }

  // 关闭WebSocket连接
  if (socket) {
    socket.close()
  }
})
</script>

<style scoped>
/* 聊天容器布局 */
.chat-container {
  display: flex;
  flex-direction: row;
  height: calc(100vh - 120px);
  gap: 1rem;
  padding: 1rem;
  background: transparent;
  align-items: stretch;
  justify-content: flex-start;
}

/* 新拟态卡片基础样式 */
.neumorphism-card {
  background: #f0f0f3;
  box-shadow: 8px 8px 16px #d1d1d4,
  -8px -8px 16px #ffffff;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.neumorphism-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg,
  rgba(255, 255, 255, 0.1) 0%,
  rgba(255, 255, 255, 0.05) 50%,
  rgba(0, 0, 0, 0.02) 100%);
  pointer-events: none;
}

/* 左侧聊天类型切换栏 */
.chat-sidebar {
  width: 200px;
  min-width: 200px;
  flex-shrink: 0;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.sidebar-header {
  margin-bottom: 2rem;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #002FA7;
}

.title-icon {
  font-size: 1.5rem;
}

.chat-type-tabs {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.chat-type-btn {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  color: #4a5568;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 4px 4px 8px rgba(0, 47, 167, 0.1),
  -2px -2px 4px rgba(255, 255, 255, 0.3);
}

.chat-type-btn:hover {
  background: rgba(0, 47, 167, 0.05);
  color: #002FA7;
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px rgba(0, 47, 167, 0.15),
  -3px -3px 6px rgba(255, 255, 255, 0.4);
}

.chat-type-btn.active {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.1) 0%, rgba(81, 123, 77, 0.05) 100%);
  color: #002FA7;
  font-weight: 600;
  box-shadow: inset 4px 4px 8px rgba(0, 47, 167, 0.1),
  inset -2px -2px 4px rgba(255, 255, 255, 0.2);
}

.chat-type-btn.active .tab-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  border-radius: 0 2px 2px 0;
}

.tab-icon {
  font-size: 1.125rem;
  transition: all 0.3s ease;
}

.tab-text {
  font-size: 0.95rem;
}

/* 中间对话列表栏 */
.conversation-list {
  width: 320px;
  min-width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 1.5rem 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.list-title {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: #002FA7;
}

.list-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  color: #517B4D;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 2px 2px 4px rgba(0, 47, 167, 0.1),
  -1px -1px 2px rgba(255, 255, 255, 0.3);
}

.action-btn:hover {
  background: rgba(81, 123, 77, 0.1);
  color: #517B4D;
  transform: scale(1.05);
}

.create-new-btn {
  width: auto;
  padding: 0.5rem 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  color: white;
  font-weight: 500;
  font-size: 0.875rem;
}

.create-new-btn:hover {
  background: linear-gradient(135deg, #001f75 0%, #3d5c39 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 4px 4px 12px rgba(0, 47, 167, 0.3),
  -2px -2px 6px rgba(255, 255, 255, 0.2);
}

.btn-text {
  white-space: nowrap;
}

.conversation-items {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  margin-bottom: 0.5rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.conversation-item:hover {
  background: rgba(0, 47, 167, 0.05);
  transform: translateX(4px);
  box-shadow: 4px 4px 8px rgba(0, 47, 167, 0.1),
  -2px -2px 4px rgba(255, 255, 255, 0.3);
}

.conversation-item.active {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.1) 0%, rgba(81, 123, 77, 0.05) 100%);
  box-shadow: inset 2px 2px 4px rgba(0, 47, 167, 0.1),
  inset -1px -1px 2px rgba(255, 255, 255, 0.2);
}

.conversation-avatar {
  /*position: relative;
  width: 48px;
  height: 48px;
  flex-shrink: 0;*/
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 10px;
}

.sender-message .message-time {
  text-align: right;
  margin-top: 5px;
  font-size: 0.8em;
  color: #888;
}

.receiver-message .message-header {
  margin-bottom: 5px;
}

.receiver-message .sender-name {
  font-weight: bold;
  margin-right: 10px;
}

.receiver-message .message-time {
  font-size: 0.8em;
  color: #888;
}

.conversation-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.25rem;
  box-shadow: 4px 4px 8px rgba(0, 47, 167, 0.2),
  -2px -2px 4px rgba(255, 255, 255, 0.1);
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: #67C23A;
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.4);
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.25rem;
}

.delete-conversation-btn {
  width: 24px;
  height: 24px;
  background: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.2);
  border-radius: 6px;
  color: #F56C6C;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.8);
  font-size: 0.75rem;
}

.conversation-item:hover .delete-conversation-btn {
  opacity: 1;
  transform: scale(1);
}

.delete-conversation-btn:hover {
  background: rgba(245, 108, 108, 0.2);
  color: #E53E3E;
  transform: scale(1.1);
  box-shadow: 2px 2px 4px rgba(245, 108, 108, 0.2);
}

.delete-conversation-btn:active {
  transform: scale(0.95);
}

.delete-conversation-btn.deleting {
  opacity: 0.5;
  cursor: not-allowed;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 0.8;
  }
}

.conversation-name {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-time {
  font-size: 0.75rem;
  color: #64748b;
  flex-shrink: 0;
}

.conversation-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.last-message {
  margin: 0;
  font-size: 0.875rem;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.conversation-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.member-count {
  font-size: 0.75rem;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.unread-badge {
  background: #F56C6C;
  color: white;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
}

/* 右侧聊天内容区域 */
.chat-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

/* 空状态 */
.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 2rem;
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 2rem;
  margin-bottom: 1.5rem;
  box-shadow: 8px 8px 16px rgba(0, 47, 167, 0.2),
  -4px -4px 8px rgba(255, 255, 255, 0.1);
}

.empty-title {
  margin: 0 0 0.5rem;
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
}

.empty-description {
  margin: 0;
  font-size: 1rem;
  color: #64748b;
}

/* 活跃聊天状态 */
.chat-active {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 聊天头部 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
}

.chat-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.chat-avatar {
  position: relative;
  width: 48px;
  height: 48px;
}

.chat-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.chat-details {
  display: flex;
  flex-direction: column;
}

.chat-name {
  margin: 0 0 0.25rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: #1e293b;
}

.chat-status {
  margin: 0;
  font-size: 0.875rem;
  color: #64748b;
}

.chat-actions {
  display: flex;
  gap: 0.5rem;
}

.header-action-btn {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  color: #517B4D;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 2px 2px 4px rgba(0, 47, 167, 0.1),
  -1px -1px 2px rgba(255, 255, 255, 0.3);
}

.header-action-btn:hover {
  background: rgba(81, 123, 77, 0.1);
  color: #517B4D;
  transform: scale(1.05);
}

/* 消息容器 */
.messages-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 消息项 */
.message-item {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.sender-message {
  flex-direction: row-reverse;
}

.receiver-message {
  flex-direction: row;
}

.message-content {
  max-width: 70%;
}

.message-item.own-message {
  display: flex;
  align-self: self-end;
  flex-direction: row-reverse;
}

.message-item.own-message .message-content {
  margin-right: 10px;
}

.message-item.own-message .conversation-avatar {
  margin-left: 10px; /* 头像左侧留间距 */
  margin-right: 0; /* 右侧无间距 */
}

.own-message .message-content {
  text-align: right;
}

.message-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.message-avatar .avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1rem;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.sender-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1e293b;
}

.message-time {
  font-size: 0.75rem;
  color: #64748b;
}

.own-message .message-time {
  align-self: flex-end;
  margin-top: 0.25rem;
}

.message-bubble {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px; /* 圆角效果 */
  /*padding: 0.75rem 1rem;*/
  box-shadow: 4px 4px 8px rgba(0, 47, 167, 0.05),
  -2px -2px 4px rgba(255, 255, 255, 0.2);
  position: relative;
  backdrop-filter: blur(10px);
  /*max-width: 50%;*/ /* 设置最大宽度，防止过长的消息占据整个屏幕 */
  display: inline-block; /* 使气泡宽度由内容决定 */
  word-wrap: break-word; /* 允许长单词或 URL 地址换行 */
  margin-bottom: 8px; /* 消息间的间距 */
  padding: 10px 15px;
  max-width: 100%;
}

.sender-message .message-bubble {
  background: linear-gradient(135deg, #517B4D 0%, #002FA7 100%);
  margin-left: auto;
}

.sender-message .message-text {
  color: white;
}

.receiver-message .message-bubble {
  background-color: #f1f0f0;
}


.own-message .message-bubble {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.1) 0%, rgba(81, 123, 77, 0.05) 100%);
  border-color: rgba(0, 47, 167, 0.2);
}

.message-text {
  margin: 0; /* 移除段落默认边距 */
  font-size: 0.95rem;
  color: #1e293b;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 消息输入区域 */
.message-input-area {
  padding: 1rem 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
}

.input-toolbar {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.toolbar-btn {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 2px 2px 4px rgba(0, 47, 167, 0.1),
  -1px -1px 2px rgba(255, 255, 255, 0.3);
}

.toolbar-btn:hover {
  background: rgba(0, 47, 167, 0.05);
  color: #002FA7;
  transform: scale(1.05);
}

.input-container {
  display: flex;
  gap: 0.75rem;
  align-items: flex-end;
}

.message-textarea {
  flex: 1;
  min-height: 44px;
  max-height: 120px;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 22px;
  font-size: 0.95rem;
  color: #1e293b;
  resize: none;
  outline: none;
  transition: all 0.3s ease;
  box-shadow: inset 2px 2px 4px rgba(0, 47, 167, 0.05),
  inset -1px -1px 2px rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.message-textarea::placeholder {
  color: #a0aec0;
}

.message-textarea:focus {
  border-color: rgba(0, 47, 167, 0.3);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: inset 1px 1px 2px rgba(0, 47, 167, 0.1),
  inset -0.5px -0.5px 1px rgba(255, 255, 255, 0.3),
  0 0 0 2px rgba(0, 47, 167, 0.1);
}

.send-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  box-shadow: 4px 4px 8px rgba(0, 47, 167, 0.3),
  -2px -2px 4px rgba(255, 255, 255, 0.1);
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05) translateY(-2px);
  box-shadow: 6px 6px 12px rgba(0, 47, 167, 0.4),
  -3px -3px 6px rgba(255, 255, 255, 0.2);
}

.send-btn:active {
  transform: scale(0.95);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #a0aec0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .chat-container {
    height: calc(100vh - 100px);
    gap: 0.5rem;
  }

  .chat-sidebar {
    width: 180px;
    min-width: 180px;
  }

  .conversation-list {
    width: 280px;
    min-width: 280px;
  }
}

@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
    height: auto;
    min-height: calc(100vh - 100px);
    gap: 0.5rem;
  }

  .chat-sidebar {
    width: 100%;
    min-width: auto;
    flex-shrink: 1;
    height: auto;
  }

  .chat-type-tabs {
    flex-direction: row;
    justify-content: space-around;
  }

  .conversation-list {
    width: 100%;
    min-width: auto;
    flex-shrink: 1;
    max-height: 300px;
  }

  .chat-content {
    min-height: 400px;
    min-width: auto;
  }
}

/* 滚动条样式 */
.conversation-items::-webkit-scrollbar,
.messages-list::-webkit-scrollbar {
  width: 6px;
}

.conversation-items::-webkit-scrollbar-track,
.messages-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.conversation-items::-webkit-scrollbar-thumb,
.messages-list::-webkit-scrollbar-thumb {
  background: rgba(0, 47, 167, 0.2);
}

.conversation-items::-webkit-scrollbar-thumb:hover,
.messages-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 47, 167, 0.3);
}

/* 删除确认对话框样式 */
:deep(.delete-conversation-dialog) {
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 47, 167, 0.15);
}

:deep(.delete-conversation-dialog .el-message-box__header) {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.05) 0%, rgba(229, 62, 62, 0.02) 100%);
  border-bottom: 1px solid rgba(245, 108, 108, 0.1);
  border-radius: 16px 16px 0 0;
  padding: 1.5rem;
}

:deep(.delete-conversation-dialog .el-message-box__title) {
  color: #F56C6C;
  font-weight: 600;
  font-size: 1.125rem;
}

:deep(.delete-conversation-dialog .el-message-box__content) {
  padding: 1.5rem;
  color: #4a5568;
  font-size: 1rem;
  line-height: 1.6;
}

:deep(.delete-conversation-dialog .el-message-box__btns) {
  padding: 1rem 1.5rem 1.5rem;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

:deep(.delete-conversation-dialog .el-button--danger) {
  background: linear-gradient(135deg, #F56C6C 0%, #E53E3E 100%);
  border: none;
  border-radius: 8px;
  padding: 0.75rem 1.5rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.delete-conversation-dialog .el-button--danger:hover) {
  background: linear-gradient(135deg, #E53E3E 0%, #C53030 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

:deep(.delete-conversation-dialog .el-button--default) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 47, 167, 0.2);
  border-radius: 8px;
  padding: 0.75rem 1.5rem;
  color: #002FA7;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.delete-conversation-dialog .el-button--default:hover) {
  background: rgba(0, 47, 167, 0.05);
  border-color: #002FA7;
  transform: translateY(-1px);
}

/* 流式响应指示器样式 */
.streaming-indicator {
  display: flex;
  gap: 4px;
  margin-top: 8px;
  align-items: center;
}

.streaming-indicator .dot {
  width: 6px;
  height: 6px;
  background-color: #64748b;
  border-radius: 50%;
  animation: streaming-pulse 1.4s infinite ease-in-out;
}

.streaming-indicator .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.streaming-indicator .dot:nth-child(2) {
  animation-delay: -0.16s;
}

.streaming-indicator .dot:nth-child(3) {
  animation-delay: 0s;
}

@keyframes streaming-pulse {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* AI消息特殊样式 */
.receiver-message .streaming-indicator .dot {
  background-color: #002FA7;
}

.sender-message .streaming-indicator .dot {
  background-color: rgba(255, 255, 255, 0.8);
}
.conversation-items::-webkit-scrollbar-thumb:hover,
.messages-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 47, 167, 0.3);
}

/* 模式选择菜单样式 */
.header-action-dropdown {
  position: relative;
  display: inline-block;
}

.mode-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 220px;
  background: #f0f0f3;
  border-radius: 16px;
  padding: 1rem;
  z-index: 1000;
  box-shadow: 0 8px 32px rgba(0, 47, 167, 0.15),
  inset 2px 2px 4px rgba(255, 255, 255, 0.8),
  inset -2px -2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
}

.mode-menu-header {
  margin-bottom: 0.75rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(0, 47, 167, 0.1);
}

.mode-menu-header span {
  font-size: 0.9rem;
  font-weight: 600;
  color: #002FA7;
}

.mode-menu-options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.mode-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1),
  inset 1px 1px 2px rgba(255, 255, 255, 0.5);
}

.mode-option:hover {
  background: rgba(0, 47, 167, 0.05);
  border-color: rgba(0, 47, 167, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 47, 167, 0.15),
  inset 1px 1px 2px rgba(255, 255, 255, 0.6);
}

.mode-icon {
  font-size: 1.1rem;
  color: #002FA7;
  width: 20px;
  text-align: center;
}

.mode-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
}

.mode-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #2d3748;
}

.mode-desc {
  font-size: 0.75rem;
  color: #64748b;
}

/* 专注模式样式 */
.focus-mode-exit {
  position: absolute;
  top: 1rem;
  right: 1rem;
  z-index: 1001;
}

.exit-focus-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: rgba(220, 38, 38, 0.9);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
  backdrop-filter: blur(10px);
}

.exit-focus-btn:hover {
  background: rgba(220, 38, 38, 1);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.4);
}

.exit-icon {
  font-size: 1rem;
}

/* 专注模式下的聊天容器样式 */
.chat-container.focus-mode {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;
  height: 100vh;
  padding: 0;
  gap: 0;
  z-index: 999;
  background: #f0f0f3;
}

.chat-container.focus-mode .chat-sidebar,
.chat-container.focus-mode .conversation-list {
  display: none;
}

.chat-container.focus-mode .chat-content {
  width: 100%;
  height: 100%;
  border-radius: 0;
  box-shadow: none;
  margin: 0;
  padding: 2rem;
  position: relative;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mode-menu {
    min-width: 200px;
    right: -20px;
  }

  .mode-option {
    padding: 0.6rem;
  }

  .mode-title {
    font-size: 0.85rem;
  }

  .mode-desc {
    font-size: 0.7rem;
  }

  .exit-focus-btn {
    padding: 0.6rem 0.8rem;
    font-size: 0.85rem;
  }
}
</style>