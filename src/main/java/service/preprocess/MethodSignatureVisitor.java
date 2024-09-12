package service.preprocess;

import antlr.Java8BaseVisitor;
import antlr.Java8Lexer;
import antlr.Java8Parser;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MethodSignatureVisitor extends Java8BaseVisitor<RuleNode> {
    Map<String, List<String>> methodSignatureMap = Maps.newHashMap();

    @Override
    public RuleNode visitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        String methodName = fetchMethodName(ctx);
        String methodParameter = fetchMethodParameter(ctx);
        String methodReturn = fetchMethodReturn(ctx);
        String signature = methodReturn + " " + methodName + "(" + methodParameter + ")";
        if (methodSignatureMap.containsKey(methodName)) {
            if (!methodSignatureMap.get(methodName).contains(signature)) {
                methodSignatureMap.get(methodName).add(signature);
            }
        } else {
            methodSignatureMap.put(methodName, Lists.newArrayList(signature));
        }

        return visitChildren(ctx);
    }

    public String fetchMethodName(ParserRuleContext ctx) {
        String methodName = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext node = (RuleContext) child;

            // 过滤掉非public的方法
            if (node.getRuleIndex() == Java8Parser.RULE_methodModifier) {
                // 存在注解的方法
                boolean existAnnotation = TestMethodService.isExistAnnotation(node);
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

    public String fetchMethodReturn(ParserRuleContext ctx) {
        String methodReturn = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext node = (RuleContext) child;

            // 过滤掉非public的方法
            if (node.getRuleIndex() == Java8Parser.RULE_methodModifier) {
                // 存在注解的方法
                boolean existAnnotation = TestMethodService.isExistAnnotation(node);
                if (existAnnotation) {
                    continue;
                }

                if (!StringUtils.equalsAnyIgnoreCase(node.getText(), "public")) {
                    return "";
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
                if (methodHeaderChildNode.getRuleIndex() == Java8Parser.RULE_result) {
                    methodReturn = methodHeaderChildNode.getText();
                }
            }
        }
        return methodReturn;
    }

    public String fetchMethodParameter(ParserRuleContext ctx) {
        String methodParameter = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            boolean isRuleContext = child instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }
            RuleContext node = (RuleContext) child;

            if (node.getRuleIndex() == Java8Parser.RULE_methodModifier) {
                boolean existAnnotation = TestMethodService.isExistAnnotation(node);
                if (existAnnotation) {
                    continue;
                }

                if (!StringUtils.equalsAnyIgnoreCase(node.getText(), "public")) {
                    return "";
                }
            }

            if (node.getRuleIndex() != Java8Parser.RULE_methodHeader) {
                continue;
            }

            for (int j = 0; j < node.getChildCount(); j++) {
                // 获取method的Header
                ParseTree methodHeaderChild = node.getChild(j);
                boolean isMethodHeaderChildRuleContext = methodHeaderChild instanceof RuleContext;
                if (!isMethodHeaderChildRuleContext) {
                    continue;
                }
                RuleContext methodHeaderChildNode = (RuleContext) methodHeaderChild;

                if (methodHeaderChildNode.getRuleIndex() == Java8Parser.RULE_methodDeclarator) {
                    for (int k = 0; k < methodHeaderChildNode.getChildCount(); k++) {
                        ParseTree child1 = methodHeaderChildNode.getChild(k);

                        // 填充methodParameter
                        if (child1 instanceof RuleNode) {
                            RuleNode ruleNode = (RuleNode) child1;
                            if (ruleNode.getRuleContext().getRuleIndex() == Java8Parser.RULE_formalParameterList) {
                                StringBuilder builder = new StringBuilder();
                                fetchCtxSourceText(ruleNode, builder);
                                methodParameter = builder.toString();
                            }
                        }
                    }
                }
            }
        }
        return methodParameter;
    }

    private void fetchCtxSourceText(ParseTree node, StringBuilder builder) {
        if (node.getChildCount() == 0) {
            builder.append(node.getText()).append(" ");
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            fetchCtxSourceText(node.getChild(i), builder);
        }
    }
}
