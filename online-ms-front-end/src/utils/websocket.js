import { takeAccessToken } from '@/api/index.js'; // 从您现有的api/index.js导入
import { ElMessage } from 'element-plus';

// 定义一个可重用的 WebSocket 连接函数
function createWebSocket(onMessageCallback) {
    const token = takeAccessToken();

    if (!token) {
        ElMessage.error('错误：未找到登录凭证，无法建立WebSocket连接。');
        console.error('WebSocket connection failed: No access token found.');
        return null;
    }

    const socket = new WebSocket('ws://localhost:8080/single', [token]);

    socket.onopen = () => {
        console.log('WebSocket 连接已成功建立。');
        ElMessage.success('实时通知服务已连接！');
    };

    socket.onmessage = (event) => {
        console.log('收到WebSocket消息:', event.data);
        if (onMessageCallback) {
            onMessageCallback(event.data);
        }
    };

    socket.onclose = (event) => {
        console.log('WebSocket 连接已关闭:', event);
        if (!event.wasClean) {
            ElMessage.warning('实时通知服务已断开，请刷新页面重试。');
        }
    };

    socket.onerror = (error) => {
        console.error('WebSocket 发生错误:', error);
        ElMessage.error('实时通知服务连接失败。');
    };

    return socket;
}

export { createWebSocket };