import Vue from "vue";
import { ActionTree } from 'vuex';
import { AuthState } from './types';
import { RootState } from '@/types/store';


export const actions: ActionTree<AuthState, RootState> = {
  AUTH_REQUEST({ commit, dispatch, state, rootState }, user) {
    return new Promise((resolve, reject) => {
      commit("AUTH_REQUEST");
      const api = rootState.appAuthenticationAPI;
      // POST to authentication API
      Vue.axios.post(api, { user: user }).then(
          // success callback
          response => {
            // Get token
            let token = response.data.token;
            localStorage.setItem("user-token", token);
            Vue.axios.defaults.headers.common["Authorization"] = token;
            commit("AUTH_SUCCESS", token);
            // dispatch("USER_REQUEST");
            resolve(response);
          },
          // error callback
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
};