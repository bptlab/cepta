<template>
  <div>
    <h1>{{id}}</h1>
    <form id="search">
      Search <input name="query" v-model="searchQuery">
    </form>
    <grid-table
        :grid-columns="gridColumns"
        :grid-data="plannedTrainData"
        :filter-key="searchQuery"
    ></grid-table>
  </div>
</template>

<script>
import MasonryLayout from "@/components/MasonryLayout";
import MasonryLayoutTile from "@/components/MasonryLayoutTile";
import RowLayout from "../components/RowLayout";
import RowLayoutRow from "../components/RowLayoutRow";
import GridTable from "../components/GridTable";

export default {
  name: "TraindataGrid",
  props: ['id'],
  components: { GridTable, RowLayoutRow, RowLayout, MasonryLayout, MasonryLayoutTile },
  data() {
    return {
      gridColumns:["trainID", "locationID", "plannedETA",],

      plannedTrainData:
          [{trainID:"43986033", locationID:"4202153", plannedETA:"2019-08-02 13:28:00"},
            {trainID:"43986033", locationID:"4202154", plannedETA:"2019-08-02 13:38:00"},
            {trainID:"43986033", locationID:"4202155", plannedETA:"2019-08-02 13:48:00"},
          ],
      /*
      trainDelayNotificationData:
          [{train_id: "43986033", location_id:"4202153", delay_cause: "Faulty Signal", delay:"+00:31:00" },
          ],
          */
      searchQuery: '',
    };
  },
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
};
</script>

<style lang="sass">
  tr td:last-child
    color: red
    font-weight: bolder


</style>
