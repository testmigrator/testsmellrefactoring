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
public class TestCodeContext {
    private String splitFilepath;
    private ParserRuleContext methodCtx;
    private String originalCode;
    private String splitFileName;
    private String focalMethod;
    private int testCodeStartLine;
    private int testCodeEndLine;
}