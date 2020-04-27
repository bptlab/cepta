<template>
  <div id="error-container" class="pos-a w-100 h-100 d-f ai-c jc-c">
    <div id="error-view" class="d-f">
      <router-view class="d-f" id="concrete-error-view" />
      <div id="global-error-view">
        <p>
          <router-link to="/" type="primary" class="btn btn-primary">
            Take me home
          </router-link>
        </p>
        <br />
        <p>
          <a id="joke" href="https://www.youtube.com/watch?v=1vrEljMfXYo">
            https://www.youtube.com/watch?v=1vrEljMfXYo
          </a>
        </p>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "Error",
  components: {}
})
export default class Error extends Vue {
  mounted() {}
}
</script>

<style lang="sass">
#error-container
  background-color: inherit
  #error-view
    display: inline-block
    position: relative
  #concrete-error-view
    position: relative
  #global-error-view
    display: table
    width: 100%
    & > p
      text-align: center
      display: table-row
    #joke
      font-size: 0.4rem
</style>
