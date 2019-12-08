<script>
import { Line, mixins } from "vue-chartjs";
const { reactiveProp } = mixins;

export default {
  extends: Line,
  mixins: [reactiveProp],
  props: {
    options: {
      type: Object
    },
    fill: {
      type: String,
      default: function() {
        return "start";
      }
    }
  },
  mounted() {
    // this.chartData is created in the mixin.
    // If you want to pass options please create a local options object
    this.addPlugin({
      id: "filler"
    });
    this.chartData.datasets.forEach(element => {
      if (!element.hasOwnProperty("fill")) {
        element["fill"] = this.fill;
      }
    });
    // let merged = { ...this.chartData, ...this.fill };
    this.renderChart(this.chartData, this.options);
  }
};
</script>
