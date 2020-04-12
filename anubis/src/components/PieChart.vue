<script lang="ts">
import { Component, Mixins, Prop, Vue } from "vue-property-decorator";
import { Pie, mixins } from "vue-chartjs";
const { reactiveProp } = mixins;

@Component({
  name: "PieChart",
  components: {
    PieChart
  }
})
export default class PieChart extends Mixins(Pie, reactiveProp) {
  @Prop() private options?: Object;

  mounted() {
    // this.chartData is created in the mixin.
    // If you want to pass options please create a local options object
    this.renderChart(this.chartData, this.options);
  }
}
</script>
