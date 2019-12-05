import { MutationTree } from 'vuex';
import { AuthState } from './types';

export const mutations: MutationTree<AuthState> = {
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
};