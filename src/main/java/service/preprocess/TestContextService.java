package service.preprocess;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import entity.TaskParam;
import entity.TestContext;
import org.apache.commons.lang3.StringUtils;
import service.TaskParamReader;
import util.FileUtil;
import util.GetFoldFileNames;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestContextService {
    public static List<String> fetchClassFilepathList() {
        TaskParam taskParam = TaskParamReader.getTaskParam();
        String projectSrcFilepath = taskParam.getProjectFilepath() + File.separator + "src" + File.separator + "main";
        return GetFoldFileNames.readfileWithType(projectSrcFilepath, "java");
    }

    public static List<String> fetchTestFilepathList() {
        TaskParam taskParam = TaskParamReader.getTaskParam();
        String projectFilepath = taskParam.getProjectFilepath();
        String testJavaDirPath = projectFilepath + File.separator + "src" + File.separator + "test" + File.separator + "java";

        File testJavaDir = new File(testJavaDirPath);

        if (testJavaDir.exists() && testJavaDir.isDirectory()) {
            List<File> testFiles = FileUtil.listFiles(testJavaDir, ".java");
            return testFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList());
        }

        return Lists.newArrayList();
    }

    public static List<TestContext> buildTestContext(String projectName) {
        List<String> classFilepathList = fetchClassFilepathList();
        List<String> testFilepathList = fetchTestFilepathList();
        List<TestContext> testContextList = Lists.newArrayList();
        Map<String, String> classNameMap = getClassNameMap(classFilepathList);

        for (String testFilepath : testFilepathList) {
            String testName = FileUtil.getNameByFilepath(testFilepath);
            if (!testName.startsWith("Test") && !testName.endsWith("Test")) {
                continue;
            }

            TestContext testContext = new TestContext();
            testContext.setProjectName(projectName);
            testContext.setTestFilepath(testFilepath);
            testContext.setClassFilepath(StringUtils.EMPTY);

            classNameMap.forEach((k, v) -> {
                if (StringUtils.equals(testName, v)) {
                    testContext.setClassFilepath(k);
                    return;
                }
                if (testName.startsWith("Test")) {
                    v = "Test" + v;
                }
                if (testName.endsWith("Test")) {
                    v = v + "Test";
                }
                if (StringUtils.equals(testName, v)) {
                    testContext.setClassFilepath(k);
                }
            });
            testContextList.add(testContext);
        }
        return testContextList;
    }

    private static Map<String, String> getClassNameMap(List<String> classFilepathList) {
        Map<String, String> map = Maps.newHashMap();
        for (String filepath : classFilepathList) {
            map.put(filepath, FileUtil.getNameByFilepath(filepath));
        }

        return map;
    }



}
