import { MutationTree } from 'vuex';
import { AppState } from './types';

export const mutations: MutationTree<AppState> = {
  TOGGLE_COLLAPSE(state) {
    state.isCollapsed = !state.isCollapsed;
  },
};