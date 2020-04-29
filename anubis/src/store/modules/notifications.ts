import {
  VuexModule,
  Module,
  Mutation,
  Action,
  getModule
} from "vuex-module-decorators";
import store from "@/store";
import { UserAnnouncementMessage } from "@/generated/protobuf/models/grpc/notification_pb";
import { User } from "@/generated/protobuf/models/internal/types/users_pb";
import { Error, StatusCode } from "grpc-web";
import { AuthModule } from "./auth";
import { TrainDelayNotification } from "../../generated/protobuf/models/events/TrainDelayNotification_pb";

export interface NotificationsState {
  socket: WebSocket;
}

@Module({ dynamic: true, store, name: "notifications" })
class Notifications extends VuexModule implements NotificationsState {
  public socket = new WebSocket(
    "ws://" + window.location.hostname + "/ws/notifications"
  );

  @Mutation
  public anncounceUser() {
    console.log("Successfully Connected");
    if (AuthModule.userID != undefined) {
      let annnouncement = new UserAnnouncementMessage();
      annnouncement.setToken(AuthModule.authToken);
      annnouncement.setUserId(AuthModule.userID);
      this.socket.send(annnouncement.serializeBinary());
    }
  }

  @Mutation
  public handleMessage(event: any) {
    let deserializedEvent = TrainDelayNotification.deserializeBinary(
      new Uint8Array(event.data)
    );
    console.log(deserializedEvent);
  }

  @Mutation
  public handleDisconnect(event: any) {
    console.log("Socket Closed Connection: ", event);
    this.socket.send("Client Closed!");
  }

  @Mutation
  public handleError(error: any) {
    console.log("Socket Error: ", error);
  }

  @Action({ rawError: true })
  public async setup(): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      this.socket.binaryType = "arraybuffer";
      console.log("Attempting Connection...");

      this.socket.onopen = this.anncounceUser;
      this.socket.onmessage = this.handleMessage;
      this.socket.onclose = this.handleDisconnect;

      this.socket.onerror = this.handleError;
    });
  }
}

export const NotificationsModule = getModule(Notifications);
