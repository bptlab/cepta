<template>
  <li>
    <router-link class="sidebar-link" :class="{ active: active }" :to="route">
      <span class="dot-holder"></span>
      <span class="title">{{ title }}</span>
    </router-link>
  </li>
</template>

<script>
export default {
  name: "SidebarDropdownElement",
  props: {
    title: {
      type: String,
      default: "Sidebar Dropdown Element"
    },
    route: {
      type: [String, Object],
      default: "/"
    },
    active: {
      type: Boolean,
      default: false
    }
  }
};
</script>

<style scoped lang="sass"></style>
