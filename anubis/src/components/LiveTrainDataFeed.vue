<template>
  <div>
    <div id="main-content">
      <input
        ref="search"
        class="form-control"
        type="text"
        placeholder="Search.."
      />
      <grid-table :grid-data="receivedUpdates"></grid-table>
    </div>
  </div>
</template>

<script lang="ts">
import BasicTable from "../components/BasicTable.vue";
import GridTable from "../components/GridTable.vue";
import store from "@/store/index";
import { GrpcModule } from "../store/modules/grpc";
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "LiveTrainDataFeed",
  components: {
    BasicTable,
    GridTable
  }
})
export default class LiveTrainDataFeed extends Vue {
  search: string = "";
  receivedUpdates: { [key: string]: string }[] = [
    {
      trainID: "43986033",
      locationID: "4202153",
      locationName: "MusterLocation",
      plannedETA: "2019-08-02 13:28:00",
      delta: "30",
      predictedETA: "2019-08-02 13:58:00"
    },
    {
      trainID: "43986033",
      locationID: "4202154",
      locationName: "ExampleTown",
      plannedETA: "2019-08-02 14:38:00",
      delta: "15",
      predictedETA: "2019-08-02 14:53:00"
    },
    {
      trainID: "43986033",
      locationID: "4202155",
      locationName: "NoWhereToFind",
      plannedETA: "2019-08-02 15:48:00",
      delta: "10",
      predictedETA: "2019-08-02 15:58:00"
    },
    {
      trainID: "43986033",
      locationID: "4202156",
      locationName: "RightHere",
      plannedETA: "2019-08-02 16:20:00",
      delta: "0",
      predictedETA: "2019-08-02 16:20:00"
    },
    {
      trainID: "43986033",
      locationID: "4202157",
      locationName: "LeftThere",
      plannedETA: "2019-08-02 17:56:00",
      delta: "-10",
      predictedETA: "2019-08-02 17:46:00"
    }
  ];

  /*
  Old websocket connection

  websocket: WebSocket = this.$store.state.websocket;

  pushUpdate(update: any, header: boolean) {
    let obj: Object = JSON.parse(update.body);
    let newInput: Array<string> = [];
    let value: any;

    for (value of Object.entries(obj)) {
      if (header) newInput.push(value[0]);
      else newInput.push(value[1]);
    }

    this.receivedUpdates.push(newInput);
  }
  * ! TODO: Good Filter needed
  get filteredTableData() {
    let tableData: Array<string> = [];
    this.receivedUpdates.filter(tableArray => {
      for (let key in tableArray) {
        console.log(tableArray);
        if (String(tableArray[key]).includes(this.search))
          tableData = tableArray;
      }
    });
    return tableData;
  }
  */

  mounted() {
    this.search = (this.$refs.search as HTMLInputElement).value;
  }
}
</script>

<style scoped lang="sass">
#mainContent
  padding: 0

.form-control
  width: 100%
  margin-bottom: 20px
</style>
