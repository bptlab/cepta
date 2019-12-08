<template>
  <navigation-dropdown-element>
    <template v-slot:icon>
      <img class="w-3r bdrs-50p" :src="image" />
    </template>
    <template v-slot:content>
      <span>
        <router-link class="fw-500" to="">
          {{ headline }}
        </router-link>
      </span>
      <p class="m-0">
        <small class="fsz-xs">{{ subHeadline }}</small>
      </p>
    </template>
  </navigation-dropdown-element>
</template>

<script>
import NavigationBarDropdownElement from "./NavbarDropdownElement";

export default {
  name: "NotificationDropdownElement",
  components: {
    "navigation-dropdown-element": NavigationBarDropdownElement
  },
  props: {
    image: {
      type: String,
      default: "../../assets/images/logo.png"
    },
    headline: {
      type: String,
      default: "This is a notification headline!"
    },
    subHeadline: {
      type: String,
      default: "This is a notification sub-headline!"
    }
  }
};
</script>

<style scoped lang="sass"></style>
