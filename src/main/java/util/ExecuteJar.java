package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ExecuteJar {
    public static void runJar(String jarPath, List<String> args){
        StringBuilder argBuilder = new StringBuilder();
        for (String arg: args) {
            argBuilder.append(" ").append(arg);
        }

        String command = "java -jar " + jarPath + " " + argBuilder;
        System.out.println(command);

        try {
            Process process = Runtime.getRuntime().exec(command);

            readStream(process.getInputStream());
            readStream(process.getErrorStream());

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("success");
            } else {
                System.out.println("fail，exitCode: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void readStream(InputStream inputStream) {
        new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
