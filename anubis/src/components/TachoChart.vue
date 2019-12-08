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
    <span class="pos-a centerXY">{{ validPercent }}%</span>
  </div>
</template>

<script>
import PieChart from "@/components/PieChart";

export default {
  name: "TachometerChart",
  components: { PieChart },
  data() {
    return {
      options: {
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
      }
    };
  },
  props: {
    size: {
      type: Number,
      default: 80
    },
    percent: {
      type: Number,
      default: 50
    },
    thickness: {
      type: Number,
      default: 5
    },
    fillColor: {
      type: String,
      default: "blue"
    },
    remainingColor: {
      type: String,
      default: "rgba(0,0,0,0.1)"
    }
  },
  computed: {
    validPercent() {
      return this.percent % 100;
    },
    chartData() {
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
};
</script>

<style lang="sass">
.tachometer-chart-container
  display: inline-block
</style>
