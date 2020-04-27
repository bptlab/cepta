<template>
  <li>
    <a class="peers fxw-nw td-n p-5 bdB cH-blue">
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
