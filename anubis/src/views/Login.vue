<template>
  <div>
    <h4 class="fw-300 c-grey-900 mB-40">Login</h4>
    <div v-if="isRedirecting">
      <i class="fas fa-spinner fa-2x fa-spin"></i>
    </div>
    <form @submit="this.login">
      <!-- Email -->
      <div class="form-group">
        <label class="text-normal text-dark">Email</label>
        <input
          v-model="email"
          type="email"
          class="form-control"
          placeholder="john@example.com"
        />
      </div>
      <!-- Password -->
      <div class="form-group">
        <label class="text-normal text-dark">Password</label>
        <input
          v-model="password"
          type="password"
          class="form-control"
          placeholder="Password"
        />
      </div>
      <div class="form-group">
        <div class="peers ai-c jc-sb fxw-nw">
          <div class="peer">
            <!-- Remember Checkbox -->
            <div class="checkbox checkbox-circle checkbox-info peers ai-c">
              <input
                v-model="shouldRemember"
                type="checkbox"
                id="inputCall1"
                name="inputCheckboxesCall"
                class="peer"
              />
              <label for="inputCall1" class=" peers peer-greed js-sb ai-c">
                <span class="peer peer-greed">Remember Me</span>
              </label>
            </div>
          </div>
          <!-- Submit -->
          <div class="peer">
            <button type="submit" class="btn btn-primary">Login</button>
          </div>
        </div>
      </div>
    </form>
    <!-- Alerts -->
    <div v-if="hasError" class="alert alert-warning" role="alert">
      <strong>{{ errorTitle }}</strong>
      <p>{{ errorMessage }}</p>
    </div>
    <!-- Signup -->
    <div v-if="appAllowsRegister || true" class="mT-40">
      Don't have an account?
      <router-link :to="{ name: 'signup' }">Sign up</router-link>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { AuthModule } from "@/store/modules/auth";

@Component({
  name: "Login",
  components: {}
})
export default class Login extends Vue {
  email: string = "";
  password: string = "";
  shouldRemember: boolean = false;
  hasError: boolean = false;
  isRedirecting: boolean = false;
  errorTitle: string = "Login failed";
  errorMessage: string = "Check your email and password";

  //computed
  get appAllowsRegister() {
    return this.$store.state.appAllowsRegister;
  }

  clearForm() {
    this.password = "";
  }

  mounted() {}

  login() {
    let jsonBody: string = `{ email: "${this.email}", password: "${this.password}"}`;
    this.$http.post(
      "http://warm-plains-47366.herokuapp.com/api/user/login",
      jsonBody
    );
  }
}
</script>
