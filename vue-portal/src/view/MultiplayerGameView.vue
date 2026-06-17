<script setup lang="ts">
/**
 * 联机对局 View：布局对齐单机，右下角折叠聊天。
 */
import { ref } from 'vue'
import { useGameRoomController } from '@/controller/useGameRoomController'
import { useGameDisplayMode } from '@/model/gameDisplayMode'
import GameScene from '@/component/game/GameScene.vue'
import DisplayModeToggle from '@/component/game/DisplayModeToggle.vue'
import PickupFlyFx, { type PickupFlight } from '@/component/game/PickupFlyFx.vue'
import HudTimer from '@/component/game/HudTimer.vue'
import TimerPressureOverlay from '@/component/game/TimerPressureOverlay.vue'
import DirectionNav from '@/component/game/DirectionNav.vue'
import NoticePopup from '@/component/game/NoticePopup.vue'
import PlayerDock from '@/component/game/PlayerDock.vue'
import RoomItemLayer from '@/component/solo/RoomItemLayer.vue'
import NpcLayer from '@/component/solo/NpcLayer.vue'
import NpcDialog from '@/component/solo/NpcDialog.vue'
import CatSeniorLayer from '@/component/solo/CatSeniorLayer.vue'
import InventoryHudSolo from '@/component/solo/InventoryHudSolo.vue'
import LockedOverlay from '@/component/solo/LockedOverlay.vue'
import RoomChatPanel from '@/component/multiplayer/RoomChatPanel.vue'
import GlassButton from '@/component/common/GlassButton.vue'

const {
  session,
  gameState,
  chatInput,
  busy,
  noticeText,
  noticeVisible,
  lockedOverlayMessage,
  npcDialogVisible,
  npcDialogTitle,
  npcDialogMessage,
  polling,
  sceneTransition,
  directionalExit,
  moveDirection,
  screenShake,
  remainingSeconds,
  timerPressure,
  timerDanger,
  timerWarning,
  move,
  moveBack,
  onBlockedExit,
  lookAround,
  takeItem,
  inventoryAction,
  talkNpc,
  closeNpcDialog,
  promptUnlock,
  sendChat,
  leaveRoom,
  dismissLockedOverlay,
  hideNotice,
  runCommand,
} = useGameRoomController()

const { displayMode } = useGameDisplayMode()

const itemFocus = ref({ active: false, x: 0.5, y: 0.7 })
const pickupFlights = ref<PickupFlight[]>([])
let pickupFlightId = 0

function onItemFocusChange(payload: { active: boolean; x: number; y: number }) {
  itemFocus.value = payload
}

function onPickupVisual(payload: { name: string; x: number; y: number }) {
  pickupFlights.value.push({
    id: ++pickupFlightId,
    name: payload.name,
    fromX: payload.x,
    fromY: payload.y,
  })
}

function onPickupDone(id: number) {
  pickupFlights.value = pickupFlights.value.filter((flight) => flight.id !== id)
}
</script>

<template>
  <div
    v-if="session && gameState"
    class="game-viewport"
    :class="{ 'item-focus-mode': itemFocus.active, 'screen-shake': screenShake }"
  >
    <div
      class="scene-stage"
      :class="{ focused: itemFocus.active }"
      :style="{
        '--focus-x': `${itemFocus.x * 100}%`,
        '--focus-y': `${itemFocus.y * 100}%`,
      }"
    >
      <GameScene
        :room-id="gameState.roomId"
        :room-description="gameState.roomDescription"
        :transitioning="sceneTransition"
        :display-mode="displayMode"
        :move-direction="moveDirection"
        :directional-exit="directionalExit"
      />

      <RoomItemLayer
        :room-id="gameState.roomId"
        :level="gameState.level"
        :items="gameState.roomItems"
        :disabled="busy || !!lockedOverlayMessage"
        @take="takeItem"
        @pickup-visual="onPickupVisual"
        @focus-change="onItemFocusChange"
      />

      <NpcLayer
        :room-id="gameState.roomId"
        :visible="gameState.actions.showNpc"
        :disabled="busy || !!lockedOverlayMessage"
        @talk="talkNpc"
      />

      <CatSeniorLayer
        :visible="gameState.actions.showFeed"
        :disabled="busy || !!lockedOverlayMessage"
        @feed="runCommand('feed')"
      />
    </div>

    <PickupFlyFx
      v-for="flight in pickupFlights"
      :key="flight.id"
      :flight="flight"
      @done="onPickupDone"
    />

    <div v-if="gameState.westTrapBanner" class="trap-banner">{{ gameState.westTrapBanner }}</div>

    <TimerPressureOverlay :remaining-seconds="remainingSeconds" />

    <HudTimer
      :timer-text="gameState.timerText"
      :level="gameState.level"
      :level-state="gameState.levelState"
      :room-name="session.roomName"
      :polling="polling"
      :danger="timerDanger"
      :warning="timerWarning"
      :pressure="timerPressure"
    />

    <div class="top-actions">
      <DisplayModeToggle compact />
      <GlassButton danger @click="leaveRoom">离开房间</GlassButton>
    </div>

    <PlayerDock
      :players="gameState.players"
      :self-player-id="session.playerId"
    />

    <InventoryHudSolo
      :items="gameState.inventory"
      :inventory-weight="gameState.inventoryWeight"
      :max-inventory-weight="gameState.maxInventoryWeight"
      :remaining-capacity="gameState.remainingCapacity"
      :disabled="busy || !!lockedOverlayMessage"
      @action="(type, item) => inventoryAction(type, item.name)"
    />

    <DirectionNav
      variant="solo"
      hint-on-blocked-exit
      :disabled="busy || !!lockedOverlayMessage"
      :exits="gameState.exits"
      :actions="gameState.actions"
      @move="move"
      @look="lookAround"
      @back="moveBack"
      @blocked="onBlockedExit"
      @combine="runCommand('combine')"
      @submit="runCommand('submit', '归寝单')"
      @unlock="promptUnlock"
      @sleep="runCommand('sleep')"
    />

    <RoomChatPanel
      v-model="chatInput"
      :messages="gameState.chatMessages"
      :self-player-id="session.playerId"
      :busy="busy"
      @send="sendChat"
    />

    <NoticePopup
      :visible="noticeVisible && !lockedOverlayMessage"
      :message="noticeText"
      @close="hideNotice"
    />

    <LockedOverlay
      v-if="lockedOverlayMessage"
      :message="lockedOverlayMessage"
      @dismiss="dismissLockedOverlay"
    />

    <NpcDialog
      v-if="npcDialogVisible"
      :title="npcDialogTitle"
      :message="npcDialogMessage"
      @close="closeNpcDialog"
    />
  </div>
</template>

<style scoped>
.game-viewport {
  position: fixed;
  inset: 0;
  overflow: hidden;
  background: var(--window-bg);
}

.scene-stage {
  position: absolute;
  inset: 0;
  transition: transform 0.42s cubic-bezier(0.22, 1, 0.36, 1);
  transform-origin: center center;
}

.scene-stage.focused {
  transform: scale(1.16);
  transform-origin: var(--focus-x) var(--focus-y);
}

.game-viewport.item-focus-mode :deep(.direction-nav),
.game-viewport.item-focus-mode :deep(.inventory-root),
.game-viewport.item-focus-mode :deep(.chat-root),
.game-viewport.item-focus-mode :deep(.player-dock),
.game-viewport.item-focus-mode :deep(.hud-timer),
.game-viewport.item-focus-mode .top-actions {
  opacity: 0.35;
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.top-actions {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 22;
  display: flex;
  align-items: center;
  gap: 10px;
}

.game-viewport :deep(.player-dock) {
  top: 68px;
}

.trap-banner {
  position: absolute;
  top: 72px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 19;
  padding: 8px 18px;
  border-radius: 999px;
  background: rgba(90, 20, 20, 0.85);
  color: #fff;
  font-size: 0.85rem;
}

.game-viewport.screen-shake {
  animation: viewportShake 0.38s ease;
}

@keyframes viewportShake {
  0%,
  100% {
    transform: translate(0, 0);
  }
  20% {
    transform: translate(-4px, 2px);
  }
  40% {
    transform: translate(4px, -2px);
  }
  60% {
    transform: translate(-3px, -2px);
  }
  80% {
    transform: translate(3px, 1px);
  }
}
</style>
