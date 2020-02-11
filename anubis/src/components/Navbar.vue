<template>
  <div class="header navbar">
    <div class="header-container">
      <!-- Leftmost search bar and sidebar toggle -->
      <ul class="nav-left">
        <li>
          <a
            id="sidebar-toggle"
            class="sidebar-toggle"
            @click.prevent="toggleSidebar"
          >
            <i class="icon-menu"></i>
          </a>
        </li>
        <!-- Search -->
        <li class="search-box" :class="{ active: searchToggled }">
          <a class="search-toggle no-pdd-right" @click.prevent="toggleSearch">
            <i class="search-icon icon-search pdd-right-10"></i>
            <i class="search-icon-close icon-close pdd-right-10"></i>
          </a>
        </li>
        <li
          class="search-input"
          @keyup.enter="checkForUpdate()"
          :class="{ active: searchToggled }"
        >
          <input
            class="form-control"
            ref="searchInput"
            v-model="search"
            type="text"
            placeholder="Search..."
          />
        </li>
      </ul>
      <!-- Rightmost notifications and account -->
      <!-- Notifications -->
      <ul class="nav-right">
        <navbar-dropdown>
          <template v-slot:icon>
            <span
              id="replayBtn"
              :class="{ btn: true, 'btn-danger': isReplaying }"
            >
              {{ replayStatus }}
              <beat-loader
                class="inline-spinner"
                v-show="isReplaying"
                :color="'#ffffff'"
                :size="'8px'"
              ></beat-loader>
            </span>
          </template>
          <template v-slot:content>
            <form>
              <div class="input-group">
                <div class="input-group-prepend">
                  <div class="input-group-text" id="btnGroupAddon">ERRID</div>
                </div>
                <input
                  type="text"
                  v-model="replayIdsInput"
                  class="form-control"
                  id="erridInput"
                  placeholder="82734629"
                  aria-describedby="btnGroupAddon"
                />
              </div>
              <div class="form-group">
                <label for="formControlRange"
                  >Frequency ({{
                    scaledReplaySpeed.toFixed(isConstantReplay ? 2 : 0)
                  }}{{ isConstantReplay ? "sec" : "x" }})</label
                >
                <input
                  type="range"
                  min="0"
                  max="100"
                  class="form-control-range"
                  id="formControlRange"
                  v-model="replaySpeed"
                />
              </div>
              <div class="form-group">
                <div class="form-check form-check-inline">
                  <input
                    class="form-check-input"
                    v-model="replayTypeInput"
                    type="radio"
                    name="inlineRadioOptions"
                    id="constantReplayCheckbox"
                    value="CONSTANT"
                  />
                  <label class="form-check-label" for="constantReplayCheckbox"
                    >Constant</label
                  >
                </div>
                <div class="form-check form-check-inline">
                  <input
                    class="form-check-input"
                    v-model="replayTypeInput"
                    type="radio"
                    name="inlineRadioOptions"
                    id="proportionalReplayCheckbox"
                    value="PROPORTIONAL"
                  />
                  <label
                    class="form-check-label"
                    for="proportionalReplayCheckbox"
                    >Proportional</label
                  >
                </div>
              </div>
              <div class="form-group">
                <button
                  @click.prevent="toggleReplay"
                  id="toggleReplayButton"
                  :class="['btn', isReplaying ? 'btn-danger' : 'btn-dark']"
                >
                  {{ isReplaying ? "Stop" : "Start" }}
                </button>
                <button
                  @click.prevent="updateReplay"
                  id="updateReplayButton"
                  class="btn btn-info"
                  :disabled="!replayerConfigChanged"
                >
                  Apply
                </button>
                <button
                  @click.prevent="resetReplay"
                  id="resetReplayButton"
                  class="btn btn-danger"
                >
                  Reset
                </button>
              </div>
            </form>
          </template>
        </navbar-dropdown>
        <notifications-dropdown
          title="Notifications"
          :number="2"
          more="notifications"
        >
          <template v-slot:icon>
            <i class="icon-bell"></i>
          </template>
          <template v-slot:entries>
            <notification-dropdown-element
              headline="A new user signed up for the platform"
              sub-headline="13 mins ago"
            />
            <notification-dropdown-element
              headline="ERRID 777 arrived 10 minutes later than predicted in Berlin - Hauptbahnhof"
              sub-headline="20 mins ago"
            />
          </template>
        </notifications-dropdown>

        <!-- User account -->
        <account-dropdown
          username="Admin"
          picture="https://randomuser.me/api/portraits/lego/5.jpg"
        />
      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import NotificationsDropdown from "@/components/NotificationsDropdown.vue";
import NotificationDropdownElement from "@/components/NotificationDropdownElement.vue";
import EmailDropdownElement from "@/components/EmailDropdownElement.vue";
import AccountDropdown from "@/components/AccountDropdown.vue";
import NavbarDropdown from "@/components/NavbarDropdown.vue";
import { GrpcModule } from "../store/modules/grpc";
import { AppModule } from "../store/modules/app";
import axios from "axios";
import BeatLoader from "vue-spinner/src/BeatLoader.vue";
import NavigationBarDropdownElement from "@/components/NavbarDropdownElement.vue";
import {
  Speed,
  ReplayStartOptions,
  ReplayType,
  ReplayTypeOption
} from "@/generated/protobuf/replayer_pb";

@Component({
  name: "NavigationBar",
  components: {
    NavbarDropdown,
    BeatLoader,
    NotificationsDropdown,
    NotificationDropdownElement,
    EmailDropdownElement,
    AccountDropdown
  }
})
export default class NavigationBar extends Vue {
  searchToggled: boolean = false;
  search: any = null;
  replaySpeed: number = 0;
  replayIdsInput: string = "";
  defaultReplayType: ReplayType =
    ReplayType[Object.keys(ReplayType)[0] as keyof typeof ReplayType];
  replayTypeInput: string = Object.keys(ReplayType)[0];

  private equalArrays(a1: string[], a2: string[]): boolean {
    return (
      a1.length === a2.length && a1.sort().every((v, i) => v === a2.sort()[i])
    );
  }

  get replayerConfigChanged() {
    let idsChanged = !this.equalArrays(this.replayingIds, this.replayIds);
    let speedChanged = !(
      (this.replayingSpeed?.getSpeed() || 0) === this.replaySpeed
    );
    let typeChanged = !(this.replayingType === this.replayType);
    return idsChanged || speedChanged || typeChanged;
  }

  get replayIds(): string[] {
    return (
      this.replayIdsInput
        ?.trim()
        ?.split(",")
        ?.filter(e => e.length > 0) || []
    );
  }

  get replayType(): ReplayType {
    let index = this.replayTypeInput
      ?.trim()
      ?.toUpperCase() as keyof typeof ReplayType;
    return ReplayType[index] === undefined
      ? this.defaultReplayType
      : ReplayType[index];
  }

  get isReplaying() {
    return GrpcModule.isReplaying;
  }

  get replayStatus() {
    return GrpcModule.replayStatus;
  }

  get replayingIds() {
    return GrpcModule.replayingOptions?.getIdsList();
  }

  get replayingType() {
    return GrpcModule.replayingOptions?.getType();
  }

  get replayingSpeed() {
    return GrpcModule.replayingOptions?.getSpeed();
  }

  get isConstantReplay(): boolean {
    return this.replayType === ReplayType.CONSTANT;
  }

  get replayFrequencyMin(): number {
    return this.isConstantReplay ? 0.0 : 1.0;
  }

  get replayFrequencyMax(): number {
    return this.isConstantReplay ? 5.0 : 50000.0;
  }

  get scaledReplaySpeed(): number {
    return (
      (this.replayFrequencyMin +
        (this.replaySpeed / 100) *
          (this.replayFrequencyMax - this.replayFrequencyMin)) |
      0
    ); // Bitwise-OR the value with zero to get int
  }

  get replayOptions() {
    let options = new ReplayStartOptions();
    let errids = this.replayIds || new Array<string>();
    options.setIdsList(errids);
    options.setType(this.replayType);
    if (this.scaledReplaySpeed) {
      let speed = new Speed();
      speed.setSpeed(this.scaledReplaySpeed);
      options.setSpeed(speed);
    }
    return options;
  }

  toggleReplay() {
    GrpcModule.toggleReplayer(this.replayOptions);
  }

  updateReplay() {
    GrpcModule.setReplayOptions(this.replayOptions);
  }

  resetReplay() {
    GrpcModule.resetReplayer();
  }

  toggleSearch() {
    this.searchToggled = !this.searchToggled;
    window.setTimeout(() => {
      // Focus the input
      (this.$refs["searchInput"] as HTMLElement).focus();
    }, 0);
  }

  toggleSidebar() {
    AppModule.toggleCollapse();
    // @ts-ignore: No such attribute
    this.$redrawVueMasonry();
    setTimeout(() => {
      // @ts-ignore: No such attribute
      this.$redrawVueMasonry();
    }, 0.2 * 500);
  }

  mounted(): void {
    GrpcModule.queryReplayer().then(() => {
      this.replaySpeed = this.replayingSpeed;
    });
  }

  checkForUpdate() {
    let id = (this.$refs["searchInput"] as HTMLInputElement).value;
    // this.send(id)
    let reg = new RegExp("^[0-9]*$");
    debugger;

    //only Numbers update our list of train data
    if (reg.test(id)) this.$router.push({ name: "traindata", params: { id } });
  }
}
</script>

<style scoped lang="sass">

// TODO: Make scoped by adding styles to child components

#toggleReplayButton
  float: right

#resetReplayButton
  float: left

// ---------------------------------------------------------
// @TOC

// + @Topbar
// + @Collapsed State

// ---------------------------------------------------------
// @Topbar
// ---------------------------------------------------------

.inline-spinner
  display: inline-block
  position: relative

.header
  +theme(background-color, bgc-navbar)
  border-bottom: 1px solid $border-color
  display: block
  margin-bottom: 0
  padding: 0
  position: fixed
  transition: all 0.2s ease
  //width: calc(100% - #{$offscreen-size})
  width: 100%
  z-index: 800

  +to($breakpoint-md)
    width: 100%


  +between($breakpoint-md, $breakpoint-xl)
    width: calc(100% - #{$collapsed-size})


  .header-container
    +clearfix

    height: $header-height

    .nav-left,
    .nav-right
      list-style: none
      margin-bottom: 0
      padding-left: 0
      position: relative

      > li
        float: left

        > a
          color: $default-text-color
          display: block
          line-height: $header-height
          min-height: $header-height
          padding: 0 15px
          transition: all 0.2s ease-in-out

          i
            font-size: 17px

          &:hover,
          &:focus
            color: $default-dark
            text-decoration: none

          +to($breakpoint-md)
            padding: 0 15px

    .nav-left
      float: left
      margin-left: 15px
      transition: 0.2s ease

      +from($breakpoint-xl)
        margin-left: 230px

    .nav-right
      float: right
      margin-right: 10px
      margin-top: 15px

      #replayBtn
        float: left
        height: calc(#{$header-height} / 2)
        margin: 0
        margin-right: 20px
        width: auto

  .search-box
    .search-icon-close
      display: none

    &.active
      .search-icon
        display: none

      .search-icon-close
        display: inline-block

  .search-input
    display: none

    &.active
      display: inline-block

    input
      background-color: transparent
      border: 0
      box-shadow: none
      font-size: 18px
      height: 40px
      margin-top: 12px
      outline: none
      padding: 5px
      width: 500px

      +to($breakpoint-sm)
        width: 160px

      +to($breakpoint-xs)
        width: 85px


      +placeholder
        color: lighten($default-text-color, 20%)
        font-style: italic

// ---------------------------------------------------------
// @Collapsed State
// ---------------------------------------------------------

.is-collapsed
  .header
    width: calc(100% - #{$collapsed-size})

    +to($breakpoint-md)
      width: 100%


    +between($breakpoint-md, $breakpoint-xl)
      width: calc(100% - #{$offscreen-size})
</style>
