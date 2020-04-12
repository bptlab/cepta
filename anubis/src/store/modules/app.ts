import {
  VuexModule,
  Module,
  Mutation,
  Action,
  getModule
} from "vuex-module-decorators";
import store from "@/store";
import { TrainDelayNotification } from "@/generated/protobuf/models/events/TrainDelayNotification_pb";

const getThemeClass = (availableThemes: string[], theme: number): string => {
  return "theme-" + availableThemes[theme];
};

export interface IAppState {
  appName: string;
  isCollapsed: boolean;
  isLoading: boolean;
  delays: TrainDelayNotification[];
  availableThemes: string[];
  theme: number;
  themeClass: string;
}

@Module({ dynamic: true, store, name: "app" })
class App extends VuexModule implements IAppState {
  public appName = "CEPTA";
  public isCollapsed = false;
  public isLoading: boolean = false;
  public delays: TrainDelayNotification[] = [];
  public availableThemes: string[] = ["light", "dark"];
  public theme: number = 0;
  public themeClass: string = getThemeClass(this.availableThemes, this.theme);

  @Mutation
  public toggleCollapse() {
    this.isCollapsed = !this.isCollapsed;
  }

  @Mutation
  public setLoading(loading: boolean) {
    this.isLoading = loading;
  }

  @Mutation
  public addDelay(event: TrainDelayNotification) {
    this.delays.push(event);
  }

  @Mutation
  public toggleTheme() {
    this.theme = (this.theme + 1) % this.availableThemes.length;
    this.themeClass = getThemeClass(this.availableThemes, this.theme);
  }
}

export const AppModule = getModule(App);
