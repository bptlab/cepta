<template>
  <table class="grid-table table-striped table-interactive">
    <thead>
      <tr>
        <th
          v-for="key in gridColumns"
          :key="'header' + key"
          @click="sortBy(key)"
          :class="{ active: sortKey == key }"
        >
          <span>
            {{ key | capitalize }}
            <span
              class="icon"
              :class="
                sortKey == key && sortOrder > 0
                  ? 'icon-angle-up'
                  : 'icon-angle-down'
              "
            >
            </span>
          </span>
        </th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(entry, index) in filteredGridData" :key="index">
        <td
          v-for="key in gridColumns"
          :key="index.toString() + key"
          :class="
            key == delayKey
              ? entry[key] <= 0
                ? 'delay-green'
                : 'delay-red'
              : 'default'
          "
        >
          <span v-if="key == delayKey">
            {{ entry[key] | formDelay }}
          </span>
          <span v-else>{{ entry[key] }}</span>
        </td>
        <router-link :to="{ name: 'map', query: { track: entry['ID'] } }">
          <td class="track"><span class="icon icon-target"></span> Track</td>
        </router-link>
      </tr>
    </tbody>
  </table>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";
import MasonryLayout from "@/components/MasonryLayout.vue";
import MasonryLayoutTile from "@/components/MasonryLayoutTile.vue";
import { formatDelay } from "../utils";

@Component({
  name: "GridTable",
  filters: {
    capitalize(str: string): string {
      return str.charAt(0).toUpperCase() + str.slice(1);
    },
    formDelay(delay: number): string {
      return formatDelay(delay);
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
        delta: "0",
        predictedETA: "none"
      }
    ]
  })
  gridData!: { [key: string]: string }[];
  // TODO: Make filterKey v-model compatible
  @Prop({ default: "" }) private filterKey!: string;
  @Prop({ default: "Delay" }) private delayKey!: string;
  sortKey: string = "delta";
  sortOrder: number = -1;

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

<style lang="sass" scoped>
.grid-table
  width: 100%

  th, td
    text-align: left
    vertical-align: middle
    padding: 8px
    border: 1px solid gray

    &.track>a
      text-align: center
      margin-right: 5px
      float: right

    &.delay-red
      +theme(color, c-delay-warning)
      font-weight: bold
  th
    cursor: pointer
    -webkit-user-select: none
    -moz-user-select: none
    -ms-user-select: none
    user-select: none

    .icon
      margin-left: 10px
      margin-right: 5px
      opacity: 0.2

    &.active
      .icon
        font-weight: bold
        opacity: 1

  th:last-child
    border: none
    width: 80px

  a .track
    +theme(color, c-table-text)

  .track
    +theme(background-color, bgc-table-button)
    +theme-color-diff(border-color, bgc-table-button, 10)
    width: 80px

    &:hover
      +theme-color-diff(background-color, bgc-table-button, 10)
</style>
