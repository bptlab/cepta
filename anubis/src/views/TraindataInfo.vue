<template>
  <div>
    <h1>Choose a TrainId</h1>
    <h3>Either in the input field below or the in the navbar</h3>
    <form class="form-inline">
      <input
        ref="search"
        class="form-control"
        id="myInput"
        type="text"
        placeholder="Search.."
      />
      <button @click="search">Search</button>
    </form>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "TraindataInfo",
  components: {}
})
export default class TraindataInfo extends Vue {
  search() {
    let id = (this.$refs["search"] as HTMLInputElement).value;
    this.$router.push({ name: "traindata", params: { id } });
  }
}
</script>

<style lang="sass">
.form-inline
  button
    margin-left: 10px
    height: 35px
    background-color: red
    color: white
    font-weight: bolder
    outline: none
    border: none
</style>
