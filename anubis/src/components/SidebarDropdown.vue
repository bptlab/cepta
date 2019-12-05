<template>
  <li class="nav-item dropdown" :class="{ lol: open }">
    <!-- Dropdown Toggle -->
    <a
      class="dropdown-toggle"
      :for="uuid"
      data-toggle="collapse"
      :href="'#' + uuid"
      aria-expanded="true"
      :aria-controls="uuid"
      @click="open = !open"
    >
      <span class="icon-holder">
        <slot name="icon"></slot>
      </span>
      <span class="title"> {{ title }} </span>
      <span class="arrow">
        <i class="icon-angle-right"></i>
      </span>
    </a>
    <!-- Dropdown menu entries -->
    <div class="collapse" :id="uuid">
      <ul class="dropdown-menu">
        <slot name="entries"></slot>
      </ul>
    </div>
  </li>
</template>

<script>
export default {
  name: "SidebarDropdown",
  props: {
    title: {
      type: String,
      default: "Sidebar Dropdown Element"
    },
    active: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      open: false
    };
  },
  watch: {},
  computed: {
    uuid: function() {
      return this.title.replace(/\s+/g, "");
    }
  },
  mounted() {}
};
</script>

<style scoped lang="sass"></style>
