import Vue from "vue";
import Router, { RouteConfig } from "vue-router";
import Main from "@/views/Main.vue";
import Error from "@/views/Error.vue";
import Landing from "@/views/Landing.vue";
import { AuthModule } from "@/store/modules/auth";
import { UserManagementModule } from "../store/modules/usermgmt";

Vue.use(Router);

let authenticationRequired: boolean = true;

const checkAuthenticated = (): boolean => {
  let token = Vue.cookies?.get("user-token") as string;
  let email = Vue.cookies?.get("user-email") as string;
  let userID = Vue.cookies?.get("user-id") as string;
  AuthModule.setAuthToken(token);
  AuthModule.setUserID(userID);
  UserManagementModule.setCurrentUserEmail(email);
  return (
    (token != undefined && token != null && token.length > 0) ||
    !authenticationRequired
  );
};

const checkNotAlreadyAuthenticated = (to: any, from: any, next: any) => {
  if (!checkAuthenticated()) {
    next();
    return;
  }
  next({ name: "dashboard" });
};

const requireAuthenticated = (to: any, from: any, next: any) => {
  if (checkAuthenticated()) {
    next();
    return;
  }
  next({ name: "login" });
};

export const routes: RouteConfig[] = [
  {
    path: "/(login|signup)",
    redirect: "/dashboard",
    component: Landing,
    children: [
      {
        path: "/login",
        name: "login",
        beforeEnter: checkNotAlreadyAuthenticated,
        component: () =>
          import(/* webpackChunkName: "login" */ "@/views/Login.vue")
      },
      {
        path: "/signup",
        name: "signup",
        beforeEnter: checkNotAlreadyAuthenticated,
        component: () =>
          import(/* webpackChunkName: "signup" */ "@/views/Signup.vue")
      }
    ]
  },
  {
    path: "/user",
    redirect: "/user/settings",
    beforeEnter: requireAuthenticated,
    component: Main,
    children: [
      {
        path: "manage/transports",
        name: "manage",
        meta: { useSearchToFilter: true },
        component: () =>
          import(
            /* webpackChunkName: "transportmanager" */ "@/views/TransportManager.vue"
          )
      },
      {
        path: "settings",
        name: "settings",
        component: () =>
          import(
            /* webpackChunkName: "usersettings" */ "@/views/UserSettings.vue"
          )
      },
      {
        path: "profile/settings",
        name: "profile",
        component: () =>
          import(
            /* webpackChunkName: "userprofilesettings" */ "@/views/UserProfileSettings.vue"
          )
      },
      {
        path: "notifications",
        name: "notifications",
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
    name: "dashboard",
    beforeEnter: requireAuthenticated,
    component: Main,
    children: [
      {
        path: "home",
        name: "home",
        component: () =>
          import(/* webpackChunkName: "dashboard" */ "@/views/Dashboard.vue")
      },
      {
        path: "users",
        name: "users",
        component: () =>
          import(/* webpackChunkName: "users" */ "@/views/Users.vue")
      },
      {
        path: "feed",
        name: "feed",
        component: () =>
          import(/* webpackChunkName: "feed" */ "@/views/Feed.vue")
      },
      {
        path: "map",
        name: "map",
        meta: { useSearchToFilter: true },
        component: () => import(/* webpackChunkName: "map" */ "@/views/Map.vue")
      },
      {
        path: "transport/:transport",
        name: "transport",
        props: true,
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

  {
    path: "/",
    redirect: "/dashboard"
  },

  // Default fallback
  { path: "*", redirect: { name: "error404" } }
];

const createRouter = () =>
  new Router({
    mode: "history",
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
