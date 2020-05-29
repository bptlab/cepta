import Vue from "vue";
import Vuex, { StoreOptions } from "vuex";
import { Notification } from "../generated/protobuf/models/internal/notifications/notification_pb";
import { IAppState } from "./modules/app";
import { IAuthState } from "./modules/auth";
import { IReplayerState } from "./modules/replayer";

Vue.use(Vuex);

export interface IRootState {
  app: IAppState;
  auth: IAuthState;
  grpc: IReplayerState;
}

// Declare empty store first, dynamically register all modules later.
// export default new Vuex.Store<IRootState>({
export default new Vuex.Store({
  state: {
    notifications: <Notification[]>[],
  },
  mutations: {
  }
});
