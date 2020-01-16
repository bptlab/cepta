import Vue from "vue";
import store from "@/store";
import VueRouter from "vue-router";
import Adminator from "@/views/Adminator.vue";
import Error from "@/views/Error.vue";
import Landing from "@/views/Landing.vue";

Vue.use(VueRouter);

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

const routes = [
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
    path: "/dashboard",
    redirect: "/dashboard/websockets",
    meta: { requiresAuth: true },
    beforeEnter: ifAuthenticated,
    component: Adminator,
    children: [
      {
        path: "dashboard",
        name: "dashboard",
        meta: { requiresAuth: true },
        beforeEnter: ifAuthenticated,
        component: () =>
          import(/* webpackChunkName: "dashboard" */ "@/views/Dashboard.vue")
      },

      {
        path: "websockets",
        name: "websockets",
        meta: { requiresAuth: true },
        beforeEnter: ifAuthenticated,
        component: () =>
          import(/* webpackChunkName: "about" */ "@/views/WebSocket.vue")
      },
      {
        path: "blank",
        name: "blank",
        meta: { requiresAuth: true },
        beforeEnter: ifAuthenticated,
        component: () =>
          import(/* webpackChunkName: "about" */ "@/views/Blank.vue")
      },


      {
        path: "traindata/:id",
        name: "traindata",
        props: true,
        meta: { requiresAuth: true },
        beforeEnter: ifAuthenticated,
        component: () =>
            import(/* webpackChunkName: "about" */ "@/views/Traindata.vue")
      },
      {
        path: "traindatainfo",
        name: "traindatainfo",
        meta: { requiresAuth: true },
        beforeEnter: ifAuthenticated,
        component: () =>
            import(/* webpackChunkName: "about" */ "@/views/TraindataInfo.vue")
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
        component: () =>
          import(/* webpackChunkName: "about" */ "@/views/404.vue")
      },
      {
        path: "500",
        name: "error500",
        component: () =>
          import(/* webpackChunkName: "about" */ "@/views/500.vue")
      }
    ]
  },
  // Default fallback
  { path: "*", redirect: { name: "error404" } }
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes
});

export default router;
