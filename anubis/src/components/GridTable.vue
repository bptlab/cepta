<template>
  <table class="table-striped table-interactive fullsize">
    <thead>
      <tr>
        <th
          v-for="key in gridColumns"
          :key="'header' + key"
          @click="sortBy(key)"
          :class="{ active: sortKey == key }"
        >
          {{ key | capitalize }}
          <span
            class="arrow"
            :class="sortKey == key && sortOrder > 0 ? 'asc' : 'dsc'"
          >
          </span>
        </th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(entry, index) in filteredGridData" :key="index">
        <td
          v-for="key in gridColumns"
          :key="index.toString() + key"
          :class="
            key == 'delay'
              ? entry[key] <= 0
                ? 'delay-green'
                : 'delay-red'
              : 'default'
          "
        >
          <span v-if="key == 'delay'">
            {{ entry[key] > 0 ? "+" : "" }}
            {{ !(entry[key] >= 0) ? "-" : "" }}{{ Math.abs(entry[key]) }}
            Minutes
          </span>
          <span v-else>{{ entry[key] }}</span>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";
import MasonryLayout from "@/components/MasonryLayout.vue";
import MasonryLayoutTile from "@/components/MasonryLayoutTile.vue";

@Component({
  name: "GridTable",
  filters: {
    capitalize(str: string) {
      return str.charAt(0).toUpperCase() + str.slice(1);
    }
  },
  components: {
    MasonryLayout,
    MasonryLayoutTile
  }
})
export default class GridTable extends Vue {
  @Prop({
    default: () => [
      {
        trainID: "none",
        locationID: "none",
        locationName: "none",
        plannedETA: "none",
        delay: "0",
        predictedETA: "none"
      }
    ]
  })
  private gridData!: { [key: string]: string }[];
  @Prop({ default: "" }) private filterKey!: string;
  sortKey: string = "";
  sortOrder: number = 1;

  get filteredGridData(): { [key: string]: string }[] {
    let filterKey = this.filterKey && this.filterKey.toLowerCase();
    let filteredGridData = this.gridData;
    if (filterKey) {
      filteredGridData = this.gridData.filter(function(row) {
        return Object.keys(row).some(function(key) {
          return (
            String(row[key])
              .toLowerCase()
              .indexOf(filterKey) > -1
          );
        });
      });
    }

    let sortKey =
      this.sortKey || this.gridColumns.length > 0 ? this.gridColumns[0] : "";
    filteredGridData = filteredGridData.slice().sort((a, b) => {
      let ak = a[this.sortKey];
      let bk = b[this.sortKey];
      return (ak === bk ? 0 : ak > bk ? 1 : -1) * this.sortOrder;
    });
    return filteredGridData;
  }

  get gridColumns(): string[] {
    return this.gridData.length > 0 ? Object.keys(this.gridData[0]) : [];
  }

  sortBy(key: string) {
    this.sortKey = key;
    this.sortOrder = (this.sortOrder || 1) * -1;
  }
}
</script>

<style lang="sass">
fullsize
  width: 100%
body
  font-size: 14px
  color: black

table
   border: 2px solid black
   border-radius: 3px
   background-color: #fff

th
   background-color: black
   color: white
   cursor: pointer
   -webkit-user-select: none
   -moz-user-select: none
   -ms-user-select: none
   user-select: none

td
   background-color: #f9f9f9

td.delay-red
  color: red
  font-weight: bold

td.delay-green
  color: green



th.active
   color: #fff

th.active .arrow
  opacity: 1

.arrow
   display: inline-block
   vertical-align: middle
   width: 0
   height: 0
   margin-left: 5px
   opacity: 0.33

   &.asc
    border-left: 4px solid transparent
    border-right: 4px solid transparent
    border-bottom: 4px solid #fff

   &.dsc
    border-left: 4px solid transparent
    border-right: 4px solid transparent
    border-top: 4px solid #fff
</style>
