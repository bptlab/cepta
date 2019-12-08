<template>
  <masonry-layout title="Charts">
    <!-- Line Chart -->
    <masonry-layout-tile section="Line Chart">
      <line-chart
        :height="80"
        :chart-data="lineChart.data"
        :options="lineChart.options"
      />
    </masonry-layout-tile>

    <!-- Random Line Chart -->
    <masonry-layout-tile section="Random Line Chart">
      <random-line-chart />
    </masonry-layout-tile>

    <!-- Area Chart -->
    <masonry-layout-tile section="Area Chart">
      <area-chart
        :height="220"
        :chart-data="areaChart.data"
        :options="areaChart.options"
      />
    </masonry-layout-tile>

    <!-- Scatter Chart -->
    <masonry-layout-tile section="Scatter Chart">
      <scatter-chart
        :height="220"
        :chart-data="scatterChart.data"
        :options="scatterChart.options"
      />
    </masonry-layout-tile>

    <!-- Bar Chart -->
    <masonry-layout-tile section="Bar Chart">
      <bar-chart
        :height="220"
        :chart-data="barChart.data"
        :options="barChart.options"
      />
    </masonry-layout-tile>

    <!-- Vue Trends -->
    <masonry-layout-tile section="Vue Trends">
      <div class="peers ai-c jc-sb fxw-nw bdB pY-15">
        <div class="peer">
          <span>Smooth Trend</span>
        </div>
        <div class="peer">
          <trend
            :data="[0, 2, 5, 9, 5, 10, 3, 5, 2, 9, 0]"
            :gradient="['#6fa8dc', '#42b983', '#2c3e50']"
            auto-draw
            smooth
          >
          </trend>
        </div>
      </div>
      <div class="peers ai-c jc-sb fxw-nw bdB pY-15">
        <div class="peer">
          <span>Normal Trend</span>
        </div>
        <div class="peer">
          <trend :data="[0, 2, 5, 10, 3, 5, 1, 8, 0]" auto-draw> </trend>
        </div>
      </div>
      <div class="peers ai-c jc-sb fxw-nw bdB pY-15">
        <div class="peer">
          <span>Trend are just SVG's</span>
        </div>
        <div class="peer">
          <trend
            :data="[0, 2, 5, 10, 3, 5, 0]"
            stroke="green"
            stroke-width="10"
            stroke-linecap="round"
            stroke-dasharray="4 1"
            auto-draw
          >
          </trend>
        </div>
      </div>
      <div class="peers ai-c jc-sb fxw-nw pT-15">
        jQuery Sparklines have been removed from the Vue version in favor of Vue
        Trends.
      </div>
    </masonry-layout-tile>

    <!-- Pie Chart -->
    <masonry-layout-tile section="Pie Chart">
      <pie-chart
        :height="220"
        :chart-data="pieChart.newUsers.data"
        :options="pieChart.newUsers.options"
      />
    </masonry-layout-tile>

    <!-- Tachometers -->
    <masonry-layout-tile section="Tachometers">
      <div>
        <div class="peers mT-20 jc-sb ta-c">
          <div class="peer">
            <tachometer-chart :size="80" :percent="75" fillColor="#f44336" />
            <h6 class="fsz-sm">New Users</h6>
          </div>
          <div class="peer">
            <tachometer-chart :size="80" :percent="50" fillColor="#2196f3" />
            <h6 class="fsz-sm">New Purchases</h6>
          </div>
          <div class="peer">
            <tachometer-chart :size="80" :percent="65" fillColor="#f44336" />
            <h6 class="fsz-sm">New Customers</h6>
          </div>
          <div class="peer">
            <tachometer-chart :size="80" :percent="90" fillColor="#ff9800" />
            <h6 class="fsz-sm">Bounce Rate</h6>
          </div>
        </div>
      </div>
    </masonry-layout-tile>
  </masonry-layout>
</template>

<script>
import { COLORS } from "@/colors";

import MasonryLayout from "@/components/MasonryLayout";
import MasonryLayoutTile from "@/components/MasonryLayoutTile";

import RandomLineChart from "@/components/RandomLineChart";
import LineChart from "@/components/LineChart";
import AreaChart from "@/components/AreaChart";
import ScatterChart from "@/components/ScatterChart";
import BarChart from "@/components/BarChart";
import PieChart from "@/components/PieChart";
import TachometerChart from "@/components/TachoChart";

export default {
  name: "Charts",
  components: {
    TachometerChart,
    PieChart,
    BarChart,
    ScatterChart,
    LineChart,
    RandomLineChart,
    AreaChart,
    MasonryLayout,
    MasonryLayoutTile
  },
  data() {
    return {
      lineChart: {
        data: {
          labels: [
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July"
          ],
          datasets: [
            {
              label: "Series A",
              backgroundColor: "rgba(237, 231, 246, 0.5)",
              borderColor: COLORS["deep-purple-500"],
              pointBackgroundColor: COLORS["deep-purple-700"],
              borderWidth: 2,
              data: [60, 50, 70, 60, 50, 70, 60]
            },
            {
              label: "Series B",
              backgroundColor: "rgba(232, 245, 233, 0.5)",
              borderColor: COLORS["blue-500"],
              pointBackgroundColor: COLORS["blue-700"],
              borderWidth: 2,
              data: [70, 75, 85, 70, 75, 85, 70]
            }
          ]
        },

        options: {
          legend: {
            display: false
          }
        }
      },
      areaChart: {
        data: {
          labels: [
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July"
          ],
          datasets: [
            {
              backgroundColor: "rgba(3, 169, 244, 0.5)",
              borderColor: COLORS["light-blue-800"],
              data: [10, 50, 20, 40, 60, 30, 70],
              label: "Dataset"
            }
          ]
        },

        options: {}
      },
      scatterChart: {
        data: {
          datasets: [
            {
              label: "My First dataset",
              borderColor: COLORS["red-500"],
              backgroundColor: COLORS["red-500"],
              data: [
                { x: 10, y: 20 },
                { x: 30, y: 40 },
                { x: 50, y: 60 },
                { x: 70, y: 80 },
                { x: 90, y: 100 },
                { x: 110, y: 120 },
                { x: 130, y: 140 }
              ]
            },
            {
              label: "My Second dataset",
              borderColor: COLORS["green-500"],
              backgroundColor: COLORS["green-500"],
              data: [
                { x: 150, y: 160 },
                { x: 170, y: 180 },
                { x: 190, y: 200 },
                { x: 210, y: 220 },
                { x: 230, y: 240 },
                { x: 250, y: 260 },
                { x: 270, y: 280 }
              ]
            }
          ]
        },
        options: {}
      },
      barChart: {
        data: {
          labels: [
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July"
          ],
          datasets: [
            {
              label: "Dataset 1",
              backgroundColor: COLORS["deep-purple-500"],
              borderColor: COLORS["deep-purple-800"],
              borderWidth: 1,
              data: [10, 50, 20, 40, 60, 30, 70]
            },
            {
              label: "Dataset 2",
              backgroundColor: COLORS["light-blue-500"],
              borderColor: COLORS["light-blue-800"],
              borderWidth: 1,
              data: [10, 50, 20, 40, 60, 30, 70]
            }
          ]
        },
        options: {
          responsive: true,
          legend: {
            position: "bottom"
          }
        }
      },
      pieChart: {
        newUsers: {
          data: {
            labels: ["Red", "Yellow", "Blue"],
            datasets: [
              {
                data: [10, 20, 30],
                backgroundColor: [
                  COLORS["deep-purple-500"],
                  COLORS["light-blue-500"],
                  COLORS["pink-500"]
                ]
              }
            ]
          },
          options: {
            responsive: true
          }
        }
      }
    };
  },
  mounted() {}
};
</script>

<style lang="sass"></style>
