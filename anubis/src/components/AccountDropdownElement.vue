<template>
  <li>
    <router-link :to="route" class="d-b td-n pY-5 bgcH-grey-100 c-grey-700">
      <slot></slot>
      <span>{{ title }}</span>
    </router-link>
  </li>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator'

  @Component({
    name: "AccountDropdownElement",
    components: {},
    props: {
      route: {
        type: String,
        default: ""
      },
      title: {
        type: String,
        default: "I provide account details"
      }
    }
  })
export default class extends Vue {

};
</script>

<style scoped lang="sass"></style>
