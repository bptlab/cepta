<template>
  <div class="app" :class="{ 'is-collapsed': isCollapsed }">
    <!-- Sidebar -->
    <sidebar-component title="Cepta" />

    <div class="page-container">
      <!-- Top Navigation bar -->
      <navbar-component />

      <!-- App Screen Content -->
      <main class="main-content bgc-grey-100">
        <transition id="mainContent" name="fade" mode="out-in">
          <router-view />
        </transition>
      </main>

      <!-- App Screen Footer -->
      <footer-component id="footer">
        &#x24B8; CEPTA 2020
      </footer-component>
    </div>
  </div>
</template>

<script>
import Sidebar from "../components/Sidebar";
import Footer from "../components/Footer";
import NavigationBar from "../components/Navbar";
import { AppModule } from "../store/modules/app";

export default {
  name: "Adminator",
  components: {
    "sidebar-component": Sidebar,
    "footer-component": Footer,
    "navbar-component": NavigationBar
  },
  props: {},
  data() {
    return {};
  },
  computed: {
    isCollapsed() {
      return AppModule.isCollapsed;
    }
  },
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
};
</script>

<style scoped lang="sass">
#footer
  z-index: 1
</style>
