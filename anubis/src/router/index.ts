import Vue from "vue";
import store from "@/store";
import Router, { Route, RouteConfig } from "vue-router";
import Adminator from "@/views/Adminator.vue";
import Error from "@/views/Error.vue";
import Landing from "@/views/Landing.vue";

Vue.use(Router);

let authenticationRequired: boolean = false;

// Handle authentication
const ifNotAuthenticated = (to: any, from: any, next: any) => {
  if (store.getters.isAuthenticated || !authenticationRequired) {
    next();
    return;
  }
  next("/");
};

const ifAuthenticated = (to: any, from: any, next: any) => {
  if (store.getters.isAuthenticated || !authenticationRequired) {
    next();
    return;
  }
  next({ name: "login" });
};

export const routes: RouteConfig[] = [
  {
    path: "/",
    beforeEnter: ifAuthenticated,
    meta: { requiresAuth: true },
    redirect: "/dashboard"
  },
  {
    path: "/(login|signup)",
    redirect: "/dashboard",
    component: Landing,
    children: [
      {
        path: "/login",
        name: "login",
        beforeEnter: ifNotAuthenticated,
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () =>
          import(/* webpackChunkName: "login" */ "@/views/Login.vue")
      },
      {
        path: "/signup",
        name: "signup",
        component: () =>
          import(/* webpackChunkName: "signup" */ "@/views/Signup.vue")
      }
    ]
  },

  {
    path: "/user",
    redirect: "/user/home",
    meta: { requiresAuth: true },
    beforeEnter: ifAuthenticated,
    component: Adminator,
    children: [
      {
        path: "home",
        name: "home",
        meta: { requiresAuth: true },
        component: () =>
          import(/* webpackChunkName: "user" */ "@/views/User.vue")
      },
      {
        path: "settings",
        name: "settings",
        meta: { requiresAuth: true },
        component: () =>
          import(/* webpackChunkName: "settings" */ "@/views/UserSettings.vue")
      }
    ]
  },
  {
    path: "/dashboard",
    redirect: "/dashboard/websockets",
    meta: { requiresAuth: true },
    beforeEnter: ifAuthenticated,
    component: Adminator,
    children: [
      {
        path: "websockets",
        name: "websockets",
        meta: { requiresAuth: true },
        beforeEnter: ifAuthenticated,
        component: () =>
          import(/* webpackChunkName: "websocket" */ "@/views/WebSocket.vue")
      },
      {
        path: "blank",
        name: "blank",
        meta: { requiresAuth: true },
        beforeEnter: ifAuthenticated,
        component: () =>
          import(/* webpackChunkName: "blank" */ "@/views/Blank.vue")
      },
      {
        path: "traindata/:id",
        name: "traindata",
        props: true,
        meta: { requiresAuth: true },
        beforeEnter: ifAuthenticated,
        component: () =>
          import(/* webpackChunkName: "traindata" */ "@/views/Traindata.vue")
      }
    ]
  },

  // Error pages
  {
    path: "/error",
    redirect: "/error/404",
    component: Error,
    children: [
      {
        path: "404",
        name: "error404",
        component: () => import(/* webpackChunkName: "404" */ "@/views/404.vue")
      },
      {
        path: "500",
        name: "error500",
        component: () => import(/* webpackChunkName: "500" */ "@/views/500.vue")
      }
    ]
  },
  // Default fallback
  { path: "*", redirect: { name: "error404" } }
];

const createRouter = () =>
  new Router({
    mode: "history", // Disabled due to Github Pages doesn't support this, enable this if you need.
    scrollBehavior: (to, from, savedPosition) => {
      if (savedPosition) {
        return savedPosition;
      } else {
        return { x: 0, y: 0 };
      }
    },
    base: process.env.BASE_URL,
    routes: routes
  });

const router = createRouter();

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter();
  (router as any).matcher = (newRouter as any).matcher; // reset router
}

export default router;
