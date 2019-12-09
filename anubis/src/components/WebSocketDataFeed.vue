<template>
  <div>
    <div id="main-content" class="container">
      <button id="replayBtn" @click.prevent="replay" class="btn btn-danger">Replay Data!</button>
      <input @keyup="filter" ref="search" class="form-control" id="myInput" type="text" placeholder="Search..">
      <div class="row">
        <basic-table ref="table"
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

<script>
import RowLayout from "../components/RowLayout";
import RowLayoutRow from "../components/RowLayoutRow";
import BasicTable from "../components/BasicTable";
import {GrpcModule} from "../store/modules/grpc";

export default {
  name: "WebSocketDataFeed",
  components: { BasicTable, RowLayoutRow, RowLayout },
  data() {
    return {
      receivedUpdates: [],
      filteredTableData: []
    };
  },
  methods: {
    connect(url = "/topic/updates"){
      this.stompClient = this.$store.state.websocket;
      this.stompClient.connect(
          {},
          () =>
            this.stompClient.subscribe(url, update => {
              console.log(update);
              this.filter();
              if (this.receivedUpdates.length < 1){
                this.pushUpdate(update, true);
              } else {
                this.pushUpdate(update, false);
              }
          },
          error => {
            console.log(error);
          })
      )
    },
    pushUpdate(update, header){
      let obj = JSON.parse(update.body);
      let newInput =  [];

      for (let value of Object.entries(obj)) {
        if (header) newInput.push(value[0]);
        else newInput.push(value[1]);
      }

      this.receivedUpdates.push(newInput);
    },
    replay() {
      // this.connect();
      GrpcModule.replayData().then()
    },
    filter(){
      let search = this.$refs.search.value;

      this.filteredTableData = this.receivedUpdates.filter((tableArray) => {
        for (let key in tableArray) {
          if (String(tableArray[key]).includes(search) || String(tableArray[key]).includes("id"))
            return tableArray;
        };
      })
    }
  },
  mounted() {
    this.connect();
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
