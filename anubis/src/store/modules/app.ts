import {
  VuexModule,
  Module,
  Mutation,
  Action,
  getModule
} from "vuex-module-decorators";
import store from "@/store";
import { TrainDelayNotification } from "@/generated/protobuf/TrainDelayNotification_pb";

export interface IAppState {
  appName: string;
  isCollapsed: boolean;
  delays: TrainDelayNotification[];
}

@Module({ dynamic: true, store, name: "app" })
class App extends VuexModule implements IAppState {
  public appName = "CEPTA";
  public isCollapsed = false;
  public delays: TrainDelayNotification[] = [];

  @Mutation
  public toggleCollapse() {
    this.isCollapsed = !this.isCollapsed;
  }

  @Mutation
  public addDelay(event: TrainDelayNotification) {
    this.delays.push(event);
  }
}

export const AppModule = getModule(App);
