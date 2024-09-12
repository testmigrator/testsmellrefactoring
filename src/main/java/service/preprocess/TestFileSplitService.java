package service.preprocess;

import antlr.Java8Parser;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import entity.TestCodeContext;
import entity.TestContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.github.javaparser.utils.Log;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.commons.lang3.StringUtils;
import util.FileUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TestFileSplitService {
    public List<TestContext> splitTest(List<TestContext> testContexts) {
        List<TestContext> newContextList = Lists.newArrayList();
        for (TestContext testContext : testContexts) {
            String projectName = testContext.getProjectName();
            String classFilepath = testContext.getClassFilepath();
            String testFilepath = testContext.getTestFilepath();

            TreeParseService treeParseService = new TreeParseService();
            Java8Parser.CompilationUnitContext parseTree = treeParseService.getParseTree(testFilepath);
            List<ParserRuleContext> testMethodCtxList = treeParseService.getTestMethodCtxList(parseTree);

            TestFileSplitService testFileSplitService = new TestFileSplitService();
            List<TestCodeContext> split = testFileSplitService.split(testFilepath, parseTree, testMethodCtxList);

            List<TestContext> splitTestContextList = split.stream().map(x -> TestContext.builder()
                    .projectName(projectName)
                    .classFilepath(classFilepath)
                    .testFilepath(x.getSplitFilepath())
                    .testOriginalCode(x.getOriginalCode())
                    .methodCtx(x.getMethodCtx())
                    .testCodeStartLine(x.getTestCodeStartLine())
                    .testCodeEndLine(x.getTestCodeEndLine())
                    .splitFileName(x.getSplitFileName())
                    .build()).toList();
            newContextList.addAll(splitTestContextList);
        }

        return newContextList;
    }

    public List<TestCodeContext> split(String testFilepath, Java8Parser.CompilationUnitContext parseTree,
                                       List<ParserRuleContext> testMethodCtxList) {
        List<TestCodeContext> testCodeContextList = Lists.newArrayList();

        int index = 0;
        for (ParserRuleContext methodCtx : testMethodCtxList) {
            String testOriginalCode = getOriginalCode(methodCtx.getStart(), methodCtx.getStart().getStartIndex(), methodCtx.getStop().getStopIndex());

            index++;
            TestSplitVisitor testSplitVisitor = new TestSplitVisitor(methodCtx.getStart(),
                    methodCtx.getStart().getStartIndex(),
                    methodCtx.getStop().getStopIndex());
            testSplitVisitor.visit(parseTree);

            testSplitVisitor.getRemoveIndexList()
                    .sort(Comparator.comparingInt(TestSplitVisitor.RemoveIndex::getStartIndex));

            // total: 1-20
            // keep:  4-6
            // remove: 8-10, 12-15, 17-18
            String newFileContent = StringUtils.EMPTY;
            try {
                newFileContent = generateNewFileContent(parseTree, testSplitVisitor);
            } catch (Exception e) {
                Log.info("generate FileContent error: " + e.getMessage());
            }

            String newFilepath = testFilepath.replace(".java", "") + "__" + index + ".java";
            if (StringUtils.isBlank(newFileContent)) {
                Log.info("newFilepath content is blank: " + newFilepath);
                continue;
            }
            newFilepath = newFilepath.replace("/test/", "/test_split/");

            FileUtil.writeToNewFile(newFileContent, newFilepath);

            TestCodeContext testCodeContext = TestCodeContext.builder()
                    .originalCode(testOriginalCode)
                    .methodCtx(methodCtx)
                    .splitFilepath(newFilepath)
                    .splitFileName(FileUtil.getNameByFilepath(newFilepath))
                    .testCodeStartLine(methodCtx.getStart().getLine())
                    .testCodeEndLine(methodCtx.getStop().getLine())
                    .build();
            testCodeContextList.add(testCodeContext);

        }
        return testCodeContextList;
    }

    private static String generateNewFileContent(Java8Parser.CompilationUnitContext parseTree, TestSplitVisitor testSplitVisitor) {
        StringBuilder splitCodeStr = new StringBuilder();
        if (testSplitVisitor.getRemoveIndexList().size() == 0) {
//                splitCodeStr.append(
//                        getOriginalCode(parseTree.getStart(), parseTree.getStart().getStartIndex(), testSplitVisitor.getRemoveIndexList().get(0).getStartIndex() - 1)
//                );
            splitCodeStr.append(
                    getOriginalCode(parseTree.getStart(), parseTree.getStart().getStartIndex(), parseTree.getStop().getStopIndex())
            );
        } else {
            for (int i = 0; i < testSplitVisitor.getRemoveIndexList().size(); i++) {
                TestSplitVisitor.RemoveIndex removeIndex = testSplitVisitor.getRemoveIndexList().get(i);
                if (i + 1 == testSplitVisitor.getRemoveIndexList().size()) {
                    splitCodeStr.append(
                            getOriginalCode(parseTree.getStart(), removeIndex.getEndIndex() + 1, parseTree.getStop().getStopIndex())
                    );
                } else {
                    TestSplitVisitor.RemoveIndex nextRemoveIndex = testSplitVisitor.getRemoveIndexList().get(i + 1);
                    if (i == 0) {
                        splitCodeStr.append(
                                getOriginalCode(parseTree.getStart(), parseTree.getStart().getStartIndex(), removeIndex.getStartIndex() - 1)
                        );
                        splitCodeStr.append(
                                getOriginalCode(parseTree.getStart(), removeIndex.getEndIndex() + 1, nextRemoveIndex.getStartIndex() - 1)
                        );
                    } else {
                        splitCodeStr.append(
                                getOriginalCode(parseTree.getStart(), removeIndex.getEndIndex() + 1, nextRemoveIndex.getStartIndex() - 1)
                        );
                    }
                }
            }
        }
        return splitCodeStr.toString();
    }

    private static String getOriginalCode(Token startToken, int startIndex, int stopIndex) {
        // Get the input stream
        CharStream inputStream = startToken.getInputStream();

        // Extract the original source code
        return inputStream.getText(new Interval(startIndex, stopIndex));
    }
}
