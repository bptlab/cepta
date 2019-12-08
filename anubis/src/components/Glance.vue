<template>
  <div class="layer w-100 glance-chart-container">
    <div class="peers ai-sb fxw-nw pos-r">
      <div class="peer peer-greed">
        <bar-chart
          class="glance-chart pos-r"
          :height="height"
          :width="width"
          :chart-data="ChartData"
          :options="options"
        >
        </bar-chart>
      </div>
      <div class="glance-percent peer pos-a centerY">
        <span
          class="d-ib lh-0 va-m fw-600 bdrs-10em pX-15 pY-15"
          :class="percentLabelColor"
          :style="labelColor ? 'background-color: ' + labelColor : ''"
          >{{ percentLabel }}%</span
        >
      </div>
    </div>
  </div>
</template>

<script>
import BarChart from "@/components/BarChart";

export default {
  components: { BarChart },
  data() {
    return {
      options: {
        responsive: false,
        tooltips: {
          enabled: false
        },
        animation: {
          animateScale: true
        },
        legend: {
          showTooltips: false,
          display: false,
          labels: {
            display: false
          }
        },
        layout: {
          padding: 1
        },
        scales: {
          yAxes: [
            {
              display: false,
              gridLines: {
                drawBorder: false,
                display: false
              }
            }
          ],
          xAxes: [
            {
              categoryPercentage: 1.0,
              barPercentage: 0.9,
              display: false,
              // maxBarThickness: 10,
              // minBarLength: 2,
              gridLines: {
                drawBorder: false,
                display: false
              }
            }
          ]
        }
      }
    };
  },
  props: {
    width: {
      type: Number,
      default: 100
    },
    height: {
      type: Number,
      default: 40
    },
    samples: {
      type: Array,
      default: function() {
        return [];
      }
    },
    maxSamples: {
      type: Number,
      default: 15
    },
    percent: {
      type: Number,
      default: 50
    },
    fillColor: {
      type: String,
      default: "blue"
    },
    hoverColor: {
      type: String,
      default: "red"
    },
    labelColor: {
      type: String,
      default: null
    },
    prefix: {
      type: String,
      default: null
    }
  },
  computed: {
    isPositive() {
      return Math.sign(this.percent) > 0;
    },
    percentSign() {
      return this.isPositive ? "+" : "";
    },
    percentLabelColor() {
      if (this.labelColor) return;
      else if (this.prefix) return "bgc-purple-50 c-purple-500";
      else if (this.isPositive) return "bgc-green-50 c-green-500";
      return "bgc-red-50 c-red-500";
    },
    percentLabel() {
      if (this.prefix) return this.prefix + this.percent;
      return this.percentSign + this.percent;
    },
    sampleData() {
      return this.samples.slice(
        Math.max(0, this.samples.length - this.maxSamples),
        this.samples.length
      );
    },
    ChartData() {
      return {
        labels: new Array(this.sampleData.length).fill("Sometime"),
        datasets: [
          {
            data: this.sampleData,
            label: "a timeseries",
            backgroundColor: this.fillColor,
            hoverBackgroundColor: this.hoverColor
          }
        ]
      };
    }
  }
};
</script>

<style scoped lang="sass">
.glance-chart-container
  display: inline-block
  overflow: visible
  .glance-percent
    right: 0
  .glance-chart
    overflow: visible
    canvas
      overflow: visible
</style>
