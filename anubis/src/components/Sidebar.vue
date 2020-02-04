<template>
  <div class="sidebar pB-40">
    <div class="sidebar-inner">
      <!-- Sidebar Header -->
      <div class="sidebar-logo">
        <div class="peers ai-c fxw-nw">
          <div class="peer peer-greed">
            <router-link class="sidebar-link td-n" to="/">
              <div class="peers ai-c fxw-nw">
                <div class="peer">
                  <div class="logo">
                    <img
                      class="centerX"
                      src="../assets/images/logo.png"
                      alt=""
                    />
                  </div>
                </div>
                <div class="peer peer-greed">
                  <h5 class="lh-1 mB-0 logo-text">{{ title }}</h5>
                </div>
              </div>
            </router-link>
          </div>
          <div class="peer">
            <div class="mobile-toggle sidebar-toggle">
              <a class="td-n" @click.prevent="toggleSidebar">
                <i class="icon-arrow-circle-left"></i>
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- Sidebar Menu -->
      <ul class="sidebar-menu pos-r">
        <!-- Websockets -->
        <sidebar-element title="Dashboard" :route="{ name: 'home' }">
          <template v-slot:icon>
            <i class="c-green-500 icon-rocket"></i>
          </template>
        </sidebar-element>

        <!-- User -->
        <sidebar-element
          title="Trainmanagement"
          :route="{ name: 'trainmanagement' }"
        >
          <template v-slot:icon>
            <i class="c-green-500 icon-book"></i>
          </template>
        </sidebar-element>

        <!-- Multiple Levels -->
        <sidebar-dropdown title="Examples">
          <template v-slot:icon>
            <i class="c-teal-500 icon-view-list-alt"></i>
          </template>
          <template v-slot:entries>
            <!-- Pages -->
            <sidebar-dropdown title="Pages">
              <template v-slot:icon>
                <i class="c-red-500 icon-files"></i>
              </template>
              <template v-slot:entries>
                <sidebar-dropdown-element
                  title="Blank"
                  :route="{ name: 'blank' }"
                />
                <sidebar-dropdown-element
                  title="404 Error"
                  :route="{ name: 'error404' }"
                />
                <sidebar-dropdown-element
                  title="500 Error"
                  :route="{ name: 'error500' }"
                />
                <sidebar-dropdown-element
                  title="Log In"
                  :route="{ name: 'login' }"
                />
                <sidebar-dropdown-element
                  title="Sign Up"
                  :route="{ name: 'signup' }"
                />
              </template>
            </sidebar-dropdown>
          </template>
        </sidebar-dropdown>
      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import SidebarElement from "./SidebarElement.vue";
import SidebarDropdown from "./SidebarDropdown.vue";
import SidebarDropdownElement from "./SidebarDropdownElement.vue";
import { AppModule } from "@/store/modules/app";
import { Component, Prop, Vue } from "vue-property-decorator";

@Component({
  name: "Sidebar",
  components: {
    SidebarElement,
    SidebarDropdown,
    SidebarDropdownElement
  }
})
export default class Sidebar extends Vue {
  @Prop({ default: "Cepta" }) private title!: string;
  @Prop({ default: "@/assets/images/logo.png" }) private logo!: string;

  toggleSidebar() {
    AppModule.toggleCollapse();
  }
  mounted() {}
}
</script>

<style lang="sass">

// ---------------------------------------------------------
// @TOC
// ---------------------------------------------------------

// + @Sidebar
// + @Sidebar Inner
// + @Sidebar Header
// + @Sidebar Menu
// + @Sidebar Collapsed

// ---------------------------------------------------------
// @Sidebar
// ---------------------------------------------------------

// Hotfix for problems with scrollbar when all elements are unfolded
// .sidebar-menu has to be set to overflow-x: hidden on revert of this fix
.is-collapsed
  .sidebar-menu
    overflow: auto !important
///////

.sidebar
  background-color: $default-white
  bottom: 0
  overflow: hidden
  position: fixed
  top: 0
  transition: all 0.2s ease
  width: $offscreen-size
  z-index: 1000

  ul
    list-style-type: none

  +between($breakpoint-md, $breakpoint-xl)
    width: $collapsed-size

    .sidebar-inner
      .sidebar-logo
        border-bottom: 1px solid transparent
        padding: 0

        a
          .logo
            background-position: center center
            width: $collapsed-size

      .sidebar-menu
        overflow: hidden

        > li
          > a
            .title
              display: none

        li
          &.dropdown
            .arrow
              opacity: 0

            &.open
              ul.dropdown-menu
                display: none !important

    &:hover
      width: $offscreen-size

      .sidebar-inner
        .sidebar-logo
          border-bottom: 1px solid $border-color
          padding: 0 20px

        .sidebar-menu
          > li
            > a
              .title
                display: inline-block

          li
            &.dropdown
              .arrow
                opacity: 1

            &.open
              > ul.dropdown-menu
                display: block !important


  +to($breakpoint-md)
    left: -$offscreen-size
    width: calc(#{$offscreen-size} - 30px)

// ---------------------------------------------------------
// @Sidebar Inner
// ---------------------------------------------------------

.sidebar-inner
  position: relative
  height: 100%

// ---------------------------------------------------------
// @Sidebar Header
// ---------------------------------------------------------

.sidebar-logo
  border-bottom: 1px solid $border-color
  border-right: 1px solid $border-color
  line-height: 0
  padding: 0 20px
  transition: all 0.2s ease

  a
    display: inline-block
    width: 100%

    .logo
      height: $header-height

      img
        position: relative
        display: inline-block
        top: 20%
        height: 60%

    .logo-text
      color: $grey-900
      padding-left: 10px

  .mobile-toggle
    display: none
    float: right
    font-size: 18px
    line-height: calc(#{$header-height} - 1px)

    a
      color: $default-text-color

    +to($breakpoint-md)
      display: inline-block


    +between($breakpoint-md, $breakpoint-xl)
      display: none

// ---------------------------------------------------------
// @Sidebar Menu
// ---------------------------------------------------------

.sidebar-menu
  +clearfix

  border-right: 1px solid $border-color
  height: calc(100vh - #{$header-height})
  list-style: none
  margin: 0
  overflow: auto
  padding: 0
  position: relative

  &::after
    // Leave some space at the bottom of the sidebar
    height: 20vh
    width: 100%

  .dropdown-toggle::after
    display: none

  .sidebar-link
    &.router-link-exact-active,
    &.router-link-active
      .dot-holder
        background: $md-blue-500
        border-radius: 50%
        content: ''
        display: block
        height: 8px
        left: -4px
        position: absolute
        top: calc(50% - 4px)
        width: 8px

      .title
        color: $default-dark
        text-decoration: none
        font-weight: bold

  li
    position: relative

    &.dropdown
      .arrow
        font-size: 10px
        line-height: 45px
        height: auto
        right: 20px
        top: 0px
        position: absolute
        transition: all 0.05s ease-in

        +to($breakpoint-md)
          right: 25px

      .dropdown-menu
        +clearfix
        position: relative
        display: block

        .arrow
          line-height: 25px

      &.lol
        > a
          color: $default-dark

          .icon-holder
            color: $default-info

          .arrow
            transform: rotate(90deg)

    a
      color: $default-text-color
      transition: all 0.3s ease

      &:hover,
      &:focus
        color: $default-dark
        text-decoration: none

        .icon-holder
          color: $default-info

  > li
    &.dropdown
      ul
        &.dropdown-menu
          border-radius: 0
          border: 0
          box-shadow: none
          float: none
          padding-top: 0
          position: relative
          width: 100%

          > li
            .dropdown-menu
              > li > a
                padding: 10px 15px 10px 85px
            > a
              display: block
              padding: 10px 15px 10px 70px

              &:hover,
              &:focus
                background-color: transparent
                color: $default-dark

            &.active
              a
                color: $default-dark

    > a
      display: block
      font-size: 15px
      font-weight: 500
      padding: 5px 15px
      position: relative
      white-space: nowrap

      .icon-holder
        border-radius: 6px
        display: inline-block
        font-size: 17px
        height: 35px
        left: 0
        line-height: 35px
        margin-right: 14px
        position: relative
        text-align: center
        transition: all 0.3s ease
        width: 35px

// ---------------------------------------------------------
// @Sidebar Collapsed
// ---------------------------------------------------------

.is-collapsed
  .sidebar
    +from($breakpoint-xl)
      width: $collapsed-size

      .sidebar-inner
        .sidebar-logo
          border-bottom: 1px solid transparent
          padding: 0

        .sidebar-menu
          > li
            > a
              .title
                display: none

          li
            &.dropdown
              .arrow
                opacity: 0

              &.open
                ul.dropdown-menu
                  display: none !important

      &:hover
        width: $offscreen-size

        .sidebar-inner
          .sidebar-logo
            border-bottom: 1px solid $border-color
            padding: 0 20px

          .sidebar-menu
            > li
              > a
                .title
                  display: inline-block

            li
              &.dropdown
                .arrow
                  opacity: 1

              &.open
                > ul.dropdown-menu
                  display: block !important


    +between($breakpoint-md, $breakpoint-xl)
      width: $offscreen-size

      .sidebar-inner
        .sidebar-logo
          border-bottom: 1px solid $border-color
          padding: 0 20px

          > a
            .logo
              background-position: center left
              width: 150px

        .sidebar-menu
          > li
            > a
              .title
                display: inline-block

          li
            &.dropdown
              .arrow
                opacity: 1

            &.open
              > ul.dropdown-menu
                display: block !important


    +to($breakpoint-md)
      left: 0
</style>
