// import { createRouter, createWebHistory } from 'vue-router'
// import type { RouteRecordRaw } from 'vue-router'
// import { useUserStore } from '@/stores/user'
// import type { UserRole } from '@/stores/user'

// const routes: RouteRecordRaw[] = [
//   {
//     path: '/',
//   beforeEnter: (to, from, next) => {
//     const userStore = useUserStore()
//     const role = userStore.userInfo?.role

//     if (!role) {
//       next('/login') // 未登录跳登录页
//     } else if (role === 'user') {
//       next('/pets')
//     } else if (role === 'provider') {
//       next('/provider/services')
//     } else if (role === 'admin') {
//       next('/admin/users')
//     } else {
//       next('/login')
//     }
//   }
//   },
//   {
//     path: '/login',
//     name: 'Login',
//     component: () => import('../views/Login.vue')
//   },
//   {
//     path: '/register',
//     name: 'Register',
//     component: () => import('../views/Register.vue')
//   },
//   // 普通用户路由
//   {
//     path: '/pets',
//     name: 'PetList',
//     component: () => import('../views/pet/PetList.vue'),
//     meta: { requiresAuth: true, roles: ['user'] }
//   },
//   {
//     path: '/pets/add',
//     name: 'AddPet',
//     component: () => import('../views/pet/AddPet.vue'),
//     meta: {
//       requiresAuth: true, // 这个页面需要登录
//       roles: ['user']      // 只允许普通用户访问 (根据您的需求调整)
//     }
//   },
//   {
//     path: '/pets/:id', // 匹配 /pets/1, /pets/2 等
//     name: 'ViewPet',
//     component: () => import('../views/pet/ViewPet.vue'), // 指向你的文件路径
//     meta: { requiresAuth: true } // 可能需要登录访问
//   },
//   {
//     path: '/pets/edit/:id',
//     name: 'EditPet',
//     component: () => import('../views/pet/EditPet.vue'),
//     meta: {
//       requiresAuth: true,
//       roles: ['user']
//     }
//   },
//   {
//     path: '/services',
//     name: 'ServiceList',
//     component: () => import('../views/service/ServiceList.vue'),
//     meta: { requiresAuth: true, roles: ['user'] }
//   },
//   {
//     path: '/orders',
//     name: 'OrderList',
//     component: () => import('../views/order/OrderList.vue'),
//     meta: { requiresAuth: true, roles: ['user'] }
//   },
//   {
//     path: '/profile',
//     name: 'Profile',
//     component: () => import('../views/Profile.vue'),
//     meta: { requiresAuth: true }
//   },
//   {
//     path: '/profile/edit',
//     name: 'EditProfile',
//     component: () => import('../views/admin/EditProfile.vue'),
//     meta: { requiresAuth: true }
//   },

//   {
//     path: '/services/search',
//     name: 'ServiceSearch',
//     component: () => import('../views/service/ServiceSearchPage.vue'),
//     meta: { requiresAuth: true }
//   },
//   // 服务商路由
//   {
//     path: '/provider',
//     component: () => import('../layouts/ProviderLayout.vue'),
//     meta: { requiresAuth: true, roles: ['provider'] },
//     children: [
//       {
//         path: 'services',
//         name: 'ProviderServiceList',
//         component: () => import('../views/provider/ServiceList.vue')
//       },
//       {
//         path: 'reservations',
//         name: 'ProviderReservationList',
//         component: () => import('../views/provider/ReservationList.vue')
//       },
//       {
//         path: 'profile/edit',
//         name: 'ProviderEditProfile',
//         component: () => import('../views/admin/EditProfile.vue')
//       },
//       {
//         path: 'services/add',
//         name: 'ProviderAddService',
//         component: () => import('../views/provider/AddService.vue'),
//         meta: { requiresAuth: true, roles: ['provider'] }
//       },
//     ]
//   },
//   // 管理员路由
//   {
//     path: '/admin',
//     component: () => import('../layouts/AdminLayout.vue'),
//     meta: { requiresAuth: true, roles: ['admin'] },
//     children: [
//       {
//         path: 'users',
//         name: 'AdminUserList',
//         component: () => import('../views/admin/UserList.vue')
//       },
//       {
//         path: 'providers',
//         name: 'AdminProviderList',
//         component: () => import('../views/admin/ProviderList.vue')
//       },
//       {
//         path: 'services',
//         name: 'AdminServiceList',
//         component: () => import('../views/admin/ServiceList.vue')
//       },
//       {
//         path: 'orders',
//         name: 'AdminOrderList',
//         component: () => import('../views/admin/OrderList.vue')
//       },
//       {
//         path: 'profile/edit',
//         name: 'AdminEditProfile',
//         component: () => import('../views/admin/EditProfile.vue')
//       }
//     ]
//   }
// ]

// const router = createRouter({
//   history: createWebHistory(),
//   routes
// })

// // 路由守卫
// router.beforeEach((to, from, next) => {
//   const userStore = useUserStore()
//   const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
//   const roles = to.meta.roles as UserRole[]

//   if (requiresAuth && !userStore.token) {
//     next('/login')
//   } else if (roles && !roles.includes(userStore.userInfo?.role as UserRole)) {
//     next('/')
//   } else {
//     next()
//   }
// })

// export default router 

import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { UserRole } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  // {
  //   path: '/',
  //   component: () => import('../views/Redirector.vue'),
  //   beforeEnter: (to, from, next) => {
  //     const userStore = useUserStore()
  //     const role = userStore.userInfo?.role

  //     if (!role) {
  //       next('/') // 未登录跳登录页
  //     } else if (role === 'user') {
  //       next('/')
  //     } else if (role === 'provider') {
  //       next('/provider/services')
  //     } else if (role === 'admin') {
  //       next('/admin/users')
  //     } else {
  //       next('/')
  //     }
  //   }
  // },
  //   {
  //   path: '/',
  //   redirect: () => {
  //     const userStore = useUserStore()
  //     const role = userStore.userInfo?.role

  //     if (!role) return '/login'
  //     if (role === 'user') return '/pets'
  //     if (role === 'provider') return '/provider/services'
  //     if (role === 'admin') return '/admin/users'
  //     return '/login'
  //   }
  // }
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  // 普通用户路由
  {
    path: '/pets',
    name: 'PetList',
    component: () => import('../views/pet/PetList.vue'),
    meta: { requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/pets/add',
    name: 'AddPet',
    component: () => import('../views/pet/AddPet.vue'),
    meta: { requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/pets/:id',
    name: 'ViewPet',
    component: () => import('../views/pet/ViewPet.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/pets/edit/:id',
    name: 'EditPet',
    component: () => import('../views/pet/EditPet.vue'),
    meta: { requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/services',
    name: 'ServiceList',
    component: () => import('../views/service/ServiceList.vue'),
    meta: { requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('../views/order/OrderList.vue'),
    meta: { requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile/edit',
    name: 'EditProfile',
    component: () => import('../views/admin/EditProfile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/services/search',
    name: 'ServiceSearch',
    component: () => import('../views/service/ServiceSearchPage.vue'),
  },
  {
    path: '/reservations/new/:serviceId', // 使用参数传递 serviceId
    name: 'CreateReservation',
    component: () => import('../views/reservation/ReservationForm.vue'), // 指向你的预约表单组件
    meta: {
      requiresAuth: true, // 需要登录
      roles: ['user']      // 只允许普通用户访问
    }
  },

  // 我的预约列表页路由
  {
    path: '/reservations/my',
    name: 'MyReservations',
    component: () => import('../views/reservation/MyReservations.vue'), // 指向你的预约列表组件
    meta: {
      requiresAuth: true, // 需要登录
      roles: ['user']      // 只允许普通用户访问
    }
  },

  // 服务商路由
  {
    path: '/provider',
    component: () => import('../layouts/ProviderLayout.vue'),
    meta: { requiresAuth: true, roles: ['provider'] },
    children: [
      {
        path: 'services',
        name: 'ProviderServiceList',
        component: () => import('../views/provider/ServiceList.vue')
      },
      {
        path: 'reservations',
        name: 'ProviderReservationList',
        component: () => import('../views/provider/ReservationList.vue')
      },
      {
        path: 'profile/edit',
        name: 'ProviderEditProfile',
        component: () => import('../views/admin/EditProfile.vue')
      },
      {
        path: 'services/add',
        name: 'ProviderAddService',
        component: () => import('../views/provider/AddService.vue'),
        meta: { requiresAuth: true, roles: ['provider'] }
      }
    ]
  },
  // 管理员路由
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['admin'] },
    children: [
      {
        path: '', // 空路径表示 /admin 本身
        name: 'AdminDashboard',
        component: () => import('../views/admin/AdminDashboard.vue') // 指向你新创建的组件
      },
      {
        path: 'users',
        name: 'AdminUserList',
        component: () => import('../views/admin/UserList.vue')
      },
      {
        path: 'providers',
        name: 'AdminProviderList',
        component: () => import('../views/admin/ProviderList.vue')
      },
      {
        path: 'services',
        name: 'AdminServiceList',
        component: () => import('../views/admin/ServiceList.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrderList',
        component: () => import('../views/admin/OrderList.vue')
      },
      {
        path: 'profile/edit',
        name: 'AdminEditProfile',
        component: () => import('../views/admin/EditProfile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
// router.beforeEach((to, from, next) => {
//   const userStore = useUserStore()
//   const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
//   const roles = to.meta.roles as UserRole[]

//   if (requiresAuth && !userStore.token) {
//     next('/login')
//   } else if (roles && !roles.includes(userStore.userInfo?.role as UserRole)) {
//     next('/')
//   } else {
//     next()
//   }
// })
router.beforeEach(async (to, from, next) => { // 可以标记为 async 如果内部需要 await
  const userStore = useUserStore();
  const token = userStore.token;
  const userInfo = userStore.userInfo; // 获取当前状态
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const requiredRoles = to.meta.roles as UserRole[] | undefined;

  // 调试日志
  // console.log(`Navigating to: ${to.path}, Requires Auth: ${requiresAuth}, Required Roles: ${requiredRoles}`);
  // console.log(`Token: ${token ? 'Exists' : 'None'}, UserInfo: ${userInfo ? userInfo.role : 'None'}`);

  // 场景1: 访问需要登录的页面
  if (requiresAuth) {
    // 1.1 没有 token，去登录页
    if (!token) {
      console.log('[Guard] Auth required, no token. Redirecting to login.');
      next({
        path: '/login',
        query: { redirect: to.fullPath } // 保存原始目标路径，登录后可以跳回
      });
      return; // 结束本次守卫
    }

    // 1.2 有 token，但 userInfo 尚未加载 (可能发生在刷新后 App.vue 正在获取时)
    if (!userInfo) {
      console.log('[Guard] Auth required, token exists, userInfo pending. Waiting for App.vue fetch...');
      // **重要**: 此时我们信任 App.vue 中的 onMounted 会获取 userInfo。
      // 这里暂时放行 (`next()`)，让 App.vue 完成加载。
      // 如果获取失败，App.vue 中的逻辑应该会处理跳转到登录。
      // 目标页面组件内部也应该能处理 userInfo 暂时为 null 的情况 (例如显示加载状态)。
      next();
      return;
    }

    // 1.3 有 token，有 userInfo，检查角色
    if (requiredRoles && !requiredRoles.includes(userInfo.role as UserRole)) {
      console.log(`[Guard] Role mismatch (Needed: ${requiredRoles}, Got: ${userInfo.role}). Redirecting to /.`);
      // 角色不匹配，跳转到公共首页或无权限页面
      next('/'); // 或者 next('/unauthorized');
      return;
    }

    // 1.4 验证通过 (有 token, userInfo 已加载且角色匹配或无需特定角色)
    console.log('[Guard] Auth check passed.');
    next(); // 放行
    return;
  }

  // 场景2: 访问公共页面 (如 /login, /register, /)
  // 2.1 如果用户已登录 (有 token 和 userInfo)，但访问的是登录或注册页
  if (token && userInfo && (to.path === '/login' || to.path === '/register')) {
    console.log(`[Guard] User logged in (${userInfo.role}). Redirecting from public auth page.`);
    // 根据角色跳转到各自的首页
    if (userInfo.role === 'provider') next('/provider/dashboard');
    else if (userInfo.role === 'admin') next('/admin/users'); // 或管理员仪表盘
    else next('/'); // 普通用户去首页
    return;
  }

  // 2.2 访问其他公共页面，或未登录访问登录/注册，直接放行
  console.log('[Guard] Accessing public route or auth page while not logged in.');
  next();
});
export default router