<template>
  <div class="tachometer-chart-container pos-r">
    <pie-chart
      class="tachometer-chart pos-r"
      :height="size"
      :width="size"
      :chart-data="chartData"
      :options="options"
    >
    </pie-chart>
    <span class="pos-a centerXY">{{
      label != undefined && label != null ? label : validPercent + "%"
    }}</span>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from "vue-property-decorator";
import PieChart from "@/components/PieChart.vue";

@Component({
  name: "TachometerChart",
  components: {
    PieChart
  }
})
export default class TachometerChart extends Vue {
  @Prop({ default: 80 }) private size!: number;
  @Prop({ default: 50 }) private percent!: number;
  @Prop({ default: 5 }) private thickness!: number;
  @Prop({ default: "blue" }) private fillColor!: string;
  @Prop({ default: "rgba(0,0,0,0.1)" }) private remainingColor!: string;
  @Prop() private label?: string;

  protected options = {
    responsive: false,
    animation: {
      animateRotate: true,
      animateScale: true
    },
    legend: {
      display: false
    },
    layout: {
      padding: 5
    },
    cutoutPercentage: 100
  };

  get validPercent() {
    return this.percent % 100;
  }

  get chartData() {
    return {
      datasets: [
        {
          data: [this.validPercent, 100 - this.validPercent],
          borderWidth: this.thickness,
          borderColor: [this.fillColor, this.remainingColor]
        }
      ]
    };
  }
}
</script>

<style scoped lang="sass">
.tachometer-chart-container
  display: inline-block
</style>
