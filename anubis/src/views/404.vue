<template>
  <div class="error-container">
    <div class="error">
      <h1 class="mB-30">404</h1>
      <h3 class="mB-10">Page Not Found</h3>
      <p class="mB-30">
        The page you are looking for does not exist or has been moved.
      </p>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "Error404",
  components: {}
})
export default class ErrorPage404 extends Vue {
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
