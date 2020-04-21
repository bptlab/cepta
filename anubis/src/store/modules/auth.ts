import { AuthenticationClient } from "@/generated/protobuf/models/grpc/authentication_grpc_web_pb";
import {
  VuexModule,
  Module,
  Mutation,
  Action,
  getModule
} from "vuex-module-decorators";
import store from "@/store";
import router from "@/router";
import Vue from "vue";
import {
  UserLoginRequest,
  AuthenticationToken,
  TokenValidationRequest
} from "@/generated/protobuf/models/grpc/authentication_pb";

export interface IAuthState {
  client: AuthenticationClient;
  authToken: string;
  authState: AuthenticationState | null;
  appAllowsRegister: boolean;
}

enum AuthenticationState {
  Loading,
  Failed,
  Authenticated
}

@Module({ dynamic: true, store, name: "auth" })
class Auth extends VuexModule implements IAuthState {
  public client = new AuthenticationClient("/grpc/auth", null, null);
  public appAllowsRegister = false;
  public authToken = "";
  public authState: AuthenticationState | null = null;

  get isAuthenticated(): boolean {
    return !!this.authToken;
  }

  get authHeader(): { Authorization: string } {
    return { Authorization: `Bearer ${this.authToken}` };
  }

  @Mutation
  private setAuthState(status: AuthenticationState | null): void {
    this.authState = status;
  }

  @Mutation
  public setAuthToken(token: string): void {
    this.authToken = token;
  }

  @Action({ rawError: true })
  public async authRequest(
    request: UserLoginRequest
  ): Promise<AuthenticationToken> {
    return new Promise<AuthenticationToken>((resolve, reject) => {
      this.setAuthState(AuthenticationState.Loading);
      this.client.login(request, undefined, (err, response) => {
        debugger;
        if (err != undefined) {
          this.setAuthState(AuthenticationState.Failed);
          Vue.cookies.remove("user-token");
          reject(err);
        } else {
          let token = response.getToken();
          this.setAuthToken(token);
          this.setAuthState(AuthenticationState.Authenticated);
          if (request.getRemember() == true) {
            Vue.cookies.set("user-token", token, {
              expires: response.getExpiration()
            });
          }
          Vue.axios.defaults.headers.common["Authorization"] = token;
          resolve(response);
        }
      });
    });
  }

  @Action({ rawError: true })
  public async checkToken(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      let request = new TokenValidationRequest();
      request.setToken(this.authToken);
      this.client.validate(request, undefined, (err, response) => {
        if (response.getValid()) {
          resolve(true);
        } else {
          this.authLogout();
          router.push("/");
          reject(false);
        }
      });
    });
  }

  @Action({ rawError: true })
  public authLogout() {
    Vue.cookies.remove("user-token");
    delete Vue.axios.defaults.headers.common["Authorization"];
    this.setAuthToken("");
    this.setAuthState(null);
    router.push({ name: "login" });
  }
}

export const AuthModule = getModule(Auth);
