package cn.edu.whut.sept.zuul.multiplayer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 捕获 {@link System#out} 输出，供联机命令结果返回给客户端。
 */
public final class OutputCapture implements AutoCloseable {

    private final PrintStream originalOut;
    private final ByteArrayOutputStream buffer;
    private final PrintStream capturedOut;

    private OutputCapture() {
        this.originalOut = System.out;
        this.buffer = new ByteArrayOutputStream();
        this.capturedOut = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        System.setOut(capturedOut);
    }

    public static OutputCapture start() {
        return new OutputCapture();
    }

    public List<String> getLines() {
        String text = buffer.toString(StandardCharsets.UTF_8);
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(text.split("\\R")));
    }

    @Override
    public void close() {
        System.setOut(originalOut);
        capturedOut.close();
    }
}
