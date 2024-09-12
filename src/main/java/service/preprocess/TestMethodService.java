package service.preprocess;

import antlr.Java8Lexer;
import antlr.Java8Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

public class TestMethodService {

    public boolean isTestMethod(Java8Parser.MethodDeclarationContext ctx){
        String methodName = fetchMethodName(ctx);
        if (methodName.toLowerCase().startsWith("test") || methodName.toLowerCase().endsWith("test")){
            return true;
        }
        boolean annotationTest = isAnnotationTest(ctx);
        return annotationTest;
    }

    /**
     * @param ctx
     * @return
     */
    private boolean isAnnotationTest(Java8Parser.MethodDeclarationContext ctx) {
        boolean isAnnotationTest = false;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof RuleContext) {
                RuleContext childRuleContext = (RuleContext) child;
                if (childRuleContext.getRuleIndex() == Java8Parser.RULE_methodModifier) {
                    ParseTree methodModifierChildCtx = childRuleContext.getChild(0);
                    if (StringUtils.equals(methodModifierChildCtx.getText(), "@Test")) {
                        isAnnotationTest = true;
                        break;
                    }
                }
            }
        }
        return isAnnotationTest;
    }

    public String fetchMethodName(ParserRuleContext ctx){
        String methodName = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext node = (RuleContext) child;

            if (node.getRuleIndex() == Java8Parser.RULE_methodModifier) {
                boolean existAnnotation = isExistAnnotation(node);
                if (existAnnotation) {
                    continue;
                }

                if (!StringUtils.equalsAnyIgnoreCase(node.getText(), "public")) {
                    methodName = "";
                }
            }

            if (node.getRuleIndex() != Java8Parser.RULE_methodHeader) {
                continue;
            }

            for (int j = 0; j < node.getChildCount(); j++) {
                ParseTree methodHeaderChild = node.getChild(j);
                boolean isMethodHeaderChildRuleContext = methodHeaderChild instanceof RuleContext;
                if (!isMethodHeaderChildRuleContext) {
                    continue;
                }
                RuleContext methodHeaderChildNode = (RuleContext) methodHeaderChild;

                if (methodHeaderChildNode.getRuleIndex() == Java8Parser.RULE_methodDeclarator) {
                    for (int k = 0; k < methodHeaderChildNode.getChildCount(); k++) {
                        ParseTree child1 = methodHeaderChildNode.getChild(k);
                        if (child1 instanceof TerminalNode) {
                            TerminalNode terminalNode = (TerminalNode) child1;
                            if (terminalNode.getSymbol().getType() == Java8Lexer.Identifier) {
                                methodName = terminalNode.getText();
                            }
                        }

                    }
                }
            }
        }
        return methodName;
    }

    public static boolean isExistAnnotation(RuleContext node) {
        boolean existAnnotation = false;
        for (int j = 0; j < node.getChildCount(); j++) {
            boolean isChildRuleContext = node.getChild(j) instanceof RuleContext;
            if (!isChildRuleContext) {
                continue;
            }
            RuleContext methodModifierNode = (RuleContext) node.getChild(j);
            if (methodModifierNode.getRuleIndex() == Java8Parser.RULE_annotation) {
                existAnnotation = true;
            }
        }
        return existAnnotation;
    }
}
