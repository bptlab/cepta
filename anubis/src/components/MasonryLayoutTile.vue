<template>
  <div v-masonry-tile :class="['item', layoutStyle]">
    <div class="shadow-tile">
      <h6>{{ section }}</h6>
      <div>
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
      return { "col-md-6": true };
    }
  })
  private layoutStyle!: object;
  @Prop({ default: true }) private wrap!: string;
  mounted() {}
}
</script>
<style scoped lang="sass">
.shadow-tile
  +transition(all 0.1s ease-in)
  +theme-color-darken(background-color, bgc-body, 3)
  // display: inline-block
  margin: 20px 30px
  padding: 20px
  border-radius: 5px
  box-shadow: 0 10px 20px rgba(0,0,0,0.04), 0 6px 6px rgba(0,0,0,0.04)

  &:hover
    box-shadow: 0 10px 20px rgba(0,0,0,0.1), 0 6px 6px rgba(0,0,0,0.1)
</style>
