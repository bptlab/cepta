import { ReplayerClient } from "@/generated/protobuf/ReplayerServiceClientPb";

import {
  VuexModule,
  Module,
  Mutation,
  Action,
  getModule
} from "vuex-module-decorators";
import store from "@/store";
import {
  Empty,
  Frequency,
  IDSet,
  ReplayOptions,
  Success,
  Timestamp
} from "@/generated/protobuf/replayer_pb";
import * as grpcWeb from "grpc-web";

export interface IGrpcState {
  replayer: ReplayerClient;
  isReplaying: boolean;
  // replayingERRID?: string;
  // replayingSpeed?: number;
  replayingOptions?: ReplayOptions;
  replayStatus: String;
}

@Module({ dynamic: true, store, name: "grpc" })
class Grpc extends VuexModule implements IGrpcState {
  public replayer = new ReplayerClient("/grpc/replayer", null, null);
  public isReplaying = false;
  // public replayingERRID = "";
  // public replayingSpeed = 0;
  public replayingOptions = new ReplayOptions();
  public replayStatus = "Replay inactive";

  @Mutation
  private CHANGE_SETTING(payload: { key: string; value: any }) {
    const { key, value } = payload;
    if (Object.prototype.hasOwnProperty.call(this, key)) {
      (this as any)[key] = value;
    }
  }

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
  public async queryReplayer() {
    this.replayer.status(new Empty(), null, (err, response) => {
      this.isReplaying = response?.getActive() || false;
    });
  }

  @Mutation
  public async setReplayerSpeed(speed: number) {
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
  public async setReplaying(isReplaying: boolean) {
    this.isReplaying = isReplaying;
    this.replayStatus = isReplaying ? "Replaying..." : "Replay inactive";
  }

  @Mutation
  public async setReplayingOptions(options: ReplayOptions) {
    this.replayingOptions = options;
  }

  @Action
  public async toggleReplayer() {
    this.isReplaying ? this.stopReplayer() : this.startReplayer();
  }

  @Action
  public async resetReplayer() {
    this.replayer.reset(new Empty(), {}, (err, response) => {
      console.log(response.getSuccess());
    });
  }

  @Action
  public async startReplayer(
    errid?: string,
    timestamp?: string,
    frequency?: number
  ) {
    let options = new ReplayOptions();
    let errids = errid?.trim().split(",") || new Array<string>();
    options.setIdsList(errids);
    if (timestamp) {
      let ts = new Timestamp();
      ts.setTimestamp(timestamp);
      options.setTimestamp(ts);
    }
    if (frequency) {
      let freq = new Frequency();
      freq.setFrequency(frequency);
      options.setFrequency(freq);
    }
    this.replayer.start(options, {}, (err, response) => {
      if (!err && response.getSuccess()) {
        this.setReplaying(true);
        this.setReplayingOptions(options);
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
