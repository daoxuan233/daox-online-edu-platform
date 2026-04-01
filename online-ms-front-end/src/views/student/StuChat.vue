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
          <span v-if="getChatTypeUnreadCount(chatTypes.FRIEND) > 0" class="tab-unread-badge">
            {{ formatUnreadCount(getChatTypeUnreadCount(chatTypes.FRIEND)) }}
          </span>
          <div class="tab-indicator"></div>
        </button>

        <button
            class="chat-type-btn"
            :class="{ active: currentChatType === chatTypes.TOOLS }"
            @click="switchChatType(chatTypes.TOOLS)"
        >
          <font-awesome-icon :icon="['fas', 'tools']" class="tab-icon"/>
          <span class="tab-text">工具</span>
          <span v-if="getChatTypeUnreadCount(chatTypes.TOOLS) > 0" class="tab-unread-badge">
            {{ formatUnreadCount(getChatTypeUnreadCount(chatTypes.TOOLS)) }}
          </span>
          <div class="tab-indicator"></div>
        </button>

        <button
            class="chat-type-btn"
            :class="{ active: currentChatType === chatTypes.AI }"
            @click="switchChatType(chatTypes.AI)"
        >
          <font-awesome-icon :icon="['fas', 'robot']" class="tab-icon"/>
          <span class="tab-text">AI助手</span>
          <span v-if="getChatTypeUnreadCount(chatTypes.AI) > 0" class="tab-unread-badge">
            {{ formatUnreadCount(getChatTypeUnreadCount(chatTypes.AI)) }}
          </span>
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
                currentChatType === chatTypes.TOOLS ? '工具列表' : 'Recent Chats'
          }}
        </h4>
        <div class="list-actions">
          <button class="action-btn" v-if="currentChatType === chatTypes.FRIEND" @click="openAddFriendModal">
            <font-awesome-icon :icon="['fas', 'user-plus']"/>
          </button>
          <button class="action-btn" v-if="currentChatType === chatTypes.FRIEND" title="待处理申请" style="position: relative;" @click="openPendingRequestsModal">
            <font-awesome-icon :icon="['fas', 'bell']"/>
            <span v-if="pendingFriendRequestsCount > 0" class="badge">{{ pendingFriendRequestsCount }}</span>
          </button>
          <button class="action-btn" v-if="currentChatType === chatTypes.TOOLS">
            <font-awesome-icon :icon="['fas', 'cog']"/>
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
                       currentChatType === chatTypes.TOOLS ? ['fas', 'tools'] : ['fas', 'user']"
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
                <span v-if="getConversationUnreadCount(conversation) > 0" class="unread-badge">
                  {{ formatUnreadCount(getConversationUnreadCount(conversation)) }}
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
                         currentChatType === chatTypes.TOOLS ? ['fas', 'tools'] : ['fas', 'user']"
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
                <span v-else-if="currentChatType === chatTypes.TOOLS">
                  智能工具 - 提升学习效率
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

        <!-- 工具卡片区域 -->
        <div v-if="currentChatType === chatTypes.TOOLS" class="tools-container" style="padding-top: 0px">
          <!-- 未启动会话时显示工具卡片 -->
          <div v-if="!pptChatSessionActive" class="tool-card neumorphism-card">
            <div class="tool-header">
              <div class="tool-icon">
                <font-awesome-icon :icon="['fas', 'file-powerpoint']" class="tool-main-icon"/>
              </div>
              <div class="tool-info">
                <h3 class="tool-title">PPT生成工具</h3>
                <p class="tool-description">基于AI技术，快速生成高质量的课程演示文稿</p>
              </div>
            </div>
            <div class="tool-content">
              <div class="tool-actions">
                <button class="tool-action-btn primary-btn neumorphism-raised" @click="startPPTGeneration">
                  <font-awesome-icon :icon="['fas', 'play']" class="btn-icon"/>
                  开始生成PPT
                </button>
                <button class="tool-action-btn secondary-btn neumorphism-raised">
                  <font-awesome-icon :icon="['fas', 'question-circle']" class="btn-icon"/>
                  使用帮助
                </button>
              </div>
            </div>
          </div>

          <!-- PPT生成对话会话区域 -->
          <div v-else class="ppt-chat-session">
            <!-- 会话头部 -->
            <div class="ppt-session-header">
              <div class="session-info">
                <div class="session-icon">
                  <font-awesome-icon :icon="['fas', 'file-powerpoint']" />
                </div>
                <div class="session-details">
                  <h4 class="session-title">{{ pptSessionTitle }}</h4>
                  <p class="session-status">
                    PPT生成助手 - 正在协助您生成演示文稿
                    <span v-if="currentPptSessionId" class="session-task-id" :title="`任务ID: ${currentPptSessionId}`">
                      | 任务: {{ currentPptSessionId.substring(0, 8) }}...
                    </span>
                  </p>
                </div>
              </div>
              <button class="close-session-btn" @click="closePPTChatSession" title="结束会话">
                <font-awesome-icon :icon="['fas', 'times']" />
              </button>
            </div>

            <!-- 消息列表 -->
            <div class="ppt-messages-container">
              <div class="ppt-messages-list">
                <div
                    v-for="message in pptChatMessages"
                    :key="message.id"
                    class="message-item"
                    :class="{
                      'sender-message': message.isOwn,
                      'receiver-message': !message.isOwn
                    }"
                >
                  <div class="conversation-avatar" v-if="!message.isOwn">
                    <div class="avatar-placeholder">
                      <font-awesome-icon :icon="['fas', 'robot']" />
                    </div>
                  </div>

                  <div class="message-content">
                    <div class="message-header" v-if="!message.isOwn">
                      <span class="sender-name">{{ message.senderName }}</span>
                      <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                    </div>
                    <div class="message-bubble">
                      <markdownRender v-if="!message.isOwn" :content="message.content" class="message-text" />
                      <p v-else class="message-text">{{ message.content }}</p>
                      <div v-if="message.isStreaming" class="streaming-indicator">
                        <span class="dot"></span>
                        <span class="dot"></span>
                        <span class="dot"></span>
                      </div>
                    </div>
                    <div class="message-time" v-if="message.isOwn">
                      {{ formatTime(message.timestamp) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 消息输入区域 -->
            <div class="ppt-message-input-area">
              <div class="input-container">
                <textarea
                    v-model="pptMessageInput"
                    @keydown="handlePPTInputKeydown"
                    placeholder="继续与PPT助手对话..."
                    class="message-textarea"
                    rows="1"
                ></textarea>
                <button
                    class="send-btn"
                    :disabled="!pptMessageInput.trim() || isPPTSending"
                    @click="sendPPTMessage"
                >
                  <font-awesome-icon v-if="isPPTSending" :icon="['fas', 'spinner']" spin />
                  <font-awesome-icon v-else :icon="['fas', 'paper-plane']" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-else class="messages-container">
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
                  <markdownRender v-if="currentChatType === chatTypes.AI && message.receiverId !== 'ai_assistant'" :content="message.content" :recordId="message.recordId" class="message-text" />
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
        <div v-if="currentChatType !== chatTypes.TOOLS" class="message-input-area">
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
            <div class="expandable-tools" 
                 :class="{ 'expanded': showToolMenu || showEmojiPicker }" 
                 @mouseenter="showToolMenu = true" 
                 @mouseleave="!showEmojiPicker && (showToolMenu = false)"
            >
              <button class="toolbar-btn toggle-btn">
                <font-awesome-icon :icon="['fas', 'plus']" :class="{ 'rotate-icon': showToolMenu || showEmojiPicker }" />
              </button>
              
              <div class="hidden-tools">
                <button
                        class="toolbar-btn neumorphism-raised"
                        title="代码"
                        @click="openCodeModal"
                >
                  <font-awesome-icon :icon="['fas', 'code']"/>
                </button>
                <button
                        class="toolbar-btn neumorphism-raised"
                        title="表情"
                        @click.stop="toggleEmojiPicker"
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
            </div>
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

  <!-- 添加好友模态框 -->
  <div v-if="showAddFriendModal" class="ppt-modal-overlay" @click="closeAddFriendModal">
    <div class="ppt-modal" @click.stop style="max-width: 500px;">
      <div class="ppt-modal-header">
        <h3>添加好友</h3>
        <button @click="closeAddFriendModal" class="close-btn">
          <font-awesome-icon :icon="['fas', 'times']" />
        </button>
      </div>
      
      <div class="ppt-modal-body">
        <div class="form-group">
          <label>查找用户</label>
          <div style="display: flex; gap: 10px;">
            <input
              v-model="addFriendIdentifier"
              type="text"
              placeholder="请输入学号/工号"
              class="form-input"
              @keyup.enter="handleSearchFriend"
            />
            <button class="btn-submit" @click="handleSearchFriend" :disabled="isSearchingFriend" style="min-width: 80px; padding: 8px 16px;">
              <font-awesome-icon v-if="isSearchingFriend" :icon="['fas', 'spinner']" spin />
              <span v-else>查找</span>
            </button>
          </div>
        </div>

        <!-- 搜索结果展示 -->
        <div v-if="searchedFriend" class="search-result neumorphism-card" style="padding: 20px; margin-top: 20px; text-align: center;">
          <div class="friend-avatar" style="width: 80px; height: 80px; margin: 0 auto 15px; border-radius: 50%; overflow: hidden;">
            <img v-if="searchedFriend.avatarUrl" :src="searchedFriend.avatarUrl" style="width: 100%; height: 100%; object-fit: cover;" />
            <div v-else class="avatar-placeholder" style="font-size: 2rem;">
              <font-awesome-icon :icon="['fas', 'user']" />
            </div>
          </div>
          <h4 style="margin: 0 0 5px; color: #002FA7;">{{ searchedFriend.userName }}</h4>
          <p style="margin: 0 0 15px; color: #64748b; font-size: 0.9rem;">
            {{ searchedFriend.role === 'student' ? '学生' : (searchedFriend.role === 'teacher' ? '教师' : searchedFriend.role) }}
          </p>
          <div v-if="searchedFriend.remark" style="margin-bottom: 10px; color: #666; font-size: 0.85rem;">
            备注: {{ searchedFriend.remark }}
          </div>

          <div class="add-friend-action" style="margin-top: 15px; border-top: 1px solid #eee; padding-top: 15px;">
             <button v-if="!showRemarkInput" @click="showRemarkInput = true" class="btn-submit" style="width: 100%;">
                 <font-awesome-icon :icon="['fas', 'user-plus']" /> 添加好友
             </button>
             
             <div v-else class="remark-input-area">
                  <input 
                    v-model="addFriendRemark" 
                    placeholder="请输入备注(可选)" 
                    class="form-input" 
                    style="margin-bottom: 10px;" 
                    @keyup.enter="confirmAddFriend"
                  />
                  <div class="action-buttons" style="display: flex; gap: 10px; justify-content: center;">
                      <button @click="showRemarkInput = false" class="btn-cancel" style="flex: 1;">取消</button>
                      <button @click="confirmAddFriend" class="btn-submit" :disabled="isAddingFriend" style="flex: 1;">
                          <font-awesome-icon v-if="isAddingFriend" :icon="['fas', 'spinner']" spin />
                          <span v-else>确认发送</span>
                      </button>
                  </div>
             </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 代码插入模态框 -->
  <div v-if="showCodeModal" class="ppt-modal-overlay" @click="closeCodeModal">
    <div class="ppt-modal" @click.stop>
      <div class="ppt-modal-header">
        <h3>插入代码片段</h3>
        <button @click="closeCodeModal" class="close-btn">
          <font-awesome-icon :icon="['fas', 'times']" />
        </button>
      </div>
      
      <div class="ppt-modal-body">
        <div class="form-group">
          <label for="code-language">编程语言</label>
          <select id="code-language" v-model="codeFormData.language" class="form-select">
            <option v-for="lang in codeLanguages" :key="lang.value" :value="lang.value">
              {{ lang.label }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="code-content">代码内容</label>
          <textarea
            id="code-content"
            v-model="codeFormData.content"
            placeholder="请在此粘贴您的代码..."
            class="form-textarea"
            rows="10"
            style="font-family: 'Consolas', 'Monaco', monospace; white-space: pre; font-size: 14px; line-height: 1.5;"
          ></textarea>
        </div>
      </div>

      <div class="ppt-modal-footer">
        <button class="btn-cancel" @click="closeCodeModal">取消</button>
        <button class="btn-submit" @click="insertCodeSnippet">
          <font-awesome-icon :icon="['fas', 'check']" /> 确认插入
        </button>
      </div>
    </div>
  </div>

  <!-- PPT生成模态对话框 -->
  <div v-if="showPPTModal" class="ppt-modal-overlay" @click="closePPTModal">
    <div class="ppt-modal" @click.stop>
      <div class="ppt-modal-header">
        <h3>PPT生成助手</h3>
        <button @click="closePPTModal" class="close-btn">
          <font-awesome-icon :icon="['fas', 'times']" />
        </button>
      </div>
      
      <div class="ppt-modal-body">
        <form @submit.prevent="submitPPTRequest">
          <div class="form-group">
            <label for="ppt-topic">PPT主题 <span class="required">*</span></label>
            <input
              id="ppt-topic"
              v-model="pptFormData.topic"
              type="text"
              placeholder="请输入PPT的主题"
              class="form-input"
              required
            />
          </div>

          <div class="form-group">
            <label for="ppt-content">内容描述 <span class="required">*</span></label>
            <textarea
              id="ppt-content"
              v-model="pptFormData.content"
              placeholder="请详细描述PPT的内容要求"
              class="form-textarea"
              rows="4"
              required
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="ppt-style">演示风格</label>
              <select id="ppt-style" v-model="pptFormData.style" class="form-select">
                <option value="professional">专业商务</option>
                <option value="academic">学术研究</option>
                <option value="creative">创意设计</option>
                <option value="simple">简约清新</option>
                <option value="colorful">活泼多彩</option>
              </select>
            </div>

            <div class="form-group">
              <label for="ppt-slides">页数要求</label>
              <input
                id="ppt-slides"
                v-model.number="pptFormData.slideCount"
                type="number"
                min="5"
                max="50"
                class="form-input"
              />
            </div>
          </div>

          <div class="form-group">
            <label for="ppt-audience">目标受众</label>
            <input
              id="ppt-audience"
              v-model="pptFormData.audience"
              type="text"
              placeholder="如：学生、企业管理者、技术人员等"
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="ppt-requirements">特殊要求</label>
            <textarea
              id="ppt-requirements"
              v-model="pptFormData.requirements"
              placeholder="其他特殊要求或注意事项"
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>
        </form>
      </div>

      <div class="ppt-modal-footer">
        <button @click="closePPTModal" class="btn-cancel" :disabled="isPPTGenerating">
          <font-awesome-icon :icon="['fas', 'times']" />
          取消
        </button>
        <button @click="submitPPTRequest" class="btn-submit" :disabled="isPPTGenerating || !pptFormData.topic.trim() || !pptFormData.content.trim()">
          <font-awesome-icon v-if="isPPTGenerating" :icon="['fas', 'spinner']" spin />
          <font-awesome-icon v-else :icon="['fas', 'magic']" />
          {{ isPPTGenerating ? '正在生成PPT...' : '开始生成' }}
        </button>
      </div>
      
      <!-- 生成进度提示 -->
      <div v-if="isPPTGenerating" class="ppt-progress-overlay">
        <div class="progress-content">
          <div class="progress-spinner">
            <font-awesome-icon :icon="['fas', 'cog']" spin />
          </div>
          <p class="progress-text">正在为您生成PPT，请稍候...</p>
          <div class="progress-steps">
            <div class="step active">
              <font-awesome-icon :icon="['fas', 'check-circle']" />
              <span>解析需求</span>
            </div>
            <div class="step active">
              <font-awesome-icon :icon="['fas', 'spinner']" spin />
              <span>生成内容</span>
            </div>
            <div class="step">
              <font-awesome-icon :icon="['fas', 'circle']" />
              <span>完成生成</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- 待处理好友申请模态框 -->
  <div v-if="showPendingRequestsModal" class="ppt-modal-overlay" @click="closePendingRequestsModal">
    <div class="ppt-modal" @click.stop style="max-width: 600px;">
      <div class="ppt-modal-header">
        <h3>待处理好友申请</h3>
        <button @click="closePendingRequestsModal" class="close-btn">
          <font-awesome-icon :icon="['fas', 'times']" />
        </button>
      </div>
      
      <div class="ppt-modal-body">
        <div v-if="isLoadingPendingRequests" class="loading-state">
          <font-awesome-icon :icon="['fas', 'spinner']" spin size="2x" />
          <p>加载中...</p>
        </div>
        
        <div v-else-if="pendingRequests.length === 0" class="empty-state">
          <font-awesome-icon :icon="['fas', 'inbox']" size="3x" style="color: #ccc; margin-bottom: 1rem;" />
          <p>暂无待处理的好友申请</p>
        </div>
        
        <div v-else class="requests-list">
          <div v-for="request in pendingRequests" :key="request.friendId" class="request-item neumorphism-card">
            <div class="request-user-info">
              <img v-if="request.avatarUrl" :src="request.avatarUrl" :alt="request.userName" class="request-avatar" />
              <div v-else class="avatar-placeholder request-avatar">
                <font-awesome-icon :icon="['fas', 'user']" />
              </div>
              <div class="request-details">
                <h4 class="request-name">
                    {{ request.userName }}
                    <span class="role-badge" v-if="request.role">{{ request.role }}</span>
                </h4>
                <p class="request-course" v-if="request.courseTitle">
                  <font-awesome-icon :icon="['fas', 'book']" />
                  {{ request.courseTitle }}
                </p>
                <p class="request-remark" v-if="request.remark">
                  备注: {{ request.remark }}
                </p>
              </div>
            </div>
            <div class="request-actions">
              <button class="confirm-btn" @click="handleConfirmRequest(request)">
                接受
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, onUnmounted, nextTick, watch} from 'vue'
import gsap from 'gsap'
import {createWebSocket} from '@/utils/websocket'
import {getCurrentUserId, parseTokenPayload} from '@/api/index'
import {getFriendList, getHistoryList, getHistoryDetail, getStudentProfile, queryFriend, addFriend, getPendingFriendRequestsCount, getPendingFriendRequests, confirmFriendRequest} from '@/api/students/stuAPI.js'
import {fetchConversationList, fetchMessagesForConversation, deleteConversationById,getAIChatResponseObject} from '@/api/students/AIChatAPI.js'
import {chatWithPptAgent, getPptTaskStatus} from '@/api/ppt/pptAPI.js'
import {ElMessage, ElMessageBox} from 'element-plus'
// import markdownRender from '@/components/markdownRender/Index.vue'
import markdownRender from '@/components/markdownRender/markstream-vue_Index.vue'

// 聊天类型
const chatTypes = {
  FRIEND: 'friend',
  TOOLS: 'tools',
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

// 待处理好友申请数量
const pendingFriendRequestsCount = ref(0)

// 加载待处理好友申请数量
const loadPendingFriendRequestsCount = async () => {
  try {
    const count = await getPendingFriendRequestsCount()
    if (typeof count === 'number') {
      pendingFriendRequestsCount.value = count
    } else if (count && typeof count.data === 'number') {
      pendingFriendRequestsCount.value = count.data
    }
  } catch (error) {
    console.warn('获取待处理好友申请数量失败:', error)
  }
}

// 待处理好友申请弹窗控制
const showPendingRequestsModal = ref(false)
const pendingRequests = ref([])
const isLoadingPendingRequests = ref(false)

// 打开待处理申请弹窗
const openPendingRequestsModal = async () => {
  showPendingRequestsModal.value = true
  isLoadingPendingRequests.value = true
  try {
    const response = await getPendingFriendRequests()
    // 兼容后端可能返回的两种格式: 直接返回数组 或 返回 { data: [] }
    if (Array.isArray(response)) {
      pendingRequests.value = response
    } else if (response && Array.isArray(response.data)) {
      pendingRequests.value = response.data
    } else {
      pendingRequests.value = []
    }
  } catch (error) {
    console.error('获取待处理好友申请失败:', error)
    ElMessage.error('无法加载好友申请列表')
  } finally {
    isLoadingPendingRequests.value = false
  }
}

// 关闭待处理申请弹窗
const closePendingRequestsModal = () => {
  showPendingRequestsModal.value = false
  pendingRequests.value = []
}

// 确认好友申请
const handleConfirmRequest = async (request) => {
  try {
    await confirmFriendRequest(request.friendId)
    ElMessage.success('已接受好友申请')
    // 从列表中移除
    pendingRequests.value = pendingRequests.value.filter(item => item.friendId !== request.friendId)
    // 更新计数
    loadPendingFriendRequestsCount()
    // 刷新好友列表
    loadFriendsList()
    
    if (pendingRequests.value.length === 0) {
      closePendingRequestsModal()
    }
  } catch (error) {
    console.error('确认好友申请失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

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

// 添加好友模态框状态
const showAddFriendModal = ref(false)
const addFriendIdentifier = ref('')
const searchedFriend = ref(null)
const isSearchingFriend = ref(false)

// 添加好友备注相关
const addFriendRemark = ref('')
const isAddingFriend = ref(false)
const showRemarkInput = ref(false)

// 确认添加好友
const confirmAddFriend = async () => {
  if (!searchedFriend.value) return
  
  // 尝试获取用户ID，根据可能的字段名
  const targetId = searchedFriend.value.friendId || searchedFriend.value.id || searchedFriend.value.userId || searchedFriend.value.studentId
  
  if (!targetId) {
    ElMessage.error('无法获取用户ID，无法添加好友')
    return
  }
  
  isAddingFriend.value = true
  try {
    await addFriend(targetId, addFriendRemark.value)
    ElMessage.success('好友申请已发送')
    showRemarkInput.value = false
    addFriendRemark.value = ''
    // 发送成功后关闭模态框
    closeAddFriendModal()
  } catch (error) {
    console.error('添加好友失败:', error)
    ElMessage.error(error.message || '发送好友申请失败')
  } finally {
    isAddingFriend.value = false
  }
}

// 打开添加好友模态框
const openAddFriendModal = () => {
  showAddFriendModal.value = true
  addFriendIdentifier.value = ''
  searchedFriend.value = null
  showRemarkInput.value = false
  addFriendRemark.value = ''
}

// 关闭添加好友模态框
const closeAddFriendModal = () => {
  showAddFriendModal.value = false
  addFriendIdentifier.value = ''
  searchedFriend.value = null
  showRemarkInput.value = false
  addFriendRemark.value = ''
}

// 查找好友
const handleSearchFriend = async () => {
  if (!addFriendIdentifier.value.trim()) {
    ElMessage.warning('请输入学号/工号')
    return
  }

  isSearchingFriend.value = true
  searchedFriend.value = null
  showRemarkInput.value = false
  addFriendRemark.value = ''

  try {
    const data = await queryFriend(addFriendIdentifier.value.trim())
    console.log('查找好友结果:', data)

    if (data) {
       searchedFriend.value = data
       ElMessage.success('查找成功')
    } else {
       ElMessage.info('未找到该用户')
    }
  } catch (error) {
    console.error('查找好友失败:', error)
    ElMessage.error(error.message || '查找好友失败')
  } finally {
    isSearchingFriend.value = false
  }
}

// 表情相关数据
const showEmojiPicker = ref(false)
const showToolMenu = ref(false)
// 代码插入相关
const showCodeModal = ref(false)
const codeFormData = ref({
  language: 'java',
  content: ''
})
const codeLanguages = [
  { label: 'Java', value: 'java' },
  { label: 'Python', value: 'python' },
  { label: 'JavaScript', value: 'javascript' },
  { label: 'TypeScript', value: 'typescript' },
  { label: 'C++', value: 'cpp' },
  { label: 'C#', value: 'csharp' },
  { label: 'HTML', value: 'html' },
  { label: 'CSS', value: 'css' },
  { label: 'SQL', value: 'sql' },
  { label: 'JSON', value: 'json' },
  { label: 'Shell/Bash', value: 'bash' },
  { label: 'Go', value: 'go' },
  { label: 'Rust', value: 'rust' },
  { label: 'PHP', value: 'php' },
  { label: 'Vue', value: 'vue' },
  { label: 'XML', value: 'xml' },
  { label: 'YAML', value: 'yaml' },
  { label: 'Markdown', value: 'markdown' },
  { label: 'Plain Text', value: 'text' }
]

const openCodeModal = () => {
  codeFormData.value = { language: 'java', content: '' }
  showCodeModal.value = true
  showToolMenu.value = false // 关闭工具菜单
}

const closeCodeModal = () => {
  showCodeModal.value = false
}

const insertCodeSnippet = () => {
  if (!codeFormData.value.content.trim()) {
    ElMessage.warning('请输入代码内容')
    return
  }
  
  const codeBlock = `\n\`\`\`${codeFormData.value.language}\n${codeFormData.value.content}\n\`\`\`\n`
  messageInput.value += codeBlock
  
  closeCodeModal()
  
  // 聚焦输入框
  nextTick(() => {
    const textarea = document.querySelector('.message-textarea')
    if (textarea) {
      textarea.focus()
      // 滚动到底部
      textarea.scrollTop = textarea.scrollHeight
    }
  })
}
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

// PPT生成模态对话框相关状态
const showPPTModal = ref(false)
const isPPTGenerating = ref(false)
const pptFormData = ref({
  topic: '',
  content: '',
  style: 'professional',
  slideCount: 10,
  audience: '',
  requirements: ''
})

// PPT聊天会话相关状态
const pptChatSessionActive = ref(false)
const pptSessionTitle = ref('')
const pptChatMessages = ref([])
const pptMessageInput = ref('')
const isPPTSending = ref(false)
const currentPptSessionId = ref(null)

// PPT任务状态轮询相关
const isPollingTaskStatus = ref(false)
const pollingTimer = ref(null)
const taskStatusCheckCount = ref(0)
const maxPollingAttempts = ref(120) // 最大轮询次数（2分钟，每秒一次）
const pollingInterval = ref(1000) // 轮询间隔（毫秒）

// 模式选择菜单相关
const showModeMenu = ref(false)
const isFocusMode = ref(false)
const isFullscreenMode = ref(false)
const modeMenuButton = ref(null)
const modeMenu = ref(null)

/**
 * 将未读数归一化为非负整数。
 *
 * @param {number|string|null|undefined} unreadCount 原始未读数。
 * @returns {number} 归一化后的未读数。
 */
const normalizeUnreadCount = (unreadCount) => {
  const numericCount = Number(unreadCount || 0)
  return Number.isFinite(numericCount) && numericCount > 0 ? Math.trunc(numericCount) : 0
}

/**
 * 统一格式化未读徽标显示文本。
 *
 * @param {number|string|null|undefined} unreadCount 未读数。
 * @returns {string} 徽标文案，超过 99 条时显示 99+。
 */
const formatUnreadCount = (unreadCount) => {
  const normalizedCount = normalizeUnreadCount(unreadCount)
  return normalizedCount > 99 ? '99+' : String(normalizedCount)
}

/**
 * 获取会话未读数在前端缓存中的统一键。
 * 好友私聊统一优先使用 friendId 或 id，避免与后端 conversationId 混用后导致红点无法正确回显。
 *
 * @param {Object|null|undefined} conversation 会话对象。
 * @returns {string|undefined} 未读数缓存键。
 */
const getUnreadCountKey = (conversation) => {
  if (!conversation) {
    return undefined
  }

  return conversation.friendId || conversation.id || conversation.conversationId
}

/**
 * 获取会话当前应展示的未读数。
 * 优先读取响应式未读映射，避免列表对象与实时消息状态不同步。
 *
 * @param {Object} conversation 会话对象。
 * @returns {number} 当前会话未读数。
 */
const getConversationUnreadCount = (conversation) => {
  if (!conversation) {
    return 0
  }

  const conversationKey = getUnreadCountKey(conversation)
  const mappedUnreadCount = unreadCounts.value[conversationKey]

  if (mappedUnreadCount !== undefined) {
    return normalizeUnreadCount(mappedUnreadCount)
  }

  return normalizeUnreadCount(conversation.unreadCount)
}

/**
 * 根据聊天类型汇总顶部标签的未读数。
 *
 * @param {string} chatType 聊天类型。
 * @returns {number} 对应分类下的未读总数。
 */
const getChatTypeUnreadCount = (chatType) => {
  const conversationMap = {
    [chatTypes.FRIEND]: friends.value,
    [chatTypes.TOOLS]: tools.value,
    [chatTypes.AI]: aiChats.value
  }

  return (conversationMap[chatType] || []).reduce((totalUnreadCount, conversation) => {
    return totalUnreadCount + getConversationUnreadCount(conversation)
  }, 0)
}

/**
 * 将未读计数同步回当前列表对象，确保视图与实时状态一致。
 *
 * @param {string} conversationId 会话 ID。
 * @param {number} unreadCount 最新未读数。
 */
const syncConversationUnreadCount = (conversationId, unreadCount) => {
  const normalizedCount = normalizeUnreadCount(unreadCount)
  const conversationLists = [friends.value, tools.value, aiChats.value, chatHistory.value]

  conversationLists.forEach((conversationList) => {
    const targetConversation = conversationList.find((conversation) => {
      return getUnreadCountKey(conversation) === conversationId || conversation.conversationId === conversationId
    })

    if (targetConversation) {
      targetConversation.unreadCount = normalizedCount
    }
  })

  if (
    currentConversation.value &&
    (getUnreadCountKey(currentConversation.value) === conversationId || currentConversation.value.conversationId === conversationId)
  ) {
    currentConversation.value.unreadCount = normalizedCount
  }
}

/**
 * 设置指定会话的未读数。
 *
 * @param {string} conversationId 会话 ID。
 * @param {number} unreadCount 未读条数。
 */
const setUnreadCount = (conversationId, unreadCount) => {
  if (!conversationId) {
    return
  }

  const normalizedCount = normalizeUnreadCount(unreadCount)
  unreadCounts.value = {
    ...unreadCounts.value,
    [conversationId]: normalizedCount
  }

  syncConversationUnreadCount(conversationId, normalizedCount)
}

/**
 * 使用后端会话历史中的未读数初始化前端未读缓存。
 * 这样首次进入页面时，好友列表与顶部标签即可直接显示真实未读条数。
 *
 * @param {Array<Object>} conversationHistoryList 会话历史列表。
 */
const hydrateUnreadCountsFromHistory = (conversationHistoryList) => {
  const nextUnreadCounts = (conversationHistoryList || []).reduce((unreadCountMap, conversation) => {
    const conversationKey = getUnreadCountKey(conversation)

    if (conversationKey) {
      unreadCountMap[conversationKey] = normalizeUnreadCount(conversation.unreadCount)
    }

    return unreadCountMap
  }, {})

  unreadCounts.value = {
    ...unreadCounts.value,
    ...nextUnreadCounts
  }
}

/**
 * 将会话历史摘要合并到好友列表中。
 * 当前界面渲染的是好友列表而非原始会话列表，因此需要把最后一条消息、最后时间和未读数同步过去。
 */
const syncFriendConversationsWithHistory = () => {
  if (!friends.value.length) {
    return
  }

  const historyByFriendId = new Map(
    chatHistory.value
      .map((conversation) => [conversation.friendId || conversation.id, conversation])
      .filter(([friendId]) => Boolean(friendId))
  )

  const mergedFriendIds = new Set()

  const mergedFriends = friends.value.map((friend) => {
    const historyConversation = historyByFriendId.get(friend.id)

    if (!historyConversation) {
      return friend
    }

    mergedFriendIds.add(friend.id)

    return {
      ...friend,
      conversationId: historyConversation.conversationId || friend.conversationId,
      friendInfo: historyConversation.friendInfo || friend.friendInfo,
      lastMessage: historyConversation.lastMessage || friend.lastMessage,
      lastTime: historyConversation.lastTime || friend.lastTime,
      lastMessageTime: historyConversation.lastMessageTime || friend.lastMessageTime,
      unreadCount: getConversationUnreadCount(historyConversation)
    }
  })

  const historyOnlyFriends = chatHistory.value
    .filter((conversation) => {
      const friendId = conversation.friendId || conversation.id
      return friendId && !mergedFriendIds.has(friendId)
    })
    .map((conversation) => ({
      id: conversation.friendId || conversation.id,
      friendId: conversation.friendId || conversation.id,
      conversationId: conversation.conversationId,
      name: conversation.name,
      avatar: conversation.avatar,
      lastMessage: conversation.lastMessage,
      lastTime: conversation.lastTime,
      lastMessageTime: conversation.lastMessageTime,
      unreadCount: getConversationUnreadCount(conversation),
      online: false,
      friendInfo: conversation.friendInfo,
      role: conversation.friendInfo?.role,
      remark: conversation.friendInfo?.remark
    }))

  friends.value = [...mergedFriends, ...historyOnlyFriends].sort((leftConversation, rightConversation) => {
    return Number(rightConversation.lastMessageTime || 0) - Number(leftConversation.lastMessageTime || 0)
  })
}

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
      friendId: friend.friendId,
      name: friend.remark || friend.userName,
      avatar: friend.avatarUrl || `https://via.placeholder.com/40/4CAF50/FFFFFF?text=${friend.userName.charAt(0)}`,
      lastMessage: '点击开始聊天',
      lastTime: '刚刚',
      online: true, // 默认在线状态，后续可根据实际需求调整
      role: friend.role,
      remark: friend.remark,
      unreadCount: unreadCounts.value[friend.friendId] || 0
    }))

    syncFriendConversationsWithHistory()

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
        id: friendId || chat.conversationId || chat.chatId || chat.id,
        friendId: friendId || chat.friendId,
        conversationId: chat.conversationId || chat.chatId || chat.id,
        name: chatName,
        avatar: avatarUrl || `https://via.placeholder.com/40/4CAF50/FFFFFF?text=${chatName.charAt(0)}`,
        type: chat.chatType || chat.type || 'friend',
        lastMessage: chat.lastMessageContent || chat.lastMessage || '暂无消息',
        lastMessageTime: chat.timestamp ? new Date(chat.timestamp).getTime() : (chat.lastMessageTime || Date.now()),
        lastTime: chat.timestamp ? formatTime(chat.timestamp) : (chat.lastTime || '刚刚'),
        unreadCount: normalizeUnreadCount(chat.unreadCount),
        // 保存原始好友信息，用于后续聊天
        friendInfo: friendInfo
      }
    }).sort((a, b) => b.lastMessageTime - a.lastMessageTime) // 按最后消息时间倒序排列

    hydrateUnreadCountsFromHistory(chatHistory.value)
    syncFriendConversationsWithHistory()

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
        content: messageData.content ? String(messageData.content) : '', // 确保 content 是字符串类型
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
  const nextUnreadCount = getConversationUnreadCount({ id: senderId }) + normalizeUnreadCount(increment)
  setUnreadCount(senderId, nextUnreadCount)
}

// 更新聊天历史记录
const updateChatHistory = (messageData) => {
  const relatedFriendId = messageData.senderId === currentUserId.value ? messageData.receiverId : messageData.senderId
  const chatIndex = chatHistory.value.findIndex(chat => {
    return chat.friendId === relatedFriendId || chat.id === relatedFriendId || chat.conversationId === messageData.conversationId
  })

  if (chatIndex !== -1) {
    chatHistory.value[chatIndex].lastMessage = messageData.content
    chatHistory.value[chatIndex].lastMessageTime = messageData.timestamp
    chatHistory.value[chatIndex].lastTime = formatTime(messageData.timestamp)

    // 重新按最后消息时间倒序排列
    chatHistory.value.sort((a, b) => b.lastMessageTime - a.lastMessageTime)
  }

  syncFriendConversationsWithHistory()
}

// 滚动到底部
const scrollToBottom = () => {
  const messagesContainer = document.querySelector('.messages-list')
  if (messagesContainer) {
    messagesContainer.scrollTop = messagesContainer.scrollHeight
  }
}

// 工具列表
const tools = ref([
  {
    id: 'ppt-generator',
    name: 'PPT生成工具',
    avatar: null,
    lastMessage: '智能生成课程PPT，提升学习效率',
    lastTime: '可用',
    unreadCount: 0,
    type: 'ppt',
    description: '基于AI技术，快速生成高质量的课程演示文稿',
    icon: 'file-powerpoint'
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
    case chatTypes.TOOLS:
      return tools.value
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

  // 列表切场动画
  nextTick(() => {
    gsap.fromTo('.conversation-item',
      { opacity: 0, x: -20 },
      { opacity: 1, x: 0, duration: 0.4, stagger: 0.05, ease: 'power2.out' }
    )
  })
}

// 选择对话
const selectConversation = async (conversation) => {
  currentConversation.value = conversation
  selectedChat.value = conversation

  // 清空当前消息列表
  currentMessages.value = []

  // 对话框进场动画
  nextTick(() => {
    gsap.fromTo('.chat-active',
      { opacity: 0, scale: 0.98 },
      { opacity: 1, scale: 1, duration: 0.4, ease: 'power2.out' }
    )
  })

  // 清除未读计数
  setUnreadCount(getUnreadCountKey(conversation), 0)

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
      
      // 计算有效时间：优先使用updatedAt，其次lastMessageTime，再次createdAt，最后当前时间
      // 这样可以确保即使updatedAt缺失，也能利用最后消息时间进行正确排序
      const effectiveTime = conv.updatedAt || conv.lastMessageTime || conv.createdAt || new Date().toISOString()

      return {
        id: conversationId, // 核心标识符，用于后续会话管理
        conversationId: conversationId, // 明确存储conversationId字段
        name: conv.title || conv.name || `AI助手对话 #${index + 1}`,
        avatar: null,
        lastMessage: conv.summary || conv.lastMessage || '',
        lastTime: formatTime(effectiveTime),
        unreadCount: 0,
        type: 'ai',
        createdAt: conv.createdAt || new Date().toISOString(),
        updatedAt: effectiveTime
      }
    }).sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())

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
          receiverId: msg.receiverId, // 保留原始receiverId用于调试
          recordId: msg.recordId // 尝试从历史消息中获取recordId
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
        content: '', // 确保初始化为空字符串
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
            // 确保 content 始终是字符串类型
            const contentToAdd = data && data.content ? String(data.content) : ''
            lastMessage.content = String(lastMessage.content || '') + contentToAdd
            
            // 提取 recordId
            if (data && data.recordId) {
              lastMessage.recordId = data.recordId
            }
            
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

  // 如果不是当年，显示完整年份
  if (date.getFullYear() !== now.getFullYear()) {
    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${year}/${month}/${day} ${hours}:${minutes}`
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

// PPT生成工具相关方法
/**
 * 解析PPT Agent响应数据
 * @param {*} response - 后端返回的响应数据
 * @returns {Object} - 包含 content 和 taskId 的对象
 */
const parsePPTResponse = (response) => {
  let content = ''
  let taskId = null
  let sessionId = null
  
  console.log('解析PPT响应，类型:', typeof response, '内容:', response)
  
  if (typeof response === 'string') {
    // 如果响应是字符串，直接使用
    content = response
  } else if (response && typeof response === 'object') {
    // 如果响应是对象，提取各个字段
    content = response.reply || response.message || response.content || ''
    taskId = response.taskId || response.task_id || response.id || null
    sessionId = response.sessionId || response.session_id || null
    
    // 如果没有找到内容字段，尝试将整个对象转为字符串
    if (!content && Object.keys(response).length > 0) {
      content = JSON.stringify(response, null, 2)
    }
  }
  
  console.log('解析结果 - 内容:', content, '任务ID:', taskId, '会话ID:', sessionId)
  
  return { content, taskId, sessionId }
}

/**
 * 启动PPT生成功能
 * 处理用户点击"开始生成PPT"按钮的事件
 */
const startPPTGeneration = () => {
  // 重置表单数据
  pptFormData.value = {
    topic: '',
    content: '',
    style: 'professional',
    slideCount: 10,
    audience: '',
    requirements: ''
  }
  // 显示模态对话框
  showPPTModal.value = true
}

/**
 * 关闭PPT生成模态对话框
 */
const closePPTModal = () => {
  showPPTModal.value = false
  // 重置表单数据
  pptFormData.value = {
    topic: '',
    content: '',
    style: 'professional',
    slideCount: 10,
    audience: '',
    requirements: ''
  }
}

/**
 * 提交PPT生成请求
 * 验证表单数据，调用API，并在成功后在工具卡片区域启动对话会话
 */
const submitPPTRequest = async () => {
  // 表单验证
  if (!pptFormData.value.topic.trim()) {
    ElMessage.error('请输入PPT主题')
    return
  }
  
  if (!pptFormData.value.content.trim()) {
    ElMessage.error('请输入PPT内容描述')
    return
  }

  try {
    isPPTGenerating.value = true
    
    // 组装chatRequest.message
    const message = `请帮我生成一个PPT，具体要求如下：
主题：${pptFormData.value.topic}
内容描述：${pptFormData.value.content}
演示风格：${pptFormData.value.style}
页数要求：${pptFormData.value.slideCount}页
目标受众：${pptFormData.value.audience || '通用'}
特殊要求：${pptFormData.value.requirements || '无'}`

    // 调用PPT Agent API
    const response = await chatWithPptAgent({
      message: message
    })

    // 解析后端响应
    const { content: replyContent, taskId, sessionId } = parsePPTResponse(response)
    
    // 保存任务ID或会话ID
    if (taskId) {
      currentPptSessionId.value = taskId
      console.log('保存PPT任务ID:', taskId)
      ElMessage.success(`PPT生成请求已提交！任务ID: ${taskId}`)
      
      // 开始轮询任务状态
      startPPTTaskPolling(taskId)
    } else if (sessionId) {
      currentPptSessionId.value = sessionId
      console.log('保存PPT会话ID:', sessionId)
      ElMessage.success('PPT生成请求已提交成功！')
    } else {
      ElMessage.success('PPT生成请求已提交成功！')
    }
    
    // 关闭模态对话框
    closePPTModal()
    
    // 启动PPT聊天会话
    pptChatSessionActive.value = true
    pptSessionTitle.value = `PPT生成 - ${pptFormData.value.topic}`
    pptChatMessages.value = []
    
    // 添加用户的PPT生成请求消息（右侧显示）
    const userMessage = {
      id: 'user-ppt-request-' + Date.now(),
      content: message,
      senderId: getCurrentUserId(),
      senderName: '我',
      senderAvatar: currentUserAvatar.value,
      timestamp: new Date().toLocaleString(),
      type: 'text',
      isOwn: true
    }
    pptChatMessages.value.push(userMessage)
    
    // 添加AI的响应消息作为开场内容（左侧显示）
    const aiResponseMessage = {
      id: 'ai-ppt-response-' + Date.now(),
      content: replyContent || '正在为您生成PPT，请稍候...',
      senderId: 'ai',
      senderName: 'PPT生成助手',
      senderAvatar: null,
      timestamp: new Date().toLocaleString(),
      type: 'text',
      isOwn: false,
      isStreaming: false,
      taskId: taskId // 保存任务ID到消息中，方便后续查询
    }
    pptChatMessages.value.push(aiResponseMessage)
    
    // 滚动到底部显示最新消息
    nextTick(() => {
      scrollPPTMessagesToBottom()
    })
    
    console.log('已在工具卡片区域启动PPT对话会话')
    
  } catch (error) {
    console.error('PPT生成失败:', error)
    console.error('PPT错误详情:', error.response || error)
    
    // 如果已经打开了会话，显示错误消息
    if (pptChatSessionActive.value) {
      const errorMessage = {
        id: 'error-' + Date.now(),
        content: `抱歉，PPT生成请求失败：${error.message || '网络错误，请稍后重试'}`,
        senderId: 'ai',
        senderName: 'PPT生成助手',
        senderAvatar: null,
        timestamp: new Date().toLocaleString(),
        type: 'text',
        isOwn: false,
        isStreaming: false
      }
      pptChatMessages.value.push(errorMessage)
    }
    
    ElMessage.error('PPT生成失败：' + (error.message || '未知错误'))
  } finally {
    isPPTGenerating.value = false
  }
}

/**
 * 关闭PPT聊天会话
 */
const closePPTChatSession = () => {
  // 停止轮询
  stopPPTTaskPolling()
  
  pptChatSessionActive.value = false
  pptSessionTitle.value = ''
  pptChatMessages.value = []
  pptMessageInput.value = ''
  currentPptSessionId.value = null
  ElMessage.info('已结束PPT生成会话')
}

/**
 * 发送PPT消息
 */
const sendPPTMessage = async () => {
  if (!pptMessageInput.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  try {
    const userMessage = pptMessageInput.value.trim()

    // 创建用户消息显示（右侧显示）
    const userLocalMessage = {
      id: Date.now(),
      content: userMessage,
      senderId: getCurrentUserId(),
      senderName: '我',
      senderAvatar: currentUserAvatar.value,
      timestamp: new Date().toLocaleString(),
      type: 'text',
      isOwn: true
    }

    // 添加用户消息到列表
    pptChatMessages.value.push(userLocalMessage)

    // 清空输入框
    pptMessageInput.value = ''

    // 滚动到底部
    nextTick(() => {
      scrollPPTMessagesToBottom()
    })

    // 创建AI回复的占位消息（左侧显示）
    const aiPlaceholderMessage = {
      id: 'ai-' + Date.now(),
      content: '',
      senderId: 'ai',
      senderName: 'PPT生成助手',
      senderAvatar: null,
      timestamp: new Date().toLocaleString(),
      type: 'text',
      isOwn: false,
      isStreaming: true
    }

    pptChatMessages.value.push(aiPlaceholderMessage)
    isPPTSending.value = true

    // 调用PPT Agent API
    const response = await chatWithPptAgent({
      message: userMessage,
      sessionId: currentPptSessionId.value
    })

    // 解析响应
    const { content: replyContent, taskId, sessionId } = parsePPTResponse(response)
    
    // 更新任务ID或会话ID（如果有新的）
    if (taskId) {
      currentPptSessionId.value = taskId
      console.log('更新PPT任务ID:', taskId)
      
      // 开始轮询新的任务状态
      startPPTTaskPolling(taskId)
    } else if (sessionId) {
      currentPptSessionId.value = sessionId
      console.log('更新PPT会话ID:', sessionId)
    }

    // 更新AI消息内容
    const lastMessage = pptChatMessages.value[pptChatMessages.value.length - 1]
    if (lastMessage && lastMessage.id === aiPlaceholderMessage.id) {
      lastMessage.content = replyContent || '收到您的消息，正在处理...'
      lastMessage.isStreaming = false
      if (taskId) {
        lastMessage.taskId = taskId
      }
    }

    // 滚动到底部
    nextTick(() => {
      scrollPPTMessagesToBottom()
    })

  } catch (error) {
    console.error('发送PPT消息失败:', error)
    console.error('PPT消息错误详情:', error.response || error)
    ElMessage.error('发送消息失败: ' + (error.message || '网络错误'))
    
    // 更新错误消息
    const lastMessage = pptChatMessages.value[pptChatMessages.value.length - 1]
    if (lastMessage && lastMessage.isStreaming) {
      lastMessage.content = `抱歉，PPT生成服务暂时不可用：${error.message || '请稍后重试'}`
      lastMessage.isStreaming = false
    }
  } finally {
    isPPTSending.value = false
  }
}

/**
 * 处理PPT输入框键盘事件
 */
const handlePPTInputKeydown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendPPTMessage()
  }
}

/**
 * 滚动PPT消息到底部
 */
const scrollPPTMessagesToBottom = () => {
  const messagesContainer = document.querySelector('.ppt-messages-list')
  if (messagesContainer) {
    messagesContainer.scrollTop = messagesContainer.scrollHeight
  }
}

/**
 * 开始PPT任务状态轮询
 * @param {string} taskId - PPT生成任务ID
 */
const startPPTTaskPolling = (taskId) => {
  if (!taskId) {
    console.warn('无法开始轮询：任务ID为空')
    return
  }

  // 如果已经在轮询，先停止之前的轮询
  if (pollingTimer.value) {
    stopPPTTaskPolling()
  }

  console.log(`开始轮询PPT任务状态，任务ID: ${taskId}`)
  isPollingTaskStatus.value = true
  taskStatusCheckCount.value = 0

  // 立即执行一次状态检查
  checkPPTTaskStatus(taskId)

  // 设置定时器进行轮询
  pollingTimer.value = setInterval(() => {
    checkPPTTaskStatus(taskId)
  }, pollingInterval.value)
}

/**
 * 停止PPT任务状态轮询
 */
const stopPPTTaskPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
  isPollingTaskStatus.value = false
  taskStatusCheckCount.value = 0
  console.log('已停止PPT任务状态轮询')
}

/**
 * 检查PPT任务状态
 * @param {string} taskId - PPT生成任务ID
 */
const checkPPTTaskStatus = async (taskId) => {
  try {
    taskStatusCheckCount.value++
    console.log(`第${taskStatusCheckCount.value}次检查PPT任务状态，任务ID: ${taskId}`)

    // 检查是否超过最大轮询次数
    if (taskStatusCheckCount.value > maxPollingAttempts.value) {
      console.warn('PPT任务状态轮询超时，停止轮询')
      stopPPTTaskPolling()
      updatePPTTaskStatusMessage('任务状态查询超时，请手动刷新或联系管理员', 'timeout')
      return
    }

    // 调用API查询任务状态
    const response = await getPptTaskStatus(taskId)
    console.log('PPT任务状态响应:', response)

    // 处理任务状态响应
    handlePPTTaskStatusResponse(response, taskId)

  } catch (error) {
    console.error('查询PPT任务状态失败:', error)
    
    // 如果是网络错误或服务器错误，继续轮询
    if (taskStatusCheckCount.value < maxPollingAttempts.value) {
      console.log('任务状态查询失败，将在下次轮询时重试')
    } else {
      // 超过最大重试次数，停止轮询
      stopPPTTaskPolling()
      updatePPTTaskStatusMessage('任务状态查询失败，请检查网络连接或联系管理员', 'error')
    }
  }
}

/**
 * 处理PPT任务状态响应
 * @param {Object} response - 任务状态响应
 * @param {string} taskId - 任务ID
 */
const handlePPTTaskStatusResponse = (response, taskId) => {
  const status = response.status || response.taskStatus || 'unknown'
  const progress = response.progress || 0
  const message = response.message || response.description || ''
  const result = response.result || response.data

  console.log(`任务状态: ${status}, 进度: ${progress}%, 消息: ${message}`)

  switch (status.toLowerCase()) {
    case 'completed':
    case 'success':
    case 'finished':
      // 任务完成
      stopPPTTaskPolling()
      updatePPTTaskStatusMessage(`PPT生成完成！${message}`, 'success', result)
      ElMessage.success('PPT生成完成！')
      break

    case 'failed':
    case 'error':
      // 任务失败
      stopPPTTaskPolling()
      updatePPTTaskStatusMessage(`PPT生成失败：${message}`, 'error')
      ElMessage.error('PPT生成失败')
      break

    case 'running':
    case 'processing':
    case 'in_progress':
      // 任务进行中
      updatePPTTaskStatusMessage(`PPT生成中...${message} (${progress}%)`, 'processing', null, progress)
      break

    case 'pending':
    case 'queued':
      // 任务排队中
      updatePPTTaskStatusMessage(`PPT生成任务已提交，正在排队处理...${message}`, 'pending')
      break

    default:
      // 未知状态，继续轮询
      console.log(`未知任务状态: ${status}，继续轮询`)
      break
  }
}

/**
 * 更新PPT任务状态消息
 * @param {string} content - 消息内容
 * @param {string} type - 消息类型 (processing, success, error, timeout, pending)
 * @param {Object} result - 任务结果（如果有）
 * @param {number} progress - 进度百分比（如果有）
 */
const updatePPTTaskStatusMessage = (content, type, result = null, progress = null) => {
  // 查找最后一条AI消息
  const lastAIMessage = [...pptChatMessages.value].reverse().find(msg => !msg.isOwn)
  
  if (lastAIMessage) {
    // 更新消息内容
    lastAIMessage.content = content
    lastAIMessage.isStreaming = type === 'processing'
    lastAIMessage.taskStatus = type
    lastAIMessage.progress = progress
    
    // 如果任务完成且有结果，保存结果
    if (result) {
      lastAIMessage.taskResult = result
    }

    // 滚动到底部显示最新状态
    nextTick(() => {
      scrollPPTMessagesToBottom()
    })
  }
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
      getStudentProfile(userInfo.sub,
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
    loadAIConversationList(), // 添加AI会话列表加载
    loadPendingFriendRequestsCount() // 加载待处理好友申请数量
  ])

  // 添加事件监听器
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  document.addEventListener('webkitfullscreenchange', handleFullscreenChange)
  document.addEventListener('msfullscreenchange', handleFullscreenChange)
  document.addEventListener('click', handleClickOutside)

  // GSAP 动画 - 页面加载时执行教育科技感进场动画
  nextTick(() => {
    const tl = gsap.timeline()
    tl.fromTo('.chat-sidebar',
      { x: -50, opacity: 0 },
      { x: 0, opacity: 1, duration: 0.6, ease: 'power3.out' }
    )
    .fromTo('.conversation-list',
      { x: -30, opacity: 0 },
      { x: 0, opacity: 1, duration: 0.6, ease: 'power3.out' },
      '-=0.4'
    )
    .fromTo('.chat-content',
      { y: 30, opacity: 0 },
      { y: 0, opacity: 1, duration: 0.6, ease: 'power3.out' },
      '-=0.4'
    )
  })
})

onUnmounted(() => {
  // 停止PPT任务轮询
  stopPPTTaskPolling()
  
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

/* 新拟态卡片基础样式 - 结合科技感磨砂玻璃 */
.neumorphism-card {
  background: rgba(255, 255, 255, 0.75);
  box-shadow:
    0 8px 32px rgba(0, 47, 167, 0.08),
    inset 0 0 0 1px rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.neumorphism-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg,
  rgba(255, 255, 255, 0.4) 0%,
  rgba(255, 255, 255, 0.1) 50%,
  rgba(0, 47, 167, 0.02) 100%);
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
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  color: #4a5568;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 47, 167, 0.05),
  inset 0 0 0 1px rgba(255, 255, 255, 0.5);
}

.chat-type-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  color: #002FA7;
  transform: translateY(-2px);
  box-shadow: 0 8px 15px rgba(0, 47, 167, 0.1),
  inset 0 0 0 1px rgba(255, 255, 255, 0.6);
}

.chat-type-btn.active {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(240, 240, 245, 0.9) 100%);
  color: #002FA7;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(0, 47, 167, 0.1),
  inset 2px 2px 5px rgba(255, 255, 255, 1);
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

.tab-unread-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  margin-left: auto;
  border-radius: 999px;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 700;
  line-height: 20px;
  text-align: center;
  box-shadow: 0 4px 10px rgba(220, 38, 38, 0.25);
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
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  color: #517B4D;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 47, 167, 0.05);
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  color: #517B4D;
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 47, 167, 0.1);
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
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 10px rgba(0, 47, 167, 0.2);
}

.create-new-btn:hover {
  background: linear-gradient(135deg, #001f75 0%, #3d5c39 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(0, 47, 167, 0.3);
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
  gap: 8px;
  padding: 8px 10px;
  margin-bottom: 4px;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  height: 60px;
}

.conversation-item:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateX(4px);
  box-shadow: 0 4px 10px rgba(0, 47, 167, 0.08);
}

.conversation-item.active {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(240, 245, 255, 0.9) 100%);
  border-left: 4px solid #002FA7;
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.1);
}

.conversation-avatar {
  /*position: relative;
  width: 48px;
  height: 48px;
  flex-shrink: 0;*/
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 5px;
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
  flex: 1;
  margin-right: 8px;
}

.conversation-time {
  font-size: 0.75rem;
  color: #333;
  font-weight: 500;
  flex-shrink: 0;
  white-space: nowrap;
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
  min-width: 22px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
  display: inline-flex;
  align-items: center;
  justify-content: center;
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
  background: rgba(255, 255, 255, 0.4);
  border-radius: 20px;
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
  box-shadow: 0 8px 16px rgba(0, 47, 167, 0.2);
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
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  overflow: hidden;
}

/* 聊天头部 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border-bottom: 1px solid rgba(0, 47, 167, 0.08);
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 10;
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
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  color: #517B4D;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0, 47, 167, 0.05);
}

.header-action-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  color: #002FA7;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.1);
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
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 1);
  border-radius: 18px;
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.05);
  position: relative;
  backdrop-filter: blur(10px);
  display: inline-block;
  word-wrap: break-word;
  margin-bottom: 8px;
  padding: 10px 15px;
  max-width: 100%;
}

.sender-message .message-bubble {
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  margin-left: auto;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.15);
}

.sender-message .message-text {
  color: white;
}

.receiver-message .message-bubble {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(255, 255, 255, 0.9);
}

.own-message .message-bubble {
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  border-color: transparent;
  color: white;
}

.own-message .message-text {
  color: white;
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
  position: relative;
  padding: 1rem 1.5rem;
  border-top: 1px solid rgba(0, 47, 167, 0.08);
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  z-index: 10;
}

.input-toolbar {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.toolbar-btn {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 47, 167, 0.05);
}

.toolbar-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  color: #002FA7;
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 47, 167, 0.1);
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
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 47, 167, 0.1);
  border-radius: 22px;
  font-size: 0.95rem;
  color: #1e293b;
  resize: none;
  outline: none;
  transition: all 0.3s ease;
  box-shadow: inset 0 2px 4px rgba(0, 47, 167, 0.02);
  backdrop-filter: blur(10px);
}

.message-textarea::placeholder {
  color: #a0aec0;
}

.message-textarea:focus {
  border-color: rgba(0, 47, 167, 0.3);
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(0, 47, 167, 0.1);
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
  box-shadow: 0 4px 10px rgba(0, 47, 167, 0.2);
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05) translateY(-2px);
  box-shadow: 0 6px 15px rgba(0, 47, 167, 0.3);
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
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 1rem;
  z-index: 1000;
  box-shadow: 0 8px 32px rgba(0, 47, 167, 0.1);
  border: 1px solid rgba(255, 255, 255, 1);
  backdrop-filter: blur(20px);
}

.mode-menu-header {
  margin-bottom: 0.75rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(0, 47, 167, 0.08);
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
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 47, 167, 0.05);
}

.mode-option:hover {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(0, 47, 167, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(0, 47, 167, 0.12);
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

  /* 工具卡片响应式样式 */
  .tools-container {
    padding: 1rem;
  }

  .tool-card {
    padding: 1.5rem;
  }

  .tool-header {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }

  .tool-icon {
    width: 60px;
    height: 60px;
  }

  .tool-main-icon {
    font-size: 2rem;
  }

  .tool-title {
    font-size: 1.5rem;
  }

  .tool-features {
    grid-template-columns: 1fr;
  }

  .tool-actions {
    flex-direction: column;
  }

  .tool-action-btn {
    min-width: auto;
    width: 100%;
  }
}

/* 工具卡片样式 */
.tools-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 90%;
  padding: 2rem;
}

.tool-card {
  width: 100%;
  max-width: 600px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 12px 32px rgba(0, 47, 167, 0.08);
  border: 1px solid rgba(255, 255, 255, 1);
  backdrop-filter: blur(20px);
  transition: all 0.3s ease;
}

.tool-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 47, 167, 0.12);
}

.tool-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid rgba(0, 47, 167, 0.1);
}

.tool-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #002FA7 0%, #0066FF 100%);
  border-radius: 20px;
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.3),
    inset 1px 1px 2px rgba(255, 255, 255, 0.2);
}

.tool-main-icon {
  font-size: 2.5rem;
  color: white;
}

.tool-info {
  flex: 1;
}

.tool-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #002FA7;
  margin: 0 0 0.5rem 0;
  background: linear-gradient(135deg, #002FA7 0%, #0066FF 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.tool-description {
  font-size: 1rem;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.tool-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.tool-features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: rgba(0, 47, 167, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(0, 47, 167, 0.1);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(0, 47, 167, 0.08);
  border-color: rgba(0, 47, 167, 0.2);
  transform: translateY(-1px);
}

.feature-icon {
  font-size: 1.2rem;
  color: #002FA7;
  width: 20px;
  text-align: center;
}

.feature-item span {
  font-size: 0.9rem;
  font-weight: 500;
  color: #2d3748;
}

.tool-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.tool-action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 2rem;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 160px;
  justify-content: center;
}

.primary-btn {
  background: linear-gradient(135deg, #002FA7 0%, #0066FF 100%);
  color: white;
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.3),
    inset 1px 1px 2px rgba(255, 255, 255, 0.2);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 
    6px 6px 12px rgba(0, 47, 167, 0.4),
    inset 1px 1px 2px rgba(255, 255, 255, 0.3);
}

.primary-btn:active {
  transform: translateY(0);
  box-shadow: 
    2px 2px 4px rgba(0, 47, 167, 0.3),
    inset 1px 1px 2px rgba(0, 0, 0, 0.1);
}

.secondary-btn {
  background: rgba(255, 255, 255, 0.9);
  color: #002FA7;
  border: 2px solid rgba(0, 47, 167, 0.2);
  box-shadow: 
    2px 2px 4px rgba(0, 47, 167, 0.1),
    inset 1px 1px 2px rgba(255, 255, 255, 0.8);
}

.secondary-btn:hover {
  background: rgba(0, 47, 167, 0.05);
  border-color: rgba(0, 47, 167, 0.3);
  transform: translateY(-1px);
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.15),
    inset 1px 1px 2px rgba(255, 255, 255, 0.9);
}

.btn-icon {
  font-size: 1rem;
}

/* PPT模态对话框样式 */
.ppt-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.ppt-modal {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 
    0 24px 48px rgba(0, 47, 167, 0.12),
    0 8px 16px rgba(0, 47, 167, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  animation: modalSlideIn 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.ppt-modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 47, 167, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: transparent;
}

.ppt-modal-header h3 {
  margin: 0;
  color: #002FA7;
  font-size: 1.25rem;
  font-weight: 600;
}

.ppt-modal-header .close-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  color: #666;
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.ppt-modal-header .close-btn:hover {
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
}

.ppt-modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #002FA7;
  font-weight: 500;
  font-size: 0.9rem;
}

.required {
  color: #e74c3c;
}

.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid rgba(0, 47, 167, 0.1);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  box-shadow: 
    inset 2px 2px 4px rgba(0, 47, 167, 0.05),
    inset -1px -1px 2px rgba(255, 255, 255, 0.8);
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #002FA7;
  background: rgba(255, 255, 255, 1);
  box-shadow: 
    0 0 0 3px rgba(0, 47, 167, 0.1),
    inset 2px 2px 4px rgba(0, 47, 167, 0.05);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.ppt-modal-footer {
  padding: 20px 24px;
  border-top: 2px solid rgba(0, 47, 167, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(240, 242, 245, 0.95) 100%);
  border-radius: 0 0 16px 16px;
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.btn-cancel,
.btn-submit {
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 120px;
}

.btn-cancel {
  background: rgba(255, 255, 255, 0.95);
  color: #64748b;
  border: 2px solid rgba(0, 47, 167, 0.15);
  box-shadow: 
    2px 2px 6px rgba(0, 47, 167, 0.08),
    inset 1px 1px 2px rgba(255, 255, 255, 0.9);
}

.btn-cancel:hover:not(:disabled) {
  background: rgba(0, 47, 167, 0.05);
  border-color: rgba(0, 47, 167, 0.25);
  color: #002FA7;
  transform: translateY(-2px);
  box-shadow: 
    4px 4px 10px rgba(0, 47, 167, 0.12),
    inset 1px 1px 2px rgba(255, 255, 255, 0.95);
}

.btn-submit {
  background: linear-gradient(135deg, #002FA7 0%, #0066FF 100%);
  color: white;
  box-shadow: 
    4px 4px 10px rgba(0, 47, 167, 0.3),
    inset 1px 1px 2px rgba(255, 255, 255, 0.2);
}

.btn-submit:hover:not(:disabled) {
  background: linear-gradient(135deg, #0066FF 0%, #002FA7 100%);
  transform: translateY(-2px);
  box-shadow: 
    6px 6px 16px rgba(0, 47, 167, 0.4),
    inset 1px 1px 2px rgba(255, 255, 255, 0.3);
}

.btn-submit:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 
    2px 2px 6px rgba(0, 47, 167, 0.3),
    inset 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.btn-submit:disabled,
.btn-cancel:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  filter: grayscale(0.3);
}

/* 按钮图标样式 */
.btn-cancel svg,
.btn-submit svg {
  font-size: 0.9rem;
}

.fa-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* PPT生成进度提示样式 */
.ppt-progress-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  backdrop-filter: blur(5px);
  z-index: 1000;
}

.progress-content {
  text-align: center;
  padding: 30px;
}

.progress-spinner {
  font-size: 48px;
  color: #002FA7;
  margin-bottom: 20px;
  animation: pulse 2s ease-in-out infinite;
}

.progress-text {
  font-size: 18px;
  color: #333;
  margin-bottom: 30px;
  font-weight: 500;
}

.progress-steps {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 20px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  opacity: 0.4;
  transition: all 0.3s ease;
}

.step.active {
  opacity: 1;
  color: #002FA7;
}

.step i {
  font-size: 20px;
  margin-bottom: 5px;
}

.step span {
  font-size: 12px;
  font-weight: 500;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

/* 按钮图标样式优化 */
.btn-submit i {
  margin-right: 8px;
  font-size: 14px;
}

.btn-submit:disabled {
  background: linear-gradient(135deg, #ccc 0%, #999 100%);
  cursor: not-allowed;
}

/* 模态框动画优化 */
.ppt-modal-overlay {
  animation: fadeIn 0.3s ease-out;
}

.ppt-modal {
  animation: slideIn 0.3s ease-out;
  position: relative;
  overflow: hidden;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from { 
    opacity: 0;
    transform: translateY(-50px) scale(0.9);
  }
  to { 
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 响应式设计 - PPT模态对话框 */
@media (max-width: 768px) {
  .ppt-modal {
    width: 95%;
    margin: 20px;
    max-height: 85vh;
  }
  
  .ppt-modal-header {
    padding: 16px 20px;
  }

  .ppt-modal-header h3 {
    font-size: 1.1rem;
  }
  
  .ppt-modal-body {
    padding: 16px 20px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .ppt-modal-footer {
    flex-direction: row;
    padding: 16px 20px;
    gap: 12px;
  }
  
  .btn-cancel,
  .btn-submit {
    flex: 1;
    min-width: auto;
    padding: 10px 16px;
    font-size: 0.9rem;
  }
  
  .progress-steps {
    flex-direction: column;
    gap: 15px;
  }
  
  .step {
    flex-direction: row;
    justify-content: center;
  }
  
  .step svg {
    margin-right: 10px;
    margin-bottom: 0;
  }
}

@media (max-width: 480px) {
  .ppt-modal {
    width: 100%;
    margin: 10px;
    border-radius: 12px;
  }

  .ppt-modal-footer {
    flex-direction: column;
    gap: 10px;
  }

  .btn-cancel,
  .btn-submit {
    width: 100%;
  }
}

/* PPT聊天会话区域样式 */
.ppt-chat-session {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 
    8px 8px 16px rgba(0, 47, 167, 0.1),
    -8px -8px 16px rgba(255, 255, 255, 0.9);
  overflow: hidden;
}

.ppt-session-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 2rem;
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.05) 0%, rgba(81, 123, 77, 0.03) 100%);
  border-bottom: 2px solid rgba(0, 47, 167, 0.1);
}

.session-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.session-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #002FA7 0%, #0066FF 100%);
  border-radius: 12px;
  color: white;
  font-size: 1.5rem;
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.2),
    inset 1px 1px 2px rgba(255, 255, 255, 0.2);
}

.session-details {
  flex: 1;
}

.session-title {
  margin: 0 0 0.25rem 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: #002FA7;
}

.session-status {
  margin: 0;
  font-size: 0.875rem;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.session-task-id {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  background: rgba(0, 47, 167, 0.1);
  border-radius: 4px;
  font-size: 0.75rem;
  color: #002FA7;
  font-family: 'Courier New', monospace;
  font-weight: 500;
  cursor: help;
  transition: all 0.2s ease;
}

.session-task-id:hover {
  background: rgba(0, 47, 167, 0.15);
  transform: scale(1.02);
}

.close-session-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.2);
  border-radius: 8px;
  color: #F56C6C;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
}

.close-session-btn:hover {
  background: rgba(245, 108, 108, 0.2);
  color: #E53E3E;
  transform: scale(1.05);
}

.ppt-messages-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0; /* 确保flex子元素可以收缩 */
}

.ppt-messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-height: 0; /* 确保可以收缩 */
}

.ppt-message-input-area {
  flex-shrink: 0; /* 防止输入区域被压缩 */
  border-top: 1px solid rgba(0, 47, 167, 0.1);
  background: rgba(255, 255, 255, 0.8);
  min-height: 80px; /* 确保输入区域有最小高度 */
  display: flex;
  align-items: center;
}

/* 滚动条样式 */
.ppt-messages-list::-webkit-scrollbar {
  width: 6px;
}

.ppt-messages-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.ppt-messages-list::-webkit-scrollbar-thumb {
  background: rgba(0, 47, 167, 0.2);
  border-radius: 3px;
}

.ppt-messages-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 47, 167, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ppt-session-header {
    padding: 1rem;
  }

  .session-icon {
    width: 40px;
    height: 40px;
    font-size: 1.25rem;
  }

  .session-title {
    font-size: 1rem;
  }

  .session-status {
    font-size: 0.75rem;
  }

  .ppt-messages-list {
    padding: 1rem;
  }

  .ppt-message-input-area {
    padding: 0.75rem 1rem;
  }
}

.badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: #ff4d4f;
  color: white;
  border-radius: 10px;
  padding: 0 5px;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  border: 1px solid white;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.requests-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: 400px;
  overflow-y: auto;
  padding: 0.5rem;
}

.request-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: white;
  border-radius: 12px;
}

.request-user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.request-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder.request-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e0e0e0;
  color: #666;
}

.request-details h4 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  color: #333;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.role-badge {
  font-size: 0.75rem;
  background: #e6f7ff;
  color: #1890ff;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #91d5ff;
}

.request-course {
  margin: 0;
  font-size: 0.85rem;
  color: #666;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.request-remark {
  margin: 0.25rem 0 0 0;
  font-size: 0.8rem;
  color: #888;
  font-style: italic;
}

.request-actions {
  display: flex;
  gap: 0.5rem;
}

.confirm-btn {
  background: #52c41a;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.confirm-btn:hover {
  background: #389e0d;
}

/* Neumorphism Raised Style */
.neumorphism-raised {
  background: #f0f0f3;
  box-shadow: 6px 6px 12px #d1d1d4,
              -6px -6px 12px #ffffff;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* Emoji Picker Styles */
.emoji-picker {
  position: absolute;
  bottom: 100%;
  left: 0;
  width: 455px;
  height: 350px;
  background: #f0f0f3;
  border-radius: 16px;
  padding: 1rem;
  margin-bottom: 1rem;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.emoji-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 0.8rem;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  margin-bottom: 0.8rem;
  font-weight: 600;
  color: #4a5568;
}

.emoji-header .close-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #a0aec0;
  font-size: 1.1rem;
  transition: color 0.2s;
  padding: 4px;
}

.emoji-header .close-btn:hover {
  color: #e53e3e;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 0.5rem;
  overflow-y: auto;
  padding-right: 0.5rem;
  flex: 1;
}

.emoji-grid::-webkit-scrollbar {
  width: 6px;
}
.emoji-grid::-webkit-scrollbar-track {
  background: rgba(0,0,0,0.02);
  border-radius: 3px;
}
.emoji-grid::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.1);
  border-radius: 3px;
}
.emoji-grid::-webkit-scrollbar-thumb:hover {
  background: rgba(0,0,0,0.2);
}

.emoji-item {
  border: none;
  background: transparent;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 8px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.emoji-item:hover {
  background: #e2e8f0;
  transform: scale(1.2);
}

.input-toolbar .toolbar-btn.active {
  box-shadow: inset 4px 4px 8px #d1d1d4,
              inset -4px -4px 8px #ffffff;
  color: #002FA7;
}

/* Expandable Tools Styles */
.expandable-tools {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 0;
  width: 36px;
  height: 36px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 10;
}

.expandable-tools.expanded {
  width: auto;
  padding-right: 4px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.toggle-btn {
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
  flex-shrink: 0;
  width: 36px !important;
  height: 36px !important;
  padding: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.rotate-icon {
  transform: rotate(45deg);
  transition: transform 0.3s ease;
  color: #ef4444;
}

.hidden-tools {
  display: flex;
  gap: 0.5rem;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
  pointer-events: none;
  width: 0;
  margin-left: 0;
}

.expandable-tools.expanded .hidden-tools {
  opacity: 1;
  transform: translateX(0);
  pointer-events: auto;
  width: auto;
  margin-left: 0.25rem;
}
</style>
