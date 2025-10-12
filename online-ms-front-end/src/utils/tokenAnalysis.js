// 解析token

const authItemName = "authorize";

/**
 * 获取存储的token
 * @returns {string|null} 返回token字符串，如果不存在则返回null
 */
function getToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    if (!str) return null;
    
    try {
        const authObj = JSON.parse(str);
        if (new Date(authObj.expire) <= new Date()) {
            return null;
        }
        return authObj.token;
    } catch (e) {
        console.error('解析存储的认证信息失败', e);
        return null;
    }
}

/**
 * 解析JWT token
 * @param {string} token - JWT token字符串
 * @returns {Object|null} 解析后的token payload，解析失败则返回null
 */
function parseToken(token) {
    try {
        // 验证JWT格式
        const parts = token.split('.');
        if (parts.length !== 3) {
            console.error('无效的JWT格式');
            return null;
        }
        
        const base64Url = parts[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        
        // 添加必要的padding
        const padding = base64.length % 4;
        const paddedBase64 = padding ? base64 + '='.repeat(4 - padding) : base64;
        
        // 使用更安全的方式解码，避免CSP问题
        const binaryString = atob(paddedBase64);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i);
        }
        
        // 将字节数组转换为UTF-8字符串
        const jsonPayload = new TextDecoder('utf-8').decode(bytes);
        
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error('Token解析失败', e);
        return null;
    }
}

/**
 * 获取token中的用户ID
 * @returns {string|null} 用户ID，如果token不存在或解析失败则返回null
 */
export function getUserId() {
    const token = getToken();
    if (!token) return null;
    const payload = parseToken(token);
    return payload ? payload.id : null;
}

/**
 * 获取token中的标识符
 * @returns {string|null} 标识符，如果token不存在或解析失败则返回null
 */
export function getIdentifier() {
    const token = getToken();
    if (!token) return null;
    const payload = parseToken(token);
    return payload ? payload.identifier : null;
}

/**
 * 获取token中的用户名
 * @returns {string|null} 用户名，如果token不存在或解析失败则返回null
 */
export function getUsername() {
    const token = getToken();
    if (!token) return null;
    const payload = parseToken(token);
    return payload ? payload.username : null;
}

/**
 * 获取token的过期时间
 * @returns {Date|null} 过期时间，如果token不存在或解析失败则返回null
 */
export function getTokenExpiration() {
    const token = getToken();
    if (!token) return null;
    const payload = parseToken(token);
    return payload ? new Date(payload.exp * 1000) : null;
}

/**
 * 检查token是否有效
 * @returns {boolean} 如果token存在且未过期返回true，否则返回false
 */
export function isTokenValid() {
    const expiration = getTokenExpiration();
    return expiration ? expiration > new Date() : false;
}

export {
    getToken, parseToken
};
