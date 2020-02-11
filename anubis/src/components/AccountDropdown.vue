<template>
  <li class="dropdown">
    <!-- User account dropdown toggle -->
    <a
      class="dropdown-toggle no-after peers fxw-nw ai-c lh-1"
      data-toggle="dropdown"
    >
      <div class="peer mR-10">
        <img class="w-2r bdrs-50p" :src="picture" alt="" />
      </div>
      <div class="peer">
        <span class="fsz-sm">{{ username }}</span>
      </div>
    </a>
    <!-- Dropdown menu items -->
    <ul class="dropdown-menu fsz-sm">
      <!-- Profile page -->
      <account-dropdown-element title="Profile" route="profile">
        <i class="icon-user mR-10"></i>
      </account-dropdown-element>
      <!-- Settings page -->
      <account-dropdown-element title="Settings" route="/user/settings">
        <i class="icon-settings mR-10"></i>
      </account-dropdown-element>
      <!-- Messages page -->
      <account-dropdown-element title="Messages" route="profile/messages">
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
    this.$store.dispatch("AUTH_LOGOUT").then(() => {
      this.$router.push("/login");
    });
  }

  mounted() {}
}
</script>

<style scoped lang="sass"></style>
