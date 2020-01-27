<template>
  <masonry-layout title="Your trains">
  <masonry-layout-tile section="IDs">
    <input ref="search" class="form-control" id="search" type="number" placeholder="search ... ;)" v-model="search" >
      <basic-table
        :table-data="filteredTableData"
        :show-indices="false"
        :striped="true"
        :bordered="true"
        cellspacing="0"
      />
    <p> Hi {{search}} </p>
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
  example: string = "Test";
  trainIDs:Array<Array<number>> = [[1], [2], [3], [4]];
  search: number = -1;
  mounted() {
  }

  get sectionTitle() {
    return 10
  }
  
  get filteredTableData() {
    return this.trainIDs.filter(this.idFilter);
  }

  idFilter(row:Array<number>) {
    if (String(row[0]).includes(String(this.search)) || this.search == -1)
      return true;
  }
}

</script>

<style lang="sass"></style>
