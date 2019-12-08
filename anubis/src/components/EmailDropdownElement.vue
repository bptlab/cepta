<template>
  <navigation-dropdown-element>
    <template v-slot:icon>
      <img class="w-3r bdrs-50p" :src="image" />
    </template>
    <template v-slot:content>
      <div>
        <div class="peers jc-sb fxw-nw mB-5">
          <router-link class="peers jc-sb fxw-nw mB-5" to="">
            <p class="fw-500 mB-0">{{ headline }}</p>
          </router-link>
          <div class="peer">
            <small class="fsz-xs">{{ status }}</small>
          </div>
        </div>
        <span class="c-grey-600 fsz-sm">
          {{ subHeadline }}
        </span>
      </div>
    </template>
  </navigation-dropdown-element>
</template>

<script>
import NavigationBarDropdownElement from "@/components/NavbarDropdownElement";

export default {
  name: "EmailDropdownElement",
  components: {
    "navigation-dropdown-element": NavigationBarDropdownElement
  },
  props: {
    image: {
      type: String,
      default: "../../assets/images/logo.png"
    },
    status: {
      type: String,
      default: "This is an email status"
    },
    headline: {
      type: String,
      default: "This is an email headline"
    },
    subHeadline: {
      type: String,
      default: "This is an email sub-headline"
    }
  }
};
</script>

<style scoped lang="sass"></style>
