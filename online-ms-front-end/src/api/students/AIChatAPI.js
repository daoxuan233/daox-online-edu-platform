import { takeAccessToken } from '../index.js'
import { ElMessage } from 'element-plus'
import router from '@/router/index.js'

// 统一管理后端的API基地址
const API_BASE_URL = 'http://localhost:8088/api/students/ai';

/**
 * 通用的API请求封装函数
 * @param {string} endpoint - API的端点路径 (例如 '/chat-stream')
 * @param {object} options - fetch API的配置选项 (例如 method, headers, body)
 * @returns {Promise<Response>} - 返回原始的 fetch Response 对象
 */
async function apiFetch(endpoint, options = {}) {
    const token = takeAccessToken();
    if (!token) {
        ElMessage.warning('用户未登录，请先登录！');
        router.push({ name: 'Login' });
        throw new Error('认证失败：Token为空');
    }

    const headers = {
        'Authorization': `Bearer ${token}`,
        ...options.headers, // 允许传入自定义的headers
    };

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers,
    });

    if (response.status === 401) {
        ElMessage.warning('登录状态已过期，请重新登录！');
        router.push({ name: 'Login' });
        throw new Error('认证失败');
    }

    if (!response.ok) {
        throw new Error(`网络响应错误: ${response.status} ${response.statusText}`);
    }

    return response;
}


/**
 * 调用AI聊天接口（流式响应）
 * @param {object} params - 包含请求参数的对象
 * @param {string} params.question - 要提问的问题。
 * @param {string|null} params.conversationId - 可选的会话ID。
 * @param {(data: string) => void} onData - 处理每个数据块的回调函数。
 * @param {() => void} onComplete - 流结束时的回调函数。
 * @param {(error: Error) => void} onError - 发生错误时的回调函数。
 */
export async function getAIChatResponse(params, onData, onComplete, onError) {
    const { question, conversationId } = params;
    const queryParams = new URLSearchParams({ question });
    if (conversationId) {
        queryParams.append('conversationId', conversationId);
    }

    try {
        // 使用通用请求函数
        const response = await apiFetch(`/chat-stream?${queryParams.toString()}`);

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
            const { value, done } = await reader.read();
            if (done) {
                if (onComplete) onComplete();
                break;
            }

            const chunk = decoder.decode(value, { stream: true });
            buffer += chunk;

            while (buffer.includes('\n')) {
                const newlineIndex = buffer.indexOf('\n');
                const line = buffer.substring(0, newlineIndex).trim();
                buffer = buffer.substring(newlineIndex + 1);

                if (line.startsWith('data:')) {
                    const content = line.substring(5).trim();
                    if (content && onData) {
                        onData(content);
                    }
                }
            }
        }
    } catch (error) {
        console.error('AI聊天请求失败:', error);
        ElMessage.error('AI聊天服务暂时不可用，请稍后重试');
        if (onError) onError(error);
    }
}

/**
 * 获取当前用户的会话历史摘要列表
 * @returns {Promise<Array<object>>} - 返回会话摘要列表
 */
export async function fetchConversationList() {
    try {
        const response = await apiFetch('/chat/history/summaries');
        return await response.json(); // 解析JSON响应体
    } catch (error) {
        console.error('获取会话历史列表失败:', error);
        ElMessage.error('无法加载会话历史，请稍后重试');
        return []; // 出错时返回空数组
    }
}

/**
 * 分页获取指定会话的聊天记录
 * @param {object} params - 包含请求参数的对象
 * @param {string} params.conversationId - 必需的会话ID。
 * @param {number} [params.page=0] - 页码 (从0开始)。
 * @param {number} [params.size=20] - 每页数量。
 * @returns {Promise<Array<object>>} - 返回消息列表
 */
export async function fetchMessagesForConversation(params) {
    const { conversationId, page = 0, size = 20 } = params;

    if (!conversationId) {
        throw new Error('获取消息记录时必须提供 conversationId');
    }

    const queryParams = new URLSearchParams({
        conversationId,
        page: page.toString(),
        size: size.toString(),
    });

    try {
        const response = await apiFetch(`/chat/history/messages?${queryParams.toString()}`);
        return await response.json();
    } catch (error) {
        console.error(`获取会话 ${conversationId} 的消息失败:`, error);
        ElMessage.error('无法加载聊天记录，请稍后重试');
        return []; // 出错时返回空数组
    }
}

/**
 * 根据会话ID删除一个AI会话 (RESTful风格)。
 * @param {string} conversationId - 要删除的会话ID。
 * @returns {Promise<object|null>} - 成功时返回服务端的RestBean对象，失败时返回null。
 */
export async function deleteConversationById(conversationId) {
    if (!conversationId) {
        console.error('删除会话时必须提供 conversationId');
        ElMessage.error('删除失败：未提供会话ID');
        return null;
    }

    try {
        // 使用通用的 apiFetch 函数发起 DELETE 请求
        // URL 将 conversationId 作为路径的一部分，符合 @PathVariable
        const response = await apiFetch(`/chat/history/${conversationId}`, {
            method: 'GET',
        });

        // 解析后端返回的 RestBean 对象
        const result = await response.json();

        // 根据后端返回的 code 处理UI提示
        if (result && result.code === 200) {
            ElMessage.success(result.message || '会话已成功删除');
        } else {
            // 如果后端返回了业务错误（如403, 500等）
            ElMessage.error(result.message || '删除失败，请稍后重试');
        }

        return result; // 返回完整的RestBean，方便调用方根据code执行不同逻辑

    } catch (error) {
        // 捕获 apiFetch 抛出的网络或认证错误
        console.error(`删除会话 ${conversationId} 的请求失败:`, error);
        // apiFetch 内部已经处理了主要的UI提示，这里只记录日志
        return null; // 在发生严重错误时返回 null
    }
}

/**
 * 调用AI聊天接口（流式响应，返回AIResponse对象）
 * @param {object} params - 包含请求参数的对象
 * @param {string} params.question - 要提问的问题。
 * @param {string|null} params.conversationId - 可选的会话ID。
 * @param {(data: AIResponse) => void} onData - 处理每个AIResponse对象的回调函数。
 * @param {() => void} onComplete - 流结束时的回调函数。
 * @param {(error: Error) => void} onError - 发生错误时的回调函数。
 */
export async function getAIChatResponseObject(params, onData, onComplete, onError) {
    const { question, conversationId } = params;
    const queryParams = new URLSearchParams({ question });
    if (conversationId) {
        queryParams.append('conversationId', conversationId);
    }

    try {
        // 使用通用请求函数
        const response = await apiFetch(`/chat-stream/res?${queryParams.toString()}`);

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
            const { value, done } = await reader.read();
            if (done) {
                if (onComplete) onComplete();
                break;
            }

            const chunk = decoder.decode(value, { stream: true });
            buffer += chunk;

            while (buffer.includes('\n')) {
                const newlineIndex = buffer.indexOf('\n');
                const line = buffer.substring(0, newlineIndex).trim();
                buffer = buffer.substring(newlineIndex + 1);

                const payload = line.startsWith('data:') ? line.substring(5).trim() : line;
                if (payload && onData) {
                    try {
                        const aiResponse = JSON.parse(payload);
                        onData(aiResponse);
                    } catch (e) {
                        console.error('解析AIResponse对象失败:', e);
                    }
                }
            }
        }
    } catch (error) {
        console.error('AI聊天请求失败:', error);
        ElMessage.error('AI聊天服务暂时不可用，请稍后重试');
        if (onError) onError(error);
    }
}

/**
 * 解析笔记标签
 * @param {object} params - 包含请求参数的对象
 * @param {string} params.content - 笔记内容
 * @returns {Promise<Array<string>>} - 返回提取到的标签列表
 */
export async function parseTag(params) {
    const { content } = params;

    // 参数校验
    if (!content) {
        throw new Error('解析标签时必须提供笔记内容');
    }

    try {
        // 构造表单数据
        const formData = new URLSearchParams();
        formData.append('content', content);

        const response = await apiFetch('/tool/parse-tag', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData
        });

        const result = await response.json();

        // 处理后端返回的RestBean
        if (result && result.code === 200) {
            return result.data || [];
        } else {
            ElMessage.error(result.message || '标签解析失败');
            return [];
        }
    } catch (error) {
        console.error('解析标签请求失败:', error);
        ElMessage.error('标签解析服务暂时不可用，请稍后重试');
        return [];
    }
}
