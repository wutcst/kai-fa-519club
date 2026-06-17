import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/view/HomeView.vue'
import SoloGameView from '@/view/SoloGameView.vue'
import LobbyView from '@/view/LobbyView.vue'
import MultiplayerGameView from '@/view/MultiplayerGameView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/solo', name: 'solo', component: SoloGameView },
    { path: '/multiplayer', name: 'multiplayer-lobby', component: LobbyView },
    { path: '/multiplayer/room', name: 'multiplayer-room', component: MultiplayerGameView },
  ],
})

export default router
