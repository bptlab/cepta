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
}

export const GrpcModule = getModule(Grpc);
