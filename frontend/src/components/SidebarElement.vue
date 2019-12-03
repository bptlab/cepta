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

<script>
export default {
  name: "SidebarElement",
  props: {
    title: {
      type: String,
      default: "Sidebar Element"
    },
    route: {
      type: [String, Object],
      default: "/"
    },
    active: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {};
  },
  mounted() {}
};
</script>

<style scoped lang="sass"></style>
