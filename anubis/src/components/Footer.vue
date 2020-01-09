<template>
  <footer class="bdT ta-c p-30 lh-0 fsz-sm c-grey-600">
    <span>
      <slot></slot>
    </span>
  </footer>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator'
  @Component({
    name: "Footer",
    components: {},
    props: {},
  })

export default class Footer extends Vue{
  mounted() {}
};
</script>

<style scoped lang="sass"></style>
