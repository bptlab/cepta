<template>
  <div>
    <h4 class="fw-300 c-grey-900 mB-40">Register</h4>
    <div v-if="isRedirecting">
      <i class="fas fa-spinner fa-2x fa-spin"></i>
    </div>
    <div v-if="!isRedirecting && appAllowsRegister">
      <form @submit.prevent="signup">
        <div class="form-group">
          <label class="text-normal text-dark">Username</label>
          <input type="text" class="form-control" Placeholder="John Doe" />
        </div>
        <div class="form-group">
          <label class="text-normal text-dark">Email Address</label>
          <input
            type="email"
            class="form-control"
            Placeholder="name@email.com"
          />
        </div>
        <div class="form-group">
          <label class="text-normal text-dark">Password</label>
          <input type="password" class="form-control" placeholder="Password" />
        </div>
        <div class="form-group">
          <label class="text-normal text-dark">Confirm Password</label>
          <input type="password" class="form-control" placeholder="Password" />
        </div>
        <div class="form-group">
          <button class="btn btn-primary">Register</button>
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

<script>
export default {
  name: "Signup",
  components: {},
  data() {
    return {
      isRedirecting: false
    };
  },
  computed: {
    appAllowsRegister() {
      return this.$store.state.appAllowsRegister;
    }
  },
  methods: {
    signup() {
      // If success
      this.isRedirecting = true;
    }
  },
  mounted() {}
};
</script>
