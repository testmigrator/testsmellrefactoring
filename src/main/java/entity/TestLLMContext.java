package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestLLMContext {
    private String testCode;
    private String testClassPathName;
    private String focalMethodSignature;
    private String codeComment;
    private String testFilepath;
    private String testFilename;
    private String testFilePrefix;
    private String testFileSuffix;
    private int testCodeStartLine;
    private int testCodeEndLine;
    private String splitFilename;
    private List<String> smellList;
}
