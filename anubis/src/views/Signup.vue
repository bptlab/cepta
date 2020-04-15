<template>
  <div>
    <h4 class="fw-300 mB-40">Register</h4>
    <div v-if="isRedirecting">
      <i class="fas fa-spinner fa-2x fa-spin"></i>
    </div>
    <div v-if="!isRedirecting && appAllowsRegister">
      <form @submit.prevent="signup">
        <div class="form-group">
          <label>Username</label>
          <input
            type="text"
            class="form-control"
            Placeholder="John Doe"
            v-model="username"
          />
        </div>
        <div class="form-group">
          <label>Email Address</label>
          <input
            type="email"
            class="form-control"
            Placeholder="name@email.com"
            v-model="email"
          />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input
            type="password"
            class="form-control"
            placeholder="Password"
            v-model="password"
          />
        </div>
        <div class="form-group">
          <label>Confirm Password</label>
          <input
            type="password"
            class="form-control"
            placeholder="Password"
            v-model="passwordConfirm"
          />
        </div>
        <div class="form-group">
          <button class="btn btn-cepta-default">Register</button>
        </div>
      </form>
    </div>
    <div v-else>
      You cannot sign up yourself. If you need access you have to manually set
      up your user account with your responsible systems administrator.
    </div>
    <div class="mT-40">
      Already have an account?
      <router-link :to="{ name: 'login' }">Log in</router-link>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { AxiosRequestConfig } from "axios";
@Component({
  name: "Signup",
  components: {}
})
export default class SignUp extends Vue {
  isRedirecting: boolean = false;
  username: string = "";
  email: string = "";
  password: string = "";
  passwordConfirm: string = "";

  //computed
  get appAllowsRegister() {
    // return this.$store.state.appAllowsRegister;
    return true;
  }

  signup() {
    let jsonBody: string = `{ username: "${this.username}", email: "${this.email}", password: "${this.password}"}`;
    console.log(jsonBody);
    // let config:AxiosRequestConfig = {headers: {'Access-Control-Allow-Origin': '*'}}
    this.$http.post(
      "http://warm-plains-47366.herokuapp.com/api/user/new",
      jsonBody
    );
    // If success
    this.isRedirecting = true;
  }
  mounted() {}
}
</script>
