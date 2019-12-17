<template>
  <div>
    <h1>{{id}}</h1>
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
import Stomp from "webstomp-client";

export default {
  name: "Traindata",
  props: ['id'],
  components: { BasicTable, RowLayoutRow, RowLayout, MasonryLayout, MasonryLayoutTile },
  data() {
    return {
      receivedUpdates:
          [["StationID", "Station", "old ETA", "Delay", "Cause", "new ETA"],
            ],
    };
  },
  methods: {
    connect(url = "/topic/updates"){
      this.websocket = this.$store.state.websocket
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
};
</script>

<style lang="sass">
  tr td:last-child
    color: red
    font-weight: bolder


</style>
