import Vue, { ComponentOptions } from "vue";
import { AxiosStatic } from "axios";
import { Action, Getter, Mutation, ModuleTree, Store } from "vuex";
import VueRouter from "vue-router";

declare module "vue/types" {
  export interface ActionTree<S, R> {
    [key: string]: Action<S, R>;
  }

  export interface MutationTree<S> {
    [key: string]: Mutation<S>;
  }

  export interface GetterTree<S, R> {
    [key: string]: Getter<S, R>;
  }

  export interface Module<S, R> {
    namespaced?: boolean;
    state?: S | (() => S);
    getters?: GetterTree<S, R>;
    actions?: ActionTree<S, R>;
    mutations?: MutationTree<S>;
    modules?: ModuleTree<R>;
  }

  export interface VueApollo {}
}

declare module "vue/types/vue" {
  interface Vue {
    $router: VueRouter;
    $apollo: object;
    apollo: object;
  }
}

declare module "vue/types/options" {
  interface ComponentOptions<V extends Vue> {
    nprogress?: any;
    axios?: AxiosStatic;
    apollo?: object;
  }
}
