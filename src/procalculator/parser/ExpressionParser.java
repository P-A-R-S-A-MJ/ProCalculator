package procalculator.parser;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.function.*;
import procalculator.ast.operator.*;
import procalculator.ast.value.*;

public class ExpressionParser {
        public static Expression parse(java.util.List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("Missing operand or empty parentheses.");
        }
        if (tokens.size() == 1) {
            String token = tokens.get(0);
            if (token.equalsIgnoreCase("x")) {
                return new Variable();
            } else if (AstUtils.isNumber(token)) {
                if (token.contains(".")) {
                    try {
                        String[] parts = token.split("\\.");
                        int decimals = (parts.length > 1) ? parts[1].length() : 0;
                        if (decimals <= 15) {
                            long den = (long) Math.pow(10, decimals);
                            long num = Long.parseLong(token.replace(".", ""));
                            return new Fraction(num, den);
                        }
                        return new Constant(Double.parseDouble(token));
                    } catch (NumberFormatException e) {
                        return new Constant(Double.parseDouble(token));
                    }
                } else {
                    try {
                        return new Fraction(Long.parseLong(token), 1);
                    } catch (NumberFormatException e) {
                        return new Constant(Double.parseDouble(token));
                    }
                }
            } else {
                throw new IllegalArgumentException("Unexpected or incomplete token: " + token);
            }
        }
        int depth = 0;
        for (int i = (tokens.size() - 1); i >= 0; i--) {
            String current = tokens.get(i);
            if (current.equalsIgnoreCase(")")) {
                depth++;
            } else if (current.equalsIgnoreCase("(")) {
                depth--;
            }
            if (depth == 0 && i > 0 && (current.equalsIgnoreCase("+") || current.equalsIgnoreCase("-"))) {
                String prev = tokens.get(i - 1);
                if (prev.equals("^") || prev.equals("*") || prev.equals("/") || prev.equals("(")
                        || prev.equals("+") || prev.equals("-")) {
                    continue;
                }
                Expression leftNode = parse(tokens.subList(0, i));
                Expression rightNode = parse(tokens.subList(i + 1, tokens.size()));
                if (current.equalsIgnoreCase("+")) {
                    return new Addition(leftNode, rightNode);
                } else if (current.equalsIgnoreCase("-")) {
                    return new Subtraction(leftNode, rightNode);
                }
            }
        }
        depth = 0;
        for (int i = (tokens.size() - 1); i >= 0; i--) {
            String current = tokens.get(i);
            if (current.equalsIgnoreCase(")")) {
                depth++;
            } else if (current.equalsIgnoreCase("(")) {
                depth--;
            }
            if (depth == 0 && (current.equalsIgnoreCase("*") || current.equalsIgnoreCase("/"))) {
                Expression leftNode = parse(tokens.subList(0, i));
                Expression rightNode = parse(tokens.subList(i + 1, tokens.size()));
                if (current.equalsIgnoreCase("*")) {
                    return new Multiplication(leftNode, rightNode);
                } else if (current.equalsIgnoreCase("/")) {
                    return new Division(leftNode, rightNode);
                }
            }
        }
        if (tokens.get(0).equalsIgnoreCase("-")) {
            return new Multiplication(new Fraction(-1, 1), parse(tokens.subList(1, tokens.size())));
        }
        if (tokens.get(0).equalsIgnoreCase("+")) {
            return parse(tokens.subList(1, tokens.size()));
        }
        depth = 0;
        for (int i = 0; i < tokens.size(); i++) {
            String current = tokens.get(i);
            if (current.equalsIgnoreCase(")")) {
                depth++;
            } else if (current.equalsIgnoreCase("(")) {
                depth--;
            }
            if (depth == 0 && (current.equalsIgnoreCase("^"))) {
                Expression leftNode = parse(tokens.subList(0, i));
                Expression rightNode = parse(tokens.subList(i + 1, tokens.size()));
                if (current.equalsIgnoreCase("^")) {
                    return new Power(leftNode, rightNode);
                }
            }
        }
        depth = 0;
        if (tokens.get(0).equalsIgnoreCase("(") && tokens.get(tokens.size() - 1).equalsIgnoreCase(")")) {
            return parse(tokens.subList(1, tokens.size() - 1));
        }
        if (tokens.get(0).equalsIgnoreCase("sin")) {
            Expression innerNode;
            if (tokens.get(1).equalsIgnoreCase("(")) {
                innerNode = parse(tokens.subList(2, (tokens.size() - 1)));
            } else {
                innerNode = parse(tokens.subList(1, tokens.size()));
            }
            return new Sin(innerNode);
        } else if (tokens.get(0).equalsIgnoreCase("cos")) {
            Expression innerNode;
            if (tokens.get(1).equalsIgnoreCase("(")) {
                innerNode = parse(tokens.subList(2, (tokens.size() - 1)));
            } else {
                innerNode = parse(tokens.subList(1, tokens.size()));
            }
            return new Cos(innerNode);
        } else if (tokens.get(0).equalsIgnoreCase("tan") || tokens.get(0).equalsIgnoreCase("tg")) {
            Expression innerNode;
            if (tokens.get(1).equalsIgnoreCase("(")) {
                innerNode = parse(tokens.subList(2, (tokens.size() - 1)));
            } else {
                innerNode = parse(tokens.subList(1, tokens.size()));
            }
            return new Tan(innerNode);
        } else if (tokens.get(0).equalsIgnoreCase("cot") || tokens.get(0).equalsIgnoreCase("cotg")) {
            Expression innerNode;
            if (tokens.get(1).equalsIgnoreCase("(")) {
                innerNode = parse(tokens.subList(2, (tokens.size() - 1)));
            } else {
                innerNode = parse(tokens.subList(1, tokens.size()));
            }
            return new Cot(innerNode);
        } else if (tokens.get(0).equalsIgnoreCase("sec")) {
            Expression innerNode;
            if (tokens.get(1).equalsIgnoreCase("(")) {
                innerNode = parse(tokens.subList(2, (tokens.size() - 1)));
            } else {
                innerNode = parse(tokens.subList(1, tokens.size()));
            }
            return new Sec(innerNode);
        } else if (tokens.get(0).equalsIgnoreCase("csc")) {
            Expression innerNode;
            if (tokens.get(1).equalsIgnoreCase("(")) {
                innerNode = parse(tokens.subList(2, (tokens.size() - 1)));
            } else {
                innerNode = parse(tokens.subList(1, tokens.size()));
            }
            return new Csc(innerNode);
        } else if (tokens.get(0).equalsIgnoreCase("ln")) {
            Expression innerNode;
            if (tokens.get(1).equalsIgnoreCase("(")) {
                innerNode = parse(tokens.subList(2, (tokens.size() - 1)));
            } else {
                innerNode = parse(tokens.subList(1, tokens.size()));
            }
            return new Ln(innerNode);
        } else if (tokens.get(0).equalsIgnoreCase("log") || tokens.get(0).equalsIgnoreCase("log10")) {
            Expression innerNode;
            if (tokens.get(1).equalsIgnoreCase("(")) {
                innerNode = parse(tokens.subList(2, (tokens.size() - 1)));
            } else {
                innerNode = parse(tokens.subList(1, tokens.size()));
            }
            return new Log10(innerNode);
        } else if (tokens.get(0).equalsIgnoreCase("sqrt")) {
            Expression innerNode;
            if (tokens.get(1).equalsIgnoreCase("(")) {
                innerNode = parse(tokens.subList(2, (tokens.size() - 1)));
            } else {
                innerNode = parse(tokens.subList(1, tokens.size()));
            }
            return new Power(innerNode, new Fraction(1, 2));
        } else if (tokens.get(0).toLowerCase().startsWith("root")) {
            Expression innerNode;
            String rootString = tokens.get(0).substring(4);

            if (tokens.get(1).equalsIgnoreCase("(")) {
                innerNode = parse(tokens.subList(2, (tokens.size() - 1)));
            } else {
                innerNode = parse(tokens.subList(1, tokens.size()));
            }

            if (rootString.isEmpty()) {
                return new Power(innerNode, new Fraction(1, 2));
            } else {
                long n = Long.parseLong(rootString);
                return new Power(innerNode, new Fraction(1, n));
            }
        }
        throw new IllegalArgumentException("Invalid syntax near: " + String.join(" ", tokens));
    }

}