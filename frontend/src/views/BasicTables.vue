<template>
  <div class="container-fluid">
    <row-layout title="Basic Tables">
      <!-- Simple Table -->
      <row-layout-row section="Simple Table">
        <p>
          Using the most basic table markup, here’s how you can use basic
          <code class="highlighter-rouge">.table</code> based Bootstrap tables
          as a component.
          <br />
          You can provide the data for the
          <code class="highlighter-rouge">&lt;basictable&gt;</code>
          component with
          <code class="highlighter-rouge">:table-data="myTableData"</code>
          where
          <code class="highlighter-rouge">myTableData</code>
          can be an Array like [[...row1...],[...row2...],...] or [{ColumnName1
          : row1Value, ...},{ColumnName1 : row2Value, ...},...].
          <br />
          For implementation have a look at the source code and always remember
          that you should
          <strong>change this theme to match your needs.</strong>, <br /><br />
          <strong>All table styles are inherited in Bootstrap 4</strong>,
          meaning any nested tables will be styled in the same manner as the
          parent.
        </p>
        <basic-table :table-data="myTableData" />
      </row-layout-row>

      <!-- Table head options -->
      <row-layout-row section="Table head options">
        <p>
          Similar to tables and dark tables, you can set the prop
          <code class="highlighter-rouge">thead="light"</code> or
          <code class="highlighter-rouge">thead="dark"</code>.
        </p>
        <basic-table :table-data="myTableData" thead="dark" />
      </row-layout-row>

      <!-- Striped rows -->
      <row-layout-row section="Striped rows">
        <p>
          You can also set the
          <code class="highlighter-rouge">:striped="true"</code>
          prop for a table with striped rows.
        </p>
        <basic-table
          :table-data="myTableData"
          :striped="true"
          :show-indices="false"
        />
      </row-layout-row>

      <!-- Bordered table -->
      <row-layout-row section="Bordered table">
        <p>
          You can also set the
          <code class="highlighter-rouge">:bordered="true"</code>
          prop for a bordered table.
        </p>
        <basic-table
          :table-data="myTableData"
          :bordered="true"
          :show-indices="false"
        />
      </row-layout-row>

      <!-- Hover-able rows -->
      <row-layout-row section="Hoverable rows">
        <p>
          You can also set the
          <code class="highlighter-rouge">:hoverable="true"</code>
          prop for a hover-able table.
        </p>
        <basic-table
          :table-data="myTableData"
          :hoverable="true"
          :show-indices="false"
        />
      </row-layout-row>
    </row-layout>
  </div>
</template>

<script>
import RowLayout from "../components/RowLayout";
import RowLayoutRow from "../components/RowLayoutRow";
import BasicTable from "../components/BasicTable";

export default {
  name: "BasicTables",
  components: { BasicTable, RowLayoutRow, RowLayout },
  data() {
    return {
      myTableData: [
        { "First Name": "Mark", "Last Name": "Otto", Username: "@mdo" },
        { "First Name": "Jacob", "Last Name": "Thornton", Username: "@fat" },
        { "First Name": "Larry", "Last Name": "the Bird", Username: "@twitter" }
      ]
    };
  },
  mounted() {}
};
</script>
