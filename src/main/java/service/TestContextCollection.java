package service;

import com.github.javaparser.utils.Log;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import entity.TestLLMContext;
import entity.TestRefactorRule;
import entity.TestSmellDetectionOutput;
import org.apache.commons.lang3.StringUtils;
import util.FileUtil;
import util.JsonUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestContextCollection {
    public List<TestLLMContext> buildTestLLMContextList(List<TestSmellDetectionOutput> llmRefactoringTestList) {
        llmRefactoringTestList = llmRefactoringTestList.stream().
                filter(x -> x.getTestRefactorRule() == TestRefactorRule.LLM).toList();

        return llmRefactoringTestList.stream()
                .filter(x -> StringUtils.isNotBlank(x.getTestOriginalCode()))
                .map(output -> {
                    String classFilepath = output.getClassFilepath();
                    List<String> smells = output.getTestSmellTypeList().stream()
                            .map(Enum::name).toList();
                    List<String> codeLines = null;
                    try {
                        codeLines = fetchCodeLines(output.getTestFilepath(), output.getTestOriginalCode());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    return TestLLMContext.builder()
                            .testFilepath(output.getTestFilepath())
                            .testFilename(FileUtil.getFileName(output.getTestFilepath()))
                            .testClassPathName(classFilepath)
                            .testCode(output.getTestOriginalCode())
                            .testFilePrefix(codeLines.get(0))
                            .testFileSuffix(codeLines.get(1))
                            .testCodeStartLine(output.getTestCodeStartLine())
                            .testCodeEndLine(output.getTestCodeEndLine())
                            .splitFilename(output.getSplitFilename())
                            .focalMethodSignature(output.getFocalMethod())
                            .codeComment("")
                            .smellList(smells)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    public void collectTestToJson(String projectName, List<TestLLMContext> testLLMContexts) {
        Map<String, List<TestLLMContext>> map = Maps.newHashMap();
        Set<String> keys = Sets.newHashSet();
        for (TestLLMContext testLLMContext : testLLMContexts) {
            keys.add(FileUtil.getNameByFilepath(testLLMContext.getTestFilepath()).split("__")[0]);
        }
        for (TestLLMContext testLLMContext : testLLMContexts) {
            String key = FileUtil.getNameByFilepath(testLLMContext.getTestFilepath()).split("__")[0];
            if (keys.contains(key)) {
                map.computeIfAbsent(key, k -> Lists.newArrayList());
                map.get(key).add(testLLMContext);
            }
        }
        map.forEach((key, value) -> {
            String focalClassName = FileUtil.getNameByFilepath(key);
            collectTestLLMContextToJson(projectName, focalClassName, value);
        });
    }
    public void collectTestForStats(String projectName, List<TestSmellDetectionOutput> testSmellList) {
        // write test need to be refactored
        List<TestSmellDetectionOutput> removeTests = testSmellList.stream().
                filter(x -> x.getTestRefactorRule() == TestRefactorRule.REMOVE).toList();
        String removeRefactoredTests = parseRefactoredTestToSimplyString(removeTests);
        String removeRefactoredTestsFilepath = System.getProperty("user.dir") + "/temp/print/" + projectName + "_remove.txt";;
        FileUtil.writeDataToFile(removeRefactoredTestsFilepath, removeRefactoredTests);

        List<TestSmellDetectionOutput> llmTests = testSmellList.stream().
                filter(x -> x.getTestRefactorRule() == TestRefactorRule.LLM).toList();
        String llmRefactoredTests = parseRefactoredTestToSimplyString(llmTests);
        String llmRefactoredTestsFilepath = System.getProperty("user.dir") + "/temp/print/" + projectName + "_llm.txt";;
        FileUtil.writeDataToFile(llmRefactoredTestsFilepath, llmRefactoredTests);
    }

    public static void collectTestLLMContextToJson(String projectName, String focalClassName, List<TestLLMContext> testLLMContextList) {
        String jsonString = JsonUtil.objectToJson(testLLMContextList);
        if (StringUtils.isBlank(jsonString)) {
            return;
        }
        String jsonfilePath = System.getProperty("user.dir") + "/temp/json/" +
                projectName + "/" + focalClassName + ".json";

        FileUtil.writeDataToFile(jsonfilePath, jsonString);
    }

    private List<String> fetchCodeLines(String testFilepath, String testCodeStr) {
        List<String> testFileContent = Lists.newArrayList();
        File file = new File(testFilepath);
        if (file.exists() && file.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    testFileContent.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.info("The file does not exist or is not a file: " + testFilepath);
        }

        List<String> testCode = Stream.of(testCodeStr.split("\n")).toList();

        List<Integer> codeLines = findMatchingTestCodeLines(testFileContent, testCode)
                .stream().sorted().toList();

        int prefixEndLine = codeLines.get(0) - 1;
        int suffixStartLine = codeLines.get(codeLines.size() - 1) + 1;

        List<String> prefixLines = Lists.newArrayList();
        List<String> suffixLines = Lists.newArrayList();
        for (int i = 0; i < testFileContent.size(); i++) {
            if (i < prefixEndLine) {
                prefixLines.add(testFileContent.get(i));
            }
            if (i >= suffixStartLine) {
                suffixLines.add(testFileContent.get(i));
            }
        }

        return Lists.newArrayList(String.join(System.lineSeparator(), prefixLines),
                String.join(System.lineSeparator(), suffixLines));
    }

    private List<Integer> findMatchingTestCodeLines(List<String> testFileContent, List<String> testCode) {
        List<Integer> matchIndexList = Lists.newArrayList();
        for (int i = 0; i < testFileContent.size() - testCode.size(); i++) {
            boolean isMatch = true;
            for (int j = 0; j < testCode.size(); j++) {
                if (!testFileContent.get(i).strip().equals(testCode.get(j).strip())) {
                    isMatch = false;
                    break;
                } else {
                    i++;
                }
            }
            if (isMatch) {
                for (int j = 0; j < testCode.size(); j++) {
                    matchIndexList.add(i - j);
                }
            }
        }

        return matchIndexList;
    }

    private String parseRefactoredTestToSimplyString(List<TestSmellDetectionOutput> tests) {
        Set<String> keys = Sets.newHashSet();
        for (TestSmellDetectionOutput test : tests) {
            keys.add(test.getTestFilepath().split("__")[0]);
        }
        Map<String, List<TestSmellDetectionOutput>> map = Maps.newHashMap();
        for (TestSmellDetectionOutput test : tests) {
            String key = test.getTestFilepath().split("__")[0];
            if (keys.contains(key)) {
                map.computeIfAbsent(key, k -> Lists.newArrayList());
                map.get(key).add(test);
            }
        }
        StringBuilder output = new StringBuilder();
        for (Map.Entry<String, List<TestSmellDetectionOutput>> entry : map.entrySet()) {
            String k = entry.getKey();
            List<TestSmellDetectionOutput> v = entry.getValue();
            output.append("Test file path :").append(k).append("\n");
            v.stream().filter(t -> StringUtils.isNotBlank(t.getTestOriginalCode())).forEach(x -> {
                String[] lines = x.getTestOriginalCode().split("\n");
                StringBuilder sb = new StringBuilder();
                sb.append("【");
                x.getTestSmellTypeList().forEach(s -> sb.append(s.name()).append(" "));
                sb.append("】");
                for (int i = 0; i < 2; i++) {
                    if (i < lines.length) {
                        output.append(lines[i]);
                    }
                }
                output.append("\n");
                output.append(sb);
                output.append("\n");
            });
            output.append("==================");
            output.append("\n\n");
        }

        return output.toString();
    }




}
