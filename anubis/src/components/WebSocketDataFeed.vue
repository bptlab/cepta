<template>
  <div>
    <div id="main-content" class="container">
      <button id="replayBtn" @click.prevent="replay" class="btn btn-danger">
        Replay Data!
      </button>
      <input
        ref="search"
        class="form-control"
        id="myInput"
        type="text"
        placeholder="Search.."
      />
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

<script lang="js">
import RowLayout from "../components/RowLayout";
import RowLayoutRow from "../components/RowLayoutRow";
import BasicTable from "../components/BasicTable";
import {GrpcModule} from "../store/modules/grpc";
import Stomp from "webstomp-client";

export default {
  name: "WebSocketDataFeed",
  components: { BasicTable, RowLayoutRow, RowLayout },
  data() {
    return {
      search: "",
      receivedUpdates: []
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
              if (this.receivedUpdates.length < 1)
                this.pushUpdate(update, true);
              this.pushUpdate(update, false);
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
      GrpcModule.replayData().then()
    }
  },
  computed: {
    filteredTableData() {
      this.search;
      this.receivedUpdates.filter((tableArray) => {
        for (let key in tableArray) {
          if (String(tableArray[key]).includes(search) || String(tableArray[key]).includes("id"))
            return tableArray;
        };
      })
    }
  },
  mounted() {
    this.connect();
    this.search = this.$refs.search.value;
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
