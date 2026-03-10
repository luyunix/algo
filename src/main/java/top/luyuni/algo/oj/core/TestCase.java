package top.luyuni.algo.oj.core;

import java.util.function.BiPredicate;

/**
 * 测试用例定义
 */
public class TestCase<Input, Output> {
    private final String name;
    private final Input input;
    private final Output expectedOutput;
    private final String description;
    private final BiPredicate<Output, Output> customEquals;

    public TestCase(String name, Input input, Output expectedOutput, String description) {
        this(name, input, expectedOutput, description, null);
    }

    public TestCase(String name, Input input, Output expectedOutput, String description,
                    BiPredicate<Output, Output> customEquals) {
        this.name = name;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.description = description;
        this.customEquals = customEquals;
    }

    public String getName() { return name; }
    public Input getInput() { return input; }
    public Output getExpectedOutput() { return expectedOutput; }
    public String getDescription() { return description; }
    public BiPredicate<Output, Output> getCustomEquals() { return customEquals; }
}
