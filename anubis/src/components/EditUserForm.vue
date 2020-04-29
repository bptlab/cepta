<template>
  <div class="edit-user-container">
    <div class="user-header">
      <div class="user-icon">
        <img v-if="user.picture" :src="user.picture" />
        <i class="icon-user"></i>
      </div>
      <div>
        <p class="full-name">{{ name }}</p>
        <p>
          <span class="email">{{ user.email }}</span>
          <span v-if="isAdmin" class="priviledge"> - Administrator</span>
        </p>
      </div>
    </div>
    <div class="user-account">
      <h5>Account</h5>
      <form>
        <div class="form-group row">
          <label for="inputFirstName" class="col-sm-2 col-form-label"
            >First Name</label
          >
          <div class="col-sm-10">
            <input
              type="email"
              class="form-control"
              id="inputFirstName"
              v-model="user.firstName"
            />
          </div>
        </div>
        <div class="form-group row">
          <label for="inputLastName" class="col-sm-2 col-form-label"
            >Last Name</label
          >
          <div class="col-sm-10">
            <input
              type="email"
              class="form-control"
              id="inputLastName"
              v-model="user.lastName"
            />
          </div>
        </div>
        <div v-if="user.username" class="form-group row">
          <label for="inputUsername" class="col-sm-2 col-form-label"
            >Username</label
          >
          <div class="col-sm-10">
            <input
              type="email"
              class="form-control"
              id="inputUsername"
              v-model="user.username"
            />
          </div>
        </div>
        <div class="form-group row">
          <label for="inputEmail" class="col-sm-2 col-form-label">Email</label>
          <div class="col-sm-10">
            <input
              type="email"
              class="form-control"
              id="inputEmail"
              v-model="user.email"
            />
          </div>
        </div>
        <div class="form-group row">
          <label for="inputPassword" class="col-sm-2 col-form-label"
            >Password</label
          >
          <div class="col-sm-10">
            <input
              type="password"
              class="form-control"
              id="inputPassword"
              placeholder="Password"
              v-model="password"
            />
          </div>
        </div>
        <div class="form-group row">
          <label for="inputConfirmPassword" class="col-sm-2 col-form-label"
            >Confirm Password</label
          >
          <div class="col-sm-10">
            <input
              type="password"
              class="form-control"
              id="inputConfirmPassword"
              placeholder="Password"
              v-model="passwordConfirm"
            />
          </div>
        </div>
        <div class="form-group row">
          <div class="col" v-if="showDeleteButton">
            <button
              class="btn btn-cepta-danger float-left"
              @click.prevent="deleteUser"
            >
              {{ removeButtonLabel }}
            </button>
          </div>
          <div class="col" v-if="showUpdateButton">
            <button
              class="btn btn-primary float-right"
              @click.prevent="updateUser"
            >
              {{ updateButtonLabel }}
            </button>
          </div>
          <div class="col" v-if="showCreateButton">
            <button
              class="btn btn-primary float-right"
              @click.prevent="createUser"
            >
              {{ createButtonLabel }}
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from "vue-property-decorator";
import {
  AddUserRequest,
  RemoveUserRequest,
  UpdateUserRequest
} from "../generated/protobuf/models/grpc/usermgmt_pb";
import {
  InternalUser,
  User,
  UserID
} from "../generated/protobuf/models/internal/types/users_pb";

export interface UserProfile {
  picture?: string;
  firstName: string;
  lastName: string;
  company?: string;
  email: string;
  priviledgeLevel?: number;
}

@Component({
  name: "EditUserForm",
  components: {}
})
export default class EditUserForm extends Vue {
  @Prop({ default: false }) protected showDeleteButton?: boolean;
  @Prop({ default: "Remove" }) protected removeButtonLabel?: string;
  @Prop({ default: false }) protected showUpdateButton?: boolean;
  @Prop({ default: "Update" }) protected updateButtonLabel?: string;
  @Prop({ default: false }) protected showCreateButton?: boolean;
  @Prop({ default: "Create" }) protected createButtonLabel?: string;
  @Prop() protected initialUser?: User;

  protected userId?: UserID = undefined;
  protected password: string = "";
  protected passwordConfirm: string = "";

  protected user: UserProfile = {
    firstName: "Max",
    lastName: "Mustermann",
    email: "max@example.com"
  };

  get name(): string {
    let n = `${this.user.firstName} ${this.user.lastName}`;
    return n.length > 1 ? n : "Unknown";
  }

  getUser(): InternalUser {
    let updatedUser = new User();
    updatedUser.setId(this.userId);
    updatedUser.setEmail(this.user.email);
    updatedUser.setFirstName(this.user.firstName);
    updatedUser.setLastName(this.user.lastName);
    let updatedInternalUser = new InternalUser();
    updatedInternalUser.setPassword(this.password);
    updatedInternalUser.setUser(updatedUser);
    return updatedInternalUser;
  }

  deleteUser() {
    if (this.userId) {
      // Make sure
      if (
        window.confirm(
          "Do you really want to delete your account and get logged out?"
        )
      ) {
        let req = new RemoveUserRequest();
        req.setUserId(this.userId);
        this.$emit("remove", req);
      }
    }
  }

  updateUser() {
    if (this.userId) {
      if (this.password != this.passwordConfirm) {
        alert("Passwords do not match!");
        return;
      }
      let req = new UpdateUserRequest();
      req.setUser(this.getUser());
      this.$emit("update", req);
    }
  }

  createUser() {
    if (this.password != this.passwordConfirm) {
      alert("Passwords do not match!");
      return;
    }
    let req = new AddUserRequest();
    req.setUser(this.getUser());
    this.$emit("create", req);
  }

  get isAdmin(): boolean {
    let lvl = this.user.priviledgeLevel;
    return lvl ? lvl > 0 : false;
  }

  @Watch("initialUser")
  onInitialUserChanged(newValue: User) {
    if (newValue) {
      this.user.email = newValue.getEmail();
      this.user.firstName = newValue.getFirstName();
      this.user.lastName = newValue.getLastName();
      this.userId = newValue.getId();
    }
  }
}
</script>
<style scoped lang="sass">
$profile-header-height: 150px

.edit-user-container
  .user-header
    width: 100%
    height: $profile-header-height
    position: relative

    .user-icon
      position: absolute
      left: 25px
      top: 25px
      border-radius: $profile-header-height - 50px / 2
      width: $profile-header-height - 50px
      height: $profile-header-height - 50px
      background-color: #b3ccff
      color: white
      text-align: center
      font-size: ($profile-header-height - 50px) / 3
      i
        width: $profile-header-height - 50px
        line-height: $profile-header-height - 50px

    div
      position: absolute
      left: $profile-header-height
      top: 25px
      height: 150px

      .full-name
        font-size: 2.25rem

      .email
        +theme(color, c-accent-text)

  .user-account
    position: relative
</style>
