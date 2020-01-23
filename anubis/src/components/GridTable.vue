<template>
  <table>
    <thead>
    <tr>
      <th v-for="key in gridColumns"
          @click="sortBy(key)"
          :class="{ active: sortKey == key }">
        {{ key | capitalize }}
        <span class="arrow" :class="sortOrders[key] > 0 ? 'asc' : 'dsc'">
          </span>
      </th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="entry in filteredGridData">
      <td v-for="key in gridColumns"
          v-bind:class="key=='delay'? (entry[key]<=0? 'delay-green':'delay-red') : 'default'">
        {{entry[key]}}
        <span v-if="key=='delay'"> Minutes</span>
      </td>
    </tr>
    </tbody>
  </table>
</template>

<script lang="ts">

import {Component, Prop, Vue} from "vue-property-decorator";

@Component({
  name: "GridTable",
  filters: {
    capitalize(str: string) {
      return str.charAt(0).toUpperCase() + str.slice(1)
    }
  }

})

export default class GridTable extends Vue {
  @Prop({ default: () => [] }) private gridData!: any[];
  @Prop({ default: () => [] }) private gridColumns!: any[];
  @Prop({ default: "" }) private filterKey!: string;
  sortKey: string = '';
  sortOrders: { [key:string]:number; } = {};

  constructor() {
    super();
    let sortOrders: { [key:string]:number; } = {};
    this.gridColumns.forEach(function ( key: string ) {
      sortOrders[key] = 1
    });

    this.sortKey = this.gridColumns[0];
    this.sortOrders = sortOrders;
  }



  //computed
  get filteredGridData() {
    let sortKey = this.sortKey;
    let filterKey = this.filterKey && this.filterKey.toLowerCase();
    let order = this.sortOrders[sortKey] || 1;
    let gridData = this.gridData;
    if (filterKey) {
      gridData = gridData.filter(function (row) {
        return Object.keys(row).some(function (key) {
          return String(row[key]).toLowerCase().indexOf(filterKey) > -1
        })
      })
    }
    if (sortKey) {
      gridData = gridData.slice().sort(function (a, b) {
        a = a[sortKey];
        b = b[sortKey];
        return (a === b ? 0 : a > b ? 1 : -1) * order
      })
    }
    return gridData
  }



  //method
  sortBy(key: string) {
    this.sortKey = key;
    this.sortOrders[key] = this.sortOrders[key] * -1
  }

};
</script>

<style lang="sass">
  body
    font-size: 14px
    color: #444

  table
     border: 2px solid #42b983
     border-radius: 3px
     background-color: #fff

  th
     background-color: #42b983
     color: rgba(255, 255, 255, 0.66)
     cursor: pointer
     -webkit-user-select: none
     -moz-user-select: none
     -ms-user-select: none
     user-select: none

  td
     background-color: #f9f9f9

  td.delay-red
    color: red

  td.delay-green
    color: green

  th, td
     min-width: 120px
     padding: 10px 20px

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