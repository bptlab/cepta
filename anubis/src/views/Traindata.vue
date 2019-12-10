<template>
  <div>
    <h1><!-- TODO: Train Number --> {{id}}</h1>
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

<script>
import MasonryLayout from "@/components/MasonryLayout";
import MasonryLayoutTile from "@/components/MasonryLayoutTile";
import RowLayout from "../components/RowLayout";
import RowLayoutRow from "../components/RowLayoutRow";
import BasicTable from "../components/BasicTable";

export default {
  name: "Traindata",
  props: ['id'],
  components: { BasicTable, RowLayoutRow, RowLayout, MasonryLayout, MasonryLayoutTile },
  data() {
    return {
      receivedUpdates:
          [["StationID", "Station", "old ETA", "Delay", "Cause", "new ETA"],
            [1, "Berlin", "10:30", "00:30", "Storm", "11:00"],
            [2, "Hamburg", "13:30", "00:25", "Wind", "13:55"]
          ],
    };
  },
  methods: {
    connect(url = "/topic/updates") {
      this.stompClient = this.$store.state.websocket;
      this.stompClient.connect(
          {},
          () =>
              this.stompClient.subscribe(url, update => {
                    console.log(update);
                  },
                  error => {
                    console.log(error);
                  })
      )
    },
  },
  mounted() {
    this.connect();

    if(!this.id){
      this.$router.path(error);
    }
  }
};
</script>

<style lang="sass">
  tr td:last-child
    color: red
    font-weight: bolder


</style>
