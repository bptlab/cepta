<template>
  <div id="map">
    <l-map ref="map" :zoom="zoom" :center="center">
      <l-tile-layer :url="url" :attribution="attribution"></l-tile-layer>
      <l-polyline :lat-lngs="coordinates" :color="transport.color"></l-polyline>
      <l-marker
        v-for="m in markers"
        v-bind:key="m.coordinate"
        :lat-lng="m.coordinate"
        :icon="m.icon"
      ></l-marker>
      <!--
      <l-marker :lat-lng="polyline.latlngs[0]" :icon="icon"></l-marker>
      <l-marker :lat-lng="polyline.latlngs[2]" :icon="icon"></l-marker>
      <l-marker :lat-lng="polyline.latlngs[1]">
        <l-icon :icon-size="icon.iconSize" :icon-anchor="[18, 75]">
          ID: 123
          <img
            src="https://findicons.com/files/icons/951/google_maps/32/train.png"
        /></l-icon>
      </l-marker>
      -->
    </l-map>
  </div>
</template>

<script lang="ts">
import { LMap, LTileLayer, LPolyline, LMarker, LIcon } from "vue2-leaflet";
import L from "leaflet";
import { Component, Vue, Prop, Watch, Ref } from "vue-property-decorator";
import {
  Coordinate,
  MappedTransport,
  TripPosition,
  MapTripPosition
} from "../models/geo";

export interface Marker {
  icon?: L.Icon;
  coordinates: [number, number];
}

@Component({
  name: "MapVisualisation",
  components: {
    LMap,
    LTileLayer,
    LPolyline,
    LMarker,
    LIcon
  }
})
export default class MapVisualisation extends Vue {
  @Prop() zoom!: number;
  @Prop() center!: Coordinate;
  @Prop({ default: "http://{s}.tile.openstreetmap.de/{z}/{x}/{y}.png" })
  url!: string;
  @Prop({
    default:
      '&copy; <a href="http://openstreetmap.de/copyright">OpenStreetMap</a> contributors'
  })
  attribution!: string;
  @Prop() transport?: MappedTransport;
  @Ref() map!: { mapObject: L.Map };

  coordinates: [number, number][];
  markers: Marker[];

  getCoordinates(positions: Marker[]): [number, number][] {
    return positions.reduce((acc: [number, number][], m: Marker): [
      number,
      number
    ][] => {
      acc.push(m.coordinates);
      return acc;
    }, [] as [number, number][]);
  }

  getMarkers(positions: Marker[]): Marker[] {
    return positions.reduce((acc: Marker[], m: Marker): Marker[] => {
      if (m.icon != undefined) {
        acc.push(m);
      }
      return acc;
    }, [] as Marker[]);
  }

  getPositions(transport: MappedTransport): Marker[] {
    return transport.positions.reduce(
      (acc: Marker[], pos: MapTripPosition): Marker[] => {
        let c = pos.position.coordinates;
        let icon: L.Icon | undefined = undefined;
        if (pos.icon != undefined) {
          icon = L.icon({
            // Icon from https://findicons.com/icon/260843/train_transportation
            iconUrl:
              pos.icon.url ??
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png",
            iconSize: pos.icon.size ?? [30, 30],
            tooltipAnchor: [16, 37]
          });
        }
        acc.push({ coordinates: [c.lat, c.lon], icon: icon });
        return acc;
      },
      [] as Marker[]
    );
  }

  loadTransport(transport?: MappedTransport) {
    if (transport == undefined) return;

    let positions = this.getPositions(transport);
    this.markers = this.getMarkers(positions);
    this.coordinates = this.getCoordinates(positions);

    // Fly to the new route
    let start = transport.positions[0].position.coordinates;
    let end =
      transport.positions[transport.positions.length - 1].position.coordinates;
    this.map.mapObject.flyToBounds(
      [
        [start.lat, start.lon],
        [end.lat, end.lon]
      ],
      { duration: 0.3, animate: true }
    );
  }

  @Watch("transport")
  onTransportChanged(newValue: MappedTransport) {
    this.loadTransport(newValue);
  }

  mounted() {
    this.loadTransport(this.transport);
  }

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
  height: 100%
  margin: 0

.leaflet-top
  z-index: 1000
</style>
