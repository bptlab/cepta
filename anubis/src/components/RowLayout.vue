<template>
  <div class="container-fluid">
    <h4 v-if="title" class="mT-10 mB-30">{{ title }}</h4>
    <slot></slot>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";
@Component({
  name: "RowLayout"
})
export default class RowLayout extends Vue {
  @Prop({ default: null }) private title!: string;
  mounted() {}
}
</script>
