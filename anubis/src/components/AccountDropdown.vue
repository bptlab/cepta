<template>
  <li class="dropdown">
    <!-- User account dropdown toggle -->
    <a
      class="dropdown-toggle no-after peers fxw-nw ai-c"
      data-toggle="dropdown"
    >
      <div class="peer mR-10 user-icon-container">
        <!--<img class="w-2r bdrs-50p" :src="picture" alt="" />-->
        <i class="icon-user mR-10"></i>
      </div>
      <div class="peer">
        <span class="fsz-sm">{{ username }}</span>
      </div>
    </a>
    <!-- Dropdown menu items -->
    <ul class="dropdown-menu fsz-sm">
      <!-- Profile page -->
      <account-dropdown-element title="Profile" :route="{ name: 'profile' }">
        <i class="icon-user mR-10"></i>
      </account-dropdown-element>
      <!-- Settings page -->
      <account-dropdown-element title="Settings" :route="{ name: 'settings' }">
        <i class="icon-settings mR-10"></i>
      </account-dropdown-element>
      <!-- Notifications page -->
      <account-dropdown-element
        title="Notifications"
        :route="{ name: 'notifications' }"
      >
        <i class="icon-email mR-10"></i>
      </account-dropdown-element>
      <li role="separator" class="divider"></li>
      <!-- Logout page -->
      <div @click.prevent="logout()">
        <account-dropdown-element title="Logout">
          <i class="icon-power-off mR-10"></i>
        </account-dropdown-element>
      </div>
    </ul>
  </li>
</template>

<script lang="ts">
import AccountDropdownElement from "../components/AccountDropdownElement.vue";
import { Component, Prop, Vue } from "vue-property-decorator";
import { AuthModule } from "../store/modules/auth";

@Component({
  name: "AccountDropdown",
  components: {
    AccountDropdownElement
  }
})
export default class AccountDropdown extends Vue {
  @Prop({ default: "Account Dropdown" }) private username!: string;
  @Prop({ default: null }) private picture!: string;

  open: boolean = false;

  logout() {
    AuthModule.authLogout();
  }

  mounted() {}
}
</script>

<style scoped lang="sass">
.dropdown
  position: relative
  display: block
  cursor: pointer
  margin-left: 15px

  a
    transition: all 0.1s ease-in-out

  .user-icon-container
    .icon-user
      padding: 10px
      border-radius: 50%
      background-color: #b3ccff
      color: white

  .dropdown-menu
    +theme(background-color, bgc-navbar)
    line-height: 35px
    margin-left: -50px
    li
      padding: 2px 12px
      &:hover
        +theme-color-diff(background-color, bgc-navbar, 10)
</style>
