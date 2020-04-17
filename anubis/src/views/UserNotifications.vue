<template>
  <div class="user-notifications">
    <div class="notification-details">
      <basic-table
        :table-data="notifications"
        :show-indices="false"
        :striped="false"
        :bordered="true"
        cellspacing="0"
      />
    </div>
  </div>
</template>

<script lang="ts">
import BasicTable from "../components/BasicTable.vue";
import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "UserNotifications",
  components: {
    BasicTable
  }
})
export default class UserNotifications extends Vue {
  protected notifications: { [key: string]: any }[] = [
    {
      ID: "43986033",
      Station: "MusterLocation",
      plannedETA: "2019-08-02 13:28:00",
      actualETA: "2019-08-02 13:58:00",
      delta: 30,
      reason: "Zug aus Vorleistung"
    },
    {
      ID: "43986033",
      Station: "ExampleTown",
      plannedETA: "2019-08-02 13:28:00",
      actualETA: "2019-08-02 13:58:00",
      delta: 30,
      reason: "Zug aus Vorleistung"
    },
    {
      ID: "43986033",
      Station: "NoWhereToFind",
      plannedETA: "2019-08-02 13:28:00",
      actualETA: "2019-08-02 13:58:00",
      delta: 30,
      reason: "Zug aus Vorleistung"
    },
    {
      ID: "43986033",
      Station: "RightHere",
      plannedETA: "2019-08-02 13:28:00",
      actualETA: "2019-08-02 13:58:00",
      delta: 30,
      reason: "Zug aus Vorleistung"
    },
    {
      ID: "43986033",
      Station: "LeftThere",
      plannedETA: "2019-08-02 13:28:00",
      actualETA: "2019-08-02 13:58:00",
      delta: 30,
      reason: "Zug aus Vorleistung"
    },
    {
      ID: "43986033",
      Station: "LeftThere",
      plannedETA: "2019-08-02 13:28:00",
      actualETA: "2019-08-02 13:58:00",
      delta: 30,
      reason: "Zug aus Vorleistung"
    },
    {
      ID: "43986033",
      Station: "LeftThere",
      plannedETA: "2019-08-02 13:28:00",
      actualETA: "2019-08-02 13:58:00",
      delta: 30,
      reason: "Zug aus Vorleistung"
    },
    {
      ID: "43986033",
      Station: "LeftThere",
      plannedETA: "2019-08-02 13:28:00",
      actualETA: "2019-08-02 13:58:00",
      delta: 30,
      reason: "Zug aus Vorleistung"
    },
    {
      ID: "43986033",
      Station: "LeftThere",
      plannedETA: "2019-08-02 13:28:00",
      actualETA: "2019-08-02 13:58:00",
      delta: 30,
      reason: "Zug aus Vorleistung"
    },
    {
      ID: "43986033",
      Station: "LeftThere",
      plannedETA: "2019-08-02 13:28:00",
      actualETA: "2019-08-02 13:58:00",
      delta: 30,
      reason: "Zug aus Vorleistung"
    },
    {
      ID: "43986033",
      Station: "LeftThere",
      plannedETA: "2019-08-02 13:28:00",
      actualETA: "2019-08-02 13:58:00",
      delta: 30,
      reason: "Zug aus Vorleistung"
    }
  ];
}
</script>
<style lang="sass" scoped>
.notification-details
  padding: 20px
</style>
