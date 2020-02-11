<template>
  <div class="error-container">
    <div class="error-image">
      <img alt="" src="../assets/images/500.png" />
    </div>

    <div class="error">
      <h1 class="mB-30">500</h1>
      <h3 class="mB-10">Internal server error</h3>
      <p class="mB-30">
        Something goes wrong with our servers, please try again later.
      </p>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "Error500",
  components: {}
})
export default class ErrorPage500 extends Vue {
  mounted() {}
}
</script>

<style lang="sass">
.error-container
  .error-image
    margin-right: 60px
  .error
    h1
      font-size: 60px
      font-weight: 900
</style>
