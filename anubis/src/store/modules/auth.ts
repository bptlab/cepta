import {
  VuexModule,
  Module,
  Mutation,
  Action,
  getModule
} from "vuex-module-decorators";
import store from "@/store";
import Vue from "vue";

export interface IAuthState {
  authToken: string;
  authStatus: string;
  appAllowsRegister: boolean;
  appAuthenticationAPI: string;
  appSignUpAPI: string;
}

@Module({ dynamic: true, store, name: "auth" })
class Auth extends VuexModule implements IAuthState {
  public appAllowsRegister = false;
  public appAuthenticationAPI =
    "http://192.168.1.7:5000/api/admin/v1/auth/login";
  public appSignUpAPI = "http://192.168.1.7:5000/api/admin/v1/auth/signup";
  public authToken = localStorage.getItem("user-token") || "";
  public authStatus = "";

  get isAuthenticated() {
    return !!this.authToken;
  }

  @Mutation
  private AUTH_LOGOUT() {
    this.authStatus = "";
    this.authToken = "";
  }

  @Mutation
  private AUTH_REQUEST() {
    this.authStatus = "loading";
  }

  @Mutation
  private AUTH_SUCCESS(token: string) {
    this.authStatus = "success";
    this.authToken = token;
  }

  @Mutation
  private AUTH_ERROR(error: string) {
    this.authStatus = error;
  }

  @Action
  public async authRequest(user: string) {
    return new Promise((resolve, reject) => {
      this.AUTH_REQUEST();
      const api = this.appAuthenticationAPI;
      Vue.axios.post(api, { user: user }).then(
        response => {
          // Get token
          let token = response.data.token;
          localStorage.setItem("user-token", token);
          Vue.axios.defaults.headers.common["Authorization"] = token;
          this.AUTH_SUCCESS(token);
          resolve(response);
        },
        err => {
          this.AUTH_ERROR(err);
          localStorage.removeItem("user-token");
          if (err.response) {
            let error_response = err.response.data;
            reject({
              status: err.response.status,
              error: error_response.error,
              message: error_response.message
            });
          }
          // Reject promise
          reject({
            status: null,
            error: "Authentication failed",
            message:
              "Could not properly connect to the server. Please try again later."
          });
        }
      );
    });
  }

  @Action
  public async authLogout() {
    return new Promise(resolve => {
      this.AUTH_LOGOUT();
      localStorage.removeItem("user-token");
      delete Vue.axios.defaults.headers.common["Authorization"];
      resolve();
    });
  }
}

export const AuthModule = getModule(Auth);
