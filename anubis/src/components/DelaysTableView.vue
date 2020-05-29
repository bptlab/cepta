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
import { Notification } from "../generated/protobuf/models/internal/notifications/notification_pb";

@Component({
  name: "DelaysTableView",
  components: {
    BasicTable,
    GridTable
  }
})
export default class DelaysTableView extends Vue {
  @Prop({ default: false }) enableSearchBar!: boolean;
  protected search!: string;

  get receivedUpdates(): { [key: string]: any }[] {
    return AppModule.notifications
      .slice(
        AppModule.notifications.length - 51,
        AppModule.notifications.length - 1
      )
      .map(mapDelayToStringKey);
  }
}

function mapDelayToStringKey(
  notification: Notification
): { [key: string]: any } {
  let delayStringKey: { [key: string]: any } = {
    CeptaStationID: notification.getDelay()?.getStationId(),
    CeptaID: notification.getDelay()?.getTransportId(),
    Delay: notification
      .getDelay()
      ?.getDelay()
      ?.getDelta(),
    Details: notification
      .getDelay()
      ?.getDelay()
      ?.getDetails(),
    Coordinates: notification.getDelay()?.getCoordinate()
  };

  return delayStringKey;
}
</script>

<style scoped lang="sass">
#mainContent
  padding: 0

.form-control
  width: 100%
  margin-bottom: 20px
</style>
