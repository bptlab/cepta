<template>
  <table
    class="table"
    :class="{
      dataTable: dataTable,
      'table-striped': striped,
      'table-bordered': bordered,
      'table-hover': hoverable
    }"
  >
    <thead :class="tableHeadClasses">
      <tr>
        <th v-if="showIndices" scope="col">#</th>
        <th
          v-for="(category, categoryIndex) in tableCategories"
          v-bind:key="'header-' + categoryIndex"
          scope="col"
        >
          {{ category }}
        </th>
      </tr>
    </thead>
    <!-- If an array was used as data -->
    <tbody v-if="dataIsArray">
      <tr
        v-for="(row, rowIndex) in sortedTableData"
        v-bind:key="'row-' + rowIndex"
      >
        <th v-if="showIndices" scope="row">{{ row._index || rowIndex }}</th>
        <td
          v-for="(item, itemIndex) in row"
          v-bind:key="'row-' + rowIndex + '-data-' + itemIndex"
        >
          {{ item }}
        </td>
      </tr>
    </tbody>
    <!-- If array was not used as data -->
    <tbody v-else>
      <tr
        v-for="(row, rowIndex) in sortedTableData"
        v-bind:key="'row-' + rowIndex"
      >
        <th v-if="showIndices" scope="row">{{ row._index || rowIndex }}</th>
        <td
          v-for="(category, additionalIndex) in tableCategories"
          v-bind:key="'row-' + rowIndex + '-data-' + additionalIndex"
        >
          {{ row[category] }}
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script lang="ts">
import $ from "jquery";
import "datatables";
import { Component, Prop, Vue } from "vue-property-decorator";

@Component({
  name: "BasicTable"
})
export default class BasicTable extends Vue {
  @Prop({ default: () => [] }) private tableData!: any[];
  @Prop({ default: false }) private dataTable!: boolean;
  @Prop({ default: true }) private showIndices!: boolean;
  @Prop({ default: false }) private striped!: boolean;
  @Prop({ default: false }) private bordered!: boolean;
  @Prop({ default: false }) private hoverable!: boolean;
  @Prop({ default: "_index" }) private sortKey!: string;
  @Prop({ default: "" }) private thead!: string;

  get firstRow(): Array<Number> {
    return this.tableData.length > 0 ? this.tableData[0] : [];
  }

  get dataIsArray(): boolean {
    return Array.isArray(this.firstRow);
  }

  get tableHeadClasses(): string {
    return this.thead.toLowerCase() === "dark"
      ? "thead-dark"
      : this.thead.toLowerCase() === "light"
      ? "thead-light"
      : "";
  }

  get sortedTableData() {
    return this.tableData
      .slice(1)
      .sort((a: { [key: string]: any }, b: { [key: string]: any }) => {
        return a[this.sortKey] > b[this.sortKey]
          ? 1
          : b[this.sortKey] > a[this.sortKey]
          ? -1
          : 0;
      });
  }

  get tableCategories() {
    return this.dataIsArray
      ? this.firstRow
      : Object.keys(this.firstRow).filter(key => {
          return key.toLowerCase().substr(0, 1) !== "_";
        });
  }
  mounted() {
    if (this.dataTable) {
      // Initialize datatable
      ($(".dataTable") as any).DataTable({
        scrollX: true
      });
    }
  }
}
</script>

<style lang="sass">
table
  &.dataTable
    &.no-footer
      border-bottom: 1px solid $border-color
      margin-bottom: 20px

.sorting_asc
  &:focus
    outline: none

.dataTables_wrapper
  overflow: hidden
  padding-bottom: 5px

  .dataTables_length
    color: $default-dark
    float: left

    +to($breakpoint-sm)
      text-align: left


    select
      border: 1px solid $border-color
      border-radius: 2px
      box-shadow: none
      height: 35px
      font-size: 14px
      padding: 5px
      margin-left: 5px
      margin-right: 5px
      color: $default-text-color
      transition: all 0.2s ease-in

  .dataTables_filter
    color: $default-dark
    float: right

    +to($breakpoint-sm)
      text-align: left


    input
      border: 1px solid $border-color
      border-radius: 2px
      box-shadow: none
      height: 35px
      font-size: 14px
      margin-left: 15px
      padding: 5px
      color: $default-text-color
      transition: all 0.2s ease-in

  .dataTables_info
    color: $default-text-color
    float: left

  .dataTables_processing
    color: $default-dark

  .dataTables_paginate
    color: $default-text-color
    float: right

    .paginate_button
      color: $default-text-color !important
      padding: 6px 12px
      border-radius: 2px
      margin-right: 10px
      transition: all 0.2s ease-in-out
      text-decoration: none

      &.next,
      &.previous,
      &.first,
      &.last
        border-radius: 2px
        text-decoration: none

        &:hover,
        &:focus
          color: #fff !important

        &.disabled
          opacity: 0.4
          pointer-events: none

      &:hover
        color: #fff !important
        background: $default-primary

      &.current
        color: #fff !important
        background: $default-primary

        &:hover
          color: $default-white !important
          background: $default-primary

  .status
    width: 5px
    height: 5px
</style>
