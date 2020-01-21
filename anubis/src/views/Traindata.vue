<template>
  <div>
    <h1>{{ id }}</h1>
    <basic-table
      :data-table="false"
      :showIndices="false"
      :striped="true"
      :bordered="true"
      cellspacing="0"
      :table-data="receivedUpdates"
    ></basic-table>
  </div>
</template>

<script lang="ts">
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import RowLayout from "../components/RowLayout.vue";
import RowLayoutRow from "../components/RowLayoutRow.vue";
import BasicTable from "../components/BasicTable.vue";
import Stomp from "webstomp-client";
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "Traindata",
  props: ['id'],
  components: { BasicTable, RowLayoutRow, RowLayout, MasonryLayout, MasonryLayoutTile },
  data() {
    return {
      gridColumn:["StationID", "Station", "old ETA", "Delay", "Cause", "new ETA"],
      receivedUpdates:
          [["StationID", "Station", "old ETA", "Delay", "Cause", "new ETA"],
              ["4202153","Mockava","2019-08-02 13:28:00", "+00:31:00", "Faulty Signal","2019-08-02 13:59:00"],
            ],
      plannedTrainData:
          [{train_id:"43986033", location_id:"4202153", location_name:"Mockava", planned_time:"2019-08-02 13:28:00"},
          ],
      trainDelayNotificationData:
          [{train_id: "43986033", location_id:"4202153", delay_cause: "Faulty Signal", delay:"+00:31:00" },

          ],
    };
  },
  methods: {
    connect(url = "/topic/updates"){
      this.websocket = this.$store.state.websocket;
      this.stompClient = Stomp.over(this.websocket);
      this.stompClient.connect(
          {},
          () =>
              this.stompClient.subscribe(url, update => {
                    console.log(update);
                  },
                  error => {
                    console.log(error);
                  }))
    },
  },
  mounted() {
    this.connect();
  }
})
export default class TrainData extends Vue {

}
</script>

<style lang="sass">
tr td:last-child
  color: red
  font-weight: bolder
</style>
