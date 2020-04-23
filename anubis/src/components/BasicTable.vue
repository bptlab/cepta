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
        :class="{
          'row-mode': usesRowSelectionMode,
          selected: isRowSelected(rowIndex)
        }"
      >
        <th v-if="showIndices" scope="row">{{ row._index || rowIndex }}</th>
        <td
          v-for="(item, itemIndex) in row"
          v-bind:key="'row-' + rowIndex + '-data-' + itemIndex"
          @click="handleSelection(rowIndex, itemIndex)"
          :class="{ selectable, selected: isItemSelected(rowIndex, itemIndex) }"
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
        :class="{
          'row-mode': usesRowSelectionMode,
          selected: isRowSelected(rowIndex)
        }"
      >
        <th v-if="showIndices" scope="row">{{ row._index || rowIndex }}</th>
        <td
          v-for="(category, additionalIndex) in tableCategories"
          v-bind:key="'row-' + rowIndex + '-data-' + additionalIndex"
          @click="handleSelection(rowIndex, additionalIndex)"
          :class="{
            selectable,
            selected: isItemSelected(rowIndex, additionalIndex)
          }"
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

export interface Selection {
  rowIndex: number;
  itemIndex: number;
}

@Component({
  name: "BasicTable"
})
export default class BasicTable extends Vue {
  @Prop({ default: () => [] as object[] }) private tableData!: object[];
  @Prop({ default: false }) private dataTable!: boolean;
  @Prop({ default: true }) private showIndices!: boolean;
  @Prop({ default: false }) private striped!: boolean;
  @Prop({ default: false }) private bordered!: boolean;
  @Prop({ default: false }) private hoverable!: boolean;
  @Prop({ default: false }) private selectable!: boolean;
  @Prop({ default: "cell" }) private selectionMode!: string;
  @Prop({ default: "_index" }) private sortKey!: string;
  @Prop({ default: "" }) private thead!: string;
  @Prop({ default: false }) private headless!: boolean;

  protected selection?: Selection | null = null;
  protected dataStartIndex: number = this.headless ? 0 : 1;

  get firstRow(): object | object[] {
    if (this.tableData.length < 1) return [];
    return this.tableData[0];
  }

  get usesRowSelectionMode(): boolean {
    return this.selectionMode.toLowerCase() === "row";
  }

  get dataIsArray(): boolean {
    return Array.isArray(this.firstRow);
  }

  isItemSelected(rowIndex: number, itemIndex: number): boolean {
    if (this.selection?.rowIndex !== rowIndex) return false;
    return this.usesRowSelectionMode
      ? true
      : this.selection?.itemIndex === itemIndex;
  }

  isRowSelected(rowIndex: number): boolean {
    return this.usesRowSelectionMode && this.selection?.rowIndex === rowIndex;
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
      .slice(this.dataStartIndex)
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

  handleSelection(rowIndex: number, itemIndex: number) {
    if (!this.selectable) return;
    this.selection = { rowIndex, itemIndex };
    this.$emit("selection", this.selection);
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

<style lang="sass" scoped>
.table
  color: inherit
  tr
    &.row-mode:hover:not(.selected)
      +theme-color-diff(background-color, bgc-body, 2)
      td.selectable
        +theme(color, c-accent-text)
    td
      padding: 20px
      &.selectable
        cursor: pointer
      &.selectable:hover:not(.selected)
        +theme(color, c-accent-text)
      &.selectable.selected
        +theme(background-color, c-accent-text)

  &.table-striped tbody tr:nth-of-type(odd)
    +theme-color-diff(background-color, bgc-body, 5)

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
        transition: all 0.2s ease-in

    .dataTables_filter
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
        transition: all 0.2s ease-in

    .dataTables_info
      float: left

    .dataTables_paginate
      float: right

      .paginate_button
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

          &.disabled
            opacity: 0.4
            pointer-events: none

        &:hover
          background: $default-primary

        &.current
          background: $default-primary

          &:hover
            background: $default-primary

    .status
      width: 5px
      height: 5px
</style>
