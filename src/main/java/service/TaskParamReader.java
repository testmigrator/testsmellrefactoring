package service;

import entity.TaskParam;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class TaskParamReader {

    public static TaskParam taskParam;

    public static TaskParam getTaskParam() {
        if (taskParam != null) {
            return taskParam;
        }
        taskParam = buildTaskParam();
        return taskParam;
    }

    private static TaskParam buildTaskParam() {
        InputStream in = ClassLoader.getSystemResourceAsStream("task.properties");
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return TaskParam.builder()
                .projectFilepath(properties.getProperty("projectFilepath"))
                .build();

    }

}
