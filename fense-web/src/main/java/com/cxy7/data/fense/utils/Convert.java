package com.cxy7.data.fense.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
public class Convert {
    public static void main(String[] args) throws IOException {
        String sourceFile = "D:/s.txt";
        List<String> list = FileUtils.readLines(new File(sourceFile), "gbk");
        List<String> result = new ArrayList<String>();
        for (String line :
                list) {
            line = line.trim();
            if (StringUtils.isBlank(line)) {
                result.add(line);
                continue;
            }
            if (line.startsWith("<script")) {
                line = line.replace("<script src=\"", "<script th:src=\"@{{path}/");
                line = line.replace("js\"></script>", "js(path=${contextPath})}\" type=\"text/javascript\"></script>");
            } else if (line.startsWith("<link")) {
                line = line.replace("<link href=\"", "<link th:href=\"@{{path}/");
                line = line.replace("css\" rel=\"stylesheet\">", "css(path=${contextPath})}\" rel=\"stylesheet\">");
            } else if (line.startsWith("<img")) {
                line = line.replace("src=\"", "th:src=\"@{{path}/");
                line = line.replace("\">", "(path=${contextPath})}\">");
                line = line.replace("\" />\n", "(path=${contextPath})}\" />\n");
            }
            result.add(line);
        }
        FileUtils.writeLines(new File("D:/t.txt"), result);
    }
}
