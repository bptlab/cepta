<template>
  <div>
    <p>
      Hey hey hey :)
    </p>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "user",
  components: {},
  props: {}
})
export default class Blank extends Vue {
  example: string = "Test";
  mounted() {}
}
</script>

<style lang="sass"></style>
