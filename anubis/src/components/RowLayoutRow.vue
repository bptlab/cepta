<template>
  <div class="row">
    <div class="col-md-12">
      <div class="bgc-white bd bdrs-3 p-20 mB-20">
        <h4 class="c-grey-900 mB-20">{{ section }}</h4>
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "RowLayoutRow",
  props: {
    section: {}
  },
  components: {},
  mounted() {}
};
</script>
