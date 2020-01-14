<template>
  <footer class="bdT ta-c p-30 lh-0 fsz-sm c-grey-600">
    <span>
      <slot></slot>
    </span>
  </footer>
</template>

<script>
export default {
  name: "Footer",
  components: {},
  props: {},
  data() {
    return {};
  },
  mounted() {}
};
</script>

<style scoped lang="sass">
   footer
     z-index: 1
     position: relative
</style>
