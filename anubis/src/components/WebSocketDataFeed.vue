<template>
  <div>
    <div id="main-content" class="container">
      <div class="row">
        <button id="replayBtn" @click.prevent="replay" class="btn btn-danger">Replay Data!</button>
        <basic-table
            :data-table="true"
            :showIndices="false"
            :striped="true"
            :bordered="true"
            cellspacing="0"
            :table-data="receivedUpdates"
        ></basic-table>
      </div>
    </div>
  </div>
</template>

<script>
import Stomp from "webstomp-client";
import RowLayout from "../components/RowLayout";
import RowLayoutRow from "../components/RowLayoutRow";
import BasicTable from "../components/BasicTable";
import {GrpcModule} from "../store/modules/grpc";

export default {
  name: "WebSocketDataFeed",
  components: { BasicTable, RowLayoutRow, RowLayout },
  data() {
    return {
      receivedUpdates:
        [
        [
          "TrainID",
          "old ETA",
          "Delay",
          "new ETA",
          "cause"
        ],
        [
          1111,
          '10:15',
          30,
          '10:45',
          'snow'
        ],
        [
          1112,
          '12:30',
          30,
          '13:00',
          'wind'
        ],

      ],
      stompClient: null
    };
  },
  methods: {
    replay() {
      GrpcModule.replayData().then()
    },
    connect(socket, url = "/queue/updates"){
      this.stompClient = Stomp.over(socket);

      this.stompClient.connect(
          {},
          frame => {
            console.log(frame);
            this.stompClient.subscribe(url, update => {
              console.log(update);
              this.receivedUpdates.push(update);
            });
          },
          error => {
            console.log(error);
          }
      );
    },
  },
  mounted () {
  }
};
</script>

<style lang="sass">
#main-content
  height: 400px
  overflow-y: auto

#replayBtn
  margin-bottom: 20px

.dataTables_wrapper
  width: 100%

.dataTables_scrollHeadInner, .dataTable
  width: 100% !important
  margin-bottom: 0px !important

.dataTables_scroll
  margin-bottom: 10px


</style>
