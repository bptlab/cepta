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
        <li class="search-input" @keyup.enter="checkForUpdate()" :class="{ active: searchToggled }">
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
          more="notifications"
        >
          <template v-slot:icon>
            <i class="icon-bell"></i>
          </template>

        </notifications-dropdown>

        <!-- User account -->
        <account-dropdown
          username="Admin"
          picture="https://randomuser.me/api/portraits/lego/5.jpg"
        />

      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import NotificationsDropdown from "@/components/NotificationsDropdown.vue";
import NotificationDropdownElement from "@/components/NotificationDropdownElement.vue";
import EmailDropdownElement from "@/components/EmailDropdownElement.vue";
import AccountDropdown from "@/components/AccountDropdown.vue";
import { GrpcModule } from "@/store/modules/grpc";
import {AppModule} from "../store/modules/app";
import axios from 'axios';
import { Component, Vue } from 'vue-property-decorator'

@Component({
  name: "NavigationBar",
  components: {
    "notifications-dropdown": NotificationsDropdown,
    "notifications-dropdown-element": NotificationDropdownElement,
    "email-dropdown-element": EmailDropdownElement,
    "account-dropdown": AccountDropdown
  },
  props: {},
})

export default class Navbar extends Vue {


  searchToggled:boolean = false;
  search:any = null;
  stompClient:any = null;


    toggleSearch() {
      this.searchToggled = !this.searchToggled;
      window.setTimeout(() => {
        // Focus the input
        (this.$refs["searchInput"] as HTMLElement).focus();
      }, 0);
    }
    toggleSidebar() {
      AppModule.toggleCollapse();
      this.$redrawVueMasonry();
      setTimeout(() => {
        this.$redrawVueMasonry();
      }, 0.2 * 500);
    }
    checkForUpdate() {
      let id = (this.$refs["searchInput"] as HTMLInputElement).value;
      // this.send(id)
      let reg = new RegExp("^[0-9]*$");
      debugger;

      //only Numbers update our list of train data
      if (reg.test(id))
        this.$router.push({ name: 'traindata', params: { id }});
    }
    /*
    send(message) {
      console.log('Sehen')
      axios.post('/api/trainid', message)
        .then(response => {console.log(response)});
    }

    send(message){
      console.log("Sending message: " + message);
      if (this.stompClient) {
        this.stompClient.send("/app/id", message, {});
      }
    },
    connect(url = "/topic/traindata") {
      this.stompClient = this.$store.state.websocket;
      this.stompClient.connect(
          {},
          () =>
              this.stompClient.subscribe(url, update => {
                    console.log(update);
                  },
                  error => {
                    console.log(error);
                  })
      )
    },
  },
  mounted() {
    // this.connect();*/

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
