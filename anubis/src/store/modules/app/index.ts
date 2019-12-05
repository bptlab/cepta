import { Module } from 'vuex';
import { getters } from './getters';
import { actions } from './actions';
import { mutations } from './mutations';
import { AppState } from './types';
import { RootState } from '@/types/store';

export const state: AppState = {
  isCollapsed: true
};

const namespaced: boolean = true;

export const app: Module<AppState, RootState> = {
  namespaced,
  state,
  getters,
  actions,
  mutations
};