<template>
  <div class="feed-container">
    <div class="row">
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
            <span class="type"
              >{{ capitalizeFirstLetter(data.type) }} Event</span
            >
            <span
              ><span class="icon-secondary icon icon-tag"></span
              >{{ capitalizeFirstLetter(data.tag) }}</span
            >
            <span
              ><span class="icon-secondary icon icon-location-pin"></span
              >{{ data.locationName }}</span
            >
            <span
              @click="toggleSpecialData(index)"
              class="icon icon-more"
            ></span>
          </li>
        </ul>
        <div class="extra-info" :class="{ show: showSpecialData }">
          <span @click="closeSpecialData" class="icon icon-close"></span>
          <div v-if="extraInfo.type == 'train'">
            <div>
              <span>Event time:</span> <span>{{ extraInfo.eventTime }}</span>
            </div>
            <div>
              <span>Event type:</span> <span>{{ extraInfo.type }}</span>
            </div>
            <div>
              <span>Tag:</span> <span>{{ extraInfo.tag }}</span>
            </div>
            <div>
              <span>Location name:</span>
              <span>{{ extraInfo.locationName }}</span>
            </div>
            <div>
              <span>Planned ETA:</span> <span>{{ extraInfo.plannedETA }}</span>
            </div>
            <div>
              <span>Delay:</span> <span>{{ extraInfo.delay }}</span>
            </div>
            <div>
              <span>Predicted ETA:</span>
              <span>{{ extraInfo.predictedETA }}</span>
            </div>
            <div>
              <span>Predicted Offset:</span>
              <span>{{ extraInfo.predictedOffset }}</span>
            </div>
          </div>
          <div v-if="extraInfo.type == 'weather'">
            <div>
              <span>Event time:</span> <span>{{ extraInfo.eventTime }}</span>
            </div>
            <div>
              <span>Event type:</span> <span>{{ extraInfo.type }}</span>
            </div>
            <div>
              <span>Tag:</span> <span>{{ extraInfo.tag }}</span>
            </div>
            <div>
              <span>Location name:</span>
              <span>{{ extraInfo.locationName }}</span>
            </div>
            <div>
              <span>Intensity:</span> <span>{{ extraInfo.intensity }}</span>
            </div>
            <div>
              <span>Event time:</span> <span>{{ extraInfo.eventTime }}</span>
            </div>
            <div>
              <span>expected Duration:</span>
              <span>{{ extraInfo.expectedDuration }}</span>
            </div>
          </div>
          <div v-if="extraInfo.type == 'location'">
            <div>
              <span>Event time:</span> <span>{{ extraInfo.eventTime }}</span>
            </div>
            <div>
              <span>Event type:</span> <span>{{ extraInfo.type }}</span>
            </div>
            <div>
              <span>Tag:</span> <span>{{ extraInfo.tag }}</span>
            </div>
            <div>
              <span>Location name:</span>
              <span>{{ extraInfo.locationName }}</span>
            </div>
            <div>
              <span>Country:</span> <span>{{ extraInfo.country }}</span>
            </div>
            <div>
              <span>Latitude:</span> <span>{{ extraInfo.latitude }}</span>
            </div>
            <div>
              <span>Longitude:</span> <span>{{ extraInfo.longitude }}</span>
            </div>
          </div>
        </div>
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
  showSpecialData: boolean = false;
  extraInfo: Object = this.eventData[0];
  lastExtraIndex: number = 0;

  mounted(): void {
    this.updateTime();
  }

  toggleSpecialData(index: number) {
    this.extraInfo = this.eventData[index];
    if (this.lastExtraIndex != index) {
      if (!this.showSpecialData) {
        this.showSpecialData = !this.showSpecialData;
      }
      this.lastExtraIndex = index;
    } else {
      this.showSpecialData = !this.showSpecialData;
    }
  }

  closeSpecialData() {
    this.showSpecialData = false;
  }

  updateTime() {
    const date = new Date();
    const time = date.toUTCString().slice(17, 22);
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
  position: relative
  height: fit-content
  display: table

  .timeline
    display: table-cell
    position: relative
    width: 10%
    padding-top: 10px

    .line
      position: absolute
      right: 0px
      height: 100%
      width: 4px
      background-color: #cdcdcd
      border-radius: 10px

    .time
      position: absolute
      right: 25px
      top: 5px
      font-size: 20px

  .circle
    border-radius: 10px
    background-color: #cdcdcd
    position: absolute

  .circle-big
    width: 20px
    height: 20px
    right: -8px

  .circle-small
    width: 10px
    height: 10px
    top: 18px
    left: -7px

  .event-list
    display: table-cell
    padding-top: 30px
    padding-right: 10%
    width: 80%

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
      box-shadow: 0 10px 20px rgba(0,0,0,0.1), 0 6px 6px rgba(0,0,0,0.1)

    .icon-secondary
      position: relative
      top: 2px
      font-size: 16px
      padding: 10px 10px

    .icon-more
      cursor: pointer

    .type
      justify-self: stretch
      border-right: 1px solid #a7a7a7
      font-size: 16px

    .eventTime
      position: absolute
      font-size: 12px
      left: -70px
      top: 15px


  .extra-info
    position: absolute
    width: 100%
    top: 50%
    left: 50%
    transform: translate(-50%, -50%)
    +theme-color-diff(background-color, bgc-body, 20)
    display: none
    box-shadow: 0 10px 20px rgba(0,0,0,0.1), 0 6px 6px rgba(0,0,0,0.1)
    padding: 20px

    div div
      display: grid
      grid-template-columns: 50% 50%

      span:first-child
        justify-self: right
        padding-right: 20px

  .show
    display: block

  .icon-close
    cursor: pointer
    position: absolute
    right: 0px

.row
  display: table-row
</style>
