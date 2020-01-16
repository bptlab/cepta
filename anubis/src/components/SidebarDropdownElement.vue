<template>
  <li>
    <router-link class="sidebar-link" :class="{ active: active }" :to="route">
      <span class="dot-holder"></span>
      <span class="title">{{ title }}</span>
    </router-link>
  </li>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";

@Component({
  name: "SidebarDropdownElement"
})
export default class SidebarDropdownElement extends Vue {
  @Prop({ default: "Sidebar Dropdown Element" }) private title!: string;
  @Prop({ default: "/" }) private route!: string | object;
  @Prop({ default: false }) private active!: boolean;
}
</script>

<style scoped lang="sass"></style>
