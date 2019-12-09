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
        [["TrainID", "Station", "old ETA", "Delay", "Cause", "new ETA"],
        ],
    };
  },
  methods: {
    connect(stomp, url = "/topic/updates"){
      this.stompClient = stomp;
      this.stompClient.connect(
          {},
          () => {
            this.stompClient.subscribe(url, update => {
              console.log(update);
              // this.pushUpdate(update);
            });
          },
          error => {
            console.log(error);
          }
      );
    },
    pushUpdate(update){
      let obj = JSON.parse(update.body);
      let newInput =  new Array();

      for (let value of Object.entries(obj)) {
        newInput.push(value[1]);
      }

      this.receivedUpdates.push(newInput);
    },
    replay() {
      GrpcModule.replayData().then()
    },
    disconnect(){
      this.stompClient.disconnect();
    }
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
