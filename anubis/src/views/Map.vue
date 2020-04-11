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
        <li
          v-for="(transport, index) in transports"
          :key="index"
          @mouseenter="handleHoverEnter(transport)"
          @mouseleave="handleHoverLeave(transport)"
        >
          <!-- alarm-clock signal na truck bolt stats-up stats-down announcement bell alert reload time control-shuffle -->
          <div class="transport-container">
            <span class="cepta-id">{{ transport.id }}</span>
            <div class="status" :class="{ late: transport.delay > 0 }">
              <span
                class="icon"
                :class="transport.delay > 0 ? 'icon-alert' : 'icon-alert'"
              ></span>
              {{ transport.delay }}
            </div>
            <div class="planned-route-info">
              <span>
                <span
                  >{{ transport.start }}
                  <span class="icon icon-arrows-horizontal"></span>
                  {{ transport.end }}</span
                >
              </span>
            </div>

            <div class="actual-route-info">
              <span>
                <span class="icon icon-location-pin"></span>
                {{ transport.lastPosition.description }}
              </span>
            </div>
            <!--
            <span class="icon icon-line-dashed"></span>
            <div class="btn btn-info btn-slim"><span class="icon icon-target"></span> Track</div>
            <div class="btn btn-info btn-slim"><span class="icon icon-new-window"></span> View</div>
            <div class="btn btn-info btn-slim"><span class="icon icon-bell"></span> Notify</div>
            <div class="btn btn-info btn-slim"><span class="icon icon-bolt"></span> Perform mitigation</div>
            -->
          </div>
        </li>
      </ul>
    </div>
    <div class="map">
      <map-visualisation
        :transport="
          currentTransport != undefined
            ? currentTransport.map
            : transports[0].map
        "
      ></map-visualisation>
    </div>
  </div>
</template>

<script lang="ts">
import MapVisualisation from "@/components/MapVisualisation.vue";
import { Component, Vue } from "vue-property-decorator";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import { MappedTransport, Transport } from "../models/geo";

@Component({
  name: "Map",
  components: {
    MapVisualisation,
    MasonryLayout,
    MasonryLayoutTile
  }
})
export default class Dashboard extends Vue {
  recentHoverTransport?: MappedTransport = undefined;
  currentHoverTransport?: MappedTransport = undefined;
  currentTransport?: MappedTransport = undefined;
  transports: Transport[] = Array(15).fill({
    id: "CPTA-7236-45832",
    start: "Hamburg",
    end: "Berlin",
    plannedDuration: 150,
    actualDuration: 170,
    delay: 20,
    delayReason: "Rain",
    lastPosition: {
      coordinates: { lat: 12, lon: 14 },
      description: "Grunewald"
    },
    map: {
      positions: [
        { position: { coordinates: { lat: 52.391385, lon: 13.092939 } } },
        { position: { coordinates: { lat: 52.395679, lon: 13.137056 } } },
        { position: { coordinates: { lat: 52.411898, lon: 13.156936 } } },
        { position: { coordinates: { lat: 52.426241, lon: 13.19041 } } },
        { position: { coordinates: { lat: 52.487991, lon: 13.260937 } } }
      ],
      color: "blue"
    }
  });

  constructor() {
    super();
    this.currentTransport = null;
    this.currentHoverTransport = null;
  }

  handleHoverEnter(transport: Transport) {
    this.currentHoverTransport = transport.map;
  }

  handleHoverLeave(transport: Transport) {
    this.currentHoverTransport = undefined;
  }
}
</script>

<style lang="sass" scoped>
.map-container
  height: 100%
  position: relative
  width: 100%
  overflow: hidden

  .btn
    color: inherit

  .btn-slim
      padding: 1px 4px

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
    max-width: calc(100% - 300px)

  .transport-list
    top: 30px
    left: 0
    position: absolute
    width: 25%
    min-width: 300px
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
        +theme-color-lighten(background-color, bgc-body, 5)
        +transition(all 0.2s ease-in)
        padding: 10px
        margin-top: 7px
        // border: 1px solid rgba(0,0,0,0.1)
        border-radius: 7px
        overflow: hidden
        height: 50px
        box-shadow: 0 5px 10px rgba(0,0,0,0.01), 0 3px 3px rgba(0,0,0,0.01)

        &:hover
          +theme-color-lighten(background-color, bgc-body, 15)
          height: 125px
          box-shadow: 0 10px 20px rgba(0,0,0,0.1), 0 6px 6px rgba(0,0,0,0.1)

        .transport-container
          position: relative

          .status
            font-size: 20px
            position: absolute
            top: 0
            right: 0
            &.late
              color: red

          .planned-route-info, .actual-route-info
            position: absolute
            left: 0
            span
              vertical-align: middle

          .planned-route-info
            top: 10px
            font-size: 15px
            span
              line-height: 15px

          .actual-route-info
            +theme(color, c-accent-text)
            top: 35px
            font-size: 16px
            span
              line-height: 16px

          .cepta-id
            position: absolute
            top: 0
            left: 0
            font-size: 8px
            +theme-color-diff(color, bgc-body, 50)
</style>
