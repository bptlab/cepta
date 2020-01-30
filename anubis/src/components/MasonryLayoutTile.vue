<template>
  <div v-masonry-tile :class="['item', layoutStyle]">
    <div :class="{ 'bgc-white p-20 bd': wrap }">
      <h6 class="c-grey-900">{{ section }}</h6>
      <div class="mT-20">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";
@Component({
  name: "MasonryLayoutTile"
})
export default class MasonryLayoutTile extends Vue {
  @Prop({ default: null }) private section!: string;
  @Prop({
    default: () => {
      return { "col-md-8": true };
    }
  })
  private layoutStyle!: object;
  @Prop({ default: true }) private wrap!: string;
  mounted() {}
}
</script>
