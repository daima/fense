package com.cxy7.data.fense.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
public class RegexGroupExtracter {
    private static final Logger logger = LoggerFactory.getLogger(RegexGroupExtracter.class);
    private static Pattern regex = Pattern.compile("<a href=\"(magnet.*?)\">");

    public static void main(String[] args) throws IOException {
        File sourceFile = new File("E:/s.txt");
        String content = FileUtils.readFileToString(sourceFile, "gbk");
        RegexGroupExtracter rge = new RegexGroupExtracter();
        Matcher matcher = regex.matcher(content);
        while (matcher.find()) {
            String txt = matcher.group(1);
            System.out.println(txt);
        }
    }
}
