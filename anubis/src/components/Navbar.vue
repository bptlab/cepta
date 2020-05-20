import {Topic} from "@/generated/protobuf/models/constants/Topic_pb";
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
            <i
              class="search-icon pdd-right-10"
              :class="isFilter ? 'icon-filter' : 'icon-search'"
            ></i>
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
            :placeholder="isFilter ? 'Filter...' : 'Search...'"
          />
          <div class="search-context">
            <ul class="results">
              <li>CPTA 1836458 <span class="icon icon-close"></span></li>
              <li>CPTA 1836458 <span class="icon icon-close"></span></li>
              <li>CPTA 1836458 <span class="icon icon-close"></span></li>
            </ul>
            <ul class="hints">
              <li>
                <div>Result 1 <span class="icon icon-plus"></span></div>
              </li>
            </ul>
          </div>
        </li>
      </ul>

      <!-- Rightmost notifications and account -->
      <!-- Notifications -->
      <ul class="nav-right">
        <!-- User account -->
        <account-dropdown
          username="Admin"
          picture="https://randomuser.me/api/portraits/lego/5.jpg"
        />

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

        <li>
          <a
            title="Change theme"
            id="themeToggleBtn"
            @click="toggleTheme"
            class="btn"
          >
            <span class="icon icon-palette"></span>
          </a>
        </li>

        <li>
          <a title="Reload" id="reloadBtn" @click="reload()" class="btn">
            <span
              class="icon icon-reload icon-flip-vertical"
              :class="{ 'icon-spin': isLoading }"
            ></span>
          </a>
        </li>

        <li v-if="monitorUrl">
          <!-- Alternativ: pulse bar-chart -->
          <a
            title="Open monitoring"
            target="_blank"
            rel="noopener noreferrer"
            :href="monitorUrl"
            id="monitorBtn"
            class="btn"
          >
            <span class="icon icon-pulse"></span>
          </a>
        </li>

        <navbar-dropdown>
          <template v-slot:icon>
            <a
              id="replayBtn"
              class="btn"
              :class="isReplaying ? 'btn-danger' : 'inactive-replayer'"
            >
              {{ replayStatus }}
              <beat-loader
                class="inline-spinner"
                v-show="isReplaying"
                :color="'#ffffff'"
                :size="'8px'"
              ></beat-loader>
            </a>
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
                  value="2"
                  class="form-control-range"
                  id="formControlRange"
                  v-model="replaySpeed"
                />
              </div>
              <div class="form-group">
                <div class="form-check form-check-inline">
                  <input
                    class="form-check-input"
                    v-model="replayModeInput"
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
                    v-model="replayModeInput"
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
                <div class="form-check form-check-inline">
                  <input
                    class="form-check-input"
                    v-model="replaySourceInput"
                    type="checkbox"
                    name="inlineCheckboxOptions"
                    id="livetraindataCheckbox"
                    value="LiveTrainData"
                    :disabled="disableSourceInput"
                  />
                  <label class="form-check-label" for="livetraindataCheckbox"
                    >LiveTrainData</label
                  >
                </div>
                <div class="form-check form-check-inline">
                  <input
                    class="form-check-input"
                    v-model="replaySourceInput"
                    type="checkbox"
                    name="inlineCheckboxOptions"
                    id="plannedtraindataCheckbox"
                    value="PlannedTrainData"
                    :disabled="disableSourceInput"
                  />
                  <label class="form-check-label" for="plannedtraindataCheckbox"
                    >PlannedTrainData</label
                  >
                </div>
              </div>
              <div class="form-group replayerOptionBtn">
                <button
                  @click.prevent="resetReplay"
                  id="resetReplayButton"
                  class="btn btn-danger"
                >
                  Reset
                </button>

                <button
                  @click.prevent="updateReplay"
                  id="updateReplayButton"
                  class="btn btn-primary"
                  :disabled="!replayerConfigChanged"
                >
                  Apply
                </button>

                <button
                  @click.prevent="toggleReplay"
                  id="toggleReplayButton"
                  :class="['btn', isReplaying ? 'btn-danger' : 'btn-dark']"
                >
                  {{ isReplaying ? "Stop" : "Start" }}
                </button>
              </div>
            </form>
          </template>
        </navbar-dropdown>

        <li>
          <a
            :title="
              connectionDegraded ? 'System degraded' : 'All systems operational'
            "
          >
            <span
              class="system-status"
              :class="{ degraded: connectionDegraded }"
            ></span>
          </a>
        </li>

        <li>
          <a class="time" @click="toggleTimezone">
            <span class="current-time">{{ currentTime }}</span
            ><span class="timezone">{{ timezones[timezone] }}</span>
          </a>
        </li>
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
import { ReplayerModule } from "../store/modules/replayer";
import { AppModule } from "../store/modules/app";
import BeatLoader from "vue-spinner/src/BeatLoader.vue";
import {
  ActiveReplayOptions,
  ReplayMode,
  ReplayOptions,
  ReplaySetOptionsRequest,
  ReplayStartOptions,
  SourceReplay,
  Speed
} from "../generated/protobuf/models/grpc/replayer_pb";
import { Topic } from "../generated/protobuf/models/constants/Topic_pb";

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
  replaySourceInput: Array<string> = [];
  disableSourceInput: boolean = false;
  replayIdsInput: string = "";
  defaultReplayMode: ReplayMode =
    ReplayMode[Object.keys(ReplayMode)[0] as keyof typeof ReplayMode];
  replayModeInput: string = Object.keys(ReplayMode)[0];
  currentTime: string = "";

  private equalArrays(a1?: string[], a2?: string[]): boolean {
    return a1 != undefined && a2 != undefined
      ? a1.length === a2.length && a1.sort().every((v, i) => v === a2.sort()[i])
      : false;
  }

  get connectionDegraded(): boolean {
    // TODO Implement
    return false;
  }

  get monitorUrl(): string | undefined {
    return process.env.MONITORING_URL;
  }

  get replayerConfigChanged() {
    let idsChanged = !this.equalArrays(this.replayingIds, this.replayIds);
    let speedChanged = !(
      (this.replayingSpeed?.getSpeed() || 0) === this.replaySpeed
    );
    let typeChanged = !(this.replayingType === this.replayMode);
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

  get replayMode(): ReplayMode {
    let index = this.replayModeInput
      ?.trim()
      ?.toUpperCase() as keyof typeof ReplayMode;
    return ReplayMode[index] === undefined
      ? this.defaultReplayMode
      : ReplayMode[index];
  }

  get isReplaying() {
    return ReplayerModule.isReplaying;
  }

  get isLoading() {
    return AppModule.isLoading;
  }

  get replayStatus() {
    return ReplayerModule.replayStatus;
  }

  get replayingIds(): string[] {
    return [
      ...new Set(
        ReplayerModule.replayingOptions
          ?.getSourcesList()
          ?.reduce((accumulator: string[], currentValue: SourceReplay) => {
            return accumulator.concat(currentValue.getIdsList());
          }, [] as string[])
      )
    ] as string[];
  }

  get replayingType() {
    return ReplayerModule.replayingOptions?.getOptions()?.getMode();
  }

  get replayingSpeed() {
    return ReplayerModule.replayingOptions?.getOptions()?.getSpeed();
  }

  get isConstantReplay(): boolean {
    return this.replayMode === ReplayMode.CONSTANT;
  }

  get replayFrequencyMin(): number {
    return this.isConstantReplay ? 1.0 : 1.0;
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

  get sourceReplayerArray(): Array<SourceReplay> {
    let sourceArray: Array<SourceReplay> = [];

    this.replaySourceInput.forEach(string => {
      switch (string) {
        case "LiveTrainData": {
          let source: SourceReplay = new SourceReplay();
          source.setSource(Topic.LIVE_TRAIN_DATA);
          sourceArray.push(source);
          break;
        }
        case "PlannedTrainData": {
          let source: SourceReplay = new SourceReplay();
          source.setSource(Topic.PLANNED_TRAIN_DATA);
          sourceArray.push(source);
          break;
        }
      }
    });

    return sourceArray;
  }

  get isFilter(): boolean {
    return this.$route.meta.useSearchToFilter as boolean;
  }

  get themeClass(): string {
    return AppModule.themeClass;
  }

  get replayStartOptions(): ReplayStartOptions {
    let options = new ReplayStartOptions();
    let errids = this.replayIds || new Array<string>();
    options.getSourcesList()?.forEach(s => s.setIdsList(errids));
    if (!options.hasOptions()) {
      options.setOptions(new ReplayOptions());
    }
    options.getOptions()?.setMode(this.replayMode);
    options.setSourcesList(this.sourceReplayerArray);
    if (this.scaledReplaySpeed) {
      let speed = new Speed();
      speed.setSpeed(this.scaledReplaySpeed);
      options.getOptions()?.setSpeed(speed);
    }
    this.disableSourceInput = true;
    return options;
  }

  get replayUpdateOptions() {
    let updateOptions = new ReplaySetOptionsRequest();
    let replayOptions = new ActiveReplayOptions();
    let options = this.replayStartOptions.getOptions();
    if (options) {
      replayOptions.setSpeed(options.getSpeed());
      replayOptions.setMode(options.getMode());
      replayOptions.setTimerange(options.getTimerange());
      replayOptions.setRepeat(options.getRepeat());
    }
    updateOptions.setOptions(replayOptions);
    return updateOptions;
  }

  dateLocale: string = "en-GB";
  timezone: number = 0;
  timezones: Array<string> = ["UTC", "EET", "WET", "CET"];
  timezonesLong: Array<string> = [
    "UTC",
    "Europe/Moscow",
    "Europe/Lisbon",
    "Europe/Berlin"
  ];

  toggleTimezone() {
    this.timezone = (this.timezone + 1) % this.timezones.length;
    this.newTime();
  }

  newTime() {
    var date = new Date();
    this.currentTime = date.toLocaleString(this.dateLocale, {
      timeZone: this.timezonesLong[this.timezone]
    });
  }

  updateTime() {
    this.newTime();
    setTimeout(this.updateTime, 1000);
  }

  reload() {
    AppModule.requestReload()
      .then(() => {})
      .catch(() => {});
  }

  toggleTheme() {
    AppModule.toggleTheme();
  }

  toggleReplay() {
    ReplayerModule.toggleReplayer(this.replayStartOptions);
  }

  updateReplay() {
    ReplayerModule.setReplayOptions(this.replayUpdateOptions);
  }

  resetReplay() {
    this.disableSourceInput = false;
    ReplayerModule.resetReplayer();
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
    this.updateTime();
    ReplayerModule.queryReplayer().then(() => {
      if (this.replayingSpeed != undefined)
        this.replaySpeed = this.replayingSpeed?.getSpeed();
    });
  }

  checkForUpdate() {
    let id = (this.$refs["searchInput"] as HTMLInputElement).value;
    let reg = new RegExp("^[0-9]*$");

    //only Numbers update our list of train data
    if (reg.test(id)) this.$router.push({ name: "traindata", params: { id } });
  }
}
</script>

<style scoped lang="sass">

// TODO: Make scoped by adding styles to child components

.header
  z-index: 1001 !important

  .replayerOptionBtn
    color: white
    .btn
      margin: 0px 20px

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
  border-bottom-width: 1px
  border-bottom-style: solid
  +theme-color-diff(border-bottom-color, bgc-navbar, 6)
  display: block
  margin-bottom: 0
  padding: 0
  position: fixed
  // transition: all 0.2s ease
  // width: calc(100% - #{$offscreen-size})
  width: 100%
  z-index: 800
  width: calc(100% - #{$offscreen-size})

  +to($breakpoint-md)
    width: 100%

  +between($breakpoint-md, $breakpoint-xl)
    width: calc(100% - #{$collapsed-size})

  .header-container
    +clearfix

    .btn
      color: inherit
      height: calc(#{$header-height} / 2)

    .nav-left,
    .nav-right
      list-style: none
      margin-bottom: 0
      padding-left: 0
      position: relative

      > li
        float: right
        transition: all 0.1s ease-in-out
        line-height: $header-height

        > a
          display: block
          line-height: $header-height
          height: $header-height
          padding: 0 15px

          i
            font-size: 17px

          +to($breakpoint-md)
            padding: 0 15px

        &:hover,
        &:focus
          +theme(color, c-accent-text)
          text-decoration: none

    .nav-left
      float: left
      width: calc(100% - 750px)
      padding-left: 15px
      transition: 0.2s ease
      li
        float: left

    .nav-right
      width: 750px
      float: right
      padding-right: 10px

      .time
        width: 190px
        padding: 0
        cursor: pointer

        .timezone
          padding-left: 10px


      .system-status
        width: 10px
        height: 10px
        border-radius: 5px
        background-color: green
        display: inline-block

        &.degraded
          background-color: red

      #replayBtn
        // margin: 0

        &.inactive-replayer:hover
          +theme(color, c-accent-text)
          // +theme-color-diff(background-color, bgc-navbar, 10)


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
    width: calc(100% - 100px)

    &.active
      display: inline-block

    input
      +theme(color, c-default-text)
      background-color: transparent
      border: 0
      box-shadow: none
      font-size: 18px
      height: 40px
      margin-top: 12px
      outline: none
      padding: 5px

      +to($breakpoint-sm)
        width: 160px

      +to($breakpoint-xs)
        width: 85px


      +placeholder
        color: lighten($default-text-color, 20%)
        font-style: italic

    .search-context
      padding-bottom: 15px
      .icon
        font-size: 0.7rem
      .results, .hints
        list-style: none
        width: 100%
        display: inline-block
        text-decoration: none
        margin: 0
        padding: 0

      .results>li, .hints>li>div
          +transition(all 0.2s ease-in)
          line-height: 20px
          vertical-align: middle
          padding: 2px 6px
          border-radius: 8px
          margin: 2px
          float: left
          cursor: pointer

    .search-context
      .results
        li
          +theme-color-diff(background-color, bgc-navbar, 10)
          &:hover
            +theme-color-diff(background-color, bgc-navbar, 20)

      .hints>li
        div
          border: 1px solid transparent
          +transition(all 0.2s ease-in)
          display: inline-block
          .icon
            opacity: 0
        &:hover
          div
            +theme-color-diff(border-color, bgc-navbar, 20)
            .icon
              opacity: 1

  #formControlRange
    appearance: none
    width: 100%
    height: 5px
    background: #d3d3d3
    outline: none
    opacity: 0.7
    -webkit-transition: .2s
    transition: opacity .2s

    &:hover
      opacity: 1

    &::-webkit-slider-thumb
      appearance: none
      width: 15px
      height: 15px
      border-radius: 50%
      background: #353535
      cursor: pointer

    &::-moz-range-thumb
      width: 15px
      height: 15px
      border-radius: 50%
      background: #353535
      cursor: pointer

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
