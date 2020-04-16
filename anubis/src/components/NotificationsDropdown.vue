<template>
  <li class="notifications dropdown">
    <!-- Toggle -->
    <a data-toggle="dropdown">
      <span class="counter">{{ number }}</span>
      <a class="dropdown-toggle no-after">
        <slot name="icon"></slot>
      </a>
    </a>
    <!-- Dropdown menu -->
    <ul class="dropdown-menu">
      <li class="pX-20 bdB">
        <i class="icon-bell pR-10"></i>
        <span class="fsz-sm fw-600">{{ title }}</span>
      </li>
      <li>
        <ul class="pos-r scrollable lis-n p-0 m-0 fsz-sm">
          <slot name="entries"></slot>
        </ul>
      </li>
      <li v-if="more" class="pX-20 ta-c bdT">
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
  height: $header-height
  cursor: pointer
  margin-left: 15px
  position: relative

  a
    transition: all 0.1s ease-in-out

  .dropdown-menu
    +theme(background-color, bgc-navbar)
    line-height: 35px
    +theme(color, c-default-text)
    left: auto
    right: 0
    min-width: 350px
    padding: 0

    +to($breakpoint-sm)
      max-width: 300px

    display: block
    margin: 0
    transform-origin: top right
    transform: scale(0, 0)

    .divider
      border-bottom-width: 1px
      border-bottom-style: solid
      +theme-color-diff(border-bottom-color, bgc-navbar, 6)
      height: 1px
      overflow: hidden

    > li
      padding: 2px 12px
      width: 100%

    li ul li, > li:last-child
      &:hover
        +theme-color-diff(background-color, bgc-navbar, 10)

    li ul li a
      +theme(color, c-default-link)

    a
      padding: 10px 15px

  .counter
    +theme(background-color, bgc-btn-default)
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

.show
  .dropdown-menu
    transform: scale(1, 1)
</style>
