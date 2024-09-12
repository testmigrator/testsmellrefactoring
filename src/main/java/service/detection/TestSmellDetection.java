package service.detection;

import com.google.common.collect.Lists;
import entity.TestContext;
import entity.TestSmellDetectionOutput;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import service.preprocess.TestContextService;
import service.preprocess.TestFileSplitService;
import service.preprocess.TestSmellDetectionService;
import util.ExecuteJar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TestSmellDetection {
    public List<TestSmellDetectionOutput> smellDetect(String projectName) {
        // collect
        List<TestContext> testContexts = TestContextService.buildTestContext(projectName);

        // split
        TestFileSplitService testFileSplitService = new TestFileSplitService();
        List<TestContext> splitTestContexts = testFileSplitService.splitTest(testContexts);

        String testCSVFilepath = writeToTestCSV(splitTestContexts);
        System.out.println("testCSVFilepath:"+testCSVFilepath);

        String jarFilepath = System.getProperty("user.dir") + "/detection/v3/TestSmellDetector.jar";
        List<String> detectArgs = Lists.newArrayList();
//        detectArgs.add('"'+testCSVFilepath+'"');
        detectArgs.add(testCSVFilepath);

        ExecuteJar.runJar(jarFilepath, detectArgs);

        return buildSmellDetection(splitTestContexts);
    }
    private List<TestSmellDetectionOutput> buildSmellDetection(List<TestContext> splitTestContexts) {
        String lastSmellDetectResult = getLastSmellDetectResult();
        TestSmellDetectionService testSmellDetectionService = new TestSmellDetectionService();
        List<TestSmellDetectionOutput> testSmellDetectionOutputs = testSmellDetectionService.buildTestSmellDetectionOutputList(lastSmellDetectResult);

        testSmellDetectionOutputs.forEach(x -> {
            TestContext testContext = splitTestContexts.stream()
                    .filter(context -> StringUtils.equals(x.getTestFilepath(), context.getTestFilepath()))
//                    .map(Preprocess.TestContext::getClassFilepath)
                    .findFirst().orElse(null);
            if (testContext == null) {
                return;
            }
            if (StringUtils.isNotBlank(testContext.getClassFilepath())) {
                x.setClassFilepath(testContext.getClassFilepath());
            }
            x.setTestOriginalCode(testContext.getTestOriginalCode());
            x.setTestCodeStartLine(testContext.getTestCodeStartLine());
            x.setTestCodeEndLine(testContext.getTestCodeEndLine());
            x.setMethodCtx(testContext.getMethodCtx());
            x.setSplitFilename(testContext.getSplitFileName());
        });

        return testSmellDetectionOutputs;
    }

    public String getLastSmellDetectResult() {
        String fileNamePrefix = "Output_TestSmellDetection_";
        String fileExtension = ".csv";

        File directory = new File(System.getProperty("user.dir"));
        File[] files = directory.listFiles(file -> {
            String name = file.getName();
            return name.startsWith(fileNamePrefix) && name.endsWith(fileExtension);
        });

        if (files != null && files.length > 0) {
            List<File> fileList = new ArrayList<>(Arrays.asList(files));
            fileList.sort(Comparator.comparing(File::getName));

            File lastFile = fileList.get(fileList.size() - 1);
            return lastFile.getAbsolutePath();
        }

        return null;
    }


    private static String writeToTestCSV(List<TestContext> testContextList) {
        if (CollectionUtils.isEmpty(testContextList)) {
            return null;
        }
        String csvFilepath = System.getProperty("user.dir") + "/temp/" + testContextList.get(0).getProjectName() +
                UUID.randomUUID().toString().substring(0, 4) + ".csv";
        File file = new File(csvFilepath);
        try {
            FileWriter writer = new FileWriter(file);
            for (TestContext testContext : testContextList) {
                String stringBuilder = testContext.getProjectName() +
                        "," +
                        testContext.getTestFilepath() +
                        "," +
                        testContext.getClassFilepath() +
                        "\n";
                writer.write(stringBuilder);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return csvFilepath;
    }


}
