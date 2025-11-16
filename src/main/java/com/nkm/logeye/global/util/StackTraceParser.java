package com.nkm.logeye.global.util;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StackTraceParser {
    private static Pattern FILE_LINE_PATTERN = Pattern.compile("\\(([^:]+:\\d+)\\)");

    public String parse(String stackTrace){
        if(stackTrace == null || stackTrace.isEmpty()){
            return "Unknown stacktrace";
        }

        String[] lines = stackTrace.split("\n");

        for(String line : lines){
            Matcher matcher = FILE_LINE_PATTERN.matcher(line);
            if(matcher.find()){
                return matcher.group(1);
            }
        }

        return "Unknown stacktrace";
    }
}