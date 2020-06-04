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
    notificationList: Array<Notification>()
  },
  mutations: {
    addNotification(state, notification: Notification) {
      if (state.notificationList.length > 100) {
        state.notificationList = state.notificationList.slice(-99, -1);
      }
      state.notificationList = [notification, ...state.notificationList];
    }
  },
  actions: {
    addNotification(context, notification: Notification) {
      context.commit("addNotification", notification);
    }
  }
});
