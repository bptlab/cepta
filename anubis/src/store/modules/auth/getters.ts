import { GetterTree } from 'vuex';
import { AuthState } from './types';
import { RootState } from '@/types/store';

export const getters: GetterTree<AuthState, RootState> = {
  isAuthenticated: state => !!state.authToken,
  authStatus: state => state.authStatus
};