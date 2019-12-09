import Vue from 'vue'
import Vuex, {StoreOptions} from 'vuex'
import { IAppState } from './modules/app'
import { IAuthState } from './modules/auth'
import { IGrpcState } from './modules/grpc'
import {state, mutations} '../profile/index.ts'

Vue.use(Vuex);

export interface IRootState {
  app: IAppState;
  auth: IAuthState;
  grpc: IGrpcState;
  websocket: string;
}

const store: StoreOptions<IRootState> = {
  state: {
    websocket: ""
  },
  mutations: {
    setWebsocket (websocket) {
      state.websocket = websocket;
    }
  }
};


// Declare empty store first, dynamically register all modules later.
export default new Vuex.Store<IRootState>({store})