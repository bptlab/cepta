<template>
  <div id="map">
    <l-map :zoom="zoom" :center="center">
      <l-tile-layer :url="url" :attribution="attribution"></l-tile-layer>
      <l-polyline :lat-lngs="polyline.latlngs" :color="polyline.color">
      </l-polyline>
      <l-marker :lat-lng="polyline.latlngs[0]" :icon="icon"></l-marker>
      <l-marker :lat-lng="polyline.latlngs[2]" :icon="icon"></l-marker>
      <l-marker :lat-lng="polyline.latlngs[1]">
        <l-icon :icon-size="icon.iconSize" :icon-anchor="[18, 75]">
          ID: 123
          <img
            src="https://findicons.com/files/icons/951/google_maps/32/train.png"
        /></l-icon>
      </l-marker>
    </l-map>
  </div>
</template>

<script lang="ts">
import { LMap, LTileLayer, LPolyline, LMarker, LIcon } from "vue2-leaflet";
import L from "leaflet";
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "Visualisation",
  components: {
    LMap,
    LTileLayer,
    LPolyline,
    LMarker,
    LIcon
  }
})
export default class Visualisation extends Vue {
  zoom: number = 13;
  center: Array<number> = [52.393997, 13.133366];
  url: string = "http://{s}.tile.osm.org/{z}/{x}/{y}.png";
  attribution: string =
    '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors';
  icon = L.icon({
    // Icon from https://findicons.com/icon/260843/train_transportation
    iconUrl:
      "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png",
    iconSize: [30, 30],
    tooltipAnchor: [16, 37]
  });
  polyline: Object = {
    // Babelsberg --> Grunewald, Koordinaten von https://www.laengengrad-breitengrad.de/
    latlngs: [
      [52.391385, 13.092939],
      [52.395679, 13.137056],
      [52.411898, 13.156936],
      [52.426241, 13.19041],
      [52.487991, 13.260937]
    ],
    color: "red"
  };
}
</script>

<style lang="sass">
@import "~leaflet/dist/leaflet.css";

#map
  height: 400px
  margin: 0

.leaflet-top
  z-index: 1000
</style>
