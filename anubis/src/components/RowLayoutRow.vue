<template>
  <div class="row">
    <div class="col-md-12">
      <div class="bgc-white bd bdrs-3 p-20 mB-20">
        <h4 class="mB-20">{{ section }}</h4>
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";

@Component({
  name: "RowLayoutRow"
})
export default class RowLayoutRow extends Vue {
  @Prop({ default: null }) private section!: string;
  mounted() {}
}
</script>
