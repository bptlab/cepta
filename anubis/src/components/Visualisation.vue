<template>
  <div id="map">
    <l-map :zoom="zoom" :center="center">
      <l-tile-layer :url="url" :attribution="attribution"></l-tile-layer>
      <l-marker :lat-lng="marker"></l-marker>
    </l-map>
  </div>
</template>

<script lang="ts">
import { LMap, LTileLayer, LMarker } from "vue2-leaflet";
import { latLng, latLngBounds } from "leaflet";
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "Visualisation",
  components: {
    LMap,
    LTileLayer,
    LMarker
  }
})
export default class Visualisation extends Vue {
  zoom: number = 13;
  center: latLng = latLng(47.41322, -1.219482);
  url: string = "http://{s}.tile.osm.org/{z}/{x}/{y}.png";
  attribution: string =
    '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors';
  marker: latLng = latLng(47.41322, -1.219482);
}
</script>

<style scoped lang="sass">
#map
  height: 100%
  margin: 0
</style>
