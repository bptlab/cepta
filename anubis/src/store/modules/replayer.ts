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
  Speed,
  Timerange,
  ReplayStartOptions,
  SourceReplay,
  ReplayOptions,
  ReplaySetOptionsRequest,
  ActiveReplayOptions
} from "@/generated/protobuf/models/grpc/replayer_pb";
import { Empty } from "@/generated/protobuf/models/internal/types/result_pb";

import { Timestamp } from "google-protobuf/google/protobuf/timestamp_pb";
import { AuthModule } from "./auth";

export interface IReplayerState {
  replayer: ReplayerClient;
  isReplaying: boolean;
  replayingOptions: ReplayStartOptions;
  replayStatus: String;
}

@Module({ dynamic: true, store, name: "replayer" })
class Replayer extends VuexModule implements IReplayerState {
  public replayer = new ReplayerClient("/api/grpc/replayer", null, null);
  public isReplaying = false;
  public replayingOptions = new ReplayStartOptions();
  public replayingSources = new SourceReplay();
  public replayStatus = "Replay inactive";

  @Mutation
  public async seekReplayer(date: Date) {
    let ts = new Timestamp();
    ts.fromDate(date);
    this.replayer.seekTo(ts, AuthModule.authHeader, (err, response) => {
      if (err) {
        alert(err.message);
      } else {
        let updatedReplayTimeRange = new Timerange();
        updatedReplayTimeRange.setEnd(
          this.replayingOptions
            .getOptions()
            ?.getTimerange()
            ?.getEnd() || new Timestamp()
        );
        updatedReplayTimeRange.setStart(ts);
        if (!this.replayingOptions.hasOptions()) {
          this.replayingOptions.setOptions(new ReplayOptions());
        }
        this.replayingOptions
          .getOptions()
          ?.setTimerange(updatedReplayTimeRange);
      }
      AuthModule.checkUnauthenticated(err);
    });
  }

  @Action({ rawError: true })
  public async queryReplayer() {
    this.replayer.getStatus(
      new Empty(),
      AuthModule.authHeader,
      (err, response) => {
        if (err) {
          alert(err.message);
        } else {
          this.setReplaying(response?.getActive() || false);
        }
        AuthModule.checkUnauthenticated(err);
      }
    );
    this.replayer.getOptions(
      new Empty(),
      AuthModule.authHeader,
      (err, response) => {
        if (err) {
          alert(err.message);
        } else {
          this.setReplayingOptions(response);
        }
        AuthModule.checkUnauthenticated(err);
      }
    );
  }

  @Mutation
  public setReplayerSpeed(value: number) {
    let speed = new Speed();
    speed.setSpeed(value);
    this.replayer.setSpeed(speed, AuthModule.authHeader, (err, response) => {
      if (err) {
        alert(err.message);
      } else {
        if (!this.replayingOptions.hasOptions()) {
          this.replayingOptions.setOptions(new ReplayOptions());
        }
        this.replayingOptions.getOptions()?.setSpeed(speed);
      }
      AuthModule.checkUnauthenticated(err);
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
  public setReplayingSource(source: SourceReplay) {
    this.replayingSources = source;
  }

  @Mutation
  public updateReplayingOptions(options: ActiveReplayOptions) {
    if (!this.replayingOptions.hasOptions()) {
      this.replayingOptions.setOptions(new ReplayOptions());
    }
    this.replayingOptions.getOptions()?.setSpeed(options.getSpeed());
    this.replayingOptions.getOptions()?.setMode(options.getMode());
    this.replayingOptions.getOptions()?.setTimerange(options.getTimerange());
    this.replayingOptions.getOptions()?.setRepeat(options.getRepeat());
  }

  @Action({ rawError: true })
  public async toggleReplayer(options?: ReplayStartOptions) {
    this.isReplaying ? this.stopReplayer() : this.startReplayer(options);
  }

  @Action({ rawError: true })
  public async resetReplayer() {
    this.replayer.reset(new Empty(), AuthModule.authHeader, (err, response) => {
      if (err) {
        alert(err.message);
      }
      AuthModule.checkUnauthenticated(err);
    });
  }

  @Action({ rawError: true })
  public async startReplayer(options?: ReplayStartOptions) {
    let newOptions = options || new ReplayStartOptions();
    //this.newOptions.getOptions()?
    console.log(newOptions.getSourcesList());
    this.replayer.start(newOptions, AuthModule.authHeader, (err, response) => {
      if (err) {
        alert(err.message);
      } else {
        this.setReplaying(true);
        this.setReplayingOptions(newOptions);
      }
      AuthModule.checkUnauthenticated(err);
    });
  }

  @Action({ rawError: true })
  public async setReplayOptions(options?: ReplaySetOptionsRequest) {
    let newOptions = options || new ReplaySetOptionsRequest();
    this.replayer.setOptions(
      newOptions,
      AuthModule.authHeader,
      (err, response) => {
        if (err) {
          alert(err.message);
        } else {
          let activeOptions =
            options?.getOptions() || new ActiveReplayOptions();
          this.updateReplayingOptions(activeOptions);
        }
        AuthModule.checkUnauthenticated(err);
      }
    );
  }

  @Action({ rawError: true })
  public async stopReplayer() {
    this.replayer.stop(new Empty(), AuthModule.authHeader, (err, response) => {
      if (err) {
        alert(err.message);
      } else {
        this.setReplaying(false);
      }
      AuthModule.checkUnauthenticated(err);
    });
  }

  @Action({ rawError: true })
  public async replayData() {
    this.resetReplayer().then(() => this.startReplayer());
  }
}

export const ReplayerModule = getModule(Replayer);
