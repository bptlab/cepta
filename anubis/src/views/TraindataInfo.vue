<template>
  <div>
    <h1>Choose a TrainId</h1>
    <h3>Either in the input field below  or in the navbar</h3>
    <form class="form-inline">
      <input ref="search" class="form-control" id="myInput" type="text" placeholder="Search..">
      <button @click.prevent="search">Search</button>
    </form>
  </div>
</template>

<script>
export default {
  name: "TraindataInfo",
  components: {},
  data() {
    return {
    };
  },
  methods: {
    search(){
      let id = this.$refs.search.value;
      this.$router.push({ name: 'traindatagrid', params: {id}});
    }
  }
};
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
