package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 极简版测试工具：仅过滤无关输出，不处理输入（避免类型转换错误）
 * @author liujing
 * @version 1.2
 */
public class TestUtils {
    private static final PrintStream ORIGINAL_OUT = System.out;
    private static final ByteArrayOutputStream OUTPUT_STREAM = new ByteArrayOutputStream();

    // 重定向输出（仅用于过滤，可选）
    public static void redirectOutput() {
        System.setOut(new PrintStream(OUTPUT_STREAM));
    }

    // 恢复输出
    public static void restoreOutput() {
        System.setOut(ORIGINAL_OUT);
    }

    // 过滤magic cookie/物品等无关输出（可选）
    public static String getFilteredOutput() {
        String fullOutput = OUTPUT_STREAM.toString();
        List<String> ignoreKeywords = Arrays.asList("magic cookie", "房间里有这些物品", "重量");
        return Arrays.stream(fullOutput.split("\n"))
                .map(String::trim)
                .filter(line -> !ignoreKeywords.stream().anyMatch(line::contains))
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"))
                .trim();
    }

    // 清空输出缓存
    public static void clearOutput() {
        OUTPUT_STREAM.reset();
    }
}