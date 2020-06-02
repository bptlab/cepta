<template>
  <div class="dashboard-container">
    <masonry-layout>
      <masonry-layout-tile
        section="Statistics"
        class="stats"
        layoutStyle="col-md-12"
      >
        <!-- Pie Charts -->
        <div class="peers jc-sb ta-c">
          <div class="metric peer">
            <p>Active Transports</p>
            <p class="metric-value">23</p>
          </div>
          <div class="peer">
            <tacho-chart :size="80" :percent="75" fillColor="#f7007c" />
            <h6 class="">Delayed transports</h6>
          </div>
          <div class="peer">
            <tacho-chart
              :size="80"
              :percent="50"
              label="130"
              fillColor="#2196f3"
            />
            <h6 class="">Total delay minutes</h6>
          </div>
          <div class="peer">
            <tacho-chart :size="80" :percent="90" fillColor="#9400f7" />
            <h6 class="">Prediction Accuracy</h6>
          </div>
        </div>
      </masonry-layout-tile>

      <!--
      <masonry-layout-tile section="Train Data info">
      <traindata-info></traindata-info>
    </masonry-layout-tile>
    -->

      <!--
    <masonry-layout-tile section="Ranked Delays">
      <ranked-delay></ranked-delay>
    </masonry-layout-tile>
    -->

      <masonry-layout-tile
        class="delays-overview"
        section="Delays"
        layoutStyle="col-md-12"
      >
        <p>You can use the navigation bar to filter</p>
        <delays-table-view></delays-table-view>
        <div class="view-all">
          <router-link :to="{ name: 'map' }">
            <div class="btn">
              View all <span class="icon icon-new-window"></span>
            </div>
          </router-link>
        </div>
      </masonry-layout-tile>

      <!--
    <masonry-layout-tile section="Live Train Data Feed">
      <live-train-data-feed></live-train-data-feed>
    </masonry-layout-tile>
    <masonry-layout-tile section="Live Map">
      <p>Click for full page view</p>
      <map-visualisation></map-visualisation>
    </masonry-layout-tile>
    <masonry-layout-tile section="Ranked Delays">
      <ranked-delay></ranked-delay>
    </masonry-layout-tile>
    -->
    </masonry-layout>
  </div>
</template>

<script lang="ts">
import DelaysTableView from "@/components/DelaysTableView.vue";
import MapVisualisation from "@/components/MapVisualisation.vue";
import TraindataInfo from "@/components/TraindataInfo.vue";
import RankedDelay from "@/components/RankedDelay.vue";
import { Component, Vue } from "vue-property-decorator";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import TachoChart from "../components/TachoChart.vue";

@Component({
  name: "Dashboard",
  components: {
    DelaysTableView,
    RankedDelay,
    TraindataInfo,
    MapVisualisation,
    MasonryLayout,
    MasonryLayoutTile,
    TachoChart
  }
})
export default class Dashboard extends Vue {}
</script>

<style lang="sass">
.dashboard-container
  width: 100%

  .shadow-tile
    overflow: hidden
    margin-bottom: 20px

  .view-all
    // FIXME: I am not part of the container?!
    // position: relative
    // display: block
    float: right
    padding: 5px 0px 0px
    .btn
      +theme(color, c-default-text)
      border-radius: 5px

      &:hover
        +theme(background-color, bc-table-button)

      .icon
        margin-left: 5px
        font-size: 12px

  .metric
    p
      text-align: left
      vertical-align: top
      font-size: 1rem

      &.metric-value
        font-size: 2.5rem
</style>
