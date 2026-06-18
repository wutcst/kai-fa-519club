/**
 * 该包包含World-of-Zuul文本冒险游戏的核心实现类，
 * 涵盖游戏控制、命令解析、房间管理等功能模块，
 * 实现了玩家与文本界面的交互逻辑。
 * 【新增】重构命令处理逻辑，采用命令模式实现命令模块化管理。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.1
 */
package cn.edu.whut.sept.zuul;
/**
 * 该类是“World-of-Zuul”应用程序的主类。
 * 《World of Zuul》是一款简单的文本冒险游戏。用户可以在一些房间组成的迷宫中探险。
 * 你们可以通过扩展该游戏的功能使它更有趣!.
 *
 * 如果想开始执行这个游戏，用户需要创建Game类的一个实例并调用“play”方法。
 *
 * Game类的实例将创建并初始化所有其他类:它创建所有房间，并将它们连接成迷宫；它创建解析器
 * 接收用户输入，并将用户输入转换成命令后开始运行游戏。
 *
 *
 * @author  Michael Kölling and David J. Barnes/liujing
 * @version 1.5
 */


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.infrastructure.InfrastructureServices;
import cn.edu.whut.sept.zuul.infrastructure.auth.AuthService;
import cn.edu.whut.sept.zuul.infrastructure.auth.AuthSession;
import cn.edu.whut.sept.zuul.infrastructure.persistence.GamePersistenceService;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.LevelConfig;
import cn.edu.whut.sept.zuul.level.LevelManager;
import cn.edu.whut.sept.zuul.level.LevelRoomContent;
import cn.edu.whut.sept.zuul.level.LevelTimer;
import cn.edu.whut.sept.zuul.level.TimerAuthority;
import cn.edu.whut.sept.zuul.multiplayer.GameStateSnapshot;
import cn.edu.whut.sept.zuul.multiplayer.PlayerStateSnapshot;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

/**
 * 游戏的主控制器类，负责协调各组件、管理游戏流程和房间状态。
 * 重构说明：移除硬编码的命令分支逻辑，引入CommandManager分发命令，
 * 遵循开闭原则，新增命令无需修改此类代码。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.4
 */
public class Game
{
    private Parser parser;// 命令解析器实例
    private Room currentRoom;// 玩家当前所在房间
    private CommandManager commandManager;// 新增命令管理器
    private List<Room> roomHistory; // 存储玩家访问过的房间历史
    private Player player; // 玩家实例【新增】
    private Map<String, Room> roomRegistry; // 房间 ID 注册表，供关卡配置引用
    private LevelManager levelManager; // 五关进度管理
    private LevelTimer levelTimer; // 熄灯倒计时
    private InfrastructureServices infrastructureServices; // 认证 + 存档（共享 H2）
    private GamePersistenceService persistenceService; // 兼容测试注入
    private AuthService authService; // 兼容测试注入
    private AuthSession authSession; // 当前登录会话
    private boolean multiplayerSessionActive; // F6 联机会话
    private final Map<String, Player> onlinePlayers = new LinkedHashMap<>();
    private final Map<String, List<Room>> onlineRoomHistories = new LinkedHashMap<>();
    private String activeOnlinePlayerId;

    /**
     * 创建游戏并初始化内部数据和解析器.
     */
    public Game() {
        commandManager = new CommandManager(); // 初始化命令管理器
        parser = new Parser(commandManager); // 传入命令管理器初始化解析器
        roomHistory = new ArrayList<>(); // 初始化房间历史记录列表
        player = new Player("冒险者", currentRoom); // 初始化玩家【新增】
        createRooms(); // 构建游戏房间地图
        levelManager = new LevelManager(this);
        levelTimer = new LevelTimer(this);
        levelManager.startLevel(1); // 从第一关开始

    }

    /**
     * 获取玩家当前所在房间
     *
     * @return 当前房间实例
     */
    public Room getCurrentRoom() {
        if (multiplayerSessionActive && activeOnlinePlayerId != null) {
            Player active = onlinePlayers.get(activeOnlinePlayerId);
            if (active != null) {
                return active.getCurrentRoom();
            }
        }
        return currentRoom;
    }

    /**
     * 设置玩家当前所在房间，并记录历史。
     * 若目标为黑暗区域且无手电筒，罚时并留在原房间。
     * 若目标为条件门且未满足准入，提示并留在原房间。
     * 若目标为传输房间，则触发随机传送。
     *
     * @param targetRoom 目标房间实例
     * @return 成功进入或传送返回 true，被黑暗区域阻挡返回 false
     */
    public boolean setCurrentRoom(Room targetRoom) {
        if (!isRoomAccessible(targetRoom)) {
            System.out.println(LevelConfig.LOCKED_EXIT_MESSAGE);
            return false;
        }

        if (targetRoom instanceof DarkRoom
                && levelManager.getCurrentLevelConfig().isMainBuildingDark()) {
            DarkRoom darkRoom = (DarkRoom) targetRoom;
            if (!darkRoom.canEnter(player)) {
                System.out.println(DarkRoom.PENALTY_MESSAGE);
                ActionTimeCost.deduct(this, ActionTimeCost.DARK_PENALTY);
                return false;
            }
        }

        if (targetRoom instanceof GatedRoom) {
            GatedRoom gatedRoom = (GatedRoom) targetRoom;
            String denialMessage = gatedRoom.getDenialMessage(this);
            if (denialMessage != null) {
                System.out.println(denialMessage);
                return false;
            }
        }

        // 在切换房间前，将当前房间加入历史记录（如果当前房间不为null）
        if (this.currentRoom != null) {
            roomHistory.add(this.currentRoom);
        }

        // 检查是否是已启用的传输房间，如果是则触发传输（E17 第五关体育馆）
        if (targetRoom instanceof TeleportRoom) {
            TeleportRoom teleportRoom = (TeleportRoom) targetRoom;
            if (teleportRoom.isTeleportEnabled()) {
                Room teleportedRoom = teleportRoom.teleport();
                this.currentRoom = teleportedRoom;
                player.setCurrentRoom(teleportedRoom);
                System.out.println("你进入了一个神秘的房间，突然被传送到了其他地方！");
            } else {
                this.currentRoom = targetRoom;
                player.setCurrentRoom(targetRoom);
            }
        } else {
            this.currentRoom = targetRoom;
            player.setCurrentRoom(targetRoom);
            if ("boxue_west".equals(targetRoom.getRoomId())) {
                levelManager.onEnterWestBuilding();
            }
        }
        return true;
    }


    /**
     * 获取命令解析器实例
     *
     * @return Parser实例
     */
    public Parser getParser() {
        return parser;
    }

    /**
     * 创建十房固定校园地图并连接出口（拓扑不变，关卡差异由 LevelConfig 控制）。
     */
    private void createRooms() {
        Room gate = new Room("gate", "武汉理工大学校门",
            "晚二十三点后凭一卡通与归寝单回寝。");
        DarkRoom boxueMain = new DarkRoom("boxue_main", "博学主楼",
            "大厅自习区。公告栏有停电通知，部分关卡会生效。");
        Room boxueNorth = new Room("boxue_north", "博学北楼",
            "志愿者服务台在二楼转角，楼梯口常有失物。");
        Room supermarket = new Room("supermarket", "教育超市",
            "卖日用品。收银台旁宿管可补办或兑换一卡通。");
        GatedRoom dormitory = GatedRoom.dormitory();
        GatedRoom library = GatedRoom.library();
        Room boxueEast = new Room("boxue_east", "博学东楼",
            "部分教室晚课结束，走廊较亮。");
        Room boxueWest = new Room("boxue_west", "博学西楼",
            "西侧旧教室，部分门锁锈蚀。");
        TeleportRoom gymnasium = new TeleportRoom("gymnasium", "体育馆",
            "器材室和失物招领在入口左手。", new ArrayList<>());
        Room canteen = new Room("canteen", "越苑食堂",
            "晚食窗口二十二点三十分关闭，失物招领在餐盘回收处。");

        gate.setExit("north", boxueMain);
        gate.setExit("west", gymnasium);
        gate.setExit("east", canteen);

        boxueMain.setExit("south", gate);
        boxueMain.setExit("north", boxueNorth);
        boxueMain.setExit("west", boxueWest);
        boxueMain.setExit("east", boxueEast);

        boxueNorth.setExit("south", boxueMain);
        boxueNorth.setExit("west", supermarket);
        boxueNorth.setExit("east", library);

        supermarket.setExit("east", boxueNorth);
        supermarket.setExit("north", dormitory);

        dormitory.setExit("south", supermarket);

        library.setExit("west", boxueNorth);
        library.setExit("south", boxueEast);

        boxueEast.setExit("north", library);
        boxueEast.setExit("west", boxueMain);

        boxueWest.setExit("east", boxueMain);

        gymnasium.setExit("east", gate);
        canteen.setExit("west", gate);

        currentRoom = gate;
        player.setCurrentRoom(gate);

        roomRegistry = new HashMap<>();
        roomRegistry.put("gate", gate);
        roomRegistry.put("boxue_main", boxueMain);
        roomRegistry.put("boxue_north", boxueNorth);
        roomRegistry.put("supermarket", supermarket);
        roomRegistry.put("dormitory", dormitory);
        roomRegistry.put("library", library);
        roomRegistry.put("boxue_east", boxueEast);
        roomRegistry.put("boxue_west", boxueWest);
        roomRegistry.put("gymnasium", gymnasium);
        roomRegistry.put("canteen", canteen);
    }

    /**
     * 按关卡更新大门任务提示等房间状态。
     *
     * @param config 当前关卡配置
     */
    public void applyLevelRoomState(LevelConfig config) {
        LevelRoomContent.apply(this, config);
        configureGymnasiumTeleport(config);
    }

    /**
     * 按关卡配置体育馆传送（E17）：仅第五关启用，目标为除寝室、图书馆外的全部房间。
     *
     * @param config 当前关卡配置
     */
    private void configureGymnasiumTeleport(LevelConfig config) {
        Room gym = getRoomById(UnlockService.GYM_ROOM_ID);
        if (!(gym instanceof TeleportRoom) || config == null) {
            return;
        }
        TeleportRoom teleportGym = (TeleportRoom) gym;
        if (config.getLevelNumber() == LevelConfig.MAX_LEVEL) {
            teleportGym.setTargetRooms(buildGymTeleportTargets());
            teleportGym.setTeleportEnabled(true);
        } else {
            teleportGym.setTeleportEnabled(false);
        }
    }

    private List<Room> buildGymTeleportTargets() {
        List<Room> targets = new ArrayList<>();
        for (Map.Entry<String, Room> entry : roomRegistry.entrySet()) {
            String roomId = entry.getKey();
            if (UnlockService.DORMITORY_ROOM_ID.equals(roomId)) {
                continue;
            }
            if (UseCommand.LIBRARY_ROOM_ID.equals(roomId)) {
                continue;
            }
            targets.add(entry.getValue());
        }
        return targets;
    }

    /**
     * 当前关卡是否允许进入目标房间（E15 按关解锁出口）。
     *
     * @param targetRoom 目标房间
     * @return 允许进入返回 true
     */
    public boolean isRoomAccessible(Room targetRoom) {
        if (targetRoom == null) {
            return false;
        }
        return levelManager.getCurrentLevelConfig().isRoomUnlocked(targetRoom.getRoomId());
    }

    /**
     *  游戏主控循环，直到用户输入退出命令后结束整个程序.
     */
    public void play()
    {
        levelTimer.setAutoTickEnabled(true);
        printWelcome();

        boolean finished = false;
        while (! finished) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
        levelTimer.shutdown();
        System.out.println("Thank you for playing.  Good bye.");
    }

    /**
     * 向用户输出欢迎信息.
     */
    private void printWelcome()
    {
        System.out.println();
        System.out.println("Welcome to the World of Zuul!");
        System.out.println("World of Zuul is a new, incredibly boring adventure game.");
        System.out.println("Type 'help' if you need help.");
        System.out.println();
        printLocationInfo(); // 显示当前位置信息
    }

    /**
     * 处理用户输入的命令，通过命令管理器分发执行
     *
     * @param command 用户输入的解析后命令
     * @return 若命令为quit则返回true，否则返回false
     */
    protected boolean processCommand(Command command) {
        String commandWord = command.getCommandWord();
        String secondWord = command.getSecondWord();
        return commandManager.executeCommand(commandWord, secondWord, this);
    }

    /**
     * 打印当前房间的详细信息（描述和出口）
     */
    public void printLocationInfo() {
        System.out.println(currentRoom.getLongDescription());
    }

    /**
     * 将玩家移动到上一个房间
     *
     * @return 如果成功返回上一个房间则为true，否则为false
     */
    /**
     * 当前是否困在博学西楼内（E14：困锁时 go/back 均不可离开）。
     *
     * @return 困锁中返回 true
     */
    public boolean isTrappedInWestBuilding() {
        return currentRoom != null
            && LevelManager.WEST_BUILDING_ROOM_ID.equals(currentRoom.getRoomId())
            && levelManager.isWestBuildingExitLocked();
    }

    public boolean goBack() {
        if (isTrappedInWestBuilding()) {
            System.out.println(LevelManager.WEST_BUILDING_TRAP_MESSAGE);
            return false;
        }
        // 检查是否有可返回的房间
        if (!roomHistory.isEmpty()) {
            // 从历史记录中取出最后一个房间并设为当前房间
            Room previousRoom = roomHistory.remove(roomHistory.size() - 1);
            // 直接修改当前房间，不调用setCurrentRoom()，避免重复添加历史
            this.currentRoom = previousRoom;
            player.setCurrentRoom(previousRoom); // 更新玩家位置【新增】
            return true;
        }
        return false;
    }

    /**
     * 获取玩家实例
     * 【新增】
     *
     * @return 玩家实例
     */
    public Player getPlayer() {
        if (multiplayerSessionActive && activeOnlinePlayerId != null) {
            Player active = onlinePlayers.get(activeOnlinePlayerId);
            if (active != null) {
                return active;
            }
        }
        return player;
    }

    /**
     * 获取命令管理器实例
     * 【新增】提供对命令管理器的访问，用于GUI
     *
     * @return 命令管理器实例
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * 获取关卡管理器实例。
     *
     * @return LevelManager 实例
     */
    public LevelManager getLevelManager() {
        return levelManager;
    }

    /**
     * 获取熄灯倒计时实例。
     *
     * @return LevelTimer 实例
     */
    public LevelTimer getLevelTimer() {
        return levelTimer;
    }

    /**
     * 根据房间 ID 获取房间，供 LevelConfig 与后续 E15 按关解锁使用。
     *
     * @param roomId 房间标识
     * @return 房间实例，不存在时返回 null
     */
    public Room getRoomById(String roomId) {
        if (roomRegistry == null || roomId == null) {
            return null;
        }
        return roomRegistry.get(roomId);
    }

    /**
     * 获取房间注册表只读视图。
     *
     * @return 不可修改的房间 ID 映射
     */
    public Map<String, Room> getRoomRegistry() {
        if (roomRegistry == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(roomRegistry);
    }

    /**
     * 关卡加载或重开时重置玩家位置，不记录历史、不触发传送。
     *
     * @param room 目标房间
     */
    public void resetPlayerPosition(Room room) {
        if (multiplayerSessionActive) {
            for (Map.Entry<String, Player> entry : onlinePlayers.entrySet()) {
                entry.getValue().setCurrentRoom(room);
                onlineRoomHistories.put(entry.getKey(), new ArrayList<>());
            }
            if (activeOnlinePlayerId != null) {
                currentRoom = room;
                roomHistory = onlineRoomHistories.computeIfAbsent(
                    activeOnlinePlayerId, key -> new ArrayList<>());
            }
            return;
        }
        this.currentRoom = room;
        player.setCurrentRoom(room);
        roomHistory.clear();
    }

    /**
     * 关卡切换时重置与 unlock 相关的房间物品（如体育馆手电筒）。
     */
    public void resetUnlockRoomState() {
        Room gym = getRoomById(UnlockService.GYM_ROOM_ID);
        if (gym != null) {
            gym.removeItemByDescription(DarkRoom.FLASHLIGHT_ITEM);
        }
    }

    /**
     * 读档时从地图上移除已拾取物品，避免重复拾取。
     *
     * @param collectedItems 背包中的物品
     */
    public void removeCollectedItemsFromRooms(List<Item> collectedItems) {
        if (collectedItems == null || roomRegistry == null) {
            return;
        }
        for (Item item : collectedItems) {
            for (Room room : roomRegistry.values()) {
                Item removed = room.removeItemByDescription(item.getDescription());
                if (removed != null) {
                    break;
                }
            }
        }
    }

    /**
     * 获取基础设施服务（认证 + 存档，共享 H2）。
     *
     * @return InfrastructureServices 实例
     */
    public InfrastructureServices getInfrastructureServices() {
        if (infrastructureServices == null) {
            infrastructureServices = InfrastructureServices.getDefault();
        }
        return infrastructureServices;
    }

    /**
     * 注入基础设施服务（测试用）。
     */
    public void setInfrastructureServices(InfrastructureServices services) {
        this.infrastructureServices = services;
        if (services != null) {
            this.persistenceService = services.getPersistenceService();
            this.authService = services.getAuthService();
        }
    }

    /**
     * 联机客户端：切换为服务端权威计时（本地不再自动 tick）。
     */
    public void useServerTimerAuthority() {
        levelTimer.setTimerAuthority(TimerAuthority.SERVER_CLIENT);
    }

    /**
     * 联机服务端：启用权威计时（本地 tick 作为服务端时钟）。
     */
    public void useServerHostTimerAuthority() {
        levelTimer.setTimerAuthority(TimerAuthority.SERVER_HOST);
    }

    /**
     * 由联机服务端推送剩余秒数（客户端调用）。
     *
     * @param remainingSeconds 权威剩余秒数
     */
    public void applyServerRemainingSeconds(int remainingSeconds) {
        levelTimer.applyAuthoritativeRemainingSeconds(remainingSeconds);
    }

    /**
     * 联机客户端：根据服务端快照刷新本地展示（房间、计时、关卡号）。
     *
     * @param snapshot 服务端状态
     * @param localPlayerId 本机玩家 ID
     */
    public void syncClientViewFromSnapshot(GameStateSnapshot snapshot, String localPlayerId) {
        if (snapshot == null || localPlayerId == null) {
            return;
        }
        startMultiplayerSession();
        useServerTimerAuthority();
        getLevelTimer().setAutoTickEnabled(false);
        applyServerRemainingSeconds(snapshot.getRemainingSeconds());
        getLevelManager().syncDisplayFromServer(snapshot.getLevel(), snapshot.getLevelState());

        String localName = snapshot.getPlayers().stream()
            .filter(player -> localPlayerId.equals(player.getPlayerId()))
            .map(PlayerStateSnapshot::getDisplayName)
            .findFirst()
            .orElse("联机玩家");
        ensureOnlinePlayer(localPlayerId, localName);
        setActiveOnlinePlayer(localPlayerId);

        Room room = getRoomById(snapshot.getRoomId());
        if (room != null) {
            currentRoom = room;
            Player local = onlinePlayers.get(localPlayerId);
            if (local != null) {
                local.setCurrentRoom(room);
                getPlayer().setName(local.getName());
            }
        }
    }

    /**
     * 确保联机玩家存在于本地镜像（客户端展示用）。
     */
    public void ensureOnlinePlayer(String playerId, String displayName) {
        if (playerId == null) {
            return;
        }
        if (!multiplayerSessionActive) {
            startMultiplayerSession();
        }
        if (!onlinePlayers.containsKey(playerId)) {
            Room gate = getRoomById("gate");
            Player onlinePlayer = new Player(
                displayName == null || displayName.trim().isEmpty() ? "联机玩家" : displayName.trim(),
                gate);
            onlinePlayers.put(playerId, onlinePlayer);
            onlineRoomHistories.put(playerId, new ArrayList<>());
        }
    }

    /**
     * 获取 H2 持久化服务。
     */
    public GamePersistenceService getPersistenceService() {
        if (persistenceService != null) {
            return persistenceService;
        }
        return getInfrastructureServices().getPersistenceService();
    }

    /**
     * 注入持久化服务（单元测试用）。
     */
    public void setPersistenceService(GamePersistenceService service) {
        this.persistenceService = service;
    }

    /**
     * 五关全部通关后写入 H2 通关记录。
     */
    public void onAllLevelsCompleted() {
        try {
            getPersistenceService().recordClear(getPlayer().getName());
        } catch (RuntimeException exception) {
            System.out.println("通关记录写入失败: " + exception.getMessage());
        }
    }

    /**
     * 获取登录注册服务（懒加载，与存档共用 H2）。
     *
     * @return AuthService 实例
     */
    public AuthService getAuthService() {
        if (authService != null) {
            return authService;
        }
        return getInfrastructureServices().getAuthService();
    }

    /**
     * 注入登录注册服务（测试用）。
     */
    public void setAuthService(AuthService service) {
        this.authService = service;
    }

    /**
     * 绑定已登录用户：同步玩家昵称，供存档与通关记录使用。
     *
     * @param session 登录会话
     */
    public void bindAuthSession(AuthSession session) {
        if (session == null) {
            this.authSession = null;
            return;
        }
        this.authSession = session;
        getPlayer().setName(session.getDisplayName());
    }

    /**
     * 获取当前登录会话；未登录时返回 null。
     *
     * @return 登录会话或 null
     */
    public AuthSession getAuthSession() {
        return authSession;
    }

    /**
     * 是否已登录。
     *
     * @return 已登录返回 true
     */
    public boolean isLoggedIn() {
        return authSession != null;
    }

    /**
     * 启用 F6 联机会话（共享世界、独立玩家位置与背包）。
     */
    public void startMultiplayerSession() {
        multiplayerSessionActive = true;
    }

    public boolean isMultiplayerSessionActive() {
        return multiplayerSessionActive;
    }

    /**
     * 向联机房间添加玩家，初始在校门。
     *
     * @param displayName 显示昵称
     * @return 玩家 ID
     */
    public String addOnlinePlayer(String displayName) {
        if (!multiplayerSessionActive) {
            startMultiplayerSession();
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("玩家昵称不能为空");
        }
        String playerId = UUID.randomUUID().toString();
        Room gate = getRoomById("gate");
        Player onlinePlayer = new Player(displayName.trim(), gate);
        onlinePlayers.put(playerId, onlinePlayer);
        onlineRoomHistories.put(playerId, new ArrayList<>());
        if (activeOnlinePlayerId == null) {
            setActiveOnlinePlayer(playerId);
        }
        return playerId;
    }

    public void removeOnlinePlayer(String playerId) {
        if (playerId == null) {
            return;
        }
        onlinePlayers.remove(playerId);
        onlineRoomHistories.remove(playerId);
        if (playerId.equals(activeOnlinePlayerId)) {
            activeOnlinePlayerId = onlinePlayers.isEmpty() ? null : onlinePlayers.keySet().iterator().next();
            if (activeOnlinePlayerId != null) {
                loadActivePlayerContext();
            }
        }
    }

    public Collection<String> getOnlinePlayerIds() {
        return Collections.unmodifiableSet(onlinePlayers.keySet());
    }

    public Map<String, Player> getOnlinePlayers() {
        return Collections.unmodifiableMap(onlinePlayers);
    }

    public String getActiveOnlinePlayerId() {
        return activeOnlinePlayerId;
    }

    /**
     * 切换命令执行上下文到指定联机玩家。
     *
     * @param playerId 玩家 ID
     */
    public void setActiveOnlinePlayer(String playerId) {
        if (!multiplayerSessionActive || playerId == null || !onlinePlayers.containsKey(playerId)) {
            return;
        }
        persistActivePlayerContext();
        activeOnlinePlayerId = playerId;
        loadActivePlayerContext();
    }

    private void persistActivePlayerContext() {
        if (!multiplayerSessionActive || activeOnlinePlayerId == null) {
            return;
        }
        Player active = onlinePlayers.get(activeOnlinePlayerId);
        if (active != null && currentRoom != null) {
            active.setCurrentRoom(currentRoom);
        }
        onlineRoomHistories.put(activeOnlinePlayerId, new ArrayList<>(roomHistory));
    }

    private void loadActivePlayerContext() {
        if (!multiplayerSessionActive || activeOnlinePlayerId == null) {
            return;
        }
        Player active = onlinePlayers.get(activeOnlinePlayerId);
        if (active == null) {
            return;
        }
        currentRoom = active.getCurrentRoom();
        roomHistory = onlineRoomHistories.computeIfAbsent(activeOnlinePlayerId, key -> new ArrayList<>());
    }

    /**
     * 创建联机专用游戏实例：服务端权威计时并自动 tick。
     *
     * @return 已配置的多人游戏实例
     */
    public static Game createMultiplayerHostGame() {
        Game game = new Game();
        game.startMultiplayerSession();
        game.useServerHostTimerAuthority();
        game.getLevelTimer().setAutoTickEnabled(true);
        return game;
    }

}