<template>
  <div>
    <input
      ref="search"
      class="form-control"
      id="trainID oder so"
      type="number"
      placeholder="search ... ;)"
    />
    <basic-table
      :table-data="trainIDs"
      :show-indices="false"
      :striped="true"
      :bordered="true"
      cellspacing="0"
      :data-table="false"
    />
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";
import BasicTable from "./BasicTable.vue";

@Component({
  name: "TrainIdList",
  components: { BasicTable }
})
export default class TrainIdList extends Vue {
  @Prop({ default: "" }) private route!: string;
  @Prop({ default: "I provide account details" }) private title!: string;
}
</script>
