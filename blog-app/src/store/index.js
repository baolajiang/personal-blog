import Vuex from 'vuex'
import Vue from 'vue'
import {getToken, setToken, removeToken} from '@/request/token'
import {login, getUserInfo, logout, register} from '@/api/login'

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    id: '',
    account: '',
    name: '',
    avatar: '',
    token: getToken(),
    // 游客数据初始值（空的）
    guest: {
      uuid: '',
      nickname: '旅人',
      email: '',
      website: '',
      avatar: ''
    }
  },
  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token;
    },
    SET_ACCOUNT: (state, account) => {
      state.account = account
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ID: (state, id) => {
      state.id = id
    },
    // 🔥 核心逻辑：初始化游客身份
    INIT_GUEST(state) {
      // 1. 先尝试从浏览器硬盘（localStorage）里拿数据
      const stored = localStorage.getItem('LUNA_GUEST_INFO');

      if (stored) {
        // ✅ A情况：找到了！是老访客
        // 把硬盘里的数据解析出来，放回 Vuex 内存里
        try {
          state.guest = JSON.parse(stored);
          console.log('欢迎回来，老朋友：', state.guest.nickname);
        } catch (e) {
          // 如果数据坏了，就重置
          localStorage.removeItem('LUNA_GUEST_INFO');
        }
      }

      // ❌ B情况：没找到（stored 为空），或者数据坏了
      // 说明是第一次来，或者清空了缓存
      if (!state.guest.uuid) {
        console.log('是新朋友，正在生成身份...');
        const uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
          var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
          return v.toString(16);
        });
        const suffix = uuid.substring(0, 4).toUpperCase();

        // 生成新数据
        const newGuest = {
          uuid: uuid,
          nickname: `旅人${suffix}`,
          email: '',
          website: '',
          avatar: ''
        };

        // 1. 存入 Vuex (立刻显示)
        state.guest = newGuest;
        // 2. 存入 LocalStorage (永久保存)
        localStorage.setItem('LUNA_GUEST_INFO', JSON.stringify(newGuest));
      }
    },

    // 更新信息时，也要同步保存到 LocalStorage
    UPDATE_GUEST(state, payload) {
      state.guest = { ...state.guest, ...payload };
      localStorage.setItem('LUNA_GUEST_INFO', JSON.stringify(state.guest));
    }
  },
  actions: {
// store/index.js
    login({commit, dispatch}, user) { // 1. 参数里加上 dispatch
      return new Promise((resolve, reject) => {
        login(user.account, user.password).then(data => {
          if(data.success){
            commit('SET_TOKEN', data.data)
            setToken(data.data)

            // 2. --- 新增：登录成功后，立刻获取用户信息 ---
            dispatch('getUserInfo').then(() => {
              resolve()
            }).catch(() => {
              // 即使获取用户信息失败，也算登录成功，Resolve 出去让页面跳转
              resolve()
            })

          }else{
            reject(data.msg)
          }
        }).catch(error => {
          reject(error)
        })
      })
    },
    // 获取用户信息
    getUserInfo({commit, state}) {
      let that = this
      return new Promise((resolve, reject) => {
        getUserInfo(state.token).then(data => {
          if (data.success) {
            commit('SET_ACCOUNT', data.data.account)
            commit('SET_NAME', data.data.nickname)
            commit('SET_AVATAR', data.data.avatar)
            commit('SET_ID', data.data.id)
            resolve(data)
          } else {
            commit('SET_ACCOUNT', '')
            commit('SET_NAME', '')
            commit('SET_AVATAR', '')
            commit('SET_ID', '')
            removeToken()
            resolve(data)
          }

        }).catch(error => {
          commit('SET_ACCOUNT', '')
          commit('SET_NAME', '')
          commit('SET_AVATAR', '')
          commit('SET_ID', '')
          removeToken()
          reject(error)
        })
      })
    },
    // 退出
    logout({commit, state}) {
      return new Promise((resolve, reject) => {
        logout(state.token).then(data => {
          if(data.success){

            commit('SET_TOKEN', '')
            commit('SET_ACCOUNT', '')
            commit('SET_NAME', '')
            commit('SET_AVATAR', '')
            commit('SET_ID', '')
            removeToken()
            resolve()
          }

        }).catch(error => {
          reject(error)
        })
      })
    },
    // 前端 登出
    fedLogOut({commit}) {
      return new Promise(resolve => {
        commit('SET_TOKEN', '')
        commit('SET_ACCOUNT', '')
        commit('SET_NAME', '')
        commit('SET_AVATAR', '')
        commit('SET_ID', '')
        removeToken()
        resolve()
      }).catch(error => {
        reject(error)
      })
    },
    register({commit}, user) {
      return new Promise((resolve, reject) => {
        register(user.account, user.nickname, user.password).then((data) => {
          if(data.success){
            commit('SET_TOKEN', data.data)
            setToken(data.data)
            resolve()
          }else{

            reject(data.msg)
          }
        }).catch((error) => {
          reject(error)
        })
      })
    }
  }
})
