<template>
  <div v-masonry-tile :class="['item', layoutStyle]">
    <div :class="{ 'bgc-white p-20 bd': wrap }">
      <h6 class="c-grey-900">{{ section }}</h6>
      <div class="mT-20">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator'


  @Component({
    name: "MasonryLayoutTile",
    props: {
      section: {},
      layoutStyle: {
        type: Object,
        default: function() {
          return { "col-md-6": true }
        }
      },
      wrap: {
        type: Boolean,
        default: true
      }
    },
  })

  export default class MasonryLayoutTile extends Vue {
  computed:any = {};
  mounted() {}
};
</script>
