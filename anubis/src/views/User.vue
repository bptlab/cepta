<template>
  <masonry-layout title="Your trains">
  <masonry-layout-tile section="IDs">
    <input ref="search" class="form-control" id="search" type="number" placeholder="search ... ;)" v-model="search" v-on:input="this.resetSelection">
      <selectable-table
        :table-data="filteredTableData"
        :show-indices="false"
        :striped="true"
        :bordered="true"
        :hoverable="true"
        :headless="true"
        :clickHandler="this.clickHandler"
        cellspacing="0"
      />
      <button v-if="this.selectedTrainId" v-on:click="this.editId" type="button" class="btn btn-block black-btn"> Edit </button>
      <button v-if="this.selectedTrainId" v-on:click="this.deleteId" type="button" class="btn btn-block black-btn"> Delete </button>
      <button v-on:click="this.addId" type="button" class="btn btn-block black-btn"> Add ID </button>
    </masonry-layout-tile>
  <masonry-layout-tile v-bind:section=sectionTitle id="sectionTitle" v-if="this.selectedTrainId">

  </masonry-layout-tile>
  </masonry-layout>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import TrainIdList from "../components/TrainIdList.vue";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import SelectableTable from "../components/SelectableTable.vue";

@Component({
  name: "user",
  components: { 
    TrainIdList,
    MasonryLayout,
    MasonryLayoutTile,
    SelectableTable },
  props: {}
})
export default class User extends Vue {
  example: string = "Test";
  trainIDs: Array<Array<number>> = [ [1], [2], [3], [4]];
  search: (number | null) = null;
  selectedRow: (HTMLTableRowElement | null) = null;
  selectedTrainId: (number | null) = null;
  mounted() {
  }

  get sectionTitle() {
    return this.selectedTrainId ? "Info for train " + this.selectedTrainId : "Please select a Train"
  }
  
  get filteredTableData() {
    return this.trainIDs.filter(this.idFilter);
  }

  idFilter(row:Array<number>) {
    if (String(row[0]).includes(String(this.search)) || !this.search)
      return true;
  }

  clickHandler(record:MouseEvent){
    let selectedElement:EventTarget  = record.target;
    let selectedId:number = Number(selectedElement.innerText);

    this.selectedRow != null ? this.selectedRow.setAttribute("class", "") : null;

    this.selectedRow = document.getElementById(selectedElement.id);
    this.selectedTrainId = selectedId;

    this.selectedRow.setAttribute("class", "selectedRow");
  }

  resetSelection(){
    console.log("hi")
    this.selectedRow != null ? this.selectedRow.setAttribute("class", "") : this.selectedRow = null;
    this.selectedTrainId = null;
  }

  addId(){
    console.log("You clicked the add button!")
  }
  editId(){
    console.log("You clicked the edit button!")
  }
  deleteId(){
    console.log("You clicked the delete button!")
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
