import { ReplayerClient } from "@/generated/protobuf/ReplayerServiceClientPb";

import {
  VuexModule,
  Module,
  Mutation,
  Action,
  getModule, MutationAction
} from "vuex-module-decorators";
import store from "@/store";
import {
  Empty,
  Frequency,
  ReplayOptions,
  Timestamp
} from "@/generated/protobuf/replayer_pb";
import * as grpcWeb from "grpc-web";

export interface IGrpcState {
  replayer: ReplayerClient;
  isReplaying: boolean;
  replayingOptions?: ReplayOptions;
  replayStatus: String;
}

@Module({ dynamic: true, store, name: "grpc" })
class Grpc extends VuexModule implements IGrpcState {
  public replayer = new ReplayerClient("/grpc/replayer", null, null);
  public isReplaying = false;
  public replayingOptions = new ReplayOptions();
  public replayStatus = "Replay inactive";

  @Mutation
  public async seekReplayer(timestamp: string) {
    let ts = new Timestamp();
    ts.setTimestamp(timestamp);
    this.replayer.seekTo(ts, null, (err, response) => {
      if (response.getSuccess()) {
        this.replayingOptions.setTimestamp(ts);
      } else {
        alert("Operation failed");
      }
    });
  }

  @Mutation
  public queryReplayer() {
    this.replayer.status(new Empty(), null, (err, response) => {
      this.isReplaying = response?.getActive() || false;
    });
  }

  @Mutation
  public setReplayerSpeed(speed: number) {
    let freq = new Frequency();
    freq.setFrequency(speed);
    this.replayer.setSpeed(freq, null, (err, response) => {
      if (response.getSuccess()) {
        this.replayingOptions.setFrequency(freq);
      } else {
        alert("Operation failed");
      }
    });
  }

  @Mutation
  public setReplaying(isReplaying: boolean) {
    this.isReplaying = isReplaying;
    this.replayStatus = isReplaying ? "Replaying..." : "Replay inactive";
  }

  @Mutation
  public setReplayingOptions(options: ReplayOptions) {
    this.replayingOptions = options;
  }

  @Action
  public async toggleReplayer(options?: ReplayOptions) {
    this.isReplaying ? this.stopReplayer() : this.startReplayer(options);
  }

  @Action
  public async resetReplayer() {
    this.replayer.reset(new Empty(), {}, (err, response) => {
      console.log(response.getSuccess());
    });
  }

  @MutationAction({ mutate: ['setReplaying', 'setReplayingOptions']})
  public async startReplayer(
    options?: ReplayOptions
  ) {
    let newOptions = options || new ReplayOptions();
    // debugger;
    this.replayer.start(newOptions, null, (err, response) => {
      if (!err && response.getSuccess()) {
        // this.setReplaying(true);
        // this.setReplayingOptions(newOptions);
        // return ({ 'setReplaying': true, 'setReplayingOptions': newOptions })
      } else {
        alert("Operation failed");
      }
    });
  }

  @Action
  public async stopReplayer() {
    this.replayer.stop(new Empty(), {}, (err, response) => {
      if (!err && response.getSuccess()) {
        this.setReplaying(false);
      } else {
        alert("Operation failed");
      }
    });
  }

  @Action
  public async replayData() {
    this.resetReplayer().then(() => this.startReplayer());
  }
}

export const GrpcModule = getModule(Grpc);
