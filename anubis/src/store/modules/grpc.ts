import { ReplayerClient } from "@/generated/protobuf/ReplayerServiceClientPb";

import {
  VuexModule,
  Module,
  Mutation,
  Action,
  getModule
} from "vuex-module-decorators";
import store from "@/store";
import { Empty } from "@/generated/protobuf/replayer_pb";

export interface IGrpcState {
  replayer: ReplayerClient;
}

@Module({ dynamic: true, store, name: "grpc" })
class Grpc extends VuexModule implements IGrpcState {
  public replayer = new ReplayerClient("/grpc/replayer", null, null);

  @Mutation
  private CHANGE_SETTING(payload: { key: string; value: any }) {
    const { key, value } = payload;
    if (Object.prototype.hasOwnProperty.call(this, key)) {
      (this as any)[key] = value;
    }
  }

  @Action
  public async resetReplayer() {
    this.replayer.reset(new Empty(), {}, (err, response) => {
      console.log(response.getSuccess());
    });
  }

  @Action
  public async startReplayer() {
    this.replayer.start(new Empty(), {}, (err, response) => {
      console.log(response.getSuccess());
    });
  }

  @Action
  public async replayData() {
    this.resetReplayer().then(() => this.startReplayer());
  }
}

export const GrpcModule = getModule(Grpc);
