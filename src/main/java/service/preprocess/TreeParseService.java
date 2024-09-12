package service.preprocess;

import antlr.Java8Lexer;
import antlr.Java8Parser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.IOException;
import java.util.List;

public class TreeParseService {

    public Java8Parser.CompilationUnitContext getParseTree(String filepath) {
        CharStream inputStream = null;
        try {
            inputStream = CharStreams.fromFileName(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inputStream == null) {
            return null;
        }

        CommonTokenStream commonTokenStream = new CommonTokenStream(new Java8Lexer(inputStream));
        Java8Parser parser = new Java8Parser(commonTokenStream);

        return parser.compilationUnit();
    }

    public List<ParserRuleContext> getTestMethodCtxList(Java8Parser.CompilationUnitContext parseTree) {
        TestCodeVisitor testCodeVisitor = new TestCodeVisitor();
        testCodeVisitor.visit(parseTree);
        //  collect all test methods: RULE_methodDeclaration
        List<ParserRuleContext> methodDeclarationCtxList = testCodeVisitor.getMethodDeclarationCtxList();
        TestMethodService testMethodService = new TestMethodService();

        return methodDeclarationCtxList.stream()
                .filter(x -> testMethodService.isTestMethod((Java8Parser.MethodDeclarationContext) x)).toList();
    }
}
