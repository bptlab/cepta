<template>
  <div class="list-view-cell">
    <div>
      <slot></slot>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "ListViewCell",
  components: {}
})
export default class ListViewCell extends Vue {}
</script>

<style lang="sass" scoped>
.list-view-cell
  position: relative
  +transition(all 0.2s ease-in)
  +theme(background-color, bgc-body)
  padding: 5px 10px
  // border: 1px solid rgba(0,0,0,0.1)
  border-radius: 7px
  margin-top: 7px
  box-shadow: 0 5px 10px rgba(0,0,0,0.01), 0 3px 3px rgba(0,0,0,0.01)

  &:hover, li.tracked
    +theme-color-lighten(background-color, bgc-body, 15)
    box-shadow: 0 10px 20px rgba(0,0,0,0.1), 0 6px 6px rgba(0,0,0,0.1)
</style>
