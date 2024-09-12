package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestContext {
    private String projectName;
    private String testFilepath;
    private String classFilepath;
    private String focalMethod;
    private String testOriginalCode;
    private ParserRuleContext methodCtx;
    private String splitFileName;
    private int testCodeStartLine;
    private int testCodeEndLine;
}