<template>
  <li class="notifications dropdown">
    <!-- Toggle -->
    <span class="counter bgc-red">{{ number }}</span>
    <a class="dropdown-toggle no-after" data-toggle="dropdown">
      <slot name="icon"></slot>
    </a>
    <!-- Dropdown menu -->
    <ul class="dropdown-menu">
      <li class="pX-20 pY-15 bdB">
        <i class="icon-bell pR-10"></i>
        <span class="fsz-sm fw-600 c-grey-900">{{ title }}</span>
      </li>
      <li>
        <ul class="ovY-a pos-r scrollable lis-n p-0 m-0 fsz-sm">
          <slot name="entries"></slot>
        </ul>
      </li>
      <li v-if="more" class="pX-20 pY-15 ta-c bdT">
        <span>
          <a class="c-grey-600 cH-blue fsz-sm td-n">
            View all {{ title }}
            <i class="icon-angle-right fsz-xs mL-10"></i>
          </a>
        </span>
      </li>
    </ul>
  </li>
</template>

<script lang="ts">

  import {Component, Vue} from "vue-property-decorator";

  @Component({
    name: "NotificationDropdown",
    props: {
      title: {
        type: String,
        default: "Notification Dropdown"
      },
      more: {
        type: String,
        default: null
      },
      number: {
        type: Number,
        default: 0
      }
    },
  })

@Component
export default class NotificationsDropdown extends Vue{


  open : boolean = false;

  watch : any = {};

  uuid : string = this.getuuId();

  getuuId() {
      return this.$props.title.replace(/\s+/g, "");
    }
  mounted() {}
};
</script>

<style scoped lang="sass">
.dropdown-menu
  left: auto
  right: 0

  > li
    width: 100%

    > a
      line-height: 1.5
      min-height: auto
      padding: 10px 15px

.notifications
  position: relative
  padding: 7px

  .counter
    background-color: $default-danger
    border-radius: 50px
    color: $default-white
    font-size: 10px
    line-height: 1
    padding: 3px 5.5px
    position: absolute
    right: 16px
    top: 16px
    opacity: 0.8

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
