package cn.edu.whut.sept.zuul.multiplayer;

import java.util.List;

import cn.edu.whut.sept.zuul.Game;

/**
 * 联机游戏引擎门面：在服务端权威执行命令并返回状态。
 */
public class GameEngineFacade {

    public GameCommandResult executeCommand(GameRoom room, String playerId,
                                            String commandWord, String secondWord) {
        if (room == null || playerId == null || commandWord == null) {
            throw new IllegalArgumentException("房间、玩家与命令不能为空");
        }
        Game game = room.getGame();
        synchronized (room.getLock()) {
            if (!room.hasPlayer(playerId)) {
                throw new IllegalArgumentException("玩家不在该房间中");
            }
            game.setActiveOnlinePlayer(playerId);
            List<String> output;
            boolean quit;
            try (OutputCapture capture = OutputCapture.start()) {
                quit = game.getCommandManager().executeCommand(commandWord, secondWord, game);
                output = capture.getLines();
            }
            GameStateSnapshot state = GameStateSnapshot.from(game, playerId);
            return new GameCommandResult(output, quit, state);
        }
    }

    public GameStateSnapshot getState(GameRoom room, String playerId) {
        if (room == null) {
            throw new IllegalArgumentException("房间不能为空");
        }
        synchronized (room.getLock()) {
            if (playerId != null && !room.hasPlayer(playerId)) {
                throw new IllegalArgumentException("玩家不在该房间中");
            }
            return GameStateSnapshot.from(room.getGame(), playerId);
        }
    }
}
