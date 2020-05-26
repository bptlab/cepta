import {
  VuexModule,
  Module,
  Mutation,
  Action,
  getModule
} from "vuex-module-decorators";
import store from "@/store";
import {
  Announcement,
  ClientMessage
} from "@/generated/protobuf/models/grpc/notification_pb";
import { User } from "@/generated/protobuf/models/internal/types/users_pb";
import { Error, StatusCode } from "grpc-web";
import { AuthModule } from "./auth";
import { AppModule } from "./app";
import { Notification } from "../../generated/protobuf/models/internal/notifications/notification_pb";
import { UserID } from "../../generated/protobuf/models/internal/types/users_pb";

export interface NotificationsState {
  socket: WebSocket;
}

@Module({ dynamic: true, store, name: "notifications" })
class Notifications extends VuexModule implements NotificationsState {
  public socket = new WebSocket(
    "ws://" + window.location.hostname + "/ws/notifications"
  );

  @Mutation
  public announceUser() {
    console.log("Successfully Connected");
    console.log(AuthModule.userID);
    if (AuthModule.userID != "") {
      let message = new ClientMessage();
      let announcement = new Announcement();
      let userID = new UserID();
      userID.setId(AuthModule.userID);
      announcement.setToken(AuthModule.authToken);
      announcement.setUserId(userID);
      message.setAnnouncement(announcement);
      this.socket.send(message.serializeBinary());
    }
  }

  @Mutation
  public handleMessage(event: any) {
    let deserializedEvent = Notification.deserializeBinary(
      new Uint8Array(event.data)
    );
    AppModule.addNotification(deserializedEvent);
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

      this.socket.onopen = this.announceUser;
      this.socket.onmessage = this.handleMessage;
      this.socket.onclose = this.handleDisconnect;

      this.socket.onerror = this.handleError;
    });
  }
}

export const NotificationsModule = getModule(Notifications);
