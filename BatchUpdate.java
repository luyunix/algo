import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class BatchUpdate {
   public static void main(String[] args) throws IOException {
        String basePath = "/Users/lyn/work/algo/src/main/java/top/luyuni/algo";
        List<String> excludeDirs = Arrays.asList("oj");
        
        Files.walk(Paths.get(basePath))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .filter(p -> !excludeDirs.contains(p.getParent().getFileName().toString()))
            .forEach(BatchUpdate::updateFile);
        
        System.out.println("完成！");
    }
    
    private static void updateFile(Path path) {
        try {
            String content = new String(Files.readAllBytes(path));
            
            // 跳过已更新的
            if (content.contains("import top.luyuni.algo.oj.core.JudgeEngine")) {
                System.out.println("✓ 已更新：" + path.getFileName());
                return;
            }
            
            // 添加导入
            if (!content.contains("import java.util")) {
                content = content.replaceFirst(
                    "package top\\.luyuni\\.algo",
                    "package top.luyuni.algo\n\nimport top.luyuni.algo.oj.core.JudgeEngine;\nimport top.luyuni.algo.oj.core.JudgeResult;\nimport top.luyuni.algo.oj.core.JudgeReporter;\n\nimport java.util.*;"
                );
            } else {
                content = content.replaceFirst(
                    "import java\\.util\\.",
                    "import top.luyuni.algo.oj.core.JudgeEngine;\nimport top.luyuni.algo.oj.core.JudgeResult;\nimport top.luyuni.algo.oj.core.JudgeReporter;\n\nimport java.util.*"
                );
            }
            
            Files.write(path, content.getBytes());
            System.out.println("✓ 更新：" + path.getFileName());
        } catch (Exception e) {
            System.err.println("✗ 失败：" + path.getFileName() + " - " + e.getMessage());
        }
    }
}
