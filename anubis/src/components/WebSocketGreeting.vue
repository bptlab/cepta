<template>
  <div>
    <div id="main-content" class="container">
      <div class="row">
        <div class="col-md-12">
          <form class="form-inline">
            <div class="form-group">
              <label for="name">What is your name?</label>
              <input type="text" id="name" class="form-control" v-model="sendMessage" placeholder="Your name here...">
              <button id="send" class="btn btn-outline-primary" type="submit" @click.prevent="send">Send</button>
            </div>
          </form>
        </div>
      </div>
      <div class="row">
        <div class="col-md-12">
          <table id="conversation" class="table table-striped">
            <thead>
            <tr>
              <th>Greetings</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in receivedMessages" :key="item">
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
import Stomp from "webstomp-client";

export default {
  name: "WebSocketGreeting",
  data() {
    return {
      receivedMessages: [],
      sendMessage: null,
      stompClient: null
    };
  },
  methods: {
    send() {
      console.log("Send message:" + this.sendMessage);
      if (this.stompClient && this.stompClient.connected) {
        const msg = { name: this.sendMessage };
        this.stompClient.send("/app/hello", JSON.stringify(msg), {});
      }
    },
    connect(socket){
      this.stompClient = Stomp.over(socket);
      this.stompClient.connect(
          {},
          frame => {
            console.log(frame);
            this.stompClient.subscribe("/topic/greetings", tick => {
              console.log(tick);
              this.receivedMessages.push(JSON.parse(tick.body).content);
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

#conversation
  margin-top: 15px

#name
  margin: 0px 10px
</style>
