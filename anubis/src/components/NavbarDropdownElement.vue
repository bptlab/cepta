<template>
  <li>
    <a class="peers fxw-nw td-n p-20 bdB c-grey-800 cH-blue bgcH-grey-100">
      <div class="peer mR-15">
        <slot name="icon"></slot>
      </div>
      <div class="peer peer-greed">
        <slot name="content"></slot>
      </div>
    </a>
  </li>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "NavigationBarDropdownElement"
})
export default class NavbarDropdownElement extends Vue {}
</script>

<style scoped lang="sass"></style>
