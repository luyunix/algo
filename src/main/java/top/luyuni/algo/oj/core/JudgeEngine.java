package top.luyuni.algo.oj.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * 判题引擎
 */
public class JudgeEngine<Input, Output> {

    private final List<TestCase<Input, Output>> testCases = new ArrayList<>();
    private long timeLimitMs = 1000;
    private BiPredicate<Output, Output> customEquals = null;

    public JudgeEngine<Input, Output> addTestCase(String name, Input input, Output expected, String description) {
        testCases.add(new TestCase<>(name, input, expected, description));
        return this;
    }

    public JudgeEngine<Input, Output> addTestCase(String name, Input input, Output expected, String description,
                                                   BiPredicate<Output, Output> customEquals) {
        testCases.add(new TestCase<>(name, input, expected, description, customEquals));
        return this;
    }

    public JudgeEngine<Input, Output> setTimeLimit(long ms) {
        this.timeLimitMs = ms;
        return this;
    }

    public List<JudgeResult> judge(Function<Input, Output> solution) {
        List<JudgeResult> results = new ArrayList<>();

        for (TestCase<Input, Output> tc : testCases) {
            JudgeResult result = runSingleTest(solution, tc);
            results.add(result);
        }

        return results;
    }

    private JudgeResult runSingleTest(Function<Input, Output> solution, TestCase<Input, Output> tc) {
        try {
            // 在运行前保存输入的原始状态（用于后续显示）
            Input originalInput = cloneInput(tc.getInput());

            long start = System.currentTimeMillis();

            final Object[] actualOutput = new Object[1];
            final Exception[] exception = new Exception[1];

            Thread worker = new Thread(() -> {
                try {
                    actualOutput[0] = solution.apply(tc.getInput());
                } catch (Exception e) {
                    exception[0] = e;
                }
            });

            worker.start();
            worker.join(timeLimitMs);

            long elapsed = System.currentTimeMillis() - start;

            if (worker.isAlive()) {
                worker.interrupt();
                return JudgeResult.tle(tc.getName(), timeLimitMs);
            }

            if (exception[0] != null) {
                return JudgeResult.re(tc.getName(), exception[0].getMessage());
            }

            @SuppressWarnings("unchecked")
            Output result = (Output) actualOutput[0];
           if (equalsOutput(tc.getExpectedOutput(), result, tc.getCustomEquals())) {
                return new JudgeResult(JudgeResult.Status.AC, "Accepted", elapsed, tc.getName(),
                    originalInput, tc.getExpectedOutput(), result);
            } else {
                return JudgeResult.wa(tc.getName(), originalInput,
                    formatOutput(tc.getExpectedOutput()),
                    formatOutput(result));
            }

        } catch (Exception e) {
            return JudgeResult.re(tc.getName(), e.getMessage());
        }
    }

    protected boolean equalsOutput(Output expected, Output actual, BiPredicate<Output, Output> customEquals) {
        if (customEquals != null) {
            return customEquals.test(expected, actual);
        }
        if (expected == null) return actual == null;

        if (expected.getClass().isArray() && actual.getClass().isArray()) {
            return arraysEqual(expected, actual);
        }

        return expected.equals(actual);
    }

    @SuppressWarnings("unchecked")
    private boolean arraysEqual(Object arr1, Object arr2) {
        if (arr1 instanceof int[] && arr2 instanceof int[]) {
            int[] a1 = (int[]) arr1;
            int[] a2 = (int[]) arr2;
            if (a1.length != a2.length) return false;
            for (int i = 0; i < a1.length; i++) {
                if (a1[i] != a2[i]) return false;
            }
            return true;
        } else if (arr1 instanceof Object[] && arr2 instanceof Object[]) {
            Object[] o1 = (Object[]) arr1;
            Object[] o2 = (Object[]) arr2;
            if (o1.length != o2.length) return false;
            for (int i = 0; i < o1.length; i++) {
                if (o1[i] == null && o2[i] == null) continue;
                if (o1[i] == null || o2[i] == null) return false;
                if (!o1[i].equals(o2[i])) return false;
            }
            return true;
        }
        return arr1.equals(arr2);
    }

    protected String formatOutput(Output output) {
        if (output == null) return "null";
        if (output.getClass().isArray()) {
            return arrayToString(output);
        }
        return output.toString();
    }

    private String arrayToString(Object arr) {
        if (arr instanceof int[]) {
            int[] a = (int[]) arr;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < a.length; i++) {
                sb.append(a[i]);
                if (i < a.length - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        } else if (arr instanceof Object[]) {
            Object[] o = (Object[]) arr;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < o.length; i++) {
                if (o[i] == null) {
                    sb.append("null");
                } else {
                    sb.append(o[i].toString());
                }
                if (i < o.length - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }
        return arr.toString();
    }

    /**
     * 深拷贝输入对象（主要用于保存链表等可变对象的原始状态）
     */
    @SuppressWarnings("unchecked")
   private Input cloneInput(Input input) {
      if (input == null) return null;

        String className = input.getClass().getName();

        // 处理 TestInput 类型
      if (className.contains("TestInput")) {
            try {
                // 获取所有字段
               java.lang.reflect.Field[] fields = input.getClass().getDeclaredFields();
             Object[] clonedValues = new Object[fields.length];

                // 克隆每个字段
              for (int i = 0; i < fields.length; i++) {
                  fields[i].setAccessible(true);
                Object value = fields[i].get(input);

                  // 如果是 ListNode 类型，进行深拷贝
               if (value != null && isListNode(value)) {
                     clonedValues[i] = cloneListNode(value);
                 } else {
                     // 其他类型直接复制引用
                    clonedValues[i] = value;
                 }
              }

                // 找到匹配的构造函数
               java.lang.reflect.Constructor<?>[] constructors = input.getClass().getDeclaredConstructors();
               java.lang.reflect.Constructor<?> targetConstructor = null;
               for (java.lang.reflect.Constructor<?> c : constructors) {
                if (c.getParameterCount() == fields.length) {
                       targetConstructor = c;
                       break;
                   }
               }

          if (targetConstructor == null) {
                 throw new RuntimeException("找不到合适的 TestInput 构造函数");
             }

             targetConstructor.setAccessible(true);
          return (Input) targetConstructor.newInstance(clonedValues);

            } catch (Exception e) {
                // 如果克隆失败，打印错误信息并返回原对象
             System.err.println("cloneInput 失败：" + e.getMessage());
              e.printStackTrace();
              return input;
            }
        }

        // 其他类型直接返回原对象
      return input;
    }

    /**
     * 判断是否是 ListNode 类型
     */
  private boolean isListNode(Object obj) {
        String className = obj.getClass().getSimpleName();
      return "ListNode".equals(className);
    }

    /**
     * 深拷贝 ListNode 链表
     */
    @SuppressWarnings("unchecked")
    private Object cloneListNode(Object node) throws Exception {
       if (node == null) return null;

        // 获取节点类信息
        Class<?> nodeClass = node.getClass();
        java.lang.reflect.Field valField = nodeClass.getDeclaredField("val");
        valField.setAccessible(true);
        java.lang.reflect.Field nextField = nodeClass.getDeclaredField("next");
        nextField.setAccessible(true);

        // 创建虚拟头节点
       java.lang.reflect.Constructor<?> intConstructor = nodeClass.getDeclaredConstructor(int.class);
       intConstructor.setAccessible(true);
     Object dummy = intConstructor.newInstance(0);
     Object curr = dummy;

        // 遍历原链表并复制每个节点
     Object src = node;
       while (src != null) {
           int val = (int) valField.get(src);
         Object newNode = intConstructor.newInstance(val);
           nextField.set(curr, newNode);
           curr = newNode;
           src = nextField.get(src);
       }

      return nextField.get(dummy);
    }
}
