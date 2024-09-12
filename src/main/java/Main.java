import com.github.javaparser.utils.Log;
import entity.TaskParam;
import entity.TestLLMContext;
import entity.TestSmellDetectionOutput;
import service.TaskParamReader;
import service.TestContextCollection;
import service.detection.TestSmellDetection;
import service.preprocess.FocalMethodExtractor;
import service.refactor.TestSmellRefactorRuleSetting;
import util.FileUtil;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskParam taskParam = TaskParamReader.getTaskParam();
        String projectName = FileUtil.getNameByFilepath(taskParam.getProjectFilepath());

        long startTime1 = System.currentTimeMillis();
        TestSmellDetection testSmellDetection = new TestSmellDetection();
        List<TestSmellDetectionOutput> testSmellDetectionOutputs = testSmellDetection.smellDetect(projectName);
        long endTime1 = System.currentTimeMillis();
        Log.info("TestSmellDetection，time consuming：" + (endTime1 - startTime1));

        TestSmellRefactorRuleSetting refactorRuleSetting = new TestSmellRefactorRuleSetting();
        List<TestSmellDetectionOutput> testSmellList = refactorRuleSetting.setRefactoringRule(testSmellDetectionOutputs);

        long startTime3 = System.currentTimeMillis();
        FocalMethodExtractor focalMethodExtractor = new FocalMethodExtractor();
        focalMethodExtractor.fillMethodSignature(testSmellList);
        long endTime3 = System.currentTimeMillis();
        Log.info("FocalMethodExtractor，time consuming：" + (endTime3 - startTime3));

        long startTime4 = System.currentTimeMillis();
        TestContextCollection testContextCollection = new TestContextCollection();
        testContextCollection.collectTestForStats(projectName, testSmellList);
        // write to json file
        List<TestLLMContext> testLLMContexts = testContextCollection.buildTestLLMContextList(testSmellList);
        testContextCollection.collectTestToJson(projectName, testLLMContexts);
        long endTime4 = System.currentTimeMillis();
        Log.info("TestContextCollection，time consuming：" + (endTime4 - startTime4));


        Log.info("Main，time consuming：" + (endTime4 - startTime1));
    }

}