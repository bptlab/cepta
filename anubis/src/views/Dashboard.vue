<template>
  <masonry-layout>
    <masonry-layout-tile section="Live Train Data Feed">
      <live-train-data-feed></live-train-data-feed>
    </masonry-layout-tile>
    <masonry-layout-tile section="Live Map">
      <visualisation></visualisation>
    </masonry-layout-tile>
    <masonry-layout-tile section="Train Data info">
      <traindata-info></traindata-info>
    </masonry-layout-tile>
  </masonry-layout>
</template>

<script lang="ts">
import LiveTrainDataFeed from "@/components/LiveTrainDataFeed.vue";
import Visualisation from "@/components/Visualisation.vue";
import TraindataInfo from "@/components/TraindataInfo.vue";
import { Component, Vue } from "vue-property-decorator";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";

@Component({
  name: "Dashboard",
  components: {
    LiveTrainDataFeed,
    TraindataInfo,
    Visualisation,
    MasonryLayout,
    MasonryLayoutTile
  }
})
export default class Dashboard extends Vue {}
</script>

<style lang="sass"></style>
