import { AuthenticationClient } from "@/generated/protobuf/models/grpc/auth_grpc_web_pb";
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
} from "@/generated/protobuf/models/grpc/auth_pb";
import { UserID } from "@/generated/protobuf/models/internal/types/users_pb";
import { Error, StatusCode } from "grpc-web";
import { UserManagementModule } from "./usermgmt";
import { NotificationsModule } from "./notifications";

export interface IAuthState {
  client: AuthenticationClient;
  authToken: string;
  userID: string;
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
  public userID = "";
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
    if (status == AuthenticationState.Authenticated) {
      NotificationsModule.announceUser();
    }
  }

  @Mutation
  public setAuthToken(token: string): void {
    this.authToken = token;
  }

  @Mutation
  public setUserID(userID: string): void {
    this.userID = userID;
  }

  @Action({ rawError: true })
  public async authRequest(
    request: UserLoginRequest
  ): Promise<AuthenticationToken> {
    return new Promise<AuthenticationToken>((resolve, reject) => {
      this.setAuthState(AuthenticationState.Loading);
      this.client.login(request, undefined, (err, response) => {
        if (err) {
          this.setAuthState(AuthenticationState.Failed);
          Vue.cookies.remove("user-token");
          Vue.cookies.remove("user-id");
          Vue.cookies.remove("user-email");
          reject(err);
        } else {
          let token = response.getToken();
          let userID = response.getUserId();
          this.setAuthToken(token);
          this.setUserID(userID?.getId() ?? "");
          this.setAuthState(AuthenticationState.Authenticated);
          if (request.getRemember() == true) {
            Vue.cookies.set("user-token", token, {
              expires: response.getExpiration()
            });
            Vue.cookies.set("user-email", response.getEmail(), {
              expires: response.getExpiration()
            });
            Vue.cookies.set("user-id", userID?.getId() ?? "", {
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
  public async checkUnauthenticated(error: Error): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      if (error != undefined) {
        if (error.code == StatusCode.UNAUTHENTICATED) this.checkToken();
      }
    });
  }

  @Action({ rawError: true })
  public authLogout() {
    Vue.cookies.remove("user-token");
    Vue.cookies.remove("user-email");
    Vue.cookies.remove("user-id");
    delete Vue.axios.defaults.headers.common["Authorization"];
    this.setAuthToken("");
    this.setUserID("");
    UserManagementModule.setCurrentUserEmail("");
    this.setAuthState(null);
    router.push({ name: "login" });
  }
}

export const AuthModule = getModule(Auth);
