import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";

import "bootstrap";
import Trend from "vuetrend";
import NProgress from "vue-nprogress";
import VueCookies from "vue-cookies-ts";
import axios from "axios";
import VueAxios from "vue-axios";
import { VueMasonryPlugin } from "vue-masonry";
// import { createProvider } from "./vue-apollo";

const options = {
  showSpinner: false,
  latencyThreshold: 70,
  router: true,
  http: true,
  parent: ".nprogress-container"
};

Vue.use(Trend);
Vue.use(VueAxios, axios);
Vue.use(NProgress, options);
Vue.use(VueCookies);
Vue.use(VueMasonryPlugin);

Vue.config.productionTip = false;

// Set up progress bar
const nprogress = new NProgress(options);

// Set authentication token if available
const authToken = Vue.cookies.get("user-token");
if (authToken) {
  Vue.axios.defaults.headers.common["Authorization"] = authToken;
}
Vue.axios.defaults.headers.common["Accept"] = "application/json";

// Set up router interceptors
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!store.getters.isAuthenticated) {
      next({
        path: "/login",
        query: { redirect: to.fullPath }
      });
    } else {
      next();
    }
  } else {
    next();
  }
});

new Vue({
  nprogress,
  router,
  axios,
  store,
  // apolloProvider: createProvider(),
  render: h => h(App)
}).$mount("#app");
