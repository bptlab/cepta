<template>
  <div class="map-container">
    <div id="map"></div>
  </div>
</template>

<script lang="ts">
import L from "leaflet";
import { Component, Vue, Prop, Watch } from "vue-property-decorator";
import { MappedTransport, MapTripPosition, TripPosition } from "../models/geo";

export interface Marker {
  marker?: L.Marker;
  coordinates: [number, number];
}

@Component({
  name: "MapVisualisation",
  components: {}
})
export default class MapVisualisation extends Vue {
  @Prop() zoom!: number;
  @Prop() center!: [number, number];
  @Prop({ default: () => [50, 50] }) flyPadding!: [number, number];
  @Prop({ default: true }) forceIcon!: boolean;
  @Prop({ default: "http://{s}.tile.openstreetmap.de/{z}/{x}/{y}.png" })
  url!: string;
  @Prop({
    default:
      '&copy; <a href="http://openstreetmap.de/copyright">OpenStreetMap</a> contributors'
  })
  attribution!: string;
  @Prop() transport?: MappedTransport;
  @Prop() stations?: MapTripPosition[];
  @Prop() stationPreview?: any;

  protected map!: L.Map;
  protected coordinates: [number, number][] = [];
  protected markers: Marker[] = [];
  protected stationMarkers: Marker[] = [];
  protected polyline: L.Polyline = L.polyline([]);

  setupRoute(transport: MappedTransport): Marker[] {
    return transport.positions.reduce(
      (acc: Marker[], pos: MapTripPosition): Marker[] => {
        let c = pos.position.coordinates;
        let markerOptions: L.MarkerOptions = {};
        let size = pos.icon?.size ?? [30, 30];
        let anchor: L.PointTuple = [0, -(size[1] / 2)];
        if (pos.icon != undefined || this.forceIcon) {
          markerOptions.icon = L.icon({
            // Icon from https://findicons.com/icon/260843/train_transportation
            iconUrl:
              pos.icon?.url ??
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png",
            iconSize: size,
            tooltipAnchor: anchor,
            popupAnchor: anchor
          });
        }
        let marker = L.marker(c, markerOptions).bindPopup("Moin", {
          autoPan: false
        });
        acc.push({ coordinates: c, marker: marker });
        return acc;
      },
      [] as Marker[]
    );
  }

  loadTransport(transport?: MappedTransport) {
    if (transport == undefined) return;

    //this.markers = this.setupRoute(transport);
    let polyline: [number, number][] = [];
    /*this.markers.forEach(p => {
      polyline.push(p.coordinates);
      p.marker?.addTo(this.map);
    });
    this.polyline = L.polyline(polyline, {
      color: transport.color ?? "black"
    }).addTo(this.map);
*/
    // Fly to the new route
    let start = transport.positions[0].position.coordinates;
    let end =
      transport.positions[transport.positions.length - 1].position.coordinates;
    this.map.flyToBounds([start, end], {
      duration: 0.3,
      animate: true,
      padding: this.flyPadding
    });
  }

  loadStations(stations?: MapTripPosition[]) {
    if (stations == undefined) {
      console.log("undefined stations");
      return;
    }

    //this.StationMarkers = this.setupStations(stations);
    for (let i = 0; i < stations.length; i++) {
      console.log(stations[i]);
      let c = stations[i].position.coordinates;
      let size = stations[i].icon?.size ?? [30, 30];
      let anchor: L.PointTuple = [0, -(size[1] / 2)];
      let markerOptions: L.MarkerOptions = {};
      if (stations[i].icon != undefined || this.forceIcon) {
        markerOptions.icon = L.icon({
          // Icon from https://findicons.com/icon/260843/train_transportation
          iconUrl:
            stations[i].icon?.url ??
            "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png",
          iconSize: size,
          tooltipAnchor: anchor,
          popupAnchor: anchor
        });
      }
      let stationName;
      if (stations[i].position.stationName == undefined) {
        stationName = "undefined station name";
      } else {
        stationName = stations[i].position.stationName;
      }
      let marker = L.marker(c, markerOptions).bindPopup(stationName, {
        autoPan: false
      });
      this.stationMarkers.push({ coordinates: c, marker: marker });
    }
    this.stationMarkers.forEach(p => {
      p.marker?.addTo(this.map);
    });
  }

  @Watch("transport")
  onTransportChanged(newValue: MappedTransport) {
    this.loadTransport(newValue);
    console.log("this.stations: " + this.stations);
    this.loadStations(this.stations);
  }

  @Watch("center")
  onCenterChanged(newValue?: [number, number]) {
    if (newValue != undefined) this.map.flyTo(newValue);
  }

  @Watch("stationPreview")
  onStationPreviewChanged(station: MapTripPosition) {
    this.markers.forEach(m => {
      if (m.coordinates == station.position.coordinates) {
        m.marker?.openPopup(m.coordinates);
      }
    });
  }

  mounted() {
    // Create map
    this.map = L.map("map").setView([52.5170365, 13.3888599], 5);

    L.tileLayer(this.url, {
      attribution: this.attribution
    }).addTo(this.map);

    if (this.transport != undefined) {
      this.loadTransport(this.transport);
    }
    if (this.stations != undefined) {
      this.loadStations(this.stations);
    }
  }

  /*
  Useful:
  var group = new L.featureGroup([marker1, marker2, marker3]);
  map.fitBounds(group.getBounds());
  */
}
</script>

<style lang="sass" scoped>
@import "~leaflet/dist/leaflet.css";
.map-container
  height: 100%
  margin: 0
  position: relative

  #map
    height: 100%

  .leaflet-top
    z-index: 1000
</style>
