<template>
  <div>
    <h1>{{ id }}</h1>
    <form id="search">Search <input name="query" v-model="searchQuery" /></form>
    <grid-table
      :grid-data="plannedTrainData"
      :filter-key="searchQuery"
    ></grid-table>
  </div>
</template>

<script lang="ts">
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "@/components/MasonryLayoutTile.vue";
import RowLayout from "../components/RowLayout.vue";
import RowLayoutRow from "../components/RowLayoutRow.vue";
import GridTable from "../components/GridTable.vue";
import { Component, Prop, Vue } from "vue-property-decorator";

@Component({
  name: "TraindataGrid",
  components: {
    GridTable,
    RowLayoutRow,
    RowLayout,
    MasonryLayout,
    MasonryLayoutTile
  }
})
export default class TraindataGrid extends Vue {
  @Prop({ default: "" }) private id?: number;

  plannedTrainData: { [key: string]: string }[] = [
    {
      trainID: "43986033",
      locationID: "4202153",
      plannedETA: "2019-08-02 13:28:00",
      delay: "30",
      predictedETA: "2019-08-02 13:58:00"
    },
    {
      trainID: "43986033",
      locationID: "4202154",
      plannedETA: "2019-08-02 14:38:00",
      delay: "15",
      predictedETA: "2019-08-02 14:53:00"
    },
    {
      trainID: "43986033",
      locationID: "4202155",
      plannedETA: "2019-08-02 15:48:00",
      delay: "10",
      predictedETA: "2019-08-02 15:58:00"
    },
    {
      trainID: "43986033",
      locationID: "4202156",
      plannedETA: "2019-08-02 16:20:00",
      delay: "0",
      predictedETA: "2019-08-02 16:20:00"
    },
    {
      trainID: "43986033",
      locationID: "4202157",
      plannedETA: "2019-08-02 17:56:00",
      delay: "-10",
      predictedETA: "2019-08-02 17:46:00"
    }
  ];
  searchQuery: string = "";

  /*
  computed: {
    preFilteredTrainDelayNotificationData: function () {
      return this.trainDelayNotificationData.filter(function (delayNotification, index, delayNotificationData) {
        return delayNotification.train_id.equal(this)
      })
    }
  },

  methods: {
    updateAllTrainData() {
      var updatedTrainData = this.plannedTrainData;
      updatedTrainData.forEach(function (trainData, index) {
        var matchingDelay = this.preFilteredTrainDelayNotificationData.find(
            (delayNotification) => delayNotification.location_id.equal(trainData.location_id))
        trainData.push(matchingDelay.delay, matchingDelay.delay_cause)
      })
      return updatedTrainData
    }
  }
   */
}
</script>

<style lang="sass">
/*tr td:last-child
  color: red
  font-weight: bolder*/
</style>
