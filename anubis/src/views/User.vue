<template>
  <masonry-layout title="Your trains">
  <masonry-layout-tile section="IDs">
    <input ref="search" class="form-control" id="trainID oder so" type="number" placeholder="search ... ;)">
      <basic-table
        :table-data="trainIDs"
        :show-indices="false"
        :striped="true"
        :bordered="true"
        cellspacing="0"
        :data-table="false"
      />
    </masonry-layout-tile>
  <masonry-layout-tile v-bind:section=sectionTitle id="sectionTitle">

  </masonry-layout-tile>
  </masonry-layout>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import TrainIdList from "../components/TrainIdList.vue";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import BasicTable from "../components/BasicTable.vue";

@Component({
  name: "user",
  components: { 
    TrainIdList,
    MasonryLayout,
    MasonryLayoutTile,
    BasicTable },
  props: {}
})
export default class User extends Vue {
  example: string = "Test";;
  trainIDs:Array<Array<number>> = [[1], [2], [3], [4]];
  mounted() {}

  get sectionTitle() {
    return 10
  }
}

</script>

<style lang="sass"></style>
