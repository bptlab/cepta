<template>
  <div>
    <train-id-list/>
    <p>
      Hey hey hey :)
    </p>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import TrainIdList from "../components/TrainIdList.vue";

@Component({
  name: "user",
  components: { TrainIdList },
  props: {}
})
export default class User extends Vue {
  example: string = "Test";
  mounted() {}
}
</script>

<style lang="sass"></style>
