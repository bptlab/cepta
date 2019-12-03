<template>
  <div>
    <div id="main-content" class="container">
      <div class="row">
        <div class="col-md-12">
          <table id="conversation" class="table table-striped">
            <thead>
            <tr>
              <th>Feed</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in receivedUpdates" :key="item">
              <td>{{ item }}</td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: "WebSocketDataFeed",
  data() {
    return {
      receivedUpdates: [],
      stompClient: undefined
    };
  },
  methods: {
    connect(stompClient, url = "/queue/updates"){
      this.stompClient = stompClient;

      this.stompClient.connect(
          {},
          frame => {
            console.log(frame);
            this.stompClient.subscribe(url, update => {
              console.log(update);
              this.receivedUpdates.push(update);
            });
          },
          error => {
            console.log(error);
          }
      );
    },
  }
};
</script>

<style lang="sass">
#main-content
  height: 400px
  overflow-y: auto

</style>
