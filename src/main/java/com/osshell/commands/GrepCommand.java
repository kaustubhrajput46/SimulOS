package com.osshell.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Implements the grep command for filtering text.
 */
public class GrepCommand implements Command {

    @Override
    public int execute(String[] args, InputStream in, PrintStream out) {
        if (args.length == 0) {
            System.err.println("grep: search term required");
            return 1;
        }

        String searchTerm = args[0];
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(searchTerm)) {
                    out.println(line);
                }
            }
        } catch (IOException e) {
            System.err.println("grep: error reading input: " + e.getMessage());
            return 1;
        }

        return 0;
    }
}


