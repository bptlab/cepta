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
import {
  MappedTransport,
  MapTripPosition,
  Transport,
  TripPosition
} from "../models/geo";
import { DelayNotification } from "../generated/protobuf/models/internal/notifications/notification_pb";
import { AppModule } from "../store/modules/app";

@Component({
  name: "SimpleMap",
  components: {
    MapVisualisation,
    MasonryLayout,
    MasonryLayoutTile,
    MapCell
  }
})
export default class Dashboard extends Vue {
  currentTransport: MappedTransport | null = null;
  tracked: string = "";

  get delays(): DelayNotification[] {
    let delayNotis: DelayNotification[] = AppModule.notifications.map(
      noti => noti.getDelay()!
    );
    return delayNotis.filter(
      delayNoti => delayNoti.getTransportId()!.toString() == this.tracked
    );
  }

  get transport(): Transport {
    let delayPositions: MapTripPosition[];
    delayPositions = this.delays.map(toPosition);
    return {
      id: this.tracked,
      start: this.delays[0].getStationId()?.toString() || "",
      end: this.delays[-1].getStationId()?.toString() || "",
      trend: {
        value: -1,
        sample: ""
      },
      connection: {
        online: false,
        lastConnection: ""
      },
      plannedDuration: -1,
      actualDuration: -1,

      // TODO!!: Are you sure this is the intended behaviour? You expect a string but try to assign an array. Will there be in the future in each element of the Array the same reason that it is enough to just return the first element?
      //delay: this.delays.map(del => del.getDelay()?.getDelta()),
      //delayReason: this.delays.map(del => del.getDelay()?.getDetails()),

      delay: this.delays[0]
        .getDelay()!
        .getDelta()!
        .getSeconds(),
      delayReason: this.delays[0].getDelay()!.getDetails(),
      // TODO: insert positions into mapped transport
      map: {
        positions: delayPositions
      }
    };
  }

  handleTrack(transport: Transport) {
    this.tracked = transport.id;
    this.$router.replace({
      name: "map",
      query: { track: this.tracked }
    });
    this.currentTransport = transport.map;
  }

  handleUntrack(transport: Transport) {
    this.tracked = "";
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
      //let transport = this.transports[0];
      this.handleTrack(this.transport);
    }
  }
}
function toPosition(delay: DelayNotification): MapTripPosition {
  let tpos: TripPosition = {
    coordinates: [1, 2], //[delay.getCoordinates().getLongitude(), delay.getCoordinates().getLatitude()],
    description:
      delay
        .getDelay()
        ?.getDelta()
        ?.toString() +
      " Grund ist: " +
      delay.getDelay()?.getDetails(),
    stationID: delay.getStationId()?.toString()
  };
  return {
    position: tpos
  };
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
