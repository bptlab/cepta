<template>
  <masonry-layout>
    <masonry-layout-tile layoutStyle='{"col-ms-12": true}'>
      <div>
        <h1>{{ id }}</h1>
        <form id="search">
          Search <input name="query" v-model="searchQuery" />
        </form>
        <grid-table
          :grid-data="plannedTrainData"
          :filter-key="searchQuery"
        ></grid-table>
      </div>
    </masonry-layout-tile>
  </masonry-layout>
</template>

<script lang="ts">
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "@/components/MasonryLayoutTile.vue";
import RowLayout from "../components/RowLayout.vue";
import RowLayoutRow from "../components/RowLayoutRow.vue";
import GridTable from "../components/GridTable.vue";
import { Component, Prop, Vue } from "vue-property-decorator";
import gql from "graphql-tag";
@Component({
  name: "TraindataGrid",
  components: {
    GridTable,
    RowLayoutRow,
    RowLayout,
    MasonryLayout,
    MasonryLayoutTile
  }
  // apollo: {
  //   // Simple query that will update the 'hello' vue property
  //   plannedTrainData: gql`
  //     query {
  //       plannedTrainData( id: this.id) {
  //         locationID
  //         locationName
  //         plannedETA(unit: DATE)
  //         delay(unit: SECONDS)
  //         predictedETA(unit: DATE)
  //         }
  //     }
  //   `
  // }
})
export default class TraindataGrid extends Vue {
  @Prop({ default: "" }) private id?: number;
  plannedTrainData: { [key: string]: string }[] = [
    {
      trainID: "43986033",
      locationID: "4202153",
      locationName: "MusterLocation",
      plannedETA: "2019-08-02 13:28:00",
      delay: "30",
      predictedETA: "2019-08-02 13:58:00"
    },
    {
      trainID: "43986033",
      locationID: "4202154",
      locationName: "ExampleTown",
      plannedETA: "2019-08-02 14:38:00",
      delay: "15",
      predictedETA: "2019-08-02 14:53:00"
    },
    {
      trainID: "43986033",
      locationID: "4202155",
      locationName: "NoWhereToFind",
      plannedETA: "2019-08-02 15:48:00",
      delay: "10",
      predictedETA: "2019-08-02 15:58:00"
    },
    {
      trainID: "43986033",
      locationID: "4202156",
      locationName: "RightHere",
      plannedETA: "2019-08-02 16:20:00",
      delay: "0",
      predictedETA: "2019-08-02 16:20:00"
    },
    {
      trainID: "43986033",
      locationID: "4202157",
      locationName: "LeftThere",
      plannedETA: "2019-08-02 17:56:00",
      delay: "-10",
      predictedETA: "2019-08-02 17:46:00"
    }
  ];
  searchQuery: string = "";
}
</script>

<style lang="sass">
/*tr td:last-child
  color: red
  font-weight: bolder*/
</style>
