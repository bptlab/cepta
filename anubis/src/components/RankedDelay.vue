<template>
  <div>
    <p>
      Delays in their different categories and their ranking will be displayed.
    </p>
    <vue-dropdown class="compare" :config="config"> </vue-dropdown>
    <vue-dropdown class="compare" :config="config"> </vue-dropdown>
    <grid-table></grid-table>
  </div>
</template>

<script lang="ts">
import VueDropdown from "vue-dynamic-dropdown";
import GridTable from "../components/GridTable.vue";
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "RankedDelay",
  components: { VueDropdown, GridTable }
})
export default class RankedDelay extends Vue {
  config = {
    options: [
      {
        value: "24 hours"
      },
      {
        value: "last week"
      }
    ],
    backgroundColor: "white",
    placeholder: "Time window",
    borderRadius: "0px",
    border: "1px solid gray",
    width: 200
  };
}
</script>

<style lang="sass">
.icon-arrow-circle-down
  margin-left: 10px

.compare
  float: left
  margin-bottom: 20px

.expand
  position: absolute
  top: 35px
  background-color: white
  border: 1px solid gray
</style>
