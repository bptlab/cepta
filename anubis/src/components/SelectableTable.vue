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
          v-bind:id="'row-' + rowIndex + '-data-' + itemIndex"
          v-bind:key="'row-' + rowIndex + '-data-' + itemIndex"
          v-on:click="clickHandler($event)"
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
          v-on:click="clickHandler($event)"
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
import BasicTable from './BasicTable.vue';

@Component({
  name: "SelectableTable",
  components: { 
    BasicTable
  }
})
export default class SelectableTable extends BasicTable {
  @Prop({ default: null }) private clickHandler!: any;
}
</script>
<style scoped>

</style>