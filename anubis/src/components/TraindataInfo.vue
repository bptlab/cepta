<template>
  <div>
    <p>
      Search for Informations on a specific train ID in the input field below or
      in the navbar.
    </p>
    <form class="form-inline">
      <input
        ref="search"
        class="form-control"
        type="text"
        placeholder="Search.."
      />
      <button @click.prevent="search">Search</button>
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
