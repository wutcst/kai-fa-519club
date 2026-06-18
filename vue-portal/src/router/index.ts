import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/view/HomeView.vue'
import AuthView from '@/view/AuthView.vue'
import UserManageView from '@/view/UserManageView.vue'
import SoloGameView from '@/view/SoloGameView.vue'
import SoloLevelSelectView from '@/view/SoloLevelSelectView.vue'
import MultiplayerLayout from '@/view/multiplayer/MultiplayerLayout.vue'
import LobbyHallView from '@/view/multiplayer/LobbyHallView.vue'
import TeamView from '@/view/multiplayer/TeamView.vue'
import MultiplayerGameView from '@/view/MultiplayerGameView.vue'
import { isLoggedIn } from '@/model/authModel'
import { releaseMultiplayerRoom } from '@/service/multiplayerExit'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/auth', name: 'auth', component: AuthView },
    { path: '/account', name: 'account', component: UserManageView },
    { path: '/solo/levels', name: 'solo-levels', component: SoloLevelSelectView },
    { path: '/solo', name: 'solo', component: SoloGameView },
    {
      path: '/multiplayer',
      component: MultiplayerLayout,
      children: [
        { path: '', name: 'multiplayer-lobby', component: LobbyHallView },
        { path: 'team', name: 'multiplayer-team', component: TeamView },
      ],
    },
    { path: '/multiplayer/room', name: 'multiplayer-room', component: MultiplayerGameView },
  ],
})

router.beforeEach(async (to, from) => {
  const leavingMultiplayer = from.path.startsWith('/multiplayer')
  const goingHome = to.name === 'home'
  if (leavingMultiplayer && goingHome) {
    await releaseMultiplayerRoom()
  }

  const requiresAuth =
    to.name === 'multiplayer-lobby'
    || to.name === 'multiplayer-team'
    || to.name === 'multiplayer-room'
    || to.name === 'account'
  if (requiresAuth && !isLoggedIn()) {
    return { name: 'auth', query: { redirect: to.fullPath } }
  }
  if (to.name === 'auth' && isLoggedIn()) {
    const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : '/'
    return redirect || '/'
  }
})

export default router
