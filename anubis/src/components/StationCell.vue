<template>
  <div @mouseenter="handleHoverEnter()" @mouseleave="handleHoverLeave()">
    <list-view-cell class="cell" :class="{ passed: isPassed }">
      <div class="station-cell-container" :class="{ passed: isPassed }">
        <table>
          <tr>
            <td class="flag" rowspan="2">
              <span class="icon icon-flag-alt"></span>
            </td>
            <td>
              {{ station.position.stationName }}
            </td>
            <td rowspan="2">
              <div
                class="status"
                :class="{
                  late: delay > delayThresholds.soft,
                  'very-late': delay > delayThresholds.hard
                }"
              >
                {{ formatDelay(delay) }}
              </div>
            </td>
            <td>
              <div
                class="eta"
                :class="{
                  late: delay > delayThresholds.soft,
                  'very-late': delay > delayThresholds.hard
                }"
              >
                ETA 14:12
                <span
                  class="icon"
                  :class="
                    delay > delayThresholds.soft ? 'icon-alert' : 'icon-time'
                  "
                ></span>
              </div>
            </td>
            <td class="view" rowspan="2" @click="viewStation()">
              <span class="icon icon-target"></span>
              <span class="label"> View</span>
            </td>
          </tr>

          <tr>
            <td>
              <span class="station-id">{{ station.position.stationID }}</span>
            </td>
            <td>
              <div class="eta buffer">
                Buffer: 12min
                <span class="icon icon-split-h"></span>
              </div>
            </td>
          </tr>
        </table>
      </div>
    </list-view-cell>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from "vue-property-decorator";
import { formatDelay } from "../utils";
import ListViewCell from "@/components/ListViewCell.vue";
import { UserManagementModule } from "@/store/modules/usermgmt";
import { MapTripPosition } from "@/models/geo";

@Component({
  name: "StationCell",
  components: {
    ListViewCell
  }
})
export default class StationCell extends Vue {
  @Prop() private station!: MapTripPosition;

  get isPassed(): boolean {
    // TODO: Calculate based on current time
    return false;
  }

  get delay(): number {
    // TODO: Calculate from planned time and current time
    return 13;
  }

  formatDelay(delay: number): string {
    return formatDelay(delay);
  }

  get delayThresholds(): { hard: number; soft: number } {
    return UserManagementModule.delayThresholds;
  }

  viewStation() {
    this.$emit("view-station", this.station);
  }

  handleHoverEnter() {
    this.$emit("hover-start", this.station);
  }

  handleHoverLeave() {
    this.$emit("hover-end", this.station);
  }
}
</script>

<style lang="sass" scoped>
$cell-height: 60px

.cell, .station-cell-container
  padding: 0
  height: $cell-height
  +transition(all 0.2s ease-in)
  &.passed
    overflow: hidden
    height: 40px
    .buffer, .status, .station-id
      display: none


.station-cell-container
  position: relative
  border-radius: 7px
  width: 100%
  +theme-color-diff(background-color, bgc-body, 5)

  .view
    +transition(all 0.1s ease-in)
    width: 20px
    cursor: pointer
    position: relative
    line-height: 100%
    overflow: hidden
    color: white
    .icon
      left: 0
    .label
      position: absolute
      left: 30px
      opacity: 0

  &:hover
    +theme-color-diff(background-color, bgc-body, 10)
    .view
      width: 75px
      border-bottom-right-radius: 7px
      border-top-right-radius: 7px
      +theme(background-color, c-accent-text)
      .label
        opacity: 1

    &.passed
      .view
        +theme-color-diff(background-color, bgc-body, 10)

  .flag
    width: 40px
    border-bottom-left-radius: 7px
    border-top-left-radius: 7px
    +theme-color-diff(background-color, bgc-body, 3)

  table
    height: 100%
    width: 100%
    td
      font-size: 15px
      padding: 0 10px
      vertical-align: middle
      div, span
        // float: right
        display: inline-block

      .status, .eta
        display: inline-block
        float: right
        font-size: 18px

      .eta
        font-size: 13px
        font-weight: bold

        &.buffer
          font-size: 10px
          font-weight: lighter

      .status
        +theme(color, c-delay-fine)
        &.late
          +theme(color, c-delay-warning)
        &.very-late
          +theme(color, c-delay-alert)

      .position
        +theme(color, c-accent-text)
        font-weight: bold
        font-size: 17px

      a
        color: inherit

      .route
        line-height: 20px
        padding-right: 5px
        color: inherit
        .new-tab
          font-size: 10px
          opacity: 0
        &:hover
          .new-tab
            opacity: 1

      .station-id
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
</style>
