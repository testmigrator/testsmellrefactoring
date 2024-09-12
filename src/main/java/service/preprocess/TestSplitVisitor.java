package service.preprocess;

import antlr.Java8BaseVisitor;
import antlr.Java8Parser;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.List;

@Getter
public class TestSplitVisitor extends Java8BaseVisitor<RuleNode> {

    public TestSplitVisitor(Token keepStartToken, int keepStartIndex, int keepEndIndex){
        this.keepStartToken = keepStartToken;
        this.keepStartIndex = keepStartIndex;
        this.keepEndIndex = keepEndIndex;
    }

    private TestMethodService testMethodService = new TestMethodService();

    private Token keepStartToken;
    private int keepStartIndex;
    private int keepEndIndex;
    private List<RemoveIndex> removeIndexList = Lists.newArrayList();


    @Override
    public RuleNode visitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        if (testMethodService.isTestMethod(ctx)) {
            if (keepStartIndex == ctx.getStart().getStartIndex() && keepEndIndex == ctx.getStop().getStopIndex()) {
                return visitChildren(ctx);
            }

            //remove
            RemoveIndex removeIndex = RemoveIndex.builder()
                    .startToken(ctx.getStart())
                    .startIndex(ctx.getStart().getStartIndex())
                    .endIndex(ctx.getStop().getStopIndex())
                    .build();
            removeIndexList.add(removeIndex);
        }
        return visitChildren(ctx);
    }

    @Data
    @Builder
    public static class RemoveIndex {
        private Token startToken;
        private int startIndex;
        private int endIndex;
    }
}
