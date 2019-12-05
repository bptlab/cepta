<template>
  <div class="container-fluid">
    <h4 v-if="title" class="c-grey-900 mT-10 mB-30">{{ title }}</h4>
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: "RowLayout",
  props: {
    title: {}
  },
  components: {},
  mounted() {}
};
</script>
