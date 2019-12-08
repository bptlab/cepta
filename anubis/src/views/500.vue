<template>
  <div class="error-container">
    <div class="error-image">
      <img alt="" src="../assets/images/500.png" />
    </div>

    <div class="error">
      <h1 class="mB-30 c-red-500">500</h1>
      <h3 class="mB-10 c-grey-900">Internal server error</h3>
      <p class="mB-30 c-grey-700">
        Something goes wrong with our servers, please try again later.
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: "Error500",
  components: {},
  mounted() {}
};
</script>

<style lang="sass">
.error-container
  .error-image
    margin-right: 60px
  .error
    h1
      font-size: 60px
      font-weight: 900
</style>
