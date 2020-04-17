<template>
  <div class="peers ai-s fxw-nw h-100vh">
    <div
      class="landing-cover d-n@sm- peer peer-greed h-100 pos-r bgr-n bgpX-c bgpY-c bgsz-cv"
    >
      <div class="pos-a centerXY">
        <div class="landing-logo pos-r">
          <img class="pos-r" src="../assets/images/logo.png" alt="" />
          <p>
            <router-link to="/" class="landing-name pos-r">{{
              appName
            }}</router-link>
          </p>
        </div>
      </div>
    </div>
    <div class="landing-content col-12 col-md-4 peer pX-40 pY-80 h-100 pos-r">
      <router-view />
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "Landing",
  components: {}
})
export default class Landing extends Vue {
  //computed
  get appName() {
    return this.$store.state.appName;
  }

  mounted() {}
}
</script>

<style scoped lang="sass">

.landing-content
  min-width: 320px
  overflow-y: scroll

.landing-cover
  background-image: url("../assets/images/bg.jpg")
  .landing-logo
    +theme(background-color, bgc-body)
    width: 140px
    border-radius: 10px
    img
      padding-top: 20px
      padding-bottom: 20px
      position: relative
      width: 70%
      left: 15%
    p
      font-weight: bold
      font-size: larger
      position: relative
      text-align: center
      padding: 0 10px 20px 10px
</style>
