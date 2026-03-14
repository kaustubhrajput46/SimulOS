package com.osshell.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses raw string input into ParsedCommand objects.
 */
public class CommandParser {

    /**
     * Parses a raw input string into a ParsedCommand.
     *
     * @param input The raw user input
     * @return ParsedCommand object containing command name, arguments, and background flag
     */
    public ParsedCommand parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String trimmedInput = input.trim();
        boolean runInBackground = false;

        // Check if command should run in background
        if (trimmedInput.endsWith("&")) {
            runInBackground = true;
            trimmedInput = trimmedInput.substring(0, trimmedInput.length() - 1).trim();
        }

        // Split input into tokens
        String[] tokens = parseTokens(trimmedInput);
        
        if (tokens.length == 0) {
            return null;
        }

        String commandName = tokens[0];
        String[] arguments = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, arguments, 0, tokens.length - 1);

        return new ParsedCommand(commandName, arguments, runInBackground);
    }

    /**
     * Splits input string into tokens, handling quoted strings.
     */
    private String[] parseTokens(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = '\0';

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '"' || c == '\'') {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                    quoteChar = '\0';
                } else {
                    currentToken.append(c);
                }
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken = new StringBuilder();
                }
            } else {
                currentToken.append(c);
            }
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens.toArray(new String[0]);
    }
}
