import Vue from "vue";
import Vuex from "vuex";
// const {HelloRequest, HelloReply} = require('./helloworld_pb.js');
//  const {GreeterClient} = require('./helloworld_grpc_web_pb.js');

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    /* Application settings */
    appName: "CEPTA",
    appAllowsRegister: false,
    appAuthenticationAPI: "http://192.168.1.7:5000/api/admin/v1/auth/login",
    appSignupAPI: "http://192.168.1.7:5000/api/admin/v1/auth/signup",

    /* Application state */
    isCollapsed: false,
    authToken: localStorage.getItem("user-token") || "",
    authStatus: ""
  },
  getters: {
    isAuthenticated: state => !!state.authToken,
    authStatus: state => state.authStatus
  },
  mutations: {
    TOGGLE_COLLAPSE(state) {
      state.isCollapsed = !state.isCollapsed;
    },
    AUTH_LOGOUT(state) {
      state.authStatus = "";
      state.authToken = "";
    },
    AUTH_REQUEST(state) {
      state.authStatus = "loading";
    },
    AUTH_SUCCESS(state, token) {
      state.authStatus = "success";
      state.authToken = token;
    },
    AUTH_ERROR(state) {
      state.authStatus = "error";
    }
  },
  actions: {
    GRPC_TEST(_) {
      var client = new GreeterClient('http://localhost:8080');

      var request = new HelloRequest();
      request.setName('World');

      client.sayHello(request, {}, (err, response) => {
        console.log(response.getMessage());
      });
    },
    AUTH_REQUEST({ commit, dispatch, state }, user, setCookie) {
      return new Promise((resolve, reject) => {
        commit("AUTH_REQUEST");
        const api = state.appAuthenticationAPI;
        // POST to authentication API
        Vue.axios.post(api, { user: user }).then(
          /* success callback */
          response => {
            // Get token
            let token = response.data.token;
            localStorage.setItem("user-token", token);
            Vue.axios.defaults.headers.common["Authorization"] = token;
            commit("AUTH_SUCCESS", token);
            // dispatch("USER_REQUEST");
            resolve(response);
          },
          /* error callback */
          err => {
            commit("AUTH_ERROR", err);
            localStorage.removeItem("user-token");
            if (err.response) {
              let error_response = err.response.data;
              reject({
                status: err.response.status,
                error: error_response.error,
                message: error_response.message
              });
            }
            // Reject promise
            reject({
              status: null,
              error: "Authentication failed",
              message:
                "Could not properly connect to the server. Please try again later."
            });
          }
        );
      });
    },
    AUTH_LOGOUT({ commit, dispatch }) {
      return new Promise((resolve, reject) => {
        commit("AUTH_LOGOUT");
        localStorage.removeItem("user-token");
        delete Vue.axios.defaults.headers.common["Authorization"];
        resolve();
      });
    }
  },
  modules: {}
});
