import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const authItemName = "authorize";

/**
 * 设置请求拦截器
 * @returns {{Authorization: string}} 请求头
 */
const accessHeader = () => {
    return {
        'Authorization': `Bearer ${takeAccessToken()}`
    }
}

/**
 * 获取token
 * @returns {axios.CancelToken|null} 取到的token
 */
export function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    if(!str) return null
    const authObj = JSON.parse(str)
    if(new Date(authObj.expire) <= new Date()) {
        deleteAccessToken()
        ElMessage.warning("登录状态已过期，请重新登录！")
        return null
    }
    return authObj.token
}

/**
 * 默认错误回调
 * @param error 错误信息
 */
const defaultError = (error) => {
    console.error(error)
    const status = error.response?.status
    if (status === 429) {
        ElMessage.error(error.response.data.message)
    } else {
        ElMessage.error('发生了一些错误，请联系管理员')
    }
}

/**
 * 默认失败回调
 * @param message 错误信息
 * @param status 状态码
 * @param url 请求地址
 */
const defaultFailure = (message, status, url) => {
    console.warn(`请求地址: ${url}, 状态码: ${status}, 错误信息: ${message}`)
    ElMessage.warning(message)
}

/**
 * 获取当前用户ID
 * @returns {string|null} 用户ID
 */
export function getCurrentUserId() {
    const userInfo = parseTokenPayload();
    return userInfo ? (userInfo.sub || userInfo.userId || userInfo.id) : null;
}

/**
 * 从JWT token中解析用户信息
 * @returns {Object|null} 用户信息对象，包含用户ID等
 */
export function parseTokenPayload() {
    const token = takeAccessToken();
    if (!token) return null;

    try {
        // JWT token格式: header.payload.signature
        const parts = token.split('.');
        if (parts.length !== 3) {
            console.error('Invalid JWT token format');
            return null;
        }

        // 解码payload部分（Base64URL解码）
        const payload = parts[1];
        const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
        const userInfo = JSON.parse(decodedPayload);

        return userInfo;
    } catch (error) {
        console.error('Failed to parse token payload:', error);
        return null;
    }
}

/**
 * 存储token
 * @param remember 是否记住
 * @param token  token
 * @param expire 过期时间
 */
function storeAccessToken(remember, token, expire){
    const authObj = {
        token: token,
        expire: expire
    }
    const str = JSON.stringify(authObj)
    if(remember)
        localStorage.setItem(authItemName, str)
    else
        sessionStorage.setItem(authItemName, str)
}

/**
 * 删除token
 * @param redirect 是否跳转回登录页
 */
function deleteAccessToken(redirect = false) {
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
    if(redirect) {
        router.push({ name: '/login' })
    }
}

/**
 * 内部post请求
 * @param url 请求地址
 * @param data 请求参数
 * @param headers 请求头
 * @param success 成功回调
 * @param failure 失败回调
 * @param error 错误回调
 */
function internalPost(url, data, headers, success, failure, error = defaultError){
    axios.post(url, data, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else if(data.code === 401) {
            failure('登录状态已过期，请重新登录！')
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

/**
 * 内部get请求
 * @param url
 * @param headers
 * @param success
 * @param failure
 * @param error
 */
function internalGet(url, headers, success, failure, error = defaultError){
    axios.get(url, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else if(data.code === 401) {
            failure('登录状态已过期，请重新登录！')
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

/**
 * 内部put请求
 * @param url
 * @param data
 * @param headers
 * @param success
 * @param failure
 * @param error
 */
function internalPut(url, data, headers, success, failure, error = defaultError){
    axios.put(url, data, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else if(data.code === 401) {
            failure('登录状态已过期，请重新登录！')
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

/**
 * 内部delete请求
 * @param url
 * @param headers
 * @param success
 * @param failure
 * @param error
 */
function internalDelete(url, headers, success, failure, error = defaultError){
    axios.delete(url, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else if(data.code === 401) {
            failure('登录状态已过期，请重新登录！')
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

/**
 * 登录方法
 * 成功后会自动存储token
 * @param username 用户名
 * @param password 密码
 * @param remember 是否记住登录状态
 * @param success 成功回调
 * @param failure 失败回调
 */
function login(username, password, remember, success, failure = defaultFailure){
    internalPost('/auth/login', {
        username: username,
        password: password
    }, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        storeAccessToken(remember, data.token, data.expire)
        ElMessage.success(`登录成功，欢迎 ${data.username} 来到我们的系统`)
        success(data)
    }, failure)
}

/**
 * 登出方法
 * @param success 登出成功回调
 * @param failure 登出失败回调
 */
function logout(success, failure = defaultFailure){
    // 检查是否有token
    const token = takeAccessToken()
    if (!token) {
        ElMessage.warning('您还未登录')
        deleteAccessToken(true)
        return
    }

    API_get('/auth/logout', () => {
        deleteAccessToken()
        ElMessage.success(`退出登录成功，欢迎您再次使用`)
        success()
    }, (message, code) => {
        // 即使后端退出失败，也要清除前端token
        deleteAccessToken()
        if (code === 401) {
            ElMessage.warning('登录状态已过期')
        } else {
            ElMessage.error(`退出登录失败: ${message}`)
        }
        // 仍然执行成功回调，因为前端token已清除
        success()
    })
}

// post请求
function API_post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader() , success, failure)
}

// get请求
function API_get(url, success, failure = defaultFailure) {
    internalGet(url, accessHeader(), success, failure)
}

// put请求
function API_put(url, data, success, failure = defaultFailure) {
    internalPut(url, data, accessHeader(), success, failure)
}

// delete请求
function API_delete(url, success, failure = defaultFailure) {
    internalDelete(url, accessHeader(), success, failure)
}

export {
    login,
    logout,
    API_post,
    API_get,
    API_put,
    API_delete,
}