import { ReplayerClient } from "@/generated/protobuf/models/grpc/replayer_grpc_web_pb";
import {
  VuexModule,
  Module,
  Mutation,
  Action,
  getModule,
  MutationAction
} from "vuex-module-decorators";
import store from "@/store";
import {
  Empty,
  Speed,
  Timerange,
  ReplayStartOptions,
  ReplayOptions
} from "@/generated/protobuf/models/grpc/replayer_pb";

import { Timestamp } from "google-protobuf/google/protobuf/timestamp_pb";

import * as grpcWeb from "grpc-web";

export interface IGrpcState {
  replayer: ReplayerClient;
  isReplaying: boolean;
  replayingOptions?: ReplayStartOptions;
  replayStatus: String;
}

@Module({ dynamic: true, store, name: "grpc" })
class Grpc extends VuexModule implements IGrpcState {
  public replayer = new ReplayerClient("/grpc/replayer", null, null);
  public isReplaying = false;
  public replayingOptions = new ReplayStartOptions();
  public replayStatus = "Replay inactive";

  @Mutation
  public async seekReplayer(date: Date) {
    let ts = new Timestamp();
    ts.fromDate(date);
    this.replayer.seekTo(ts, undefined, (err, response) => {
      if (response.getSuccess()) {
        let updatedReplayTimeRange = new Timerange();
        updatedReplayTimeRange.setEnd(
          this.replayingOptions.getRange()?.getEnd() || new Timestamp()
        );
        updatedReplayTimeRange.setStart(ts);
        this.replayingOptions.setRange(updatedReplayTimeRange);
      } else {
        alert("Operation failed");
      }
    });
  }

  @Action({ rawError: true })
  public async queryReplayer() {
    this.replayer.getStatus(new Empty(), undefined, (err, response) => {
      this.setReplaying(response?.getActive() || false);
    });
    this.replayer.getOptions(new Empty(), undefined, (err, response) => {
      this.setReplayingOptions(response);
    });
  }

  @Mutation
  public setReplayerSpeed(value: number) {
    let speed = new Speed();
    speed.setSpeed(value);
    this.replayer.setSpeed(speed, undefined, (err, response) => {
      if (response.getSuccess()) {
        this.replayingOptions.setSpeed(speed);
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
  public setReplayingOptions(options: ReplayStartOptions) {
    this.replayingOptions = options;
  }

  @Mutation
  public updateReplayingOptions(options: ReplayOptions) {
    let updatedOptions = new ReplayStartOptions();
    updatedOptions.setIdsList(this.replayingOptions.getIdsList());
    updatedOptions.setSpeed(options.getSpeed());
    updatedOptions.setRange(options.getRange());
    updatedOptions.setType(options.getType());
    this.setReplayingOptions(updatedOptions);
  }

  @Action({ rawError: true })
  public async toggleReplayer(options?: ReplayStartOptions) {
    this.isReplaying ? this.stopReplayer() : this.startReplayer(options);
  }

  @Action({ rawError: true })
  public async resetReplayer() {
    this.replayer.reset(new Empty(), undefined, (err, response) => {
      if (!err && response.getSuccess()) {
      } else {
        alert("Operation failed");
      }
    });
  }

  @Action({ rawError: true })
  public async startReplayer(options?: ReplayStartOptions) {
    let newOptions = options || new ReplayStartOptions();
    this.replayer.start(newOptions, undefined, (err, response) => {
      if (!err && response.getSuccess()) {
        this.setReplaying(true);
        this.setReplayingOptions(newOptions);
      } else {
        alert("Operation failed");
      }
    });
  }

  @Action({ rawError: true })
  public async setReplayOptions(options?: ReplayOptions) {
    let newOptions = options || new ReplayOptions();
    this.replayer.setOptions(newOptions, undefined, (err, response) => {
      if (!err && response.getSuccess()) {
        this.setReplaying(true);
        this.updateReplayingOptions(newOptions);
      } else {
        alert("Operation failed");
      }
    });
  }

  @Action({ rawError: true })
  public async stopReplayer() {
    this.replayer.stop(new Empty(), undefined, (err, response) => {
      if (!err && response.getSuccess()) {
        this.setReplaying(false);
      } else {
        alert("Operation failed");
      }
    });
  }

  @Action({ rawError: true })
  public async replayData() {
    this.resetReplayer().then(() => this.startReplayer());
  }
}

export const GrpcModule = getModule(Grpc);
