package service.preprocess;

import com.google.common.collect.Lists;
import entity.TestSmellDetectionOutput;
import entity.TestSmellType;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestSmellDetectionService {

    public List<TestSmellDetectionOutput> buildTestSmellDetectionOutputList(String csvFilepath) {
        String line = "";
        String csvSplitBy = ",";
        List<TestSmellDetectionOutput> outputs = Lists.newArrayList();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilepath))) {

            line = br.readLine();
            String[] headers = line.split(csvSplitBy);
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(csvSplitBy);

                List<String> csvLineData = Arrays.asList(fields);
                TestSmellDetectionOutput output = new TestSmellDetectionOutput();
                output.setAppName(csvLineData.get(0));
                output.setTestClassName(csvLineData.get(1));
                output.setTestFilepath(csvLineData.get(2));

                List<TestSmellType> testSmellTypeList = Lists.newArrayList();
                if (StringUtils.isNotBlank(csvLineData.get(7)) && !StringUtils.equals(csvLineData.get(7), "0")) {
                    testSmellTypeList.add(TestSmellType.Assertion_Roulette);
                }
                if (StringUtils.isNotBlank(csvLineData.get(8)) && !StringUtils.equals(csvLineData.get(8), "0")) {
                    testSmellTypeList.add(TestSmellType.Conditional_Test_Logic);
                }
                if (StringUtils.isNotBlank(csvLineData.get(9)) && !StringUtils.equals(csvLineData.get(9), "0")) {
                    testSmellTypeList.add(TestSmellType.Constructor_Initialization);
                }
                if (StringUtils.isNotBlank(csvLineData.get(10)) && !StringUtils.equals(csvLineData.get(10), "0")) {
                    testSmellTypeList.add(TestSmellType.Default_Test);
                }
                if (StringUtils.isNotBlank(csvLineData.get(11)) && !StringUtils.equals(csvLineData.get(11), "0")) {
                    testSmellTypeList.add(TestSmellType.EmptyTest);
                }
                if (StringUtils.isNotBlank(csvLineData.get(12)) && !StringUtils.equals(csvLineData.get(12), "0")) {
                    testSmellTypeList.add(TestSmellType.Exception_Catching_Throwing);
                }
                if (StringUtils.isNotBlank(csvLineData.get(13)) && !StringUtils.equals(csvLineData.get(13), "0")) {
                    testSmellTypeList.add(TestSmellType.General_Fixture);
                }
                if (StringUtils.isNotBlank(csvLineData.get(14)) && !StringUtils.equals(csvLineData.get(14), "0")) {
                    testSmellTypeList.add(TestSmellType.Mystery_Guest);
                }
                if (StringUtils.isNotBlank(csvLineData.get(15)) && !StringUtils.equals(csvLineData.get(15), "0")) {
                    testSmellTypeList.add(TestSmellType.Print_Statement);
                }
                if (StringUtils.isNotBlank(csvLineData.get(16)) && !StringUtils.equals(csvLineData.get(16), "0")) {
                    testSmellTypeList.add(TestSmellType.Redundant_Assertion);
                }
                if (StringUtils.isNotBlank(csvLineData.get(17)) && !StringUtils.equals(csvLineData.get(17), "0")) {
                    testSmellTypeList.add(TestSmellType.Sensitive_Equality);
                }
                if (StringUtils.isNotBlank(csvLineData.get(18)) && !StringUtils.equals(csvLineData.get(18), "0")) {
                    testSmellTypeList.add(TestSmellType.Verbose_Test);
                }
                if (StringUtils.isNotBlank(csvLineData.get(19)) && !StringUtils.equals(csvLineData.get(19), "0")) {
                    testSmellTypeList.add(TestSmellType.Sleepy_Test);
                }
                if (StringUtils.isNotBlank(csvLineData.get(20)) && !StringUtils.equals(csvLineData.get(20), "0")) {
                    testSmellTypeList.add(TestSmellType.Eager_Test);
                }
                if (StringUtils.isNotBlank(csvLineData.get(21)) && !StringUtils.equals(csvLineData.get(21), "0")) {
                    testSmellTypeList.add(TestSmellType.Lazy_Test);
                }
                if (StringUtils.isNotBlank(csvLineData.get(22)) && !StringUtils.equals(csvLineData.get(22), "0")) {
                    testSmellTypeList.add(TestSmellType.Duplicate_Assert);
                }
                if (StringUtils.isNotBlank(csvLineData.get(23)) && !StringUtils.equals(csvLineData.get(23), "0")) {
                    testSmellTypeList.add(TestSmellType.Unknown_Test);
                }
                if (StringUtils.isNotBlank(csvLineData.get(24)) && !StringUtils.equals(csvLineData.get(24), "0")) {
                    testSmellTypeList.add(TestSmellType.IgnoredTest);
                }
                if (StringUtils.isNotBlank(csvLineData.get(25)) && !StringUtils.equals(csvLineData.get(25), "0")) {
                    testSmellTypeList.add(TestSmellType.Resource_Optimism);
                }
                if (StringUtils.isNotBlank(csvLineData.get(26)) && !StringUtils.equals(csvLineData.get(26), "0")) {
                    testSmellTypeList.add(TestSmellType.Magic_Number_Test);
                }
                if (StringUtils.isNotBlank(csvLineData.get(27)) && !StringUtils.equals(csvLineData.get(27), "0")) {
                    testSmellTypeList.add(TestSmellType.Dependent_Test);
                }
                output.setTestSmellTypeList(testSmellTypeList);

                outputs.add(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputs;
    }
}
