<template>
  <div>
    <h1>{{ id }}</h1>
    <basic-table
      :data-table="false"
      :showIndices="false"
      :striped="true"
      :bordered="true"
      cellspacing="0"
      :table-data="testDelays"
    ></basic-table>
  </div>
</template>

<script lang="ts">
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import RowLayout from "../components/RowLayout.vue";
import RowLayoutRow from "../components/RowLayoutRow.vue";
import BasicTable from "../components/BasicTable.vue";
import { Component, Prop, Vue } from "vue-property-decorator";
import { AppModule } from "@/store/modules/app";

@Component({
  name: "Traindata",
  components: {
    BasicTable,
    RowLayoutRow,
    RowLayout,
    MasonryLayout,
    MasonryLayoutTile
  }
})
export default class TrainData extends Vue {
  @Prop({ default: "" }) private id?: number;

  get testDelays() {
    return AppModule.delays.map(e => e.toObject()).reverse();
  }

  receivedUpdates: Array<Array<string>> = [
    ["StationID", "Station", "old ETA", "Delay", "Cause", "new ETA"],
    [
      "4202153",
      "Mockava",
      "2019-08-02 13:28:00",
      "+00:31:00",
      "Faulty Signal",
      "2019-08-02 13:59:00"
    ]
  ];
  plannedTrainData: { [key: string]: string }[] = [
    {
      train_id: "43986033",
      location_id: "4202153",
      location_name: "Mockava",
      planned_time: "2019-08-02 13:28:00"
    }
  ];
  trainDelayNotificationData: { [key: string]: string }[] = [
    {
      train_id: "43986033",
      location_id: "4202153",
      delay_cause: "Faulty Signal",
      delay: "+00:31:00"
    }
  ];
}
</script>

<style lang="sass">
tr td:last-child
  color: red
  font-weight: bolder
</style>
