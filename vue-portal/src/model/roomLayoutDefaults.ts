/**
 * 房间物品锚点：相对坐标 0—1（左上为原点）。
 *
 * 图片没有「地面」标记，因此每个房间配置一条 **地面带（floorBand）**：
 * 开发时对照房间 PNG 目测地面/广场所在区域，用 xMin/xMax/yMin/yMax 框住，
 * 物品只在带内横向排开。试玩后可在本文件微调数值。
 */

export type ItemPlacement = 'floor' | 'wall' | 'shelf'

export interface ItemAnchor {
  x: number
  y: number
  placement: ItemPlacement
}

/** 画面中的可摆放区域（归一化坐标） */
export interface RoomFloorBand {
  xMin: number
  xMax: number
  /** 区域上沿（远处 / 贴建筑） */
  yMin: number
  /** 区域下沿（近处；floor 时物品优先落在此线） */
  yMax: number
  /** spread=横向铺开；center=向画面水平中心收窄（如楼梯口） */
  distribute?: 'spread' | 'center'
  /** floor=地面；ledge=窗台/台阶立面 */
  surface?: 'floor' | 'ledge'
}

const DEFAULT_FLOOR_BAND: RoomFloorBand = {
  xMin: 0.12,
  xMax: 0.88,
  yMin: 0.68,
  yMax: 0.88,
}

/**
 * 各房间地面带（对照 assets/gui/rooms/*.png 目测标定，可继续微调）。
 * y 越大越靠近画面下方（人行道/广场近景）。
 */
const ROOM_FLOOR_BANDS: Record<string, RoomFloorBand> = {
  gate: { xMin: 0.14, xMax: 0.86, yMin: 0.58, yMax: 0.86 },
  /** 越园食堂：窗台/门楣一带（y 越大越靠下） */
  canteen: {
    xMin: 0.2,
    xMax: 0.8,
    yMin: 0.46,
    yMax: 0.66,
    surface: 'ledge',
  },
  boxue_main: { xMin: 0.1, xMax: 0.9, yMin: 0.66, yMax: 0.9 },
  /** 博学北楼：主楼梯在中下方，物品集中排在台阶区域（志愿者 NPC 独立锚点不变） */
  boxue_north: {
    xMin: 0.38,
    xMax: 0.62,
    yMin: 0.74,
    yMax: 0.93,
    distribute: 'center',
  },
  boxue_west: { xMin: 0.12, xMax: 0.88, yMin: 0.64, yMax: 0.88 },
  boxue_east: { xMin: 0.12, xMax: 0.88, yMin: 0.64, yMax: 0.88 },
  supermarket: { xMin: 0.15, xMax: 0.85, yMin: 0.68, yMax: 0.89 },
  dormitory: { xMin: 0.18, xMax: 0.82, yMin: 0.7, yMax: 0.89 },
  library: { xMin: 0.12, xMax: 0.88, yMin: 0.66, yMax: 0.88 },
  gymnasium: { xMin: 0.1, xMax: 0.9, yMin: 0.64, yMax: 0.9 },
}

/** NPC 门口立绘位置（相对坐标，非地面物品） */
export interface NpcDoorAnchor {
  x: number
  y: number
  title: string
  /** 立绘相对默认尺寸的缩放，默认 1 */
  scale?: number
}

const NPC_DOOR_ANCHORS: Record<string, NpcDoorAnchor> = {
  supermarket: { x: 0.76, y: 0.58, title: '宿管阿姨', scale: 1.4 },
  boxue_north: { x: 0.63, y: 0.94, title: '志愿者' },
  library: { x: 0.52, y: 0.82, title: '志愿者', scale: 1.4 },
}

/** 猫学长照片（志愿者旁，不可拾取） */
export const CAT_SENIOR_ANCHOR: ItemAnchor = { x: 0.78, y: 0.95, placement: 'floor' }

/** 全局物品落点（不限关卡） */
const ITEM_ANCHOR_OVERRIDES: Record<string, Partial<ItemAnchor> & { x: number; y: number }> = {
  社团传单: { x: 0.86, y: 0.88 },
  一根二手数据线: { x: 0.46, y: 0.93, placement: 'floor' },
  一台打开的电脑: { x: 0.54, y: 0.91, placement: 'floor' },
}

/**
 * 按「房间 + 关卡」覆盖（同一物品在不同关位置可不同）。
 * L4+ 食堂中仍存在的 L3 配置项会沿用 L3 坐标。
 */
const ROOM_LEVEL_ITEM_OVERRIDES: Record<
  string,
  Record<number, Record<string, Partial<ItemAnchor> & { x: number; y: number }>>
> = {
  canteen: {
    2: {
      一双一次性筷子: { x: 0.4, y: 0.66, placement: 'shelf' },
      一根火腿肠: { x: 0.58, y: 0.66, placement: 'shelf' },
      食堂纸条: { x: 0.3, y: 0.56, placement: 'shelf' },
    },
    3: {
      一双一次性筷子: { x: 0.36, y: 0.68, placement: 'shelf' },
      一根火腿肠: { x: 0.58, y: 0.6, placement: 'shelf' },
      食堂纸条: { x: 0.34, y: 0.7, placement: 'shelf' },
    },
  },
  boxue_north: {
    3: {
      寝室省电攻略: { x: 0.44, y: 0.9, placement: 'floor' },
    },
  },
}

function resolveItemOverride(
  roomId: string | null | undefined,
  itemName: string,
  level?: number,
): (Partial<ItemAnchor> & { x: number; y: number }) | null {
  const trimmed = itemName.trim()
  const room = roomId?.trim()
  if (room && level && level > 0) {
    const byLevel = ROOM_LEVEL_ITEM_OVERRIDES[room]
    if (byLevel) {
      const exact = byLevel[level]?.[trimmed]
      if (exact) {
        return exact
      }
      if (level > 3 && byLevel[3]?.[trimmed]) {
        return byLevel[3][trimmed]
      }
    }
  }
  return ITEM_ANCHOR_OVERRIDES[trimmed] ?? null
}

function clamp(value: number, min: number, max: number) {
  return Math.min(max, Math.max(min, value))
}

export function getRoomFloorBand(roomId: string | null | undefined): RoomFloorBand {
  if (!roomId?.trim()) {
    return DEFAULT_FLOOR_BAND
  }
  return ROOM_FLOOR_BANDS[roomId.trim()] ?? DEFAULT_FLOOR_BAND
}

function anchorOnFloorBand(band: RoomFloorBand, index: number, total: number): ItemAnchor {
  const count = Math.max(1, total)
  const surface = band.surface ?? 'floor'
  const placement: ItemPlacement = surface === 'ledge' ? 'shelf' : 'floor'

  if (band.distribute === 'center') {
    const spacing = Math.min(0.1, (band.xMax - band.xMin) / Math.max(1, count))
    const x = 0.5 + (index - (count - 1) / 2) * spacing
    const row = index % 2
    const ySpan = band.yMax - band.yMin
    const y = count <= 2 ? band.yMax : band.yMax - row * ySpan * 0.35

    return {
      x: clamp(x, band.xMin, band.xMax),
      y: clamp(y, band.yMin, band.yMax),
      placement,
    }
  }

  const cols = Math.min(count, Math.max(2, Math.ceil(Math.sqrt(count))))
  const rows = Math.ceil(count / cols)
  const row = Math.floor(index / cols)
  const col = index % cols

  const xSpan = band.xMax - band.xMin
  const ySpan = band.yMax - band.yMin

  const x = band.xMin + ((col + 0.5) / cols) * xSpan
  const y =
    rows <= 1
      ? band.yMax
      : band.yMax - (row / (rows - 1)) * ySpan * 0.55

  return {
    x: clamp(x, band.xMin, band.xMax),
    y: clamp(y, band.yMin, band.yMax),
    placement,
  }
}

export function getItemAnchor(
  roomId: string | null | undefined,
  itemName: string,
  index: number,
  totalCount = 1,
  level?: number,
): ItemAnchor {
  const band = getRoomFloorBand(roomId)
  const override = resolveItemOverride(roomId, itemName, level)
  if (override) {
    return {
      x: override.x,
      y: override.y,
      placement: override.placement ?? (band.surface === 'ledge' ? 'shelf' : 'floor'),
    }
  }
  return anchorOnFloorBand(band, index, totalCount)
}

/** @deprecated 物品改由地面带自动排布；保留供调试或文档引用 */
export function getRoomItemAnchors(roomId: string | null | undefined): ItemAnchor[] {
  const band = getRoomFloorBand(roomId)
  return Array.from({ length: 6 }, (_, index) => anchorOnFloorBand(band, index, 6))
}

export function getNpcDoorAnchor(roomId: string | null | undefined): NpcDoorAnchor | null {
  if (!roomId?.trim()) {
    return null
  }
  return NPC_DOOR_ANCHORS[roomId.trim()] ?? null
}
