<template>
  <div id="main-content">
    <input
      v-if="enableSearchBar"
      v:model="search"
      class="form-control"
      type="text"
      placeholder="Search.."
    />
    <grid-table :grid-data="receivedUpdates"></grid-table>
  </div>
</template>

<script lang="ts">
import BasicTable from "../components/BasicTable.vue";
import GridTable from "../components/GridTable.vue";
import { AppModule } from "../store/modules/app";
import { ReplayerModule } from "../store/modules/replayer";
import { Component, Vue, Prop } from "vue-property-decorator";
import {Notification} from "../generated/protobuf/models/internal/notifications/notification_pb";

@Component({
  name: "TransportTableView",
  components: {
    BasicTable,
    GridTable
  }
})
export default class DelaysTableView extends Vue {
  @Prop({ default: false }) enableSearchBar!: boolean;
  protected search!: string;

  protected receivedUpdates: { [key: string]: any }[]  =
          AppModule.notifications.slice(0, 5).map(this.mapDelayToStringKey);
  mapDelayToStringKey(noti){
    var ding = {
      CeptaStationID: noti.getDelay().getStationId(),
      CeptaID: noti.getDelay().getId(),
      Delay: noti.getDelay().getDelay()
    };
    console.log("KRAM");
    console.log(ding);
    return ding;
  };
}
</script>

<style scoped lang="sass">
#mainContent
  padding: 0

.form-control
  width: 100%
  margin-bottom: 20px
</style>
