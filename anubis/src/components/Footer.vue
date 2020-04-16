<template>
  <footer class="ta-c lh-0 fsz-sm">
    <span>
      <slot></slot>
    </span>
  </footer>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
@Component({
  name: "Footer"
})
export default class Footer extends Vue {
  mounted() {}
}
</script>

<style scoped lang="sass">
footer
  z-index: 1000
  +theme-color-diff(background-color, bgc-body, 10)
  border-top-width: 1px
  border-top-style: solid
  +theme-color-diff(border-top-color, bgc-navbar, 10)
</style>
