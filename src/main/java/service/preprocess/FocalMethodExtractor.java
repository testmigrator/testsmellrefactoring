package service.preprocess;

import antlr.Java8Parser;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import entity.TestRefactorRule;
import entity.TestSmellDetectionOutput;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FocalMethodExtractor {

    //before:time consuming：120429
    //after:time consuming：11952
    public void fillMethodSignature(List<TestSmellDetectionOutput> testSmellList) {
        testSmellList = testSmellList.stream().
                filter(x -> x.getTestRefactorRule() == TestRefactorRule.LLM).toList();

        Map<String, Map<String, List<String>>> map = Maps.newHashMap();
        List<String> classPathSet = testSmellList.stream().map(TestSmellDetectionOutput::getClassFilepath)
                .filter(StringUtils::isNotBlank)
                .distinct().toList();
        for (String classPath : classPathSet) {
            Map<String, List<String>> methodSignatureMap = fillMethodSignatureMap(classPath);
            map.put(classPath, methodSignatureMap);
        }

        for (TestSmellDetectionOutput output : testSmellList) {
            if (StringUtils.isNotBlank(output.getClassFilepath())) {
                Map<String, List<String>> methodSignatureMap = map.get(output.getClassFilepath());
                String focalMethodInfo = parseFocalMethodInfo(output.getMethodCtx(), methodSignatureMap);
                output.setFocalMethod(focalMethodInfo);
            } else {
                output.setFocalMethod(StringUtils.EMPTY);
            }
        }
    }

    public String parseFocalMethodInfo(ParserRuleContext testMethodCtx, Map<String, List<String>> methodSignatureMap) {
        if (testMethodCtx == null || methodSignatureMap == null || methodSignatureMap.isEmpty()) {
            return StringUtils.EMPTY;
        }

        List<String> invokeMethodNames = fetchInvokeMethodNames(testMethodCtx);
        if (CollectionUtils.isEmpty(invokeMethodNames)) {
            return StringUtils.EMPTY;
        }

        invokeMethodNames = invokeMethodNames.stream().distinct().collect(Collectors.toList());
        List<String> signatures = Lists.newArrayList();
        for (String invokeMethodName : invokeMethodNames) {
            if (methodSignatureMap.containsKey(invokeMethodName)) {
                String join = Joiner.on(",").join(methodSignatureMap.get(invokeMethodName));
                signatures.add(join);
            }
        }

        return Joiner.on(";").join(signatures);
    }

    private List<String> fetchInvokeMethodNames(ParserRuleContext testMethodCtx) {
        MethodInvocationExtractor methodInvocationExtractor = new MethodInvocationExtractor();
        methodInvocationExtractor.visit(testMethodCtx);
        return methodInvocationExtractor.getMethodNames();
    }

    public Map<String, List<String>> fillMethodSignatureMap(String classFilepath) {
        TreeParseService treeParseService = new TreeParseService();
        Java8Parser.CompilationUnitContext classParseTree = treeParseService.getParseTree(classFilepath);
        MethodSignatureVisitor visitor = new MethodSignatureVisitor();
        visitor.visit(classParseTree);

        return visitor.getMethodSignatureMap();
    }


}
