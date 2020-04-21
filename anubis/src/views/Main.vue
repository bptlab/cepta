<template>
  <div class="app" :class="{ 'is-collapsed': isCollapsed }">
    <!-- Sidebar -->
    <sidebar-component title="CEPTA" />

    <div class="page-container">
      <!-- Top Navigation bar -->
      <navbar-component />

      <!-- App Screen Content -->
      <div class="main-content">
        <transition name="fade" mode="out-in">
          <router-view />
        </transition>
      </div>
      <!-- App Screen Footer -->
      <footer-component>
        <span id="footer"
          >{{ version }} &#x24B8; CEPTA 2020
          <a href="https://github.com/bptlab/cepta"
            >GitHub <span class="icon icon-new-window"></span></a
        ></span>
      </footer-component>
    </div>
  </div>
</template>

<script lang="ts">
import Sidebar from "@/components/Sidebar.vue";
import Footer from "@/components/Footer.vue";
import NavigationBar from "@/components/Navbar.vue";
import { AppModule } from "@/store/modules/app";

import { Component, Vue, Watch } from "vue-property-decorator";
import { COOKIE_THEME } from "../constants";

@Component({
  name: "Main",
  components: {
    "sidebar-component": Sidebar,
    "footer-component": Footer,
    "navbar-component": NavigationBar
  }
})
export default class Main extends Vue {
  get isCollapsed() {
    return AppModule.isCollapsed;
  }

  get version() {
    return process.env.STABLE_VERSION;
  }

  mounted() {
    /*
      window.addEventListener("load", () => {
        if ($(".masonry").length > 0) {
          new Masonry(".masonry", {
            itemSelector: ".masonry-item",
            columnWidth: ".masonry-sizer",
            percentPosition: true
          });
        }
      });
      */
  }
}
</script>

<style scoped lang="sass">
.app
  height: 100%

#footer
  z-index: 1000
  line-height: calc(#{$footer-height} - 1px)
  vertical-align: middle
  display: inline-block

.page-container
  height: 100%

.main-content
  position: relative
  overflow-x: hidden
  +theme(background-color, bgc-content)
</style>
