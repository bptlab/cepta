import { VuexModule, Module, Mutation, Action, getModule } from 'vuex-module-decorators'
import store from '@/store'

export interface IAppState {
  appName: string
  isCollapsed: boolean
}

Module({ dynamic: true, store, name: 'app' })
class App extends VuexModule implements IAppState {
  public appName = "CEPTA";
  public isCollapsed = false;
}

export const AppModule = getModule(App);