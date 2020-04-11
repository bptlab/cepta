<template>
  <li class="notifications dropdown">
    <!-- Toggle -->
    <a data-toggle="dropdown">
      <span class="counter bgc-red">{{ number }}</span>
      <a class="dropdown-toggle no-after">
        <slot name="icon"></slot>
      </a>
    </a>
    <!-- Dropdown menu -->
    <ul class="dropdown-menu">
      <li class="pX-20 pY-15 bdB">
        <i class="icon-bell pR-10"></i>
        <span class="fsz-sm fw-600">{{ title }}</span>
      </li>
      <li>
        <ul class="ovY-a pos-r scrollable lis-n p-0 m-0 fsz-sm">
          <slot name="entries"></slot>
        </ul>
      </li>
      <li v-if="more" class="pX-20 pY-15 ta-c bdT">
        <span>
          <a class="cH-blue fsz-sm td-n">
            View all {{ title }}
            <i class="icon-angle-right fsz-xs mL-10"></i>
          </a>
        </span>
      </li>
    </ul>
  </li>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";
@Component({
  name: "NotificationDropdown"
})
export default class NotificationsDropdown extends Vue {
  @Prop({ default: "Notification Dropdown" }) private title!: string;
  @Prop({ default: null }) private more!: string;
  @Prop({ default: 0 }) private number!: number;

  open: boolean = false;

  watch: any = {};

  uuid: string = this.getuuId();

  getuuId() {
    return this.title.replace(/\s+/g, "");
  }
  mounted() {}
}
</script>

<style scoped lang="sass">
.dropdown
  display: block
  line-height: $header-height
  height: $header-height
  cursor: pointer
  margin-left: 15px

  .dropdown-menu
    left: auto
    right: 0

    > li
      width: 100%

      > a
        padding: 10px 15px

.notifications
  position: relative

  .counter
    background-color: $default-danger
    border-radius: 10px
    color: $default-white
    font-size: 10px
    line-height: 20px
    text-align: center
    display: block
    width: 20px
    height: 20px
    position: absolute
    left: calc(50% - 20px)
    bottom: calc(50% - 17px)

  .dropdown-menu
    min-width: 350px
    padding: 0

    +to($breakpoint-sm)
      max-width: 300px

.dropdown-menu
  display: block
  margin: 0
  transform-origin: top right
  transform: scale(0, 0)
  transition: transform 0.15s ease-out

  .divider
    border-bottom: 1px solid $border-color
    height: 1px
    overflow: hidden

  > li
    > a
      transition: all 0.2s ease-out

.show
  .dropdown-menu
    transform: scale(1, 1)
</style>
