<template>
  <div class="map-container">
    <div class="transport-list">
      <ul>
        <li>HEY 1</li>
        <li>HEY 2</li>
        <li>HEY 3</li>
        <li>HEY 4</li>
      </ul>
    </div>
    <div class="map">
      <map-visualisation></map-visualisation>
    </div>
  </div>
    <!--
    <masonry-layout-tile section="Train ID's" layoutStyle='{"col-md-2": true}'>
      <table>
        <tr>
          <th>ID</th>
        </tr>
        <tr v-for="(id, index) in ids" :key="index">
          <td>{{ id }}</td>
        </tr>
      </table>
    </masonry-layout-tile>
    <masonry-layout-tile section="Live Map">
      <map-visualisation></map-visualisation>
    </masonry-layout-tile>
    -->
  
</template>

<script lang="ts">
import MapVisualisation from "@/components/MapVisualisation.vue";
import { Component, Vue } from "vue-property-decorator";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";

@Component({
  name: "Map",
  components: {
    MapVisualisation,
    MasonryLayout,
    MasonryLayoutTile
  }
})
export default class Dashboard extends Vue {
  ids: Array<number> = [1, 2, 3, 4];
}
</script>

<style lang="sass">
.map-container
  height: 100%
  position: relative
  width: 100%

  .map
    top: 0
    float: left
    position: relative
    width: 75%
    height: 100%
    position: relative

  .transport-list
    top: 0
    float: left
    position: relative
    width: 25%
    height: 100%

    ul
      text-decoration: none
      list-style: none
      padding: 0
      li
        +theme-color-lighten(background-color, bgc-body, 5)
        padding: 20px
        margin: 10px
        border-radius: 10px

        &:hover
          +theme-color-lighten(background-color, bgc-body, 15)
          // box-shadow: 0 10px 20px rgba(255,255,255,0.19), 0 6px 6px rgba(1,1,1,0.23)
          box-shadow: 0 10px 20px rgba(0,0,0,1), 0 6px 6px rgba(0,0,0,1)
          // box-shadow: 0 10px 20px rgba(0,0,0,0.19), 0 6px 6px rgba(0,0,0,0.23)
</style>
