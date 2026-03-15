package com.osshell.commands;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
public class SortCommand implements Command {
    @Override
    public int execute(String[] args, InputStream in, PrintStream out) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            List<String> lines = reader.lines().collect(Collectors.toList());
            Collections.sort(lines);
            for (String line : lines) {
                out.println(line);
            }
        } catch (Exception e) {
            System.err.println("sort: error: " + e.getMessage());
            return 1;
        }
        return 0;
    }
}
