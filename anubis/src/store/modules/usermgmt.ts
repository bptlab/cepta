import { UserManagementClient } from "@/generated/protobuf/models/grpc/usermgmt_grpc_web_pb";
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
  GetUserRequest,
  UpdateUserRequest,
  AddUserRequest,
  RemoveUserRequest,
  UserCount
} from "@/generated/protobuf/models/grpc/usermgmt_pb";
import { Empty } from "@/generated/protobuf/models/internal/types/result_pb";
import { User } from "@/generated/protobuf/models/internal/types/users_pb";
import { Error, StatusCode } from "grpc-web";
import { AuthModule } from "./auth";

export interface UserManagementState {
  client: UserManagementClient;
  currentUser?: User | null;
  delayThresholds: { hard: number; soft: number };
}

@Module({ dynamic: true, store, name: "user" })
class UserManagement extends VuexModule implements UserManagementState {
  public client = new UserManagementClient("/api/grpc/usermgmt", null, null);
  public currentUser?: User | null = null;
  public delayThresholds = { hard: 30, soft: 5 }; // Accept up to 5 minutes of delay as acceptable, up to 30 minutes as manageable

  @Mutation
  public setCurrentUserEmail(email: string | null): void {
    if (email && email != "") {
      if (this.currentUser == undefined) {
        this.currentUser = new User();
      }
      this.currentUser.setEmail(email);
    } else {
      this.currentUser = undefined;
    }
  }

  @Action({ rawError: true })
  public async getUserCount(): Promise<UserCount> {
    return new Promise<UserCount>((resolve, reject) => {
      this.client.getUserCount(
        new Empty(),
        AuthModule.authHeader,
        (err, response) => {
          if (err) {
            reject(err);
          } else {
            resolve(response);
          }
        }
      );
    });
  }

  @Action({ rawError: true })
  public async addUser(request: AddUserRequest): Promise<Empty> {
    return new Promise<Empty>((resolve, reject) => {
      this.client.addUser(request, AuthModule.authHeader, (err, response) => {
        if (err) {
          reject(err);
        } else {
          resolve(response);
        }
      });
    });
  }

  @Action({ rawError: true })
  public async updateUser(request: UpdateUserRequest): Promise<Empty> {
    return new Promise<Empty>((resolve, reject) => {
      this.client.updateUser(
        request,
        AuthModule.authHeader,
        (err, response) => {
          if (err) {
            reject(err);
          } else {
            resolve(response);
          }
        }
      );
    });
  }

  @Action({ rawError: true })
  public async getUser(request: GetUserRequest): Promise<User> {
    return new Promise<User>((resolve, reject) => {
      this.client.getUser(request, AuthModule.authHeader, (err, response) => {
        if (err) {
          reject(err);
        } else {
          resolve(response);
        }
      });
    });
  }

  @Action({ rawError: true })
  public async loadCurrentUser(): Promise<User> {
    return new Promise<User>((resolve, reject) => {
      if (this.currentUser == undefined) {
        reject({ code: StatusCode.UNAUTHENTICATED, message: "Not logged in" });
      } else {
        let req = new GetUserRequest();
        req.setEmail(this.currentUser.getEmail());
        this.getUser(req)
          .then(user => resolve(user))
          .catch(err => reject(err));
      }
    });
  }

  @Action({ rawError: true })
  public async removeUser(request: RemoveUserRequest): Promise<Empty> {
    return new Promise<Empty>((resolve, reject) => {
      this.client.removeUser(
        request,
        AuthModule.authHeader,
        (err, response) => {
          if (err) {
            reject(err);
          } else {
            resolve(response);
          }
        }
      );
    });
  }
}

export const UserManagementModule = getModule(UserManagement);
