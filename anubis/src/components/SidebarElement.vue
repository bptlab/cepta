<template>
  <li class="nav-item">
    <router-link class="sidebar-link" :class="{ active: active }" :to="route">
      <span class="dot-holder"></span>
      <span class="icon-holder">
        <slot name="icon"></slot>
      </span>
      <span class="title">{{ title }}</span>
    </router-link>
  </li>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";

@Component({
  name: "SidebarElement"
})
export default class SidebarElement extends Vue {
  @Prop({ default: "Sidebar Element" }) private title!: string;
  @Prop({ default: "/" }) private route!: string | object;
  @Prop({ default: false }) private active!: boolean;

  mounted() {}
}
</script>

<style scoped lang="sass"></style>
