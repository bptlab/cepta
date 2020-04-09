<template>
  <div class="app" :class="{ 'is-collapsed': isCollapsed }">
    <!-- Sidebar -->
    <sidebar-component title="Cepta" />

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
        <span id="footer">{{ version }} &#x24B8; CEPTA 2020</span>
      </footer-component>
    </div>
  </div>
</template>

<script lang="ts">
import Sidebar from "@/components/Sidebar.vue";
import Footer from "@/components/Footer.vue";
import NavigationBar from "@/components/Navbar.vue";
import { AppModule } from "@/store/modules/app";

import { Component, Vue } from "vue-property-decorator";

@Component({
  name: "Adminator",
  components: {
    "sidebar-component": Sidebar,
    "footer-component": Footer,
    "navbar-component": NavigationBar
  }
})
export default class Adminator extends Vue {
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
#footer
  z-index: 1
  line-height: 30px
  vertical-align: middle
  display: inline-block

.main-content
  +theme(background-color, bgc-content)
  // height: 500px !important
</style>
