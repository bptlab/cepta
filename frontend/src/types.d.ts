import Vue, { ComponentOptions } from "vue";
import { AxiosStatic } from "axios";

declare module "vue/types/options" {
  interface ComponentOptions<V extends Vue> {
    nprogress?: any;
    axios?: AxiosStatic;
  }
}
