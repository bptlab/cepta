<template>
  <masonry-layout title="WebSockets">
    <!-- Random Line Chart -->
    <masonry-layout-tile section="Test Greeting">
      <web-socket-greeting ref="greeting"></web-socket-greeting>
    </masonry-layout-tile>

    <masonry-layout-tile section="Data Feed">
      <web-socket-data-feed ref="feed"></web-socket-data-feed>
    </masonry-layout-tile>

  </masonry-layout>
</template>

<script>
import WebSocketGreeting from "@/components/WebSocketGreeting.vue";
import WebSocketDataFeed from "@/components/WebSocketDataFeed.vue";
import MasonryLayout from "@/components/MasonryLayout";
import MasonryLayoutTile from "@/components/MasonryLayoutTile";
import SockJS from "sockjs-client";

export default {
  name: "WebSocket",
  components: {
    WebSocketDataFeed,
    WebSocketGreeting,
    MasonryLayout,
    MasonryLayoutTile
  },
  methods: {
    connect() {
      this.socket = new SockJS("http://localhost:8082/ws");
      this.$refs.greeting.connect(this.socket);
      this.$refs.feed.connect(this.socket);
    },
    disconnect() {
      this.$refs.greeting.stompClient.disconnect();
      this.$refs.feed.stompClient.disconnect();
    }
  },
  mounted() {
    this.connect();
  },
  destroyed() {
    this.disconnect();
  }
};
</script>

<style lang="sass"></style>
