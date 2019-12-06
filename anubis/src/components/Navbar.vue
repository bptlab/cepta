<template>
  <div class="header navbar">
    <div class="header-container">
      <!-- Leftmost search bar and sidebar toggle -->
      <ul class="nav-left">
        <li>
          <a
            id="sidebar-toggle"
            class="sidebar-toggle"
            @click.prevent="toggleSidebar"
          >
            <i class="icon-menu"></i>
          </a>
        </li>
        <!-- Search -->
        <li class="search-box" :class="{ active: searchToggled }">
          <a class="search-toggle no-pdd-right" @click.prevent="toggleSearch">
            <i class="search-icon icon-search pdd-right-10"></i>
            <i class="search-icon-close icon-close pdd-right-10"></i>
          </a>
        </li>
        <li class="search-input" :class="{ active: searchToggled }">
          <input
            class="form-control"
            ref="searchInput"
            v-model="search"
            type="text"
            placeholder="Search..."
          />
        </li>
      </ul>
      <!-- Rightmost notifications and account -->
      <!-- Notifications -->
      <ul class="nav-right">
        <!--<button id="replayBtn" @click="replayData" class="btn btn-danger">Replay Data!</button>-->
        <notifications-dropdown
          title="Notifications"
          :number="3"
          more="notifications"
        >
          <template v-slot:icon>
            <i class="icon-bell"></i>
          </template>
          <template v-slot:entries>
            <notifications-dropdown-element
              image="https://randomuser.me/api/portraits/men/1.jpg"
              headline="John Doe liked your post"
              sub-headline="5 mins ago"
            />
            <notifications-dropdown-element
              image="https://randomuser.me/api/portraits/women/60.jpg"
              headline="Moo Doe liked your cover image"
              sub-headline="7 mins ago"
            />
            <notifications-dropdown-element
              image="https://randomuser.me/api/portraits/men/3.jpg"
              headline="Lee Doe commented on your video"
              sub-headline="10 mins ago"
            />
          </template>
        </notifications-dropdown>
        <!-- Email alerts
        <notifications-dropdown title="Emails" :number="3" more="mails">
          <template v-slot:icon>
            <i class="icon-email"></i>
          </template>
          <template v-slot:entries>
            <email-dropdown-element
              image="https://randomuser.me/api/portraits/men/1.jpg"
              headline="John Doe"
              sub-headline="Want to create your own customized data generator for your app..."
              status="5 mins ago"
            />
            <email-dropdown-element
              image="https://randomuser.me/api/portraits/men/2.jpg"
              headline="Moo Doe"
              sub-headline="Want to create your own customized data generator for your app..."
              status="15 mins ago"
            />
            <email-dropdown-element
              image="https://randomuser.me/api/portraits/men/3.jpg"
              headline="Lee Doe"
              sub-headline="Want to create your own customized data generator for your app..."
              status="25 mins ago"
            />
          </template>
        </notifications-dropdown>
        -->
        <!-- User account -->
        <account-dropdown
          username="Admin"
          picture="https://randomuser.me/api/portraits/lego/5.jpg"
        />

      </ul>
    </div>
  </div>
</template>

<script>
import NotificationsDropdown from "@/components/NotificationsDropdown";
import NotificationDropdownElement from "@/components/NotificationDropdownElement";
import EmailDropdownElement from "@/components/EmailDropdownElement";
import AccountDropdown from "@/components/AccountDropdown";
import { GrpcModule } from "@/store/modules/grpc";
import {AppModule} from "../store/modules/app";

export default {
  name: "NavigationBar",
  components: {
    "notifications-dropdown": NotificationsDropdown,
    "notifications-dropdown-element": NotificationDropdownElement,
    "email-dropdown-element": EmailDropdownElement,
    "account-dropdown": AccountDropdown
  },
  props: {},
  data() {
    return {
      searchToggled: false,
      search: null
    };
  },
  methods: {
    toggleSearch() {
      this.searchToggled = !this.searchToggled;
      window.setTimeout(() => {
        // Focus the input
        this.$refs.searchInput.focus();
      }, 0);
    },
    toggleSidebar() {
      AppModule.toggleCollapse();
      this.$redrawVueMasonry();
      setTimeout(() => {
        this.$redrawVueMasonry();
      }, 0.2 * 500);
    }
  },
  mounted() {}
};
</script>

<style scoped lang="sass">

// TODO: Make scoped by adding styles to child components

// ---------------------------------------------------------
// @TOC

// + @Topbar
// + @Collapsed State

// ---------------------------------------------------------
// @Topbar
// ---------------------------------------------------------

.header
  background-color: $default-white
  border-bottom: 1px solid $border-color
  display: block
  margin-bottom: 0
  padding: 0
  position: fixed
  transition: all 0.2s ease
  //width: calc(100% - #{$offscreen-size})
  width: 100%
  z-index: 800

  +to($breakpoint-md)
    width: 100%


  +between($breakpoint-md, $breakpoint-xl)
    width: calc(100% - #{$collapsed-size})


  .header-container
    +clearfix

    height: $header-height

    .nav-left,
    .nav-right
      list-style: none
      margin-bottom: 0
      padding-left: 0
      position: relative

      > li
        float: left

        > a
          color: $default-text-color
          display: block
          line-height: $header-height
          min-height: $header-height
          padding: 0 15px
          transition: all 0.2s ease-in-out

          i
            font-size: 17px

          &:hover,
          &:focus
            color: $default-dark
            text-decoration: none

          +to($breakpoint-md)
            padding: 0 15px

    .nav-left
      float: left
      margin-left: 15px
      transition: 0.2s ease

      +from($breakpoint-xl)
        margin-left: 230px

    .nav-right
      float: right
      margin-right: 10px
      margin-top: 15px

      #replayBtn
        float: left
        margin-right: 20px

  .search-box
    .search-icon-close
      display: none

    &.active
      .search-icon
        display: none

      .search-icon-close
        display: inline-block

  .search-input
    display: none

    &.active
      display: inline-block

    input
      background-color: transparent
      border: 0
      box-shadow: none
      font-size: 18px
      height: 40px
      margin-top: 12px
      outline: none
      padding: 5px
      width: 500px

      +to($breakpoint-sm)
        width: 160px

      +to($breakpoint-xs)
        width: 85px


      +placeholder
        color: lighten($default-text-color, 20%)
        font-style: italic

// ---------------------------------------------------------
// @Collapsed State
// ---------------------------------------------------------

.is-collapsed
  .header
    width: calc(100% - #{$collapsed-size})

    +to($breakpoint-md)
      width: 100%


    +between($breakpoint-md, $breakpoint-xl)
      width: calc(100% - #{$offscreen-size})
</style>
