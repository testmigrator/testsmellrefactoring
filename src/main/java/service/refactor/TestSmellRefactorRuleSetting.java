package service.refactor;

import entity.TestRefactorRule;
import entity.TestSmellDetectionOutput;
import entity.TestSmellType;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class TestSmellRefactorRuleSetting {

// Verbose_Test， Sleepy_Test
    public List<TestSmellDetectionOutput> setRefactoringRule(List<TestSmellDetectionOutput> testSmellList) {
        testSmellList = testSmellList.stream()
                .filter(x -> CollectionUtils.isNotEmpty(x.getTestSmellTypeList()))
                .toList();
        // RULE setting
        for (TestSmellDetectionOutput testSmell : testSmellList) {
            // set default refactoring rule
            testSmell.setTestRefactorRule(TestRefactorRule.LLM);

            //REMOVE
            if (testSmell.getTestSmellTypeList().contains(TestSmellType.Default_Test)
                    || testSmell.getTestSmellTypeList().contains(TestSmellType.EmptyTest)
                    || testSmell.getTestSmellTypeList().contains(TestSmellType.IgnoredTest)
                    || testSmell.getTestSmellTypeList().contains(TestSmellType.Unknown_Test)) {
                testSmell.setTestRefactorRule(TestRefactorRule.REMOVE);
            }

            if (testSmell.getTestSmellTypeList().contains(TestSmellType.Lazy_Test)
                    || testSmell.getTestSmellTypeList().contains(TestSmellType.General_Fixture)
                    || testSmell.getTestSmellTypeList().contains(TestSmellType.Dependent_Test)
                    || testSmell.getTestSmellTypeList().contains(TestSmellType.Constructor_Initialization)) {
                testSmell.setTestRefactorRule(TestRefactorRule.FILE);
            }
        }

        return testSmellList;
    }


}
