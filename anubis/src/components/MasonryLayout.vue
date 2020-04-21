<template>
  <div>
    <h4 v-if="title" class="pos-r m-0">{{ title }}</h4>
    <div
      v-masonry
      item-selector=".item"
      columnWidth=".masonry-sizer"
      :percentPosition="true"
      class="row gap-10 pos-r"
    >
      <div class="masonry-sizer col-md-6"></div>
      <slot></slot>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";

@Component({
  name: "MasonryLayout"
})
export default class MasonryLayout extends Vue {
  @Prop({ default: null }) private title!: string;

  mounted() {
    // @ts-ignore: No such attribute
    this.$redrawVueMasonry();
  }
}
</script>
