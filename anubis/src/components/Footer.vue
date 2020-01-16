<template>
  <footer class="bdT ta-c p-30 lh-0 fsz-sm c-grey-600">
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
  position: relative
</style>
