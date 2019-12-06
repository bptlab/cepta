import { VuexModule, Module, Mutation, Action, getModule } from 'vuex-module-decorators'
import store from '@/store'

export interface IAuthState {
  authToken: string
  authStatus: string
}

@Module({ dynamic: true, store, name: 'auth' })
class Auth extends VuexModule implements IAuthState {
  public authToken = "";
  public authStatus = "";
}

export const AuthModule = getModule(Auth);