/** 与 Java AssetCatalog 对齐的资源路径（Model 层） */

const ASSET_ROOT = '/assets/gui'
const ROOMS_DIR = `${ASSET_ROOT}/rooms`
const ITEMS_DIR = `${ASSET_ROOT}/items`

/**
 * 物品短名 → 图片 slug（须与 Java AssetCatalog 保持一致）。
 */
const ITEM_SLUGS: Record<string, string> = {
  // essentials
  '湿漉漉的三十元钱': 'money_30yuan',
  '三十元钱': 'money_30yuan',
  '一卡通': 'campus_card',
  '归寝单': 'dorm_form',
  '锤子': 'hammer',
  '简易锤子': 'hammer',
  '棍子': 'stick',
  '石头': 'stone',
  '绳子': 'rope',
  '手电筒': 'flashlight',
  '一根火腿肠': 'sausage',
  '火腿肠': 'sausage',
  'magic cookie': 'magic_cookie',
  '一块遗弃的秒表': 'stopwatch',
  '退寝条': 'withdrawal_slip',
  '别人饭卡': 'wrong_meal_card',
  '食堂纸条': 'canteen_note',
  '投影仪遥控器': 'projector_remote',
  // distractions
  '高数及格祈福黄纸': 'prayer_paper',
  '半瓶奶茶': 'milk_tea',
  '一杯奶茶': 'milk_tea',
  '社团传单': 'club_flyer',
  '一根二手数据线': 'usb_cable',
  '寝室省电攻略': 'power_saving_guide',
  '一台打开的电脑': 'open_computer',
  '墙上的一张A4纸': 'a4_notice',
  '磨损的护膝': 'knee_pad',
  '赛事纪念帽': 'event_cap',
  '一双一次性筷子': 'chopsticks',
  '失物招领号码牌': 'lost_found_tag',
  '志愿者马甲': 'volunteer_vest',
  '一把生锈的钥匙': 'rusty_key',
  '一张英语四级准考证': 'cet4_ticket',
  '晚安玛卡巴卡抱枕': 'makabaka_pillow',
  '一块电工使用的胶带': 'electrical_tape',
  '一本数据库概论': 'database_book',
  '一个水杯': 'water_cup',
  '一张过期的借阅条': 'expired_borrow_slip',
  '一份外卖': 'takeout',
  '外卖': 'takeout',
  '一块闪光的校友纪念章': 'alumni_badge',
  '一块印章': 'club_stamp',
  '一块闪闪发光的金块': 'fake_gold',
  '一张猫学长的照片': 'cat_photo',
  '一张购物小票': 'receipt',
  '一张写了“吉”的抽签条': 'fortune_slip',
  '一个辣椒包': 'chili_packet',
  '辣椒包': 'chili_packet',
}

export function roomImageUrl(roomId: string | null | undefined): string {
  const slug = roomId?.trim() || 'gate'
  return `${ROOMS_DIR}/${slug}.png`
}

export function itemSlug(itemName: string | null | undefined): string {
  if (!itemName?.trim()) {
    return '_default'
  }
  const trimmed = itemName.trim()
  const direct = ITEM_SLUGS[trimmed]
  if (direct) {
    return direct
  }
  const lower = trimmed.toLowerCase()
  for (const [key, slug] of Object.entries(ITEM_SLUGS)) {
    if (key.toLowerCase() === lower) {
      return slug
    }
  }
  return '_default'
}

export function itemImageUrl(itemName: string | null | undefined): string {
  return `${ITEMS_DIR}/${itemSlug(itemName)}.png`
}

/** NPC 立绘路径（与 Java AssetCatalog.npcImagePathForRoom 对齐） */
export function npcImageUrl(roomId: string | null | undefined): string {
  const id = roomId?.trim() ?? ''
  if (id === 'supermarket') {
    return `${ASSET_ROOT}/npcs/dorm_aunt.png`
  }
  if (id === 'boxue_north' || id === 'library') {
    return `${ASSET_ROOT}/npcs/volunteer.png`
  }
  return `${ASSET_ROOT}/npcs/_default.png`
}

/** 玩家 Q 版立绘（沉浸模式，可替换为 PNG sprite sheet） */
export function playerCharacterUrl(variant: 'idle' | 'walk' = 'idle'): string {
  return `${ASSET_ROOT}/player/campus_${variant}.svg`
}

export const LOBBY_BACKGROUND = roomImageUrl('gate')
