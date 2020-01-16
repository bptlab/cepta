<template>
  <div>
    <h1>{{ id }}</h1>
    <basic-table
      :data-table="false"
      :showIndices="false"
      :striped="true"
      :bordered="true"
      cellspacing="0"
      :table-data="receivedUpdates"
    ></basic-table>
  </div>
</template>

<script lang="ts">
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import RowLayout from "../components/RowLayout.vue";
import RowLayoutRow from "../components/RowLayoutRow.vue";
import BasicTable from "../components/BasicTable.vue";
import Stomp from "webstomp-client";
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "Traindata",
  props: ["id"],
  components: {
    BasicTable,
    RowLayoutRow,
    RowLayout,
    MasonryLayout,
    MasonryLayoutTile
  }
})
export default class TrainData extends Vue {

}
</script>

<style lang="sass">
tr td:last-child
  color: red
  font-weight: bolder
</style>
