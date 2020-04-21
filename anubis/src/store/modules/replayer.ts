import { ReplayerClient } from "@/generated/protobuf/models/grpc/replayer_grpc_web_pb";
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
  Speed,
  Timerange,
  ReplayStartOptions,
  ReplayOptions
} from "@/generated/protobuf/models/grpc/replayer_pb";

import { Timestamp } from "google-protobuf/google/protobuf/timestamp_pb";
import { AuthModule } from "./auth";

export interface IReplayerState {
  replayer: ReplayerClient;
  isReplaying: boolean;
  replayingOptions?: ReplayStartOptions;
  replayStatus: String;
}

@Module({ dynamic: true, store, name: "replayer" })
class Replayer extends VuexModule implements IReplayerState {
  public replayer = new ReplayerClient("/api/grpc/replayer", null, null);
  public isReplaying = false;
  public replayingOptions = new ReplayStartOptions();
  public replayStatus = "Replay inactive";

  @Mutation
  public async seekReplayer(date: Date) {
    let ts = new Timestamp();
    ts.fromDate(date);
    this.replayer.seekTo(ts, AuthModule.authHeader, (err, response) => {
      if (err) AuthModule.checkToken();
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
    this.replayer.getStatus(
      new Empty(),
      AuthModule.authHeader,
      (err, response) => {
        if (err) AuthModule.checkToken();
        this.setReplaying(response?.getActive() || false);
      }
    );
    this.replayer.getOptions(
      new Empty(),
      AuthModule.authHeader,
      (err, response) => {
        if (err) AuthModule.checkToken();
        this.setReplayingOptions(response);
      }
    );
  }

  @Mutation
  public setReplayerSpeed(value: number) {
    let speed = new Speed();
    speed.setSpeed(value);
    this.replayer.setSpeed(speed, AuthModule.authHeader, (err, response) => {
      if (err) AuthModule.checkToken();
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
    this.replayer.reset(new Empty(), AuthModule.authHeader, (err, response) => {
      if (err) AuthModule.checkToken();
      if (!err && response.getSuccess()) {
      } else {
        alert("Operation failed");
      }
    });
  }

  @Action({ rawError: true })
  public async startReplayer(options?: ReplayStartOptions) {
    let newOptions = options || new ReplayStartOptions();
    this.replayer.start(newOptions, AuthModule.authHeader, (err, response) => {
      if (err) AuthModule.checkToken();
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
    this.replayer.setOptions(
      newOptions,
      AuthModule.authHeader,
      (err, response) => {
        if (err) AuthModule.checkToken();
        if (!err && response.getSuccess()) {
          this.setReplaying(true);
          this.updateReplayingOptions(newOptions);
        } else {
          alert("Operation failed");
        }
      }
    );
  }

  @Action({ rawError: true })
  public async stopReplayer() {
    this.replayer.stop(new Empty(), AuthModule.authHeader, (err, response) => {
      if (err) AuthModule.checkToken();
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

export const ReplayerModule = getModule(Replayer);
