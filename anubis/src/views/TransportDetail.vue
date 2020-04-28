<template>
  <div class="transport-detail-container">
    <div class="transport-detail-sidebar">
      <div class="transport-overview">
        <table>
          <tr>
            <td>
              <span class="cepta-id">{{ transport.id }}</span>
            </td>
            <td rowspan="2">
              <div
                class="status"
                :class="{
                  late: transport.delay > delayThresholds.soft,
                  'very-late': transport.delay > delayThresholds.hard
                }"
              >
                {{ delay }}
                <span
                  class="icon"
                  :class="
                    transport.delay > delayThresholds.soft
                      ? 'icon-alert'
                      : 'icon-time'
                  "
                ></span>
              </div>
            </td>
          </tr>
          <tr>
            <td>
              <span class="route"
                >{{ transport.start }}
                <span class="icon icon-arrows-horizontal"></span>
                {{ transport.end }}
              </span>
            </td>
          </tr>
          <tr>
            <td>
              <span class="position"
                ><span class="icon icon-location-pin"></span>
                {{ transport.lastPosition.description }}
              </span>
            </td>
            <td>
              <div
                class="eta"
                :class="{
                  late: transport.delay > delayThresholds.soft,
                  'very-late': transport.delay > delayThresholds.hard
                }"
              >
                ETA: 13:45
              </div>
            </td>
          </tr>
        </table>

        <div v-if="transport.routeProgress" class="progress mT-10">
          <div
            class="progress-bar"
            role="progressbar"
            :aria-valuenow="transport.routeProgress"
            aria-valuemin="0"
            aria-valuemax="1"
            :style="'width:' + transport.routeProgress * 100 + '%;'"
          ></div>
        </div>

        <div class="transport-metadata">
          <!--</span> <span class="icon icon-line-dashed"></span>-->
          <table>
            <tr class="trend" v-if="transport.trend != undefined">
              <td>
                <span
                  class="icon"
                  :class="
                    transport.trend.value > 0
                      ? 'icon-stats-up'
                      : 'icon-stats-down'
                  "
                ></span>
              </td>
              <td>
                Observed delay
                {{ transport.trend.value > 0 ? "increased" : "decreased" }} by
                {{ transport.trend.value }} during the
                {{ transport.trend.sample }}
              </td>
            </tr>
            <tr class="signal" v-if="transport.connection != undefined">
              <td>
                <span
                  class="icon"
                  :class="
                    transport.connection.online ? 'icon-signal' : 'icon-na'
                  "
                ></span>
              </td>
              <td>{{ transport.connection.online ? "Online" : "Offline" }}</td>
            </tr>
          </table>

          <table class="transport-actions">
            <tr>
              <td>
                <div
                  class="btn btn-cepta-default btn-slim track"
                  @click="track()"
                >
                  <span class="icon icon-target"></span>
                  Track
                </div>
              </td>
              <td>
                <div class="btn btn-cepta-default btn-slim" @click="notify()">
                  <span class="icon icon-bell"></span> Notify
                </div>
              </td>
            </tr>
          </table>
        </div>
      </div>
      <p>Stations</p>
      <div class="station-list">
        <ul>
          <li
            v-for="position in transport.map.positions"
            :key="position.coordinates"
          >
            <station-cell
              v-on:view-station="handleViewStation"
              v-on:hover-start="handleHoverStation"
              :station="position"
            ></station-cell>
          </li>
        </ul>
      </div>
    </div>
    <div class="map">
      <map-visualisation
        :station-preview="hoveredStation"
        :transport="transport.map"
        :center="selectedStationCoords"
      ></map-visualisation>
    </div>
  </div>
</template>

<script lang="ts">
import MapVisualisation from "@/components/MapVisualisation.vue";
import { Component, Vue } from "vue-property-decorator";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import { MapTripPosition, Transport } from "../models/geo";
import StationCell from "@/components/StationCell.vue";
import { formatDelay } from "@/utils";
import { UserManagementModule } from "@/store/modules/usermgmt";

@Component({
  name: "Map",
  components: {
    MapVisualisation,
    MasonryLayout,
    MasonryLayoutTile,
    StationCell
  }
})
export default class TrainData extends Vue {
  private notifyEnabled: boolean = false;
  private recentlyHoveredStation: MapTripPosition | null = null;
  private hoveredStation: MapTripPosition | null = null;
  private selectedStation: MapTripPosition | null = null;

  get delayThresholds(): { hard: number; soft: number } {
    return UserManagementModule.delayThresholds;
  }

  get selectedStationCoords(): [number, number] | undefined {
    return this.selectedStation?.position?.coordinates;
  }

  transport: Transport = {
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
      coordinates: [12, 14],
      description: "Grunewald"
    },
    map: {
      positions: [
        {
          position: {
            coordinates: [52.391385, 13.092939],
            stationName: "A",
            stationID: "1"
          }
        },
        {
          position: {
            coordinates: [52.395679, 13.137056],
            stationName: "B",
            stationID: "2"
          }
        },
        {
          position: {
            coordinates: [52.411898, 13.156936],
            stationName: "C",
            stationID: "3"
          }
        },
        {
          position: {
            coordinates: [52.426241, 13.19041],
            stationName: "D",
            stationID: "4"
          }
        }
      ],
      color: "blue"
    }
  };

  track() {}

  mitigate() {
    this.$emit("mitigate", this.transport);
  }

  notify() {
    this.notifyEnabled = !this.notifyEnabled;
    this.$emit("notify", this.transport, this.notifyEnabled);
  }

  handleViewStation(station: MapTripPosition) {
    this.selectedStation = station;
    this.handleHoverStation(station);
  }

  handleHoverStation(station: MapTripPosition) {
    this.hoveredStation = station;
    this.recentlyHoveredStation = this.hoveredStation;
  }

  get delay(): string {
    return formatDelay(this.transport.delay);
  }

  mounted() {
    // Check for track parameter
    let trackRequest = this.$route.query.track;
  }
}
</script>

<style lang="sass" scoped>
.transport-detail-container
  height: 100%
  position: relative
  width: 100%

  .map
    top: 0
    right: 0
    position: absolute
    width: 60%
    height: 100%
    max-width: calc(100% - 330px)

  .transport-detail-sidebar
    margin: 0
    width: 40%
    min-width: 330px
    height: 100%
    position: relative

    &>p
      line-height: 20px
      padding-left: 20px
      margin: 0

    .transport-overview
      position: relative
      padding: 20px
      height: 350px
      table
        width: 100%
        td
          font-size: 15px
          padding: 5px 10px
          vertical-align: middle
          span
            display: inline-block

          .status, .eta
            float: right
            font-size: 20px

          .status
            +theme(color, c-delay-fine)
            &.late
              +theme(color, c-delay-warning)
            &.very-late
              +theme(color, c-delay-alert)

          .position
            +theme(color, c-map-position-text)
            font-weight: bold
            font-size: 20px

          .route
            font-size: 25px
            color: inherit

          .cepta-id
            font-size: 10px
            +theme-color-diff(color, bgc-body, 50)

      .transport-metadata
        margin-top: 5px
        font-size: 16px
        span
          line-height: 16px
        td
          padding: 2px
          font-size: 12px

      .transport-actions
        td
          padding: 3px
          float: right
          display: inline-block

    .progress, .transport-metadata
      margin: 20px 10px

    .progress
      +theme-color-diff(background-color, bgc-body, 20)
      div
        +theme-color-diff(background-color, bgc-body, 70)
        +transition(all 0.2s ease-in)

    .station-list
      height: calc(100% - 370px)
      width: 100%
      padding: 0 20px 50px 20px
      overflow-y: scroll
      -ms-overflow-style: none

      // Hide scrollbar for Chrome, Safari and Opera
      &::-webkit-scrollbar
        display: none

      ul
        text-decoration: none
        list-style: none
        padding: 0
</style>
