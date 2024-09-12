package service.preprocess;

import antlr.Java8BaseVisitor;
import antlr.Java8Lexer;
import antlr.Java8Parser;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class TestCodeVisitor extends Java8BaseVisitor<RuleNode> {

    public List<ParserRuleContext> methodDeclarationCtxList = Lists.newArrayList();

    /**
     * @param ctx the parse tree
     * @return
     */
    @Override
    public RuleNode visitClassBody(Java8Parser.ClassBodyContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            boolean isRuleContext = ctx.getChild(i) instanceof RuleContext;
            if (!isRuleContext) {
                continue;
            }

            RuleContext childRuleContext = (RuleContext) ctx.getChild(i);
            if (childRuleContext.getRuleIndex() == Java8Parser.RULE_classBodyDeclaration) {
                fillClassMemberContext(childRuleContext);
            }
        }
        return visitChildren(ctx);
    }
    /**
     */
    private void fillClassMemberContext(RuleContext childRuleContext) {
        ParseTree child = childRuleContext.getChild(0);
        boolean isChildRuleContext = child instanceof RuleContext;
        if (!isChildRuleContext) {
            return;
        }
        RuleContext node = (RuleContext) child;
        if (node.getRuleIndex() != Java8Parser.RULE_classMemberDeclaration) {
            return;
        }

        ParseTree declarationChild = node.getChild(0);
        boolean isSubRuleContext = declarationChild instanceof RuleContext;
        if (!isSubRuleContext) {
            return;
        }
        RuleContext subNode = (RuleContext) declarationChild;

        if (subNode.getRuleIndex() == Java8Parser.RULE_methodDeclaration) {
            methodDeclarationCtxList.add((ParserRuleContext) subNode);
        }

    }




}
