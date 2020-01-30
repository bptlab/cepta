<template>
  <div>
    <form action="www.google.de" method="post">
      <div class="form-group">
        <label class="text-normal text-dark">Username</label>
        <input
          type="text"
          class="form-control"
          Placeholder="John Doe"
          name="username"
        />
      </div>
      <div class="form-group">
        <label class="text-normal text-dark">Email Address</label>
        <input
          type="email"
          class="form-control"
          Placeholder="name@email.com"
          name="email"
        />
      </div>
      <div class="form-group">
        <label class="text-normal text-dark">Password</label>
        <input
          type="password"
          class="form-control"
          placeholder="Password"
          name="password"
        />
      </div>
      <div class="form-group">
        <label class="text-normal text-dark">Confirm Password</label>
        <input
          type="password"
          class="form-control"
          placeholder="Password"
          name="password"
        />
      </div>
      <div class="form-group">
        <button class="btn btn-primary">Update</button>
      </div>
    </form>
    <button class="btn btn-secondary red" v-on:click="this.deleteUser">Delete</button>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
@Component({
  name: "UserSettings",
  components: {}
})
export default class SignUp extends Vue {
  isRedirecting: boolean = false;

  //computed
  get appAllowsRegister() {
    return this.$store.state.appAllowsRegister;
  }

  updateSettings() {
    console.log(
      "you could have updated your settings if it was implemented ;-)"
    );
  }

  deleteUser() {
    console.log(
      "you would have deleted yourself if it was implemented ;-)"
    );
  }

  mounted() {}
}
</script>
<style lang="sass">
.red
  background-color: red
</style>