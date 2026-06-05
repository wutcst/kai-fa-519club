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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

    /**
     * 创建游戏并初始化内部数据和解析器.
     */
    public Game() {
        commandManager = new CommandManager(); // 初始化命令管理器
        parser = new Parser(commandManager); // 传入命令管理器初始化解析器
        roomHistory = new ArrayList<>(); // 初始化房间历史记录列表
        player = new Player("冒险者", currentRoom); // 初始化玩家【新增】
        createRooms(); // 构建游戏房间地图

    }

    /**
     * 获取玩家当前所在房间
     *
     * @return 当前房间实例
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * 设置玩家当前所在房间，并记录历史
     * 如果房间是传输房间，则触发传输
     *
     * @param currentRoom 目标房间实例
     */
    public void setCurrentRoom(Room currentRoom) {
        // 在切换房间前，将当前房间加入历史记录（如果当前房间不为null）
        if (this.currentRoom != null) {
            roomHistory.add(this.currentRoom);
        }

        // 检查是否是传输房间，如果是则触发传输
        if (currentRoom instanceof TeleportRoom) {
            TeleportRoom teleportRoom = (TeleportRoom) currentRoom;
            Room targetRoom = teleportRoom.teleport();
            this.currentRoom = targetRoom;
            player.setCurrentRoom(targetRoom); // 更新玩家位置【新增】
            System.out.println("你进入了一个神秘的房间，突然被传送到了其他地方！");
        } else {
            this.currentRoom = currentRoom;
            player.setCurrentRoom(currentRoom); // 更新玩家位置【新增】
        }
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
     * 创建所有房间对象并连接其出口用以构建迷宫.
     */
    private void createRooms()
    {
        Room outside, theater, pub, lab, office,teleportRoom;

        // create the rooms
        outside = new Room("outside the main entrance of the university");
        theater = new Room("in a lecture theater");
        pub = new Room("in the campus pub");
        lab = new Room("in a computing lab");
        office = new Room("in the computing admin office");

        // 创建传输房间，设置可能传送到的目标房间
        List<Room> targetRooms = Arrays.asList(outside, theater, pub, lab, office);
        teleportRoom = new TeleportRoom("in a mysterious teleportation room", targetRooms);

        // 为房间添加物品【新增】
        outside.addItem(new Item("一把旧钥匙", 50));
        theater.addItem(new Item("一本教科书", 800));
        pub.addItem(new Item("一个空酒杯", 200));
        lab.addItem(new Item("一台旧电脑", 3000));
        office.addItem(new Item("一张校园地图", 100));
        teleportRoom.addItem(new Item("一个闪烁的传送水晶", 500));

        // 随机在一个房间添加魔法饼干【新增】
        // 创建房间列表
        List<Room> allRooms = Arrays.asList(outside, theater, pub, lab, office, teleportRoom);
        // 随机选择房间
        Random random = new Random();
        int cookieRoomIndex = random.nextInt(allRooms.size());
        Room cookieRoom = allRooms.get(cookieRoomIndex);
        cookieRoom.addItem(new Item("magic cookie", 100));
        System.out.println("提示：magic cookie已藏在 " + cookieRoom.getShortDescription() + " 中！");

        // initialise room exits
        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", pub);
        outside.setExit("north", teleportRoom);

        theater.setExit("west", outside);

        pub.setExit("east", outside);

        lab.setExit("north", outside);
        lab.setExit("east", office);

        office.setExit("west", lab);

        // 设置传输房间的出口
        teleportRoom.setExit("south", outside);

        currentRoom = outside;  // start game outside
        player.setCurrentRoom(outside);// 设置玩家初始位置【新增】

    }

    /**
     *  游戏主控循环，直到用户输入退出命令后结束整个程序.
     */
    public void play()
    {
        printWelcome();

        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.

        boolean finished = false;
        while (! finished) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
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
    public boolean goBack() {
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

}