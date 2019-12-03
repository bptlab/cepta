<template>
  <div>
    <h4 class="fw-300 c-grey-900 mB-40">Login</h4>
    <div v-if="isRedirecting">
      <i class="fas fa-spinner fa-2x fa-spin"></i>
    </div>
    <form v-else @submit.prevent="login">
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

<script>
export default {
  name: "Login",
  components: {},
  data() {
    return {
      email: "",
      password: "",
      shouldRemember: false,
      hasError: false,
      isRedirecting: false,
      errorTitle: "Login failed",
      errorMessage: "Check your email and password"
    };
  },
  computed: {
    appAllowsRegister() {
      return this.$store.state.appAllowsRegister;
    }
  },
  methods: {
    clearForm() {
      this.password = "";
    },
    login() {
      this.hasError = false;
      this.$store
        .dispatch(
          "AUTH_REQUEST",
          { email: this.email, password: this.password },
          this.shouldRemember
        )
        .then(
          () => {
            this.isRedirecting = true;
            setTimeout(() => {
              this.$router.push("/");
            }, 1000);
          },
          ({ error, message }) => {
            this.hasError = true;
            this.errorTitle = error;
            this.errorMessage = message;
            this.clearForm();
          }
        );
    }
  },
  mounted() {}
};
</script>
