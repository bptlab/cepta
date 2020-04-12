<template>
  <div id="map">
    <!--
    <l-map ref="map" :zoom="zoom" :center="center">
      <l-tile-layer :url="url" :attribution="attribution"></l-tile-layer>
      <l-polyline :lat-lngs="coordinates" :color="transport.color"></l-polyline>
      <l-marker
        v-for="m in markers"
        v-bind:key="m.coordinate"
        :lat-lng="m.coordinate"
        :icon="m.icon"
      ></l-marker>
      -->
    <div id="map"></div>
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
  </div>
</template>

<script lang="ts">
import L from "leaflet";
import { Component, Vue, Prop, Watch, Ref } from "vue-property-decorator";
import {
  Coordinate,
  MappedTransport,
  TripPosition,
  MapTripPosition
} from "../models/geo";
import VueRouter from "vue-router";

export interface Marker {
  icon?: L.Icon;
  coordinates: [number, number];
}

@Component({
  name: "MapVisualisation",
  components: {}
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
  protected map!: L.Map;

  coordinates: [number, number][] = [];
  markers: Marker[] = [];
  polyline: L.Polyline = L.polyline([]);

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

  setupRoute(
    transport: MappedTransport
  ): { coordinates: [number, number]; marker: L.Marker }[] {
    return transport.positions.reduce(
      (
        acc: { coordinates: [number, number]; marker: L.Marker }[],
        pos: MapTripPosition
      ): { coordinates: [number, number]; marker: L.Marker }[] => {
        let c = pos.position.coordinates;
        let markerOptions: L.MarkerOptions = {};
        if (pos.icon != undefined) {
          markerOptions.icon = L.icon({
            // Icon from https://findicons.com/icon/260843/train_transportation
            iconUrl:
              pos.icon.url ??
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png",
            iconSize: pos.icon.size ?? [30, 30],
            tooltipAnchor: [16, 37]
          });
          // L.marker([51.5, -0.09]).addTo(this.map)
          // .bindPopup('A pretty CSS3 popup.<br> Easily customizable.')
          // .openPopup();
        }
        let marker = L.marker([c.lat, c.lon], markerOptions).bindPopup(
          "A pretty CSS3 popup.<br> Easily customizable."
        );
        acc.push({ coordinates: [c.lat, c.lon], marker: marker });
        return acc;
      },
      [] as { coordinates: [number, number]; marker: L.Marker }[]
    );
  }

  loadTransport(transport?: MappedTransport) {
    if (transport == undefined) return;

    let positions = this.setupRoute(transport);
    let polyline: [number, number][] = [];
    positions.forEach(p => {
      polyline.push(p.coordinates);
      p.marker.addTo(this.map);
    });
    this.polyline = L.polyline(polyline, { color: transport.color }).addTo(
      this.map
    );
    // this.markers = this.getMarkers(positions);
    // this.coordinates = this.getCoordinates(positions);

    // Fly to the new route
    let start = transport.positions[0].position.coordinates;
    let end =
      transport.positions[transport.positions.length - 1].position.coordinates;
    this.map.flyToBounds(
      [
        [start.lat, start.lon],
        [end.lat, end.lon]
      ],
      { duration: 0.3, animate: true, padding: [10, 10] }
    );
  }

  @Watch("transport")
  onTransportChanged(newValue: MappedTransport) {
    // Load new transport
    this.loadTransport(newValue);
  }

  mounted() {
    // Create map
    this.map = L.map("map").setView([52.5170365, 13.3888599], 5);

    L.tileLayer(this.url, {
      attribution: this.attribution
    }).addTo(this.map);

    /*
    L.marker([51.5, -0.09]).addTo(map)
    .bindPopup('A pretty CSS3 popup.<br> Easily customizable.')
    .openPopup();
    */
  }

  /*
  Useful:
  var group = new L.featureGroup([marker1, marker2, marker3]);
  map.fitBounds(group.getBounds());
  */
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
