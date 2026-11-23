package procalculator.parser;

import java.util.ArrayList;
import procalculator.ast.AstUtils;

public class Tokenizer {
        public static ArrayList<String> tokenize(String input) {
        ArrayList<String> tokens = new ArrayList<>();
        int i = 0;

        while (i < input.length()) {
            char c = input.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^' ||
                    c == '=' || c == '(' || c == ')' || c == '\'' || c == 'd') {
                tokens.add(String.valueOf(c));
                i++;
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                while (i < input.length() && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    num.append(input.charAt(i));
                    i++;
                }
                tokens.add(num.toString());
                continue;
            }
            if (Character.isLetter(c)) {
                String restOfInput = input.substring(i).toLowerCase();
                boolean matchedFunc = false;
                String[] funcs = { "integral", "sqrt", "cotg", "log10", "sin", "cos", "tan", "cot", "sec", "csc", "log",
                        "ln", "tg",
                        "root" };

                for (String f : funcs) {
                    if (restOfInput.startsWith(f)) {

                        if (f.equals("root")) {
                            StringBuilder rootToken = new StringBuilder("root");
                            i += 4;
                            while (i < input.length() && (Character.isDigit(input.charAt(i)))) {
                                rootToken.append(input.charAt(i));
                                i++;
                            }
                            tokens.add(rootToken.toString());
                        } else {
                            tokens.add(f);
                            i += f.length();
                        }

                        matchedFunc = true;
                        break;
                    }
                }
                if (!matchedFunc) {
                    tokens.add(String.valueOf(c));
                    i++;
                }
                continue;
            }
            i++;
        }
        return tokens;
    }
    public static void handleImplicitMultiplication(ArrayList<String> list) {
        for (int i = 0; i < (list.size() - 1); i++) {
            String current = list.get(i);
            String next = list.get(i + 1);
            if ((AstUtils.isNumber(current) && next.equals("("))
                    || (AstUtils.isNumber(current) && next.matches("[a-zA-Z0-9]+"))
                    || (current.equals(")") && next.equals("("))
                    || (current.equals(")") && next.matches("[a-zA-Z0-9]+"))
                    || (current.equalsIgnoreCase("x") && next.equals("("))
                    || (current.equalsIgnoreCase("x") && next.matches("[a-zA-Z0-9]+"))) {

                list.add(i + 1, "*");
                i++;
            }
        }
    }

}