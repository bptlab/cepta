<template>
  <li>
    <router-link :to="route" class="d-b td-n">
      <slot></slot>
      <span>{{ title }}</span>
    </router-link>
  </li>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";

@Component({
  name: "AccountDropdownElement",
  components: {}
})
export default class extends Vue {
  @Prop({ default: "/" }) private route!: string;
  @Prop({ default: "I provide account details" }) private title!: string;
}
</script>

<style scoped lang="sass">
a
  +theme(color, c-default-text)
  color: inherit
</style>
