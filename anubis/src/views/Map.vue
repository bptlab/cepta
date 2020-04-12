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
    <div class="transport-list">
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
    delay: 21,
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
      coordinates: { lat: 12, lon: 14 },
      description: "Grunewald"
    },
    map: {
      positions: [
        {
          position: { coordinates: { lat: 52.391385, lon: 13.092939 } },
          icon: {
            iconUrl:
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png"
          }
        },
        {
          position: { coordinates: { lat: 52.395679, lon: 13.137056 } },
          icon: {
            iconUrl:
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png"
          }
        },
        {
          position: { coordinates: { lat: 52.411898, lon: 13.156936 } },
          icon: {
            iconUrl:
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png"
          }
        },
        {
          position: { coordinates: { lat: 52.426241, lon: 13.19041 } },
          icon: {
            iconUrl:
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png"
          }
        },
        {
          position: { coordinates: { lat: 52.487991, lon: 13.260937 } },
          icon: {
            iconUrl:
              "https://findicons.com/files/icons/2219/dot_pictograms/128/train_transportation.png"
          }
        }
      ],
      color: "blue"
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
    height: calc(100% - 31px)
    overflow-y: scroll
    padding: 0 7px
    // Hide scrollbar for IE and Edge
    -ms-oversflow-style: none

    // Hide scrollbar for Chrome, Safari and Opera
    &::-webkit-scrollbar
      display: none

    ul
      text-decoration: none
      list-style: none
      padding: 0
      li
        +theme(background-color, bgc-body)
        padding: 5px 10px
        margin-top: 7px
        // border: 1px solid rgba(0,0,0,0.1)
        border-radius: 7px
        overflow: hidden
        box-shadow: 0 5px 10px rgba(0,0,0,0.01), 0 3px 3px rgba(0,0,0,0.01)

        &:hover, li.tracked
          +theme-color-lighten(background-color, bgc-body, 15)
          box-shadow: 0 10px 20px rgba(0,0,0,0.1), 0 6px 6px rgba(0,0,0,0.1)
</style>
