import Vue from "vue";
import Vuex, { StoreOptions } from 'vuex';
import { RootState } from '@/types/store';
import { grpc } from './modules/grpc/index';
import { auth } from './modules/auth/index';
import { app } from './modules/app/index';

Vue.use(Vuex);

export default new Vuex.Store<RootState>({
  state: {
    appName: "CEPTA",
    appAllowsRegister: false,
    appAuthenticationAPI: "http://192.168.1.7:5000/api/admin/v1/auth/login",
    appSignUpAPI: "http://192.168.1.7:5000/api/admin/v1/auth/signup",

    // Application state
    isCollapsed: false,
    authToken: localStorage.getItem("user-token") || "",
    authStatus: ""
  },
  modules: {
    grpc, auth, app
  }
});