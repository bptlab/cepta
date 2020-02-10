<template>
  <masonry-layout>
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
  </masonry-layout>
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
table
  border-collapse: collapse

tr
  border: 1px solid black
  text-align: center

td, th
  padding: 10px

th
  background-color: black
  color: white

td:hover
  background-color: #bbb
</style>
