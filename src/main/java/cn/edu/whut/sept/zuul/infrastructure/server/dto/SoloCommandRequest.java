package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 单机命令请求。
 */
public class SoloCommandRequest {

    private String commandWord;
    private String secondWord;

    public String getCommandWord() {
        return commandWord;
    }

    public void setCommandWord(String commandWord) {
        this.commandWord = commandWord;
    }

    public String getSecondWord() {
        return secondWord;
    }

    public void setSecondWord(String secondWord) {
        this.secondWord = secondWord;
    }
}
