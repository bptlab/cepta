import { VuexModule, Module, getModule } from "vuex-module-decorators";
import store from "@/store";

export interface UserState {
  delayThresholds: { hard: number; soft: number };
}

@Module({ dynamic: true, store, name: "user" })
class User extends VuexModule implements UserState {
  public delayThresholds = { hard: 30, soft: 5 }; // Accept up to 5 minutes of delay as acceptable, up to 30 minutes as manageable
}

export const UserModule = getModule(User);
