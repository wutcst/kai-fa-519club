package cn.edu.whut.sept.zuul.multiplayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 联机命令执行结果：输出文本 + 最新状态。
 */
public class GameCommandResult {

    private final List<String> messages;
    private final boolean quitRequested;
    private final GameStateSnapshot state;

    public GameCommandResult(List<String> messages, boolean quitRequested, GameStateSnapshot state) {
        this.messages = messages == null ? new ArrayList<>() : new ArrayList<>(messages);
        this.quitRequested = quitRequested;
        this.state = state;
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public boolean isQuitRequested() {
        return quitRequested;
    }

    public GameStateSnapshot getState() {
        return state;
    }
}
