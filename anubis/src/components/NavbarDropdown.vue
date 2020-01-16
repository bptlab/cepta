<template>
  <li class="generic-dropdown dropdown">
    <!-- Toggle -->
    <span v-if="number" class="counter bgc-red">{{ number }}</span>
    <!--<a class="dropdown-toggle no-after" data-toggle="dropdown">
      <slot name="icon"></slot>
    </a>-->
    <a class="dropdown-toggle no-after fxw-nw ai-c lh-1" data-toggle="dropdown">
      <slot name="icon"></slot>
    </a>
    <!-- Dropdown menu -->
    <ul class="dropdown-menu">
      <li class="pX-20 pY-15 ta-c bdT">
        <slot name="content"></slot>
      </li>
    </ul>
  </li>
</template>

<script>
export default {
  name: "NavbarDropdown",
  props: {
    title: {
      type: String,
      default: "Notification Dropdown"
    },
    more: {
      type: String,
      default: null
    },
    number: {
      type: Number,
      default: 0
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

<style scoped lang="sass">
.dropdown
  margin-left: 15px

.dropdown-menu
  left: auto
  right: 0

  > li
    width: 100%

    > a
      line-height: 1.5
      min-height: auto
      padding: 10px 15px

.generic-dropdown
  .dropdown-menu
    min-width: 350px
    padding: 0
    transform: scale(0, 0)

    +to($breakpoint-sm)
      max-width: 300px


.dropdown-menu
  display: block
  margin: 0
  .divider
    border-bottom: 1px solid $border-color
    height: 1px
    overflow: hidden

  > li
    > a
      transition: all 0.2s ease-out

.show
  .dropdown-menu
    transform: scale(1, 1)
</style>
