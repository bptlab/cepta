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
import { AuthModule } from "./store/modules/auth";
import { AppModule } from "./store/modules/app";
import { TrainDelayNotification } from "./generated/protobuf/models/events/TrainDelayNotification_pb";

@Component({
  name: "App",
  components: {
    NprogressContainer
  }
})
export default class App extends Vue {
  redraw() {
    // @ts-ignore: No such attribute
    this.$redrawVueMasonry();
  }

  connectWebsocket() {
    let socket: WebSocket = new WebSocket(
      "ws://" + window.location.hostname + "/ws/userdata"
    );
    socket.binaryType = "arraybuffer";
    console.log("Attempting Connection...");
    socket.onopen = () => {
      console.log("Successfully Connected");
      socket.send(this.generateRandomUserID(10));
    };
    socket.onmessage = event => {
      let deserializedEvent = TrainDelayNotification.deserializeBinary(
        new Uint8Array(event.data)
      );
      console.log(deserializedEvent);
      AppModule.addDelay(deserializedEvent);
      // var message = JSON.parse(event.data);
    };

    socket.onclose = event => {
      console.log("Socket Closed Connection: ", event);
      socket.send("Client Closed!");
    };
    socket.onerror = error => {
      console.log("Socket Error: ", error);
    };
  }

  generateRandomUserID(quantity: number): string {
    var userId: string = Math.floor(Math.random() * quantity).toString();
    return userId;
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
  /*
  destroyed() {
    this.socket.close();
  }
  */
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

#app
  height: 100%
  +theme(background-color, bgc-body)
  +theme(color, c-default-text)

/* width */
::-webkit-scrollbar
  width: 8px
  height: 8px

/* Track */
::-webkit-scrollbar-track
  box-shadow: inset 0 0 5px grey
  border-radius: 10px

/* Handle */
::-webkit-scrollbar-thumb
  +theme(background-color, bgc-scrollbar)
  border-radius: 5px

/* Handle on hover */
::-webkit-scrollbar-thumb:hover
  +theme-color-diff(background-color, bgc-scrollbar, 10)
</style>
