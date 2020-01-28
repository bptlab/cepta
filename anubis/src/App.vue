<template>
  <div id="app">
    <!-- Progress bar -->
    <nprogress-container></nprogress-container>
    <!-- Main views -->
    <router-view />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import NprogressContainer from "vue-nprogress/src/NprogressContainer.vue";
import SockJS from "sockjs-client";
import { AuthModule } from "@/store/modules/auth";

@Component({
  name: "App",
  components: {
    NprogressContainer
  }
})
export default class App extends Vue {
  socket = new SockJS("http://localhost:5000/ws");

  redraw() {
    // @ts-ignore: No such attribute
    this.$redrawVueMasonry();
  }

  connectWebsocket() {
    this.$store.commit("setWebsocket", this.socket);
  }

  created() {
    this.axios.interceptors.response.use(undefined, err => {
      return new Promise((resolve, reject) => {
        if (err.status === 401 && err.config && !err.config.__isRetryRequest) {
          // if you ever get an unauthorized, logout the user
          AuthModule.authLogout();
          // you can also redirect to /login if needed !
          this.$router.push("/404");
        }
        throw err;
      });
    });
  }

  mounted() {
    this.redraw();
    this.connectWebsocket();
  }

  destroyed() {
    this.socket.close();
  }
}
</script>

<style lang="scss">
@import "~bootstrap/scss/bootstrap";
@font-face {
  font-family: "themify";
  src: url(~themify-icons/themify-icons/fonts/themify.eot?-fvbane);
  src: url(~themify-icons/themify-icons/fonts/themify.eot?#iefix-fvbane)
      format("embedded-opentype"),
    url(~themify-icons/themify-icons/fonts/themify.woff?-fvbane) format("woff"),
    url(~themify-icons/themify-icons/fonts/themify.ttf?-fvbane)
      format("truetype"),
    url(~themify-icons/themify-icons/fonts/themify.svg?-fvbane#themify)
      format("svg");
  font-weight: normal;
  font-style: normal;
}
@import "~themify-icons/themify-icons/variables";
@import "~themify-icons/themify-icons/mixins";
@import "~themify-icons/themify-icons/core";
@import "~themify-icons/themify-icons/extras";
@import "~themify-icons/themify-icons/icons";
</style>

<style lang="sass">
@import "/style/custom.sass"
@import "/style/spec/index.sass"
@import "/style/vendor/index.sass"

.ps__rail-y
  right: 0 !important
  left: auto !important

.nprogress-container
  height: 3px
  top: 0
  z-index: 9999999
  position: fixed
  width: 100%

#nprogress .bar
  background: #42b983
  height: 3px

/* Base */
html, html a, body
  -webkit-font-smoothing: antialiased

a
  transition: all 0.3s ease-in-out

body
  font-family: $font-primary
  font-size: 14px
  color: $default-text-color
  line-height: 1.5
  letter-spacing: 0.2px
  overflow-x: hidden
h1,
h2,
h3,
h4,
h5,
h6
  font-family: $font-secondary
  letter-spacing: 0.5px
  line-height: 1.5

  a
    font-family: $font-secondary

  small
    font-weight: 300
    color: lighten($default-dark, 5%)

p
  font-family: $font-primary
  line-height: 1.9

.lead
  font-size: 18px

ul
  margin-bottom: 0

a
  color: $default-info

  &:hover,
  &:focus
    text-decoration: none
    color: darken($default-info, 10%)

  &:focus
    outline: none

  &.text-gray
    &:hover,
    &:focus,
    &.active
      color: $default-dark !important

\:focus
  outline: none
hr
  border-top: 1px solid $border-color
</style>
