package top.luyuni.algo.oj.core;

/**
 * 判题结果
 */
public class JudgeResult {
    public enum Status {
        AC,     // Accepted - 通过
        WA,     // Wrong Answer - 答案错误
        TLE,    // Time Limit Exceeded - 超时
        RE,     // Runtime Error - 运行错误
        CE      // Compile Error - 编译错误
    }

    private final Status status;
    private final String message;
    private final long timeMs;
    private final String testCaseName;
    private final Object input;      // 新增：输入参数
    private final Object expected;   // 新增：期望输出
    private final Object actual;     // 新增：实际输出

    public JudgeResult(Status status, String message, long timeMs, String testCaseName) {
        this(status, message, timeMs, testCaseName, null, null, null);
    }

    // 新增构造函数，包含输入输出信息
    public JudgeResult(Status status, String message, long timeMs, String testCaseName,
                       Object input, Object expected, Object actual) {
        this.status = status;
        this.message = message;
        this.timeMs = timeMs;
        this.testCaseName = testCaseName;
        this.input = input;
        this.expected = expected;
        this.actual = actual;
    }

    public static JudgeResult ac(String testCaseName, long timeMs) {
        return new JudgeResult(Status.AC, "Accepted", timeMs, testCaseName);
    }

    public static JudgeResult wa(String testCaseName, String expected, String actual) {
        return new JudgeResult(Status.WA,
                String.format("Expected: %s, Actual: %s", expected, actual), 0, testCaseName);
    }

    // 新增：包含输入输出的 WA 方法
    public static JudgeResult wa(String testCaseName, Object input, Object expected, Object actual) {
        return new JudgeResult(Status.WA,
                String.format("Expected: %s, Actual: %s", expected, actual), 0, testCaseName,
                input, expected, actual);
    }

    public static JudgeResult re(String testCaseName, String error) {
        return new JudgeResult(Status.RE, error, 0, testCaseName);
    }

    public static JudgeResult tle(String testCaseName, long limitMs) {
        return new JudgeResult(Status.TLE, "Time limit exceeded: " + limitMs + "ms", limitMs, testCaseName);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    // 新增 getter 方法
    public Object getInput() {
        return input;
    }

    public Object getExpected() {
        return expected;
    }

    public Object getActual() {
        return actual;
    }

    public boolean isAccepted() {
        return status == Status.AC;
    }
}
