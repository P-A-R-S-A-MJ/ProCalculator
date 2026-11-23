package procalculator;

import java.util.ArrayList;
import java.util.Scanner;
import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.operator.Subtraction;
import procalculator.engine.NumericalEngine;
import procalculator.parser.ExpressionParser;
import procalculator.parser.Tokenizer;

public class ProCalculator {
     public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            ArrayList<String> characters = new ArrayList<>();
            AstUtils.isDegreeMode = false;
            System.out.println("Enter your numbers to calculate :");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("end")) {
                break;
            }
            try {
                characters = Tokenizer.tokenize(input);

                String mainVar = "";
                boolean isMulti = false;
                for (int i = 0; i < characters.size(); i++) {
                    String word = characters.get(i);
                    if (word.length() == 1 && word.matches("[a-zA-Z]")) {
                        if (word.equalsIgnoreCase("d")) {
                            continue;
                        }
                        if (mainVar.equals("")) {
                            mainVar = word;
                        } else if (!(mainVar.equals(word))) {
                            isMulti = true;
                            break;
                        }

                    }
                }
                if (isMulti) {
                    System.out.println("This type of equation is not supported yet");
                    continue;
                }
                if ((!(mainVar.equals(""))) && (!(mainVar.equals("x")))) {
                    java.util.Collections.replaceAll(characters, mainVar, "x");
                }

                Tokenizer.handleImplicitMultiplication(characters);
                int parenBalance = 0;
                for (String token : characters) {
                    if (token.equals("(")) {
                        parenBalance++;
                    } else if (token.equals(")")) {
                        parenBalance--;
                    }
                    if (parenBalance < 0) {
                        throw new IllegalArgumentException("Unbalanced parentheses.");
                    }
                }
                if (parenBalance != 0) {
                    throw new IllegalArgumentException("Unbalanced parentheses.");
                }
                boolean hasTrig = false;
                for (int i = 0; i < characters.size(); i++) {
                    if (characters.get(i).equalsIgnoreCase("sin") || characters.get(i).equalsIgnoreCase("cos")
                            || characters.get(i).equalsIgnoreCase("tan") || characters.get(i).equalsIgnoreCase("tg")
                            || characters.get(i).equalsIgnoreCase("cot")
                            || characters.get(i).equalsIgnoreCase("cotg")
                            || characters.get(i).equalsIgnoreCase("sec")
                            || characters.get(i).equalsIgnoreCase("csc")) {
                        hasTrig = true;
                        break;
                    }
                }
                String format = "normal";
                double minRange, maxRange;
                if (hasTrig) {
                    System.out.println("How do you want your answer? (R/D)(Press Enter for default(R)) : ");
                    format = scanner.nextLine();
                    if (format.isEmpty()) {
                        format = "R";
                    }
                    if (format.equalsIgnoreCase("D")) {
                        AstUtils.isDegreeMode = true;
                    }
                }
                if (characters.contains("=") && characters.contains("x")) {
                    System.out.println("Equation detected. Going to solver..."
                            + "\n\n<--------------------------------------------------->\n");
                    int equalIndex = characters.indexOf("=");
                    ArrayList<String> lefList = new ArrayList<>(characters.subList(0, equalIndex));
                    ArrayList<String> rightList = new ArrayList<>(
                            characters.subList((equalIndex + 1), characters.size()));
                    Expression leftTree = ExpressionParser.parse(lefList);
                    Expression rightTree = ExpressionParser.parse(rightList);

                    System.out.println("Enter the minimum of your answer's range : (or press Enter for default) ");
                    String minInput = scanner.nextLine();
                    if (minInput.trim().isEmpty()) {
                        if (!(hasTrig)) {
                            minRange = -100;
                            maxRange = 100;
                        } else {
                            minRange = 0;
                            if (format.equalsIgnoreCase("D")) {
                                maxRange = 360;
                            } else {
                                maxRange = 2 * Math.PI;
                            }
                        }
                    } else {
                        minRange = Double.parseDouble(minInput);
                        System.out.println("Enter the maximum of your answer's range : ");
                        maxRange = Double.valueOf(scanner.nextLine());
                    }
                    Expression equationTree = new Subtraction(leftTree, rightTree);
                    String printVar = mainVar.isEmpty() ? "x" : mainVar;
                    NumericalEngine.solveNewtonAST(equationTree, format, minRange, maxRange, printVar);

                } else if (characters.contains("=") && !characters.contains("x")) {
                    int equalIndex = characters.indexOf("=");
                    ArrayList<String> lefList = new ArrayList<>(characters.subList(0, equalIndex));
                    ArrayList<String> rightList = new ArrayList<>(
                            characters.subList((equalIndex + 1), characters.size()));
                    Expression leftTree = ExpressionParser.parse(lefList);
                    Expression rightTree = ExpressionParser.parse(rightList);
                    Expression simpLeft = leftTree.Simplify();
                    Expression simpRight = rightTree.Simplify();
                    double leftAnswer = simpLeft.evaluate(0);
                    double rightAnswer = simpRight.evaluate(0);
                    if (simpLeft.equals(simpRight) || Math.abs(leftAnswer - rightAnswer) < 1e-9) {
                        System.out.println("True");
                    } else {
                        System.out.println("false");
                    }
                } else if (characters.contains("'") || characters.contains("d")) {
                    System.out.println("Derivative detected. Going to Calculus Engine..."
                            + "\n\n<--------------------------------------------------->\n");
                    for (int i = (characters.size() - 1); i >= 0; i--) {
                        if (characters.get(i).equalsIgnoreCase("'") || characters.get(i).equalsIgnoreCase("d")) {
                            characters.remove(i);
                        }
                    }
                    if (!characters.isEmpty() && characters.get(characters.size() - 1).equals("*")) {
                        characters.remove(characters.size() - 1);
                    }
                    Expression mathTree = ExpressionParser.parse(characters);
                    Expression derivativeTree = mathTree.derive();
                    Expression simplifiedDerivative = derivativeTree.Simplify();
                    String result = simplifiedDerivative.print();
                    if (!mainVar.isEmpty() && !mainVar.equals("x")) {
                        result = result.replace("x", mainVar);
                    }
                    if (result.startsWith("(") && result.endsWith(")")) {
                        result = result.substring(1, result.length() - 1);
                    }
                    System.out.println("Derivative: " + result);

                } else if (characters.contains("integral") || characters.contains("Integral")) {
                    System.out.println("Integral detected. Going to Calculus Engine..."
                            + "\n\n<--------------------------------------------------->\n");

                    while (characters.contains("integral"))
                        characters.remove("integral");
                    while (characters.contains("Integral"))
                        characters.remove("Integral");

                    Expression mathTree = ExpressionParser.parse(characters);

                    System.out.println("Definite or Indefinite Integral? (D/I) : ");
                    String intType = scanner.nextLine();

                    if (intType.equalsIgnoreCase("D")) {
                        System.out.println("Enter the lower limit of integral (a) : ");
                        double a = Double.parseDouble(scanner.nextLine());
                        System.out.println("Enter the upper limit of integral (b) : ");
                        double b = Double.parseDouble(scanner.nextLine());

                        double area = NumericalEngine.calculateIntegral(mathTree, a, b);
                        System.out.println("Definite Integral Result (Area): " + AstUtils.format(area));

                    } else {
                        Expression integralTree = mathTree.Simplify().integrate();

                        if (integralTree == null) {
                            System.out.println(
                                    "Sorry! This type of integral (like Division or Multiplication of functions) is not supported yet.");
                        } else {
                            Expression simplifiedIntegral = integralTree.Simplify();
                            String resultStr = simplifiedIntegral.print();

                            if (!mainVar.isEmpty() && !mainVar.equals("x")) {
                                resultStr = resultStr.replace("x", mainVar);
                            }

                            if (resultStr.startsWith("(") && resultStr.endsWith(")")) {
                                resultStr = resultStr.substring(1, resultStr.length() - 1);
                            }

                            System.out.println("Indefinite Integral: " + resultStr + " + C");
                        }
                    }

                } else {
                    System.out.println("Expression detected. Going to calculator..."
                            + "\n\n<--------------------------------------------------->\n");
                    Expression mathTree = ExpressionParser.parse(characters);
                    Expression simplifiedTree = mathTree.Simplify();
                    String resultStr = simplifiedTree.print();
                    if (!mainVar.isEmpty() && !mainVar.equals("x")) {
                        resultStr = resultStr.replace("x", mainVar);
                    }
                    System.out.println("Simplified Result: " + resultStr);
                }
            } catch (ArithmeticException e) {
                System.out.println("Math Error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Syntax Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Invalid mathematical expression! Please try again.");
                e.printStackTrace();
            }

        }
        scanner.close();
    }
}
