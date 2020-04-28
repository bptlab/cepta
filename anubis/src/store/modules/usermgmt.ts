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
  RemoveUserRequest
} from "@/generated/protobuf/models/grpc/usermgmt_pb";
import { Empty } from "@/generated/protobuf/models/types/result_pb";
import { User } from "@/generated/protobuf/models/types/users_pb";
import { Error, StatusCode } from "grpc-web";

export interface UserManagementState {
  client: UserManagementClient;
  delayThresholds: { hard: number; soft: number };
}

@Module({ dynamic: true, store, name: "user" })
class UserManagement extends VuexModule implements UserManagementState {
  public client = new UserManagementClient("/grpc/usermgmt", null, null);
  public delayThresholds = { hard: 30, soft: 5 }; // Accept up to 5 minutes of delay as acceptable, up to 30 minutes as manageable

  @Action({ rawError: true })
  public async addUser(request: AddUserRequest): Promise<Empty> {
    return new Promise<Empty>((resolve, reject) => {
      this.client.addUser(request, undefined, (err, response) => {
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
      this.client.updateUser(request, undefined, (err, response) => {
        if (err) {
          reject(err);
        } else {
          resolve(response);
        }
      });
    });
  }

  @Action({ rawError: true })
  public async getUser(request: GetUserRequest): Promise<User> {
    return new Promise<User>((resolve, reject) => {
      this.client.getUser(request, undefined, (err, response) => {
        if (err) {
          reject(err);
        } else {
          resolve(response);
        }
      });
    });
  }

  @Action({ rawError: true })
  public async removeUser(request: RemoveUserRequest): Promise<Empty> {
    return new Promise<Empty>((resolve, reject) => {
      this.client.removeUser(request, undefined, (err, response) => {
        if (err) {
          reject(err);
        } else {
          resolve(response);
        }
      });
    });
  }
}

/*
Vue.cookies.set("user-token", token, {
              expires: response.getExpiration()
            });
 */

export const UserManagementModule = getModule(UserManagement);
