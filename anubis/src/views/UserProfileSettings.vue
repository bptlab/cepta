<template>
  <div class="user-profile-container">
    <div class="user-header">
      <img :src="user.picture" />
      <div>
        <p class="full-name">{{ user.firstName }} {{ user.lastName }}</p>
        <p>
          <span class="email">{{ user.email }}</span> -
          <span v-id="isAdmin" class="priviledge">Administrator</span>
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
              :value="user.firstName"
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
              :value="user.lastName"
            />
          </div>
        </div>
        <div class="form-group row">
          <label for="inputUsername" class="col-sm-2 col-form-label"
            >Username</label
          >
          <div class="col-sm-10">
            <input
              type="email"
              class="form-control"
              id="inputUsername"
              :value="user.username"
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
              :value="user.email"
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
            />
          </div>
        </div>
        <!--
        <fieldset class="form-group">
          <div class="row">
            <legend class="col-form-label col-sm-2 pt-0">Radios</legend>
            <div class="col-sm-10">
              <div class="form-check">
                <input class="form-check-input" type="radio" name="gridRadios" id="gridRadios1" value="option1" checked>
                <label class="form-check-label" for="gridRadios1">
                  First radio
                </label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" name="gridRadios" id="gridRadios2" value="option2">
                <label class="form-check-label" for="gridRadios2">
                  Second radio
                </label>
              </div>
              <div class="form-check disabled">
                <input class="form-check-input" type="radio" name="gridRadios" id="gridRadios3" value="option3" disabled>
                <label class="form-check-label" for="gridRadios3">
                  Third disabled radio
                </label>
              </div>
            </div>
          </div>
        </fieldset>
        <div class="form-group row">
          <div class="col-sm-2">Checkbox</div>
          <div class="col-sm-10">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="gridCheck1">
              <label class="form-check-label" for="gridCheck1">
                Example checkbox
              </label>
            </div>
          </div>
        </div>
        -->
        <div class="form-group row">
          <div class="col">
            <button
              class="btn btn-danger float-left"
              @click.prevent="deleteUser"
            >
              Delete
            </button>
          </div>
          <div class="col">
            <button
              class="btn btn-primary float-right"
              @click.prevent="updateUser"
            >
              Update
            </button>
          </div>
        </div>
      </form>

      <!--
      <form>
      <div class="form-group form-inline">
        <label for="inputUsername">Username</label>
        <input
          type="text"
          class="form-control"
          Placeholder="John Doe"
          name="username"
          id="inputUsername"
        />
      </div>
      <div class="form-group form-inline">
        <label for="inputEmail">Email Address</label>
        <input
          type="email"
          class="form-control"
          Placeholder="name@email.com"
          name="email"
          id="inputEmail"
        />
      </div>
      <div class="form-group form-inline">
        <label for="inputPassword">Password</label>
        <input
          type="password"
          class="form-control"
          placeholder="Password"
          name="password"
          id="inputPassword"
        />
      </div>
      <div class="form-group form-inline">
        <label for="inputConfirmPassword">Confirm Password</label>
        <input
          type="password"
          class="form-control"
          placeholder="Password"
          name="password"
          id="inputConfirmPassword"
        />
      </div>
      <div class="form-group form-inline">
        <button class="btn btn-primary">Update</button>
        <button class="btn btn-secondary red" v-on:click="deleteUser">
      Delete
    </button>
      </div>
    </form>
    --></div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from "vue-property-decorator";

export interface User {
  picture?: string;
  username: string;
  firstName?: string;
  lastName?: string;
  company?: string;
  email: string;
  priviledgeLevel: number;
}

@Component({
  name: "UserSettings",
  components: {}
})
export default class UserSettings extends Vue {
  protected user: User = {
    picture: "https://randomuser.me/api/portraits/men/17.jpg",
    username: "carguwe",
    firstName: "Uwe",
    lastName: "Schenker",
    email: "uwe.schenker@db.cargo.de",
    company: "DB Cargo",
    priviledgeLevel: 1
  };

  get isAdmin(): boolean {
    return this.user.priviledgeLevel > 0;
  }
}
</script>
<style scoped lang="sass">
.user-profile-container
  margin: 30px
  padding: 20px
  border-radius: 5px
  box-shadow: 0 10px 20px rgba(0,0,0,0.1), 0 6px 6px rgba(0,0,0,0.1)

  .user-header
    width: 100%
    height: 200px
    position: relative

    img
      position: absolute
      left: 25px
      top: 25px
      border-radius: 75px
      width: 150px
      height: 150px

    div
      position: absolute
      left: 200px
      top: 25px
      height: 150px

      .full-name
        font-size: 2.5rem

      .email
        +theme(color, c-accent-text)

  .user-account
    position: relative
</style>
