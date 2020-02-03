<template>
  <masonry-layout title="Your trains">
    <masonry-layout-tile section="IDs" layoutStyle='{"col-ms-2": true}'>
      <input
        ref="search"
        class="form-control"
        id="search"
        type="number"
        placeholder="search ..."
        v-model="search"
        v-on:input="this.resetSelection"
      />
      <selectable-table
        :table-data="filteredTableData"
        :show-indices="false"
        :striped="true"
        :bordered="true"
        :hoverable="true"
        :headless="true"
        :clickHandler="this.rowClickHandler"
        cellspacing="0"
      />
      <button
        v-if="this.selectedTrainId"
        v-on:click="this.editId"
        type="button"
        class="btn btn-block black-btn"
      >
        Edit
      </button>
      <button
        v-if="this.selectedTrainId"
        v-on:click="this.deleteId"
        type="button"
        class="btn btn-block black-btn"
      >
        Delete
      </button>
      <button
        v-on:click="this.addId"
        type="button"
        class="btn btn-block black-btn"
      >
        Add ID
      </button>
    </masonry-layout-tile>
    <masonry-layout-tile
      v-bind:section="sectionTitle"
      layoutStyle='{"col-ms-10": true}'
      id="sectionTitle"
      v-if="this.selectedTrainId"
    >
      <grid-table :grid-data="trainData" :filter-key="search"></grid-table>
    </masonry-layout-tile>
  </masonry-layout>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import SelectableTable from "../components/SelectableTable.vue";
import GridTable from "../components/GridTable.vue";

@Component({
  name: "user",
  components: {
    MasonryLayout,
    MasonryLayoutTile,
    SelectableTable,
    GridTable
  },
  props: {}
})
export default class User extends Vue {
  example: string = "Test";
  trainIDs: Array<Array<number>> = [[43986033], [2], [3], [4]];
  search: number | null = null;
  selectedRow: HTMLTableRowElement | null = null;
  selectedTrainId: number | null = null;
  mounted() {}

  get sectionTitle() {
    return this.selectedTrainId
      ? "Info for train " + this.selectedTrainId
      : "Please select a Train";
  }

  get filteredTableData() {
    return this.trainIDs.filter(this.idFilter);
  }

  idFilter(row: Array<number>) {
    if (String(row[0]).includes(String(this.search)) || !this.search)
      return true;
  }

  rowClickHandler(record: MouseEvent) {
    let targetElement: HTMLElement | null = record.target as HTMLElement;
    let elementId = targetElement != null ? targetElement.id : null;
    let selectedElement: HTMLTableRowElement = document.getElementById(
      targetElement.id
    ) as HTMLTableRowElement;
    let selectedId: number = Number(selectedElement.innerText);

    if (this.selectedRow != selectedElement) {
      this.resetSelection();
      this.selectedRow = selectedElement;
      this.selectedTrainId = selectedId;
      this.selectedRow.setAttribute("class", "selectedRow");
    } else {
      this.resetSelection();
    }
  }

  resetSelection() {
    if (this.selectedRow != null) {
      this.selectedRow.setAttribute("class", "");
      this.selectedRow = null;
    }
    this.selectedTrainId = null;
  }

  addId() {
    // add id to user
    console.log("You clicked the add button!");
  }
  editId() {
    // change id -> remove old from user and add new id to user
    console.log("You clicked the edit button!");
  }
  deleteId() {
    // remove id from user
    console.log("You clicked the delete button!");
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

<style lang="sass">
.selectedRow
  background-color: red
.black-btn
  background-color: black
  color: white
</style>
