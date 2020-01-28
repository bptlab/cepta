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
    let target:EventTarget  = record.target;
    let selectedElement: HTMLTableRowElement = document.getElementById(target.id);
    let selectedId:number = Number(selectedElement.innerText);


    if (this.selectedRow != selectedElement){
      this.resetSelection();
      this.selectedRow = selectedElement;
      this.selectedTrainId = selectedId;
      this.selectedRow.setAttribute("class", "selectedRow");
    } else {
      this.resetSelection();
    }

  }

  resetSelection(){
    if (this.selectedRow != null ){
      this.selectedRow.setAttribute("class", "");
      this.selectedRow = null;
    }
    this.selectedTrainId = null;
  }

  addId(){
    // add id to user
    console.log("You clicked the add button!")
  }
  editId(){
    // change id -> remove old from user and add new id to user
    console.log("You clicked the edit button!")
  }
  deleteId(){
    // remove id from user
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
