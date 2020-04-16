<template>
  <div class="transport-manager-container">
    <masonry-layout>
      <masonry-layout-tile
        class="test"
        section="Manage transports"
        layoutStyle="col-md-12"
      >
        <p>You can use the navigation bar to filter</p>
        <div class="transport-list-container">
          <div class="transport-list noscrollbar">
            <basic-table
              :table-data="formattedMonitoredTransports"
              :show-indices="false"
              :striped="true"
              :bordered="true"
              :hoverable="true"
              :selectable="true"
              selectionMode="row"
              :headless="false"
              v-on:selection="handleSelection"
              cellspacing="0"
            />
          </div>
          <span @click="addTransport()" class="add btn btn-block">
            Add ID
          </span>
        </div>
        <div class="transport-details">
          <span class="selected-transport">{{ selectedID["CEPTA ID"] }}</span>
          <basic-table
            :table-data="trainData"
            :show-indices="false"
            :striped="false"
            :bordered="true"
            cellspacing="0"
          />
          <!--<grid-table :grid-data="trainData"></grid-table>-->
        </div>
        <div class="clearfix"></div>
      </masonry-layout-tile>
    </masonry-layout>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import GridTable from "../components/GridTable.vue";
import BasicTable, { Selection } from "@/components/BasicTable.vue";
@Component({
  name: "TransportManager",
  components: {
    MasonryLayout,
    MasonryLayoutTile,
    BasicTable,
    GridTable
  }
})
export default class TransportManager extends Vue {
  monitoredTransports: {
    id: string;
    route: { start: string; end: string };
  }[] = [
    { id: "CPTA-43986033", route: { start: "Hamburg", end: "Berlin" } },
    { id: "CPTA-3453", route: { start: "Koeln", end: "Muenchen" } },
    { id: "CPTA-123", route: { start: "Hamburg", end: "Berlin" } },
    { id: "CPTA-345", route: { start: "Paris", end: "London" } },
    { id: "CPTA-43986033", route: { start: "Hamburg", end: "Berlin" } },
    { id: "CPTA-3453", route: { start: "Koeln", end: "Muenchen" } },
    { id: "CPTA-123", route: { start: "Hamburg", end: "Berlin" } },
    { id: "CPTA-345", route: { start: "Paris", end: "London" } },
    { id: "CPTA-43986033", route: { start: "Hamburg", end: "Berlin" } },
    { id: "CPTA-3453", route: { start: "Koeln", end: "Muenchen" } },
    { id: "CPTA-123", route: { start: "Hamburg", end: "Berlin" } },
    { id: "CPTA-345", route: { start: "Paris", end: "London" } }
  ];

  get formattedMonitoredTransports(): { [key: string]: string }[] {
    return this.monitoredTransports.flatMap(t => {
      return { "CEPTA ID": t.id, Route: `${t.route.start} - ${t.route.end}` };
    });
  }

  protected selectedRow: number | null = null;
  protected selectedID: { [key: string]: string } = {};

  handleSelection(selection: Selection) {
    this.selectedRow = selection.rowIndex;
    this.selectedID = this.formattedMonitoredTransports[selection.rowIndex];
  }

  addTransport() {
    // TODO: Implement popup
  }
  editTransport() {
    // TODO: Implement popup
  }
  deleteTransport() {
    // TODO: Implement popup
  }

  get trainData(): { [key: string]: string }[] {
    // here should be the request of the actual data for the selected train id
    return [
      {
        locationID: "4202153",
        locationName: "MusterLocation",
        plannedETA: "2019-08-02 13:28:00",
        delay: "30",
        predictedETA: "2019-08-02 13:58:00"
      },
      {
        locationID: "4202154",
        locationName: "ExampleTown",
        plannedETA: "2019-08-02 14:38:00",
        delay: "15",
        predictedETA: "2019-08-02 14:53:00"
      },
      {
        locationID: "4202155",
        locationName: "NoWhereToFind",
        plannedETA: "2019-08-02 15:48:00",
        delay: "10",
        predictedETA: "2019-08-02 15:58:00"
      },
      {
        locationID: "4202156",
        locationName: "RightHere",
        plannedETA: "2019-08-02 16:20:00",
        delay: "0",
        predictedETA: "2019-08-02 16:20:00"
      },
      {
        locationID: "4202157",
        locationName: "LeftThere",
        plannedETA: "2019-08-02 17:56:00",
        delay: "-10",
        predictedETA: "2019-08-02 17:46:00"
      }
    ];
  }
}
</script>

<style lang="sass" scoped>
.transport-manager-container
  position: relative
  width: 100%

  .transport-list-container, .transport-details
    top: 0
    padding: 20px

  .transport-list-container
    width: 30%
    float: left
    min-width: 300px
    .add
      margin-top: 10px
      +theme-color-diff(background-color, bgc-body, 20)
      color: inherit
      &:hover
        +theme(color, c-accent-text)

    .transport-list
      height: 500px
      overflow-y: scroll
      tr
        cursor: pointer

  .transport-details
    width: 70%
    float: right
    max-width: calc(100% - 300px)

    .selected-transport
      display: inline-block
      padding-bottom: 20px
      font-size: 1.4rem
      font-weight: bolder

  .clearfix
    clear: both
</style>
