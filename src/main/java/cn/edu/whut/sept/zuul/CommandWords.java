package cn.edu.whut.sept.zuul;

import java.util.HashMap;

public class CommandWords
{
    private HashMap<String, Command> commands;

    public CommandWords()
    {
        commands = new HashMap<>();
        commands.put("go", new GoCommand());
        commands.put("help", new HelpCommand(this));
        commands.put("quit", new QuitCommand());
    }

    public Command get(String word)
    {
        return commands.get(word);
    }

    public void showAll()
    {
        for (String command : commands.keySet()) {
            System.out.print(command + "  ");
        }
        System.out.println();
    }
}
