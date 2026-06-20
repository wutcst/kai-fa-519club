<script setup lang="ts">
import { ref } from 'vue'
import { useSoloGameController } from '@/controller/useSoloGameController'
import { useGameDisplayMode } from '@/model/gameDisplayMode'
import GameScene from '@/component/game/GameScene.vue'
import DisplayModeToggle from '@/component/game/DisplayModeToggle.vue'
import PickupFlyFx, { type PickupFlight } from '@/component/game/PickupFlyFx.vue'
import HudTimer from '@/component/game/HudTimer.vue'
import TimerPressureOverlay from '@/component/game/TimerPressureOverlay.vue'
import DirectionNav from '@/component/game/DirectionNav.vue'
import NoticePopup from '@/component/game/NoticePopup.vue'
import RoomItemLayer from '@/component/solo/RoomItemLayer.vue'
import NpcLayer from '@/component/solo/NpcLayer.vue'
import CatSeniorLayer from '@/component/solo/CatSeniorLayer.vue'
import NpcDialog from '@/component/solo/NpcDialog.vue'
import InventoryHudSolo from '@/component/solo/InventoryHudSolo.vue'
import OutcomeOverlay from '@/component/solo/OutcomeOverlay.vue'
import LockedOverlay from '@/component/solo/LockedOverlay.vue'
import CombinePrompt from '@/component/solo/CombinePrompt.vue'
import UnlockPasswordModal from '@/component/game/UnlockPasswordModal.vue'
import GlassButton from '@/component/common/GlassButton.vue'

const {
  soloSession,
  viewState,
  busy,
  noticeText,
  noticeVisible,
  combineVisible,
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
  lookAround,
  talkNpc,
  closeNpcDialog,
  takeItem,
  inventoryAction,
  runCommand,
  confirmCombine,
  cancelCombine,
  dismissOutcome,
  promptUnlock,
  unlockModalVisible,
  unlockPassword,
  closeUnlockModal,
  confirmUnlock,
  dismissLocked,
  quitGame,
  hideNotice,
  error,
} = useSoloGameController()

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
  <div v-if="!soloSession || !viewState" class="solo-loading">
    <p>正在加载游戏…</p>
    <p v-if="error" class="solo-loading-error">{{ error }}</p>
  </div>

  <div
    v-else
    class="solo-viewport"
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
        :room-id="viewState.roomId"
        :room-description="viewState.roomDescription"
        :transitioning="sceneTransition"
        :display-mode="displayMode"
        :move-direction="moveDirection"
        :directional-exit="directionalExit"
      />

      <RoomItemLayer
        :room-id="viewState.roomId"
        :level="viewState.level"
        :items="viewState.roomItems"
        :disabled="viewState.interactionBlocked || busy"
        @take="takeItem"
        @pickup-visual="onPickupVisual"
        @focus-change="onItemFocusChange"
      />

      <NpcLayer
        :room-id="viewState.roomId"
        :visible="viewState.actions.showNpc"
        :disabled="viewState.interactionBlocked || busy"
        @talk="talkNpc"
      />

      <CatSeniorLayer
        :visible="viewState.actions.showFeed"
        :disabled="viewState.interactionBlocked || busy"
        @feed="runCommand('feed')"
      />
    </div>

    <PickupFlyFx
      v-for="flight in pickupFlights"
      :key="flight.id"
      :flight="flight"
      @done="onPickupDone"
    />

    <div v-if="viewState.westTrapBanner" class="trap-banner">{{ viewState.westTrapBanner }}</div>

    <TimerPressureOverlay :remaining-seconds="remainingSeconds" />

    <HudTimer
      :timer-text="viewState.timerText"
      :level="viewState.level"
      :level-state="viewState.levelState"
      :room-name="viewState.levelTitle"
      :polling="polling"
      :danger="timerDanger"
      :warning="timerWarning"
      :pressure="timerPressure"
    />

    <InventoryHudSolo
      :items="viewState.inventory"
      :inventory-weight="viewState.inventoryWeight"
      :max-inventory-weight="viewState.maxInventoryWeight"
      :remaining-capacity="viewState.remainingCapacity"
      :disabled="viewState.interactionBlocked || busy"
      @action="(type, item) => inventoryAction(type, item.name)"
    />

    <DirectionNav
      variant="solo"
      :disabled="viewState.interactionBlocked || busy"
      :exits="viewState.exits"
      :actions="viewState.actions"
      @move="move"
      @look="lookAround"
      @back="moveBack"
      @combine="runCommand('combine')"
      @submit="runCommand('submit', '归寝单')"
      @unlock="promptUnlock"
      @sleep="runCommand('sleep')"
    />

    <div class="top-actions">
      <DisplayModeToggle compact />
      <GlassButton danger @click="quitGame">退出</GlassButton>
    </div>

    <NoticePopup
      :visible="noticeVisible && !viewState.outcome"
      :message="noticeText"
      @close="hideNotice"
    />

    <OutcomeOverlay
      :outcome="viewState.outcome"
      @action="dismissOutcome"
    />

    <LockedOverlay
      v-if="viewState.lockedOverlayMessage && !viewState.outcome"
      :message="viewState.lockedOverlayMessage"
      @dismiss="dismissLocked"
    />

    <CombinePrompt
      :visible="combineVisible"
      @confirm="confirmCombine"
      @cancel="cancelCombine"
    />

    <UnlockPasswordModal
      :visible="unlockModalVisible"
      :password="unlockPassword"
      :loading="busy"
      @update:password="unlockPassword = $event"
      @confirm="confirmUnlock"
      @cancel="closeUnlockModal"
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
.solo-viewport {
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

.solo-viewport.item-focus-mode :deep(.direction-nav),
.solo-viewport.item-focus-mode :deep(.inventory-root),
.solo-viewport.item-focus-mode .top-actions,
.solo-viewport.item-focus-mode :deep(.hud-timer) {
  opacity: 0.35;
  pointer-events: none;
  transition: opacity 0.3s ease;
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

.top-actions {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 21;
  display: flex;
  align-items: center;
  gap: 10px;
}

.solo-loading {
  min-height: 100vh;
  display: grid;
  place-content: center;
  gap: 8px;
  color: var(--text-muted);
  text-align: center;
}

.solo-loading-error {
  color: #ffb4b4;
}

.solo-viewport.screen-shake {
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
