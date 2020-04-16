<template>
  <li class="generic-dropdown dropdown">
    <!-- Toggle -->
    <a class="dropdown-toggle no-after fxw-nw ai-c" data-toggle="dropdown">
      <slot name="icon"></slot>
    </a>
    <!-- Dropdown menu -->
    <ul class="dropdown-menu">
      <li class="pX-20 ta-c bdT">
        <slot name="content"></slot>
      </li>
    </ul>
  </li>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";
@Component({
  name: "NavigationBar",
  components: {}
})
export default class NavbarDropdown extends Vue {
  @Prop({ default: "Notification Dropdown" }) private title!: string;
  @Prop({ default: "" }) private more!: string;
  @Prop({ default: 0 }) private number!: number;

  open: boolean = false;

  get uuid() {
    return this.title.replace(/\s+/g, "");
  }
}
</script>

<style scoped lang="sass">
.dropdown
  transition: all 0.1s ease-in-out
  cursor: pointer
  margin-left: 15px

  a
    transition: all 0.1s ease-in-out

  .dropdown-menu
    +theme(background-color, bgc-navbar)
    left: auto
    right: 0
    +theme(color, c-default-text)
    line-height: 35px
    display: block
    margin: 0
    .divider
      border-bottom-width: 1px
      border-bottom-style: solid
      +theme-color-diff(border-bottom-color, bgc-navbar, 6)
      height: 1px
      overflow: hidden

    > li
      padding: 2px 12px
      width: 100%
      &:hover
          +theme-color-diff(background-color, bgc-navbar, 10)

      > a
        transition: all 0.2s ease-out
        line-height: 1.5
        min-height: auto
        padding: 10px 15px

.generic-dropdown
  .dropdown-menu
    line-height: 35px
    min-width: 350px
    padding: 0
    transform: scale(0, 0)

    +to($breakpoint-sm)
      max-width: 300px

.show
  .dropdown-menu
    transform: scale(1, 1)
</style>
