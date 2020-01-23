<template>
  <masonry-layout title="Your trainIDs">
  <masonry-layout-tile section="some Section">
    <input ref="search" class="form-control" id="trainID oder so" type="number" placeholder="search ... ;)">
      <train-id-info/>
    </masonry-layout-tile>
    <masonry-layout-tile section="hallo Section">
      <basic-table
        :table-data="trainIDs"
        :show-indices="false"
        :striped="true"
        :bordered="true"
        cellspacing="0"
        :data-table="true"
      />
    </masonry-layout-tile>
  </masonry-layout>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import TrainIdList from "../components/TrainIdList.vue";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";

@Component({
  name: "user",
  components: { 
    TrainIdList,
    MasonryLayout,
    MasonryLayoutTile },
  props: {}
})
export default class User extends Vue {
  example: string = "Test";
  mounted() {}
}
</script>

<style lang="sass"></style>
