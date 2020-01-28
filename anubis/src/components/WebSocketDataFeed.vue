<template>
  <div>
    <div id="main-content" class="container">
      <input ref="search" class="form-control" id="myInput" type="text" placeholder="Search..">
      <div class="row">
        <basic-table
          ref="table"
          :showIndices="false"
          :striped="true"
          :bordered="true"
          cellspacing="0"
          :table-data="filteredTableData"
        ></basic-table>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import RowLayout from "../components/RowLayout.vue";
import RowLayoutRow from "../components/RowLayoutRow.vue";
import BasicTable from "../components/BasicTable.vue";
import store from '@/store/index';
import {GrpcModule} from "../store/modules/grpc";
import Stomp, {SubscribeHeaders} from "webstomp-client";
import { Component, Vue } from 'vue-property-decorator'

@Component({
  name: "WebSocketDataFeed",
  components: { BasicTable, RowLayoutRow, RowLayout }
})
export default class WebSocketDataFeed extends Vue {
  search : string =  "";
  receivedUpdates: Array<Array<string>> = [];
  websocket : WebSocket = this.$store.state.websocket;

  pushUpdate(update : any, header:boolean){
    let obj : Object = JSON.parse(update.body);
    let newInput : Array <string> =  [];
    let value : any;

    for (value of Object.entries(obj)) {
      if (header) newInput.push(value[0]);
      else newInput.push(value[1]);
    }

    this.receivedUpdates.push(newInput);
  }

  // computed
  get filteredTableData() {
    let tableData : Array<string> = []
    this.receivedUpdates.filter((tableArray) => {
      for (let key in tableArray) {
        if (String(tableArray[key]).includes(this.search))
          tableData =  tableArray;
      };
    })
    return tableData;
  }

  // Mount
  mounted() {
    this.search = (<HTMLInputElement>this.$refs.search).value;
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
