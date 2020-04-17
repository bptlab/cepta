<template>
  <div class="user-settings-container">
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
    <div class="user-settings">
      <h5>Einstellungen</h5>
      <form>
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
        -->
        <div class="form-group row">
          <label for="dropdown1" class="col-sm-2 col-form-label"
            >Soft Treshold Delays
          </label>
          <div class="dropdown" id="dropdown1">
            <button
              class="btn btn-outline-primary dropdown-toggle"
              type="button"
              id="dropdownMenuButton1"
              data-toggle="dropdown"
              aria-haspopup="true"
              aria-expanded="false"
            >
              -- Choose Setting --
            </button>
            <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
              <a class="dropdown-item" href="#">5</a>
              <a class="dropdown-item" href="#">10</a>
              <a class="dropdown-item" href="#">15</a>
              <a class="dropdown-item" href="#">20</a>
              <a class="dropdown-item" href="#">25</a>
              <a class="dropdown-item" href="#">30</a>
            </div>
          </div>
        </div>
        <div class="form-group row">
          <label for="dropdown2" class="col-sm-2 col-form-label"
            >Hard Treshold Delays
          </label>
          <div class="dropdown" id="dropdown2">
            <button
              class="btn btn-outline-primary dropdown-toggle"
              type="button"
              id="dropdownMenuButton2"
              data-toggle="dropdown"
              aria-haspopup="true"
              aria-expanded="false"
            >
              -- Choose Setting --
            </button>
            <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
              <a class="dropdown-item" href="#">15</a>
              <a class="dropdown-item" href="#">20</a>
              <a class="dropdown-item" href="#">25</a>
              <a class="dropdown-item" href="#">30</a>
              <a class="dropdown-item" href="#">35</a>
              <a class="dropdown-item" href="#">40</a>
            </div>
          </div>
        </div>
        <div class="form-group row">
          <div class="col-sm-2">Receive E-mails</div>
          <div class="col-sm-10">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="gridCheck1" />
              <label class="form-check-label" for="gridCheck1">
                Notification Updates
              </label>
            </div>
          </div>
          <div class="offset-sm-2 col-sm-10">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="gridCheck1" />
              <label class="form-check-label" for="gridCheck1">
                Other Updates
              </label>
            </div>
          </div>
        </div>
        <div class="form-group row">
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
    </div>
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
    company: "DB Cargo"
  };
}
</script>
<style scoped lang="sass">
.user-settings-container
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

  .user-settings
    position: relative
</style>
