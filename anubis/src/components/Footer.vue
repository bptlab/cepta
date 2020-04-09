<template>
  <footer class="bdT ta-c lh-0 fsz-sm">
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
  z-index: 1
</style>
