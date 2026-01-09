
import Vue from 'vue'
import App from './App'
import request from './request' // 引封装好的 service
import router from './router'
import store from './store'
import 'default-passive-events'
// 引入写的 Message
import myMessage from './utils/Message.js'

import lodash from 'lodash'

import ElementUI from 'element-ui'
import '@/assets/theme/index.css'
import 'element-ui/lib/theme-chalk/index.css'


import {formatTime} from "./utils/time";
import animate from 'animate.css'


import nprogress from 'nprogress' // 进度条
import 'nprogress/nprogress.css' //进度条样式



Vue.use(animate)

Vue.config.productionTip = false
// 将自定义request挂载到Vue原型，替代原生axios
Vue.prototype.$axios = request
// 设置全局变量
Vue.prototype.$myName = '月之别邸';
// 挂载到原型上，起个名字，比如 $myMessage 以免和 element 冲突
Vue.prototype.$myMessage = myMessage
Vue.use(ElementUI)
Object.defineProperty(Vue.prototype, '$_', { value: lodash })


Vue.directive('title',  function (el, binding) {
  document.title = el.dataset.title
})
// 格式话时间
Vue.filter('format', formatTime)

//进度条
router.beforeEach((to, from , next) => {
	//每次切换页面时，调用进度条
	nprogress.start();
	// 这个一定要加，没有next()页面不会跳转的
	next();
});
router.afterEach(() => {
	// 在即将进入新的页面组件前，关闭掉进度条
	nprogress.done()
})

new Vue({
  el: '#app',
  router,
  store,
  template: '<App/>',
  components: { App }
})
