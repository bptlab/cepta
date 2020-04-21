<template>
  <div id="main-content">
    <input
      v-if="enableSearchBar"
      v:model="search"
      class="form-control"
      type="text"
      placeholder="Search.."
    />
    <grid-table :grid-data="receivedUpdates"></grid-table>
  </div>
</template>

<script lang="ts">
import BasicTable from "../components/BasicTable.vue";
import GridTable from "../components/GridTable.vue";
import { ReplayerModule } from "../store/modules/replayer";
import { Component, Vue, Prop } from "vue-property-decorator";

@Component({
  name: "TransportTableView",
  components: {
    BasicTable,
    GridTable
  }
})
export default class TransportTableView extends Vue {
  @Prop({ default: false }) enableSearchBar!: boolean;
  protected search!: string;

  protected receivedUpdates: { [key: string]: any }[] = [
    {
      ID: "43986033",
      Station: "MusterLocation",
      plannedETA: "2019-08-02 13:28:00",
      delta: 30,
      predictedETA: "2019-08-02 13:58:00"
    },
    {
      ID: "43986033",
      Station: "ExampleTown",
      plannedETA: "2019-08-02 14:38:00",
      delta: 15,
      predictedETA: "2019-08-02 14:53:00"
    },
    {
      ID: "43986033",
      Station: "NoWhereToFind",
      plannedETA: "2019-08-02 15:48:00",
      delta: 10,
      predictedETA: "2019-08-02 15:58:00"
    },
    {
      ID: "43986033",
      Station: "RightHere",
      plannedETA: "2019-08-02 16:20:00",
      delta: 0,
      predictedETA: "2019-08-02 16:20:00"
    },
    {
      ID: "43986033",
      Station: "LeftThere",
      plannedETA: "2019-08-02 17:56:00",
      delta: -10,
      predictedETA: "2019-08-02 17:46:00"
    }
  ];
}
</script>

<style scoped lang="sass">
#mainContent
  padding: 0

.form-control
  width: 100%
  margin-bottom: 20px
</style>
