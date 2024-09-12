package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSmellDetectionOutput {
//    App,TestClass,TestFilePath,ProductionFilePath,RelativeTestFilePath,RelativeProductionFilePath,NumberOfMethods,
//    Assertion Roulette,Conditional Test Logic,Constructor Initialization,Default Test,EmptyTest,Exception Catching Throwing,General Fixture,
//    Mystery Guest,Print Statement,Redundant Assertion,Sensitive Equality,Verbose Test,Sleepy Test,Eager Test,
//    Lazy Test,Duplicate Assert,Unknown Test,IgnoredTest,Resource Optimism,Magic Number Test,Dependent Test

    private String appName;
    private String testFilepath;
    private String testClassName;
    private String classFilepath;
    private String focalMethod;
    private String testOriginalCode;
    private ParserRuleContext methodCtx;
    private String splitFilename;
    private int testCodeStartLine;
    private int testCodeEndLine;
    private List<TestSmellType> testSmellTypeList;

    private TestRefactorRule testRefactorRule;
}
