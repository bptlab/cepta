<template>
  <masonry-layout title="WebSockets">
    <masonry-layout-tile section="Data Feed">
      <web-socket-data-feed ref="feed"></web-socket-data-feed>
    </masonry-layout-tile>

  </masonry-layout>
</template>

<script>
import WebSocketDataFeed from "@/components/WebSocketDataFeed.vue";
import { Component, Vue } from 'vue-property-decorator';
import MasonryLayout from "@/components/MasonryLayout";
import MasonryLayoutTile from "@/components/MasonryLayoutTile";

@Component({
  name: "WebSocket",
  components: {
    WebSocketDataFeed,
    MasonryLayout,
    MasonryLayoutTile
  }
})
export default class Websocket extends Vue {

};
</script>

<style lang="sass"></style>
