<template>
  <div class="feed-container">
    <div class="timeline">
      <div class="circle circle-big"></div>
      <div class="line"></div>
      <div class="time">
        <span class="current-time">{{ currentTime }}</span>
      </div>
    </div>
    <div class="event-list">
      <ul>
        <li
          class="event"
          v-for="(data, index) in eventData"
          v-bind:key="'item-' + index"
        >
          <span class="eventTime">{{ cutDate(data.eventTime) }}</span>
          <span class="circle circle-small"></span>
          <span :class="['icon', 'icon-' + getIcon(data.type)]"></span>
          <span class="type">{{ capitalizeFirstLetter(data.type) }} Event</span>
          <span
            ><span class="icon-secondary icon icon-tag"></span
            >{{ capitalizeFirstLetter(data.tag) }}</span
          >
          <span
            ><span class="icon-secondary icon icon-location-pin"></span
            >{{ data.locationName }}</span
          >
          <span @click="toggleSpecialData(index)" class="icon icon-more"></span>
        </li>
      </ul>
      <div v-if="(specialEventData.type = 'train')">
        <span>{{ specialEventData.plannedETA }}</span>
        <span>{{ specialEventData.delay }}</span>
        <span>{{ specialEventData.predictedETA }}</span>
        <span>{{ specialEventData.predictedOffset }}</span>
      </div>
      <div v-if="(specialEventData.type = 'weather')">
        <span>{{ specialEventData.intensity }}</span>
        <span>{{ specialEventData.eventTime }}</span>
        <span>{{ specialEventData.expectedDuration }}</span>
      </div>
      <div v-if="(specialEventData.type = 'weather')">
        <span>{{ specialEventData.country }}</span>
        <span>{{ specialEventData.latitude }}</span>
        <span>{{ specialEventData.longitude }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
@Component({
  name: "Feed",
  components: {},
  props: {}
})
export default class User extends Vue {
  currentTime: string = "";
  eventData: Array<Object> = this.receiveData();
  specialEventData: Object = {};

  mounted(): void {
    this.updateTime();
  }

  toggleSpecialData(index: number) {
    this.specialEventData != this.eventData[index]
      ? (this.specialEventData = {})
      : (this.specialEventData = this.eventData[index]);
  }

  updateTime() {
    const date = new Date();
    const time = date.toUTCString().slice(17, 25);
    this.currentTime = time;
    setTimeout(this.updateTime, 1000);
  }

  capitalizeFirstLetter(string: string): string {
    return string.charAt(0).toUpperCase() + string.slice(1);
  }

  getIcon(string: string): string {
    switch (string) {
      case "train":
        return "truck";
      case "weather":
        return "shine";
      case "location":
        return "map-alt";
      default:
        return "";
    }
  }

  cutDate(string: string): string {
    return string.slice(11);
  }

  receiveData(): { [key: string]: string }[] {
    return [
      {
        type: "train",
        tag: "49054",
        locationName: "MusterLocation",
        plannedETA: "2019-08-02 13:28:00",
        eventTime: "2019-08-02 13:58:00",
        delay: "30",
        predictedETA: "2019-08-02 13:55:00",
        predictedOffset: "offset"
      },
      {
        type: "weather",
        tag: "storm",
        locationName: "Niemandsland",
        intensity: "5",
        eventTime: "2019-08-02 13:28:00",
        expectedDuration: "02:00:00"
      },
      {
        type: "train",
        tag: "49054",
        locationName: "MusterLocation",
        plannedETA: "2019-08-02 13:28:00",
        eventTime: "2019-08-02 13:58:00",
        delay: "30",
        predictedETA: "2019-08-02 13:55:00",
        predictedOffset: "offset"
      },
      {
        type: "train",
        tag: "49054",
        locationName: "MusterLocation",
        plannedETA: "2019-08-02 13:28:00",
        eventTime: "2019-08-02 13:58:00",
        delay: "30",
        predictedETA: "2019-08-02 13:55:00",
        predictedOffset: "offset"
      },
      {
        type: "weather",
        tag: "storm",
        locationName: "Niemandsland",
        intensity: "5",
        eventTime: "2019-08-02 13:28:00",
        expectedDuration: "02:00:00"
      },
      {
        type: "train",
        tag: "49054",
        locationName: "MusterLocation",
        plannedETA: "2019-08-02 13:28:00",
        eventTime: "2019-08-02 13:58:00",
        delay: "30",
        predictedETA: "2019-08-02 13:55:00",
        predictedOffset: "offset"
      },
      {
        type: "location",
        tag: "Addition",
        eventTime: "2019-08-02 13:58:00",
        locationName: "MustermannLocation",
        country: "Germany",
        latitude: "11.23,1122",
        longitude: "11.23,1122"
      },
      {
        type: "train",
        tag: "49054",
        locationName: "MusterLocation",
        plannedETA: "2019-08-02 13:28:00",
        eventTime: "2019-08-02 13:58:00",
        delay: "30",
        predictedETA: "2019-08-02 13:55:00",
        predictedOffset: "offset"
      },
      {
        type: "weather",
        tag: "storm",
        locationName: "Niemandsland",
        intensity: "5",
        eventTime: "2019-08-02 13:28:00",
        expectedDuration: "02:00:00"
      }
    ];
  }
}
</script>

<style lang="sass">
.feed-container
  min-height: 100%
  position: relative
  margin-left: 100px

  .timeline
    position: absolute
    left: 100px
    top: 10px
    height: 95%

    .line
      min-height: 100%
      width: 4px
      background-color: #cdcdcd
      border-radius: 10px

    .time
      position: absolute
      left: -100px
      top: -5px
      font-size: 20px

  .circle
    border-radius: 10px
    background-color: #cdcdcd
    position: absolute

  .circle-big
    width: 20px
    height: 20px
    top: 0px
    left: -8px

  .circle-small
    width: 10px
    height: 10px
    top: 18px
    left: -23px

  .event-list
    position: absolute
    left: 120px
    top: 30px
    width: 80%
    min-height: calc(100% - 30px)

    ul
      list-style: none
      padding: 0

  .icon
    font-size: 25px
    justify-self: end
    padding-right: 15px

  .event
    position: relative
    width: 100%
    display: inline-grid
    grid-template-columns: 5% 25% 15% 50% 5%
    justify-items: start
    align-items: center
    //border-top: 1px solid #cdcdcd
    padding: 10px
    font-size: 12px

    &:hover
      box-shadow: 3px 3px 5px #cdcdcd

    .icon-secondary
      position: relative
      top: 2px
      font-size: 16px
      padding: 10px 10px

    .type
      justify-self: stretch
      border-right: 1px solid #a7a7a7
      font-size: 16px

    .eventTime
      position: absolute
      font-size: 12px
      left: -80px
      top: 15px

  .hidden
    display: none
</style>
