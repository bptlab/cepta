<template>
  <div
    @mouseenter="handleHoverEnter()"
    @mouseleave="handleHoverLeave()"
    :class="{ tracked: isTracked }"
  >
    <!-- alarm-clock signal na truck bolt stats-up stats-down announcement bell alert reload time control-shuffle -->
    <list-view-cell>
      <div class="map-cell-container">
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
              <router-link
                :to="{ name: 'transport', params: { transport: transport.id } }"
              >
                <span class="route"
                  >{{ transport.start }}
                  <span class="icon icon-arrows-horizontal"></span>
                  {{ transport.end }}
                  <span class="new-tab icon icon-new-window"></span>
                </span>
              </router-link>
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
                  @click="trackTransport()"
                >
                  <span class="icon icon-target"></span>
                  {{ isTracked ? "Untrack" : "Track" }}
                </div>
              </td>
              <td>
                <div class="btn btn-cepta-default btn-slim" @click="notify()">
                  <span class="icon icon-bell"></span> Notify
                </div>
              </td>
              <td
                v-if="enableMitigate && transport.delay > delayThresholds.soft"
              >
                <div class="btn btn-cepta-default btn-slim" @click="mitigate()">
                  <span class="icon icon-bolt"></span> Mitigate
                </div>
              </td>
            </tr>
          </table>
        </div>
      </div>
    </list-view-cell>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from "vue-property-decorator";
import { MappedTransport, Transport } from "../models/geo";
import { formatDelay } from "../utils";
import ListViewCell from "@/components/ListViewCell.vue";
import { UserManagementModule } from "@/store/modules/usermgmt";

@Component({
  name: "MapCell",
  components: {
    ListViewCell
  }
})
export default class MapCell extends Vue {
  @Prop() private transport!: Transport;
  @Prop() private tracked?: string;
  @Prop() private shouldNotify?: boolean;
  private enableMitigate: boolean = false;
  private notifyEnabled: boolean = this.shouldNotify ?? false;

  get isTracked(): boolean {
    return this.tracked === this.transport.id;
  }

  get delay(): string {
    return formatDelay(this.transport.delay);
  }

  get delayThresholds(): { hard: number; soft: number } {
    return UserManagementModule.delayThresholds;
  }

  trackTransport() {
    this.$emit(this.isTracked ? "untrack" : "track", this.transport);
  }

  mitigate() {
    this.$emit("mitigate", this.transport);
  }

  notify() {
    this.notifyEnabled = !this.notifyEnabled;
    this.$emit("notify", this.transport, this.notifyEnabled);
  }

  handleHoverEnter() {
    this.$emit("hover:start", this.transport);
  }

  handleHoverLeave() {
    this.$emit("hover:end", this.transport);
  }
}
</script>

<style lang="sass" scoped>
$cell-preview-height: 75px
$cell-height: 180px

.map-cell-container
  position: relative
  overflow: hidden
  border-radius: 7px
  +transition(all 0.2s ease-in)
  height: $cell-preview-height
  display: inline-block

  &:hover, &.tracked
    height: $cell-height

  .progress
    +theme-color-diff(background-color, bgc-body, 20)
    div
      +theme-color-diff(background-color, bgc-body, 70)
      +transition(all 0.2s ease-in)

  table
    width: 100%
    td
      font-size: 15px
      padding: 0
      vertical-align: middle
      span
        display: inline-block

      .status, .eta
        float: right
        font-size: 18px

      .eta
        font-weight: bold

      .status
        // +theme(color, c-delay-fine)
        +theme-color-diff(color, bgc-body, 50)
        &.late
          +theme(color, c-delay-warning)
        &.very-late
          +theme(color, c-delay-alert)

      .eta
        font-size: 13px

      .position
        +theme(color, c-map-position-text)
        font-weight: 500
        font-size: 17px

      a
        color: inherit

      .route
        +transition(all 0.1s ease-in)
        line-height: 20px
        padding-right: 5px
        +theme-color-diff(color, bgc-body, 50)
        .new-tab
          font-size: 10px
          opacity: 0
        &:hover
          color: inherit
          .new-tab
            opacity: 1

      .cepta-id
        font-size: 7px
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
</style>
