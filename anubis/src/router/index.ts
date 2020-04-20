import Vue from "vue";
import store from "@/store";
import Router, { Route, RouteConfig } from "vue-router";
import Main from "@/views/Main.vue";
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
          import(/* webpackChunkName: "about" */ "@/views/Login.vue")
      },
      {
        path: "/signup",
        name: "signup",
        component: () =>
          import(/* webpackChunkName: "about" */ "@/views/Signup.vue")
      }
    ]
  },

  {
    path: "/user",
    redirect: "/user/settings",
    meta: { requiresAuth: true },
    beforeEnter: ifAuthenticated,
    component: Main,
    children: [
      {
        path: "manage/transports",
        name: "manage",
        meta: { requiresAuth: true, useSearchToFilter: true },
        component: () =>
          import(
            /* webpackChunkName: "transportmanager" */ "@/views/TransportManager.vue"
          )
      },
      {
        path: "settings",
        name: "settings",
        meta: { requiresAuth: true },
        component: () =>
          import(
            /* webpackChunkName: "usersettings" */ "@/views/UserSettings.vue"
          )
      },
      {
        path: "profile/settings",
        name: "profile",
        meta: { requiresAuth: true },
        component: () =>
          import(
            /* webpackChunkName: "userprofilesettings" */ "@/views/UserProfileSettings.vue"
          )
      },
      {
        path: "notifications",
        name: "notifications",
        meta: { requiresAuth: true },
        component: () =>
          import(
            /* webpackChunkName: "usernotifications" */ "@/views/UserNotifications.vue"
          )
      }
    ]
  },
  {
    path: "/dashboard",
    redirect: "/dashboard/home",
    meta: { requiresAuth: true },
    beforeEnter: ifAuthenticated,
    component: Main,
    children: [
      {
        path: "home",
        name: "home",
        meta: { requiresAuth: true, useSearchToFilter: true },
        beforeEnter: ifAuthenticated,
        component: () =>
          import(/* webpackChunkName: "dashboard" */ "@/views/Dashboard.vue")
      },
      {
        path: "feed",
        name: "feed",
        meta: { requiresAuth: true },
        beforeEnter: ifAuthenticated,
        component: () =>
          import(/* webpackChunkName: "feed" */ "@/views/Feed.vue")
      },
      {
        path: "map",
        name: "map",
        meta: { requiresAuth: true, useSearchToFilter: true },
        beforeEnter: ifAuthenticated,
        component: () => import(/* webpackChunkName: "map" */ "@/views/Map.vue")
      },
      {
        path: "transport/:transport",
        name: "transport",
        props: true,
        meta: { requiresAuth: true },
        beforeEnter: ifAuthenticated,
        component: () =>
          import(
            /* webpackChunkName: "transportdetail" */ "@/views/TransportDetail.vue"
          )
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
