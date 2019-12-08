<template>
  <div>
    <line-chart :chart-data="dataCollection"></line-chart>
    <button
      type="button"
      class="btn btn-sm cur-p btn-danger"
      @click="fillData()"
    >
      Randomize
    </button>
  </div>
</template>

<script>
import LineChart from "@/components/LineChart";

export default {
  name: "RandomLineChart",
  components: {
    LineChart
  },
  data() {
    return {
      dataCollection: { labels: [], datasets: [] }
    };
  },
  mounted() {
    this.fillData();
  },
  methods: {
    fillData() {
      this.datacollection = {
        labels: [this.getRandomInt(), this.getRandomInt()],
        datasets: [
          {
            label: "Data One",
            backgroundColor: "#f87979",
            data: [this.getRandomInt(), this.getRandomInt()]
          },
          {
            label: "Data Two",
            backgroundColor: "#f85979",
            data: [this.getRandomInt(), this.getRandomInt()]
          }
        ]
      };
    },
    getRandomInt() {
      return Math.floor(Math.random() * (50 - 5 + 1)) + 5;
    }
  }
};
</script>

<style lang="sass"></style>
