<template>
  <div id="fuck">
    <p>
      This view is a perfect starting point if you want to build new, custom
      views
    </p>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "Blank",
  components: {},
  props: {}
})
export default class Blank extends Vue {
  example: string = "Test";
  mounted() {}
}
</script>

<style lang="sass">
#fuck
  height: 100%
  position: relative
  background-color: yellow
</style>
