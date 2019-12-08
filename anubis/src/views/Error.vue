<template>
  <div id="error-container" class="pos-a w-100 h-100 d-f  ai-c jc-c">
    <div id="error-view" class="d-f">
      <router-view class="d-f" id="concrete-error-view" />
      <div id="global-error-view">
        <div>
          <a id="joke" href="https://www.youtube.com/watch?v=1vrEljMfXYo">
            https://www.youtube.com/watch?v=1vrEljMfXYo
          </a>
          <router-link to="/" type="primary" class="btn btn-primary">
            Take me home
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Error",
  components: {},
  mounted() {}
};
</script>

<style lang="sass">
#error-container
  position: relative
  #error-view
    display: inline-block
    position: relative
  #concrete-error-view
    position: relative
  #global-error-view
    position: relative
    margin: 0 auto
    & > *
      display: inline-block
      position: relative
    a
      position: relative
      margin: 20px
</style>
