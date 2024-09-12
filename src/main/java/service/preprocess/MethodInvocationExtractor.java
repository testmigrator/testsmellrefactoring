package service.preprocess;

import antlr.Java8BaseVisitor;
import antlr.Java8Lexer;
import antlr.Java8Parser;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Getter
public class MethodInvocationExtractor extends Java8BaseVisitor<RuleNode> {
    private List<String> methodNames = Lists.newArrayList();

    @Override
    public RuleNode visitMethodInvocation(Java8Parser.MethodInvocationContext ctx) {
        fillCalleeMethodInvocation(ctx);
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitMethodInvocation_lf_primary(Java8Parser.MethodInvocation_lf_primaryContext ctx) {
        fillCalleeMethodInvocation(ctx);
        return visitChildren(ctx);
    }

    @Override
    public RuleNode visitMethodInvocation_lfno_primary(Java8Parser.MethodInvocation_lfno_primaryContext ctx) {
        fillCalleeMethodInvocation(ctx);
        return visitChildren(ctx);
    }

    private void fillCalleeMethodInvocation(ParserRuleContext ctx) {
        String calleeMethodName = fetchCalleeMethodName(ctx);
        methodNames.add(calleeMethodName);
    }

    private String fetchCalleeMethodName(ParserRuleContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode) {
                TerminalNode node = (TerminalNode) child;
                if (node.getSymbol().getType() == Java8Lexer.Identifier) {
                    return node.getText();
                }
            }
            if (child instanceof RuleContext) {
                RuleContext node = (RuleContext) child;
                if (node.getRuleIndex() == Java8Parser.RULE_methodName) {
                    return node.getText();
                }
            }
        }
        return StringUtils.EMPTY;
    }
}
