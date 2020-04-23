<template>
  <div class="map-container">
    <div class="sort">
      <div class="btn-group dropright">
        <button
          type="button"
          class="btn btn-slim dropdown-toggle"
          data-toggle="dropdown"
          aria-haspopup="true"
          aria-expanded="false"
        >
          <span class="icon icon-exchange-vertical"></span> Sort by
        </button>
        <div class="dropdown-menu">
          <a class="dropdown-item" href="#">Relevance</a>
          <a class="dropdown-item" href="#">Time</a>
          <a class="dropdown-item" href="#"></a>
          <!--<div class="dropdown-divider"></div>-->
        </div>
      </div>
    </div>
    <div class="transport-list noscrollbar">
      <ul>
        <li v-for="transport in transports" :key="transport.id">
          <map-cell
            :transport="transport"
            v-on:track="handleTrack"
            v-on:untrack="handleUntrack"
            :tracked="tracked"
          ></map-cell>
        </li>
      </ul>
    </div>
    <div class="map">
      <map-visualisation :transport="currentTransport"></map-visualisation>
    </div>
  </div>
</template>

<script lang="ts">
import MapVisualisation from "@/components/MapVisualisation.vue";
import { Component, Vue } from "vue-property-decorator";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import MapCell from "../components/MapCell.vue";
import { MappedTransport, Transport } from "../models/geo";

@Component({
  name: "Map",
  components: {
    MapVisualisation,
    MasonryLayout,
    MasonryLayoutTile,
    MapCell
  }
})
export default class Dashboard extends Vue {
  currentTransport: MappedTransport | null = null;
  tracked: string | null = null;

  transports: Transport[] = Array(15).fill({
    id: "CPTA-7236-45832",
    start: "Hamburg",
    end: "Berlin",
    plannedDuration: 150,
    actualDuration: 170,
    delay: 67,
    delayReason: "Rain",
    trend: {
      value: 13,
      sample: "last 3 stations"
    },
    connection: {
      online: false,
      lastConnection: ""
    },
    routeProgress: 0.35,
    lastPosition: {
      coordinates: [12, 14],
      description: "Grunewald"
    },
    map: {
      positions: [
        {
          position: { coordinates: [52.391385, 13.092939] },
          icon: {
            iconUrl:
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png"
          }
        },
        {
          position: { coordinates: [52.395679, 13.137056] },
          icon: {
            iconUrl:
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png"
          }
        },
        {
          position: { coordinates: [52.411898, 13.156936] },
          icon: {
            iconUrl:
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png"
          }
        },
        {
          position: { coordinates: [52.426241, 13.19041] },
          icon: {
            iconUrl:
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png"
          }
        },
        {
          position: { coordinates: [52.487991, 13.260937] },
          icon: {
            iconUrl:
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png"
          }
        }
      ]
    }
  });

  handleTrack(transport: Transport) {
    this.tracked = transport.id;
    this.$router.replace({
      name: "map",
      query: { track: this.tracked }
    });
    this.currentTransport = transport.map;
  }

  handleUntrack(transport: Transport) {
    this.tracked = null;
    this.$router.replace({
      name: "map",
      query: {}
    });
  }

  mounted() {
    // Check for track parameter
    let trackRequest = this.$route.query.track;
    if (trackRequest != undefined && trackRequest != null) {
      // TODO: Query for the real transport here
      let transport = this.transports[0];
      this.handleTrack(transport);
    }
  }
}
</script>

<style lang="sass" scoped>
.map-container
  +theme-color-diff(background-color, bgc-body, 5)
  height: 100%
  position: relative
  width: 100%
  overflow: hidden

  .btn
    color: inherit

  .sort
    padding: 7px
    top: 0
    line-height: 16px
    position: absolute
    vertical-align: middle

  .map
    top: 0
    right: 0
    position: absolute
    width: 75%
    height: 100%
    max-width: calc(100% - 330px)

  .transport-list
    top: 30px
    left: 0
    position: absolute
    width: 25%
    min-width: 330px
    height: calc(100% - #{$footer-height})
    overflow-y: scroll
    padding: 0 7px

    ul
      text-decoration: none
      list-style: none
      padding: 0
</style>
