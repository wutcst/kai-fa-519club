/**
 * 测试Parser对多单词参数的支持
 *
 * @author liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ParserTest {
    private CommandManager commandManager;
    private Parser parser;

    @Before
    public void setUp() {
        commandManager = new CommandManager();
        parser = new Parser(commandManager);
    }

    /**
     * 测试解析单单词命令
     */
    @Test
    public void testParseSingleWordCommand() {
        // 模拟输入
        InputStream inputStream = new ByteArrayInputStream("look\n".getBytes());
        System.setIn(inputStream);

        // 创建新的parser使用模拟输入
        parser = new Parser(commandManager);

        Command command = parser.getCommand();
        assertEquals("look", command.getCommandWord());
        assertNull(command.getSecondWord());
        assertFalse(command.isUnknown());
    }

    /**
     * 测试解析带单单词参数的命令
     */
    @Test
    public void testParseCommandWithSingleWordParameter() {
        InputStream inputStream = new ByteArrayInputStream("go east\n".getBytes());
        System.setIn(inputStream);
        parser = new Parser(commandManager);

        Command command = parser.getCommand();
        assertEquals("go", command.getCommandWord());
        assertEquals("east", command.getSecondWord());
        assertFalse(command.isUnknown());
    }

    /**
     * 测试解析带多单词参数的命令
     */
    @Test
    public void testParseCommandWithMultiWordParameter() {
        InputStream inputStream = new ByteArrayInputStream("take magic cookie\n".getBytes());
        System.setIn(inputStream);
        parser = new Parser(commandManager);

        Command command = parser.getCommand();
        assertEquals("take", command.getCommandWord());
        assertEquals("magic cookie", command.getSecondWord());
        assertFalse(command.isUnknown());
    }

    /**
     * 测试解析带多单词参数和额外空格的命令
     */
    @Test
    public void testParseCommandWithMultiWordParameterAndSpaces() {
        InputStream inputStream = new ByteArrayInputStream("take  一张校园地图  \n".getBytes());
        System.setIn(inputStream);
        parser = new Parser(commandManager);

        Command command = parser.getCommand();
        assertEquals("take", command.getCommandWord());
        assertEquals("一张校园地图", command.getSecondWord());
        assertFalse(command.isUnknown());
    }

    /**
     * 测试解析带长多单词参数的命令
     */
    @Test
    public void testParseCommandWithLongMultiWordParameter() {
        InputStream inputStream = new ByteArrayInputStream("take a very long item name with multiple words\n".getBytes());
        System.setIn(inputStream);
        parser = new Parser(commandManager);

        Command command = parser.getCommand();
        assertEquals("take", command.getCommandWord());
        assertEquals("a very long item name with multiple words", command.getSecondWord());
        assertFalse(command.isUnknown());
    }

    /**
     * 测试解析eat cookie命令
     */
    @Test
    public void testParseEatCookieCommand() {
        InputStream inputStream = new ByteArrayInputStream("eat cookie\n".getBytes());
        System.setIn(inputStream);
        parser = new Parser(commandManager);

        Command command = parser.getCommand();
        assertEquals("eat", command.getCommandWord());
        assertEquals("cookie", command.getSecondWord());
        assertFalse(command.isUnknown());
    }

    /**
     * 测试解析未知命令
     */
    @Test
    public void testParseUnknownCommand() {
        InputStream inputStream = new ByteArrayInputStream("unknown command\n".getBytes());
        System.setIn(inputStream);
        parser = new Parser(commandManager);

        Command command = parser.getCommand();
        assertNull(command.getCommandWord());
        assertEquals("command", command.getSecondWord()); // 只有第一个单词后的内容
        assertTrue(command.isUnknown());
    }
}