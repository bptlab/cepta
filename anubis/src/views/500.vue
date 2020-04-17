<template>
  <div class="error-container">
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
  name: "ErrorPage500",
  components: {}
})
export default class ErrorPage500 extends Vue {
  mounted() {}
}
</script>

<style lang="sass">
.error-container
  position: relative
  .error-image
    margin-right: 60px
  .error
    width: 100%
    text-align: center
    h1
      font-size: 60px
      font-weight: 900
</style>
