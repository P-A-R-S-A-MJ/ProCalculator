import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ProCalculator {

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
                String[] funcs = { "sqrt", "cotg", "log10", "sin", "cos", "tan", "cot", "sec", "csc", "log", "ln", "tg",
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
            if ((isNumber(current) && next.equals("("))
                    || (isNumber(current) && next.matches("[a-zA-Z0-9]+"))
                    || (current.equals(")") && next.equals("("))
                    || (current.equals(")") && next.matches("[a-zA-Z0-9]+"))
                    || (current.equalsIgnoreCase("x") && next.equals("("))
                    || (current.equalsIgnoreCase("x") && next.matches("[a-zA-Z0-9]+"))) {

                list.add(i + 1, "*");
                i++;
            }
        }
    }

    public static String format(double num) {
        double cleanNum = Math.round(num * 1e7) / 1e7;

        long rounded = Math.round(cleanNum);
        if (Math.abs(cleanNum - rounded) < 1e-7) {
            return String.valueOf(rounded);
        } else {
            return String.valueOf(cleanNum);
        }
    }

    public static boolean isNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Expression parse(java.util.List<String> tokens) {
        if (tokens.size() == 1) {
            String token = tokens.get(0);
            if (token.equalsIgnoreCase("x")) {
                return new Variable();
            } else {
                if (token.contains(".")) {
                    return new Constant(Double.parseDouble(token));
                } else {
                    return new Fraction(Long.parseLong(token), 1);
                }
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
                if (current.equalsIgnoreCase("-")) {
                    String prev = tokens.get(i - 1);
                    if (prev.equals("^") || prev.equals("*") || prev.equals("/") || prev.equals("(")) {
                        continue;
                    }
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
            return new Multiplication(new Constant(-1), parse(tokens.subList(1, (tokens.size()))));
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
            return new Power(innerNode, new Constant(0.5));
        } else if (tokens.get(0).toLowerCase().startsWith("root")) {
            Expression innerNode;
            String rootString = tokens.get(0).substring(4);

            if (tokens.get(1).equalsIgnoreCase("(")) {
                innerNode = parse(tokens.subList(2, (tokens.size() - 1)));
            } else {
                innerNode = parse(tokens.subList(1, tokens.size()));
            }
            double powerValue;
            if (rootString.isEmpty()) {
                powerValue = 0.5;
            } else {
                double n = Double.parseDouble(rootString);
                powerValue = 1.0 / n;
            }

            return new Power(innerNode, new Constant(powerValue));
        }
        return null;
    }

    public static void solveNewtonAST(Expression f, String format, double minRange, double maxRange, String varName) {
        Expression df = f.derive();
        ArrayList<Double> answers = new ArrayList<>();

        for (double startX = minRange - 1; startX < maxRange + 1; startX += 0.5) {
            double x = startX;
            for (int j = 0; j < 50; j++) {
                double fx = f.evaluate(x);
                double dx = df.evaluate(x);
                if (Double.isNaN(dx) || Double.isInfinite(dx) || Double.isNaN(fx) || Double.isInfinite(fx)
                        || Math.abs(dx) < (Math.pow(10, -8))) {
                    break;
                }
                double nextx = x - (fx / dx);
                if (Math.abs((nextx - x)) < Math.pow(10, -8)) {
                    x = nextx;
                    break;
                } else {
                    x = nextx;
                }
            }
            double finalFx = f.evaluate(x);
            double finalDx = df.evaluate(x);
            if (Math.abs(finalFx) < Math.pow(10, -3)
                    || (!Double.isNaN(finalFx / finalDx) && Math.abs(finalFx / finalDx) < Math.pow(10, -7))) {
                boolean isDuplicate = false;
                if (x >= minRange - 0.001 && x <= maxRange + 0.001) {
                    for (int j = 0; j < answers.size(); j++) {
                        if (Math.abs(x - (answers.get(j))) < Math.pow(10, -3)) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!(isDuplicate)) {
                        answers.add(x);
                    }
                }
            }
        }
        java.util.Collections.sort(answers);
        if (answers.size() == 0) {
            System.out.println("This equation does not have any answers!!");
        } else if (answers.size() == 1) {
            double answer = answers.get(0);
            System.out.println("The answer : " + varName + " = " + ProCalculator.format(answer));
        } else {
            System.out.println("Answer's : ");
            for (int i = 0; i < answers.size(); i++) {
                double answer = answers.get(i);
                if (i == (answers.size() - 1)) {
                    System.out.print(varName + (i + 1) + " = " + ProCalculator.format(answer) + "\n");
                } else {
                    System.out.print(varName + (i + 1) + " = " + ProCalculator.format(answer) + " , ");
                }
            }
        }
    }

    public static double calculateIntegral(Expression f, double a, double b) {
        double area = 0;
        double dx = 0.00001;
        int sign = 1;
        if (a > b) {
            double temp = a;
            a = b;
            b = temp;
            sign = -1;
        }
        for (double x = a; x < b; x += dx) {
            area = (f.evaluate(x) * dx) + area;
        }
        return (sign * area);
    }

    public static TermInfo extractTerm(Expression expr) {
        if (expr instanceof Fraction) {
            return new TermInfo((Fraction) expr, new Fraction(1, 1));
        } else if (expr instanceof Constant) {
            long val = (long) expr.evaluate(0);
            return new TermInfo(new Fraction(val, 1), new Fraction(1, 1));
        } else if (expr instanceof Multiplication) {
            Multiplication multi = (Multiplication) expr;
            if (multi.getLeft() instanceof Fraction) {
                return new TermInfo((Fraction) multi.getLeft(), multi.getRight());
            } else if (multi.getLeft() instanceof Constant) {
                long val = (long) multi.getLeft().evaluate(0);
                return new TermInfo(new Fraction(val, 1), multi.getRight());
            }
            if (multi.getRight() instanceof Fraction) {
                return new TermInfo((Fraction) multi.getRight(), multi.getLeft());
            } else if (multi.getRight() instanceof Constant) {
                long val = (long) multi.getRight().evaluate(0);
                return new TermInfo(new Fraction(val, 1), multi.getLeft());
            }
        }
        return new TermInfo(new Fraction(1, 1), expr);
    }

    public static PowerInfo extractPower(Expression expr) {
        if (expr instanceof Power) {
            Power pow = (Power) expr;

            return new PowerInfo(pow.getBase(), pow.getPower());
        }

        return new PowerInfo(expr, new Constant(1));
    }

    public static double getDegree(Expression expr) {
        if (expr instanceof Constant) {
            return 0;
        } else if (expr instanceof Variable) {
            return 1;
        } else if (expr instanceof Power) {
            Power pow = (Power) expr;
            if (pow.getPower() instanceof Constant) {
                return pow.getPower().evaluate(0);
            }
        } else if (expr instanceof Multiplication) {
            Multiplication mul = (Multiplication) expr;
            return Math.max(getDegree(mul.getLeft()), getDegree(mul.getRight()));
        }
        return -100;
    }

    public static boolean isDegreeMode = false;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            ArrayList<String> characters = new ArrayList<>();
            isDegreeMode = false;
            System.out.println("Enter your numbers to calculate :");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("end")) {
                break;
            }
            try {
                characters = ProCalculator.tokenize(input);

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

                handleImplicitMultiplication(characters);
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
                        isDegreeMode = true;
                    }
                }
                if (characters.contains("=") && characters.contains("x")) {
                    System.out.println("Equation detected. Going to solver..."
                            + "\n\n<--------------------------------------------------->\n");
                    int equalIndex = characters.indexOf("=");
                    ArrayList<String> lefList = new ArrayList<>(characters.subList(0, equalIndex));
                    ArrayList<String> rightList = new ArrayList<>(
                            characters.subList((equalIndex + 1), characters.size()));
                    Expression leftTree = parse(lefList);
                    Expression rightTree = parse(rightList);

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
                    solveNewtonAST(equationTree, format, minRange, maxRange, printVar);

                } else if (characters.contains("=") && !characters.contains("x")) {
                    int equalIndex = characters.indexOf("=");
                    ArrayList<String> lefList = new ArrayList<>(characters.subList(0, equalIndex));
                    ArrayList<String> rightList = new ArrayList<>(
                            characters.subList((equalIndex + 1), characters.size()));
                    Expression leftTree = parse(lefList);
                    Expression rightTree = parse(rightList);
                    double leftAnswer = leftTree.evaluate(0);
                    double rightAnswer = rightTree.evaluate(0);
                    if (leftAnswer == rightAnswer) {
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
                    Expression mathTree = parse(characters);
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

                    Expression mathTree = parse(characters);

                    System.out.println("Definite or Indefinite Integral? (D/I) : ");
                    String intType = scanner.nextLine();

                    if (intType.equalsIgnoreCase("D")) {
                        System.out.println("Enter the lower limit of integral (a) : ");
                        double a = Double.parseDouble(scanner.nextLine());
                        System.out.println("Enter the upper limit of integral (b) : ");
                        double b = Double.parseDouble(scanner.nextLine());

                        double area = calculateIntegral(mathTree, a, b);
                        System.out.println("Definite Integral Result (Area): " + ProCalculator.format(area));

                    } else {
                        Expression integralTree = mathTree.integrate();

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
                    Expression mathTree = parse(characters);
                    Expression simplifiedTree = mathTree.Simplify();
                    String resultStr = simplifiedTree.print();
                    if (!mainVar.isEmpty() && !mainVar.equals("x")) {
                        resultStr = resultStr.replace("x", mainVar);
                    }
                    System.out.println("Simplified Result: " + resultStr);
                }
            } catch (Exception e) {
                System.out.println("Invalid mathematical expression! Please try again.");
                e.printStackTrace();
            }

        }
        scanner.close();
    }
}

interface Expression {
    double evaluate(double x);

    Expression Simplify();

    Expression integrate();

    Expression derive();

    String print();
}

class Constant implements Expression {
    private double value;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Constant otherConstant = (Constant) obj;

        return Double.compare(this.value, otherConstant.value) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(value);
    }

    public Constant(double value) {
        this.value = value;
    }

    public Expression Simplify() {
        return this;
    }

    public double evaluate(double x) {
        return this.value;
    }

    public Expression derive() {
        return new Constant(0);
    }

    public Expression integrate() {
        return new Multiplication(this, new Variable());
    }

    public String print() {
        return ProCalculator.format(this.value);
    }

}

class Variable implements Expression {
    private String name;

    public Variable() {
        this.name = "x";
    }

    public Variable(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Variable otherVar = (Variable) obj;
        return this.name.equals(otherVar.name);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name);
    }

    public double evaluate(double x) {
        return x;
    }

    public Expression Simplify() {
        return this;
    }

    public Expression derive() {
        return new Constant(1);
    }

    public Expression integrate() {
        Constant half = new Constant(0.5);
        Power squared = new Power(this, new Constant(2));
        return new Multiplication(half, squared);
    }

    public String print() {
        return this.name;
    }
}

class Fraction implements Expression {
    private long numerator;
    private long denominator;

    public Fraction(long numerator, long denominator) {
        if (denominator == 0) {
            throw (new ArithmeticException("Division by zero"));
        } else if (denominator < 0) {
            this.numerator = -numerator;
            this.denominator = -denominator;
        } else {
            this.numerator = numerator;
            this.denominator = denominator;
        }
        long gcdNum = gcd(Math.abs(numerator), Math.abs(denominator));
        this.numerator = this.numerator / gcdNum;
        this.denominator = this.denominator / gcdNum;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Fraction otherFrac = (Fraction) obj;

        return this.numerator == otherFrac.numerator && this.denominator == otherFrac.denominator;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.numerator, this.denominator);
    }

    public long gcd(long a, long b) {
        if (b == 0) {
            return a;
        } else {
            return gcd(b, a % b);
        }
    }

    public Fraction add(Fraction other) {
        long newNum = (this.numerator * other.denominator) + (other.numerator * this.denominator);
        long newDen = this.denominator * other.denominator;
        return new Fraction(newNum, newDen);
    }

    public Fraction multiply(Fraction other) {
        long newNum = this.numerator * other.numerator;
        long newDen = this.denominator * other.denominator;
        return new Fraction(newNum, newDen);
    }

    public long getNumerator() {
        return this.numerator;
    }

    public long getDenominator() {
        return this.denominator;
    }

    public double evaluate(double x) {
        double doubleNumerator = (double) numerator;
        return doubleNumerator / denominator;
    }

    public Expression Simplify() {
        return this;
    }

    public Expression derive() {
        return new Constant(0);
    }

    public Expression integrate() {
        return new Multiplication(this, new Variable());
    }

    public String print() {
        if (denominator == 1) {
            String strNumerator = Long.toString(numerator);
            return strNumerator;
        } else {
            return "(" + numerator + "/" + denominator + ")";
        }

    }
}

class Addition implements Expression {
    private Expression left;
    private Expression right;

    public Addition(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Addition otherAdd = (Addition) obj;

        return this.left.equals(otherAdd.left) && this.right.equals(otherAdd.right);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.left, this.right);
    }

    public ArrayList<Expression> getSummands(Expression expr) {
        ArrayList<Expression> list = new ArrayList<>();
        if (expr instanceof Addition) {
            Addition addExpr = (Addition) expr;
            list.addAll(getSummands(addExpr.left));
            list.addAll(getSummands(addExpr.right));
        } else {
            list.add(expr);
        }
        return list;
    }

    public Expression getLeft() {
        return this.left;
    }

    public Expression getRight() {
        return this.right;
    }

    public double evaluate(double x) {
        return left.evaluate(x) + right.evaluate(x);
    }

    public Expression Simplify() {
        Expression simplifiedLeft = left.Simplify();
        Expression simplifiedRight = right.Simplify();
        if (simplifiedLeft instanceof Constant && simplifiedLeft.evaluate(0) == 0) {
            return simplifiedRight;
        }
        if (simplifiedRight instanceof Constant && simplifiedRight.evaluate(0) == 0) {
            return simplifiedLeft;
        }
        if (simplifiedRight instanceof Constant && simplifiedLeft instanceof Constant
                && simplifiedRight.evaluate(0) == 0 && simplifiedLeft.evaluate(0) == 0) {
            return simplifiedLeft;
        }
        Addition tempAdd = new Addition(simplifiedLeft, simplifiedRight);
        ArrayList<Expression> allTerms = getSummands(tempAdd);
        HashMap<Expression, Fraction> map = new HashMap<>();
        for (Expression term : allTerms) {
            TermInfo info = ProCalculator.extractTerm(term);
            if (map.containsKey(info.core)) {
                Fraction oldCoe = map.get(info.core);
                Fraction sum = oldCoe.add(info.coefficient);
                map.put(info.core, sum);
            } else {
                map.put(info.core, info.coefficient);
            }
        }

        ArrayList<Expression> finalTerms = new ArrayList<>();
        for (Map.Entry<Expression, Fraction> entry : map.entrySet()) {
            Expression core = entry.getKey();
            Fraction coeff = entry.getValue();

            if (coeff.getNumerator() == 0) {
                continue;
            }

            if ((core instanceof Constant && core.evaluate(0) == 1) ||
                    (core instanceof Fraction
                            && ((Fraction) core).getNumerator() == ((Fraction) core).getDenominator())) {
                finalTerms.add(coeff);
            }

            else if (coeff.getNumerator() == coeff.getDenominator()) {
                finalTerms.add(core);
            }

            else {
                finalTerms.add(new Multiplication(coeff, core));
            }
        }
        finalTerms.sort((expr1, expr2) -> {
            double degree1 = ProCalculator.getDegree(expr1);
            double degree2 = ProCalculator.getDegree(expr2);
            return Double.compare(degree2, degree1);
        });
        if (finalTerms.isEmpty()) {
            return new Constant(0);
        }
        if (finalTerms.size() == 1) {
            return finalTerms.get(0);
        }

        Expression result = finalTerms.get(0);
        for (int i = 1; i < finalTerms.size(); i++) {
            result = new Addition(result, finalTerms.get(i));
        }

        return result;
    }

    public Expression derive() {
        return new Addition(left.derive(), right.derive());
    }

    public Expression integrate() {
        Expression leftInt = left.integrate();
        Expression rightInt = right.integrate();
        if (leftInt == null || rightInt == null) {
            return null;
        }

        return new Addition(leftInt, rightInt);
    }

    public String print() {
        String leftStr = left.print();
        String rightStr = right.print();

        if (ProCalculator.isNumber(leftStr) && ProCalculator.isNumber(rightStr)) {
            return ProCalculator.format(this.evaluate(0));
        }

        if (rightStr.startsWith("-")) {
            return "(" + leftStr + rightStr + ")";
        }

        return "(" + leftStr + "+" + rightStr + ")";
    }
}

class Subtraction implements Expression {
    private Expression left;
    private Expression right;

    public Subtraction(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Subtraction otherSub = (Subtraction) obj;

        return this.left.equals(otherSub.left) && this.right.equals(otherSub.right);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.left, this.right);
    }

    public double evaluate(double x) {
        return left.evaluate(x) - right.evaluate(x);
    }

    public Expression getLeft() {
        return this.left;
    }

    public Expression getRight() {
        return this.right;
    }

    public Expression Simplify() {
        Expression simplifiedLeft = left.Simplify();
        Expression simplifiedRight = right.Simplify();
        if (simplifiedRight instanceof Constant && simplifiedRight.evaluate(0) == 0) {
            return simplifiedLeft;
        }
        if (simplifiedLeft instanceof Constant && simplifiedLeft.evaluate(0) == 0) {
            return new Multiplication(new Constant(-1), simplifiedRight);
        }
        Constant minus = new Constant(-1);
        Multiplication negativeNum = new Multiplication(minus, simplifiedRight);
        Addition answer = new Addition(simplifiedLeft, negativeNum);
        return answer.Simplify();
    }

    public Expression derive() {
        return new Subtraction(left.derive(), right.derive());
    }

    public Expression integrate() {
        Expression leftInt = left.integrate();
        Expression rightInt = right.integrate();
        if (leftInt == null || rightInt == null) {
            return null;
        }

        return new Addition(leftInt, rightInt);
    }

    public String print() {
        String leftStr = left.print();
        String rightStr = right.print();
        if (ProCalculator.isNumber(leftStr) && ProCalculator.isNumber(rightStr)) {
            return ProCalculator.format(this.evaluate(0));
        }
        if (leftStr.equalsIgnoreCase("0")) {
            return "-" + rightStr;
        } else if (rightStr.equalsIgnoreCase("0")) {
            return leftStr;
        } else {
            return "(" + leftStr + "-" + rightStr + ")";
        }
    }
}

class Multiplication implements Expression {
    private Expression left;
    private Expression right;

    public Multiplication(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Multiplication otherMulti = (Multiplication) obj;

        return this.left.equals(otherMulti.left) && this.right.equals(otherMulti.right);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.left, this.right);
    }

    public Expression getLeft() {
        return this.left;
    }

    public Expression getRight() {
        return this.right;
    }

    public ArrayList<Expression> getFactors(Expression expr) {
        ArrayList<Expression> list = new ArrayList<>();
        if (expr instanceof Multiplication) {
            Multiplication mulExpr = (Multiplication) expr;
            list.addAll(getFactors(mulExpr.left));
            list.addAll(getFactors(mulExpr.right));
        } else {
            list.add(expr);
        }
        return list;
    }

    public double evaluate(double x) {
        return left.evaluate(x) * right.evaluate(x);
    }

    public Expression Simplify() {
        Expression simplifiedLeft = left.Simplify();
        Expression simplifiedRight = right.Simplify();
        if (simplifiedRight instanceof Addition) {
            Addition add = (Addition) simplifiedRight;
            return new Addition(new Multiplication(simplifiedLeft, add.getLeft()),
                    new Multiplication(simplifiedLeft, add.getRight())).Simplify();
        }
        if (simplifiedRight instanceof Subtraction) {
            Subtraction sub = (Subtraction) simplifiedRight;
            return new Subtraction(new Multiplication(simplifiedLeft, sub.getLeft()),
                    new Multiplication(simplifiedLeft, sub.getRight())).Simplify();
        }

        if (simplifiedLeft instanceof Addition) {
            Addition add = (Addition) simplifiedLeft;
            return new Addition(new Multiplication(add.getLeft(), simplifiedRight),
                    new Multiplication(add.getRight(), simplifiedRight)).Simplify();
        }
        if (simplifiedLeft instanceof Subtraction) {
            Subtraction sub = (Subtraction) simplifiedLeft;
            return new Subtraction(new Multiplication(sub.getLeft(), simplifiedRight),
                    new Multiplication(sub.getRight(), simplifiedRight)).Simplify();
        }
        Multiplication tempMul = new Multiplication(simplifiedLeft, simplifiedRight);
        ArrayList<Expression> allFactors = getFactors(tempMul);

        Fraction totalConstant = new Fraction(1, 1);
        HashMap<Expression, Expression> map = new HashMap<>();

        for (Expression factor : allFactors) {
            if (factor instanceof Fraction) {
                totalConstant = totalConstant.multiply((Fraction) factor);
                continue;
            } else if (factor instanceof Constant) {
                long val = (long) factor.evaluate(0);
                totalConstant = totalConstant.multiply(new Fraction(val, 1));
                continue;
            }

            PowerInfo info = ProCalculator.extractPower(factor);
            if (map.containsKey(info.base)) {
                Expression oldExp = map.get(info.base);
                Expression newExp = new Addition(oldExp, info.exponent).Simplify();
                map.put(info.base, newExp);
            } else {
                map.put(info.base, info.exponent);
            }
        }

        if (totalConstant.getNumerator() == 0) {
            return new Constant(0);
        }
        ArrayList<Expression> finalFactors = new ArrayList<>();
        if (totalConstant.getNumerator() != totalConstant.getDenominator() || map.isEmpty()) {
            finalFactors.add(totalConstant);
        }

        for (Map.Entry<Expression, Expression> entry : map.entrySet()) {
            Expression base = entry.getKey();
            Expression exp = entry.getValue();
            if (exp instanceof Constant && exp.evaluate(0) == 0) {
                continue;
            } else if ((exp instanceof Constant && exp.evaluate(0) == 1) ||
                    (exp instanceof Fraction && ((Fraction) exp).getNumerator() == ((Fraction) exp).getDenominator())) {
                finalFactors.add(base);
            } else {
                finalFactors.add(new Power(base, exp));
            }
        }
        if (finalFactors.isEmpty()) {
            return new Constant(1);
        }

        Expression result = finalFactors.get(0);
        for (int i = 1; i < finalFactors.size(); i++) {
            result = new Multiplication(result, finalFactors.get(i));
        }

        return result;
    }

    public Expression derive() {
        Expression derive1 = new Multiplication(left.derive(), right);
        Expression derive2 = new Multiplication(left, right.derive());
        return new Addition(derive1, derive2);
    }

    public Expression integrate() {
        if (left instanceof Constant) {
            Expression rightInt = right.integrate();
            if (rightInt != null)
                return new Multiplication(left, rightInt);
        } else if (right instanceof Constant) {
            Expression leftInt = left.integrate();
            if (leftInt != null)
                return new Multiplication(right, leftInt);
        }
        Expression u = null;
        Expression dv = null;
        if ((left instanceof Variable || left instanceof Power)
                && !(right instanceof Variable || right instanceof Power)) {
            u = left;
            dv = right;
        } else if ((right instanceof Variable || right instanceof Power)
                && !(left instanceof Variable || left instanceof Power)) {
            u = right;
            dv = left;
        }
        if (u != null && dv != null) {
            Expression v = dv.integrate();
            if (v != null) {
                Expression du = u.derive();

                Expression uv = new Multiplication(u, v);
                Expression v_du = new Multiplication(v, du);

                Expression integral_v_du = v_du.integrate();

                if (integral_v_du != null) {
                    return new Subtraction(uv, integral_v_du);
                }
            }
        }

        return null;
    }

    public String print() {
        String leftStr = left.print();
        String rightStr = right.print();
        if (ProCalculator.isNumber(leftStr) && ProCalculator.isNumber(rightStr)) {
            return ProCalculator.format(this.evaluate(0));
        }
        if (leftStr.equals("1")) {
            return rightStr;
        } else if (rightStr.equals("1")) {
            return leftStr;
        } else if (leftStr.equals("-1")) {
            return "-" + rightStr;
        } else if (rightStr.equals("-1")) {
            return "-" + leftStr;
        } else if (ProCalculator.isNumber(leftStr) && !ProCalculator.isNumber(rightStr)) {
            return leftStr + rightStr;
        } else if (!ProCalculator.isNumber(leftStr) && ProCalculator.isNumber(rightStr)) {
            return rightStr + leftStr;
        } else {
            return "(" + leftStr + "*" + rightStr + ")";
        }
    }
}

class Division implements Expression {
    private Expression left;
    private Expression right;

    public Division(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Division otherDiv = (Division) obj;

        return this.left.equals(otherDiv.left) && this.right.equals(otherDiv.right);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.left, this.right);
    }

    public double evaluate(double x) {
        return left.evaluate(x) / right.evaluate(x);
    }

    public ArrayList<Expression> getFactors(Expression expr) {
        ArrayList<Expression> list = new ArrayList<>();
        if (expr instanceof Multiplication) {
            Multiplication mulExpr = (Multiplication) expr;
            list.addAll(getFactors(mulExpr.getLeft()));
            list.addAll(getFactors(mulExpr.getRight()));
        } else {
            list.add(expr);
        }
        return list;
    }

    public Expression Simplify() {
        Expression simplifiedLeft = left.Simplify();
        Expression simplifiedRight = right.Simplify();

        if (simplifiedRight instanceof Constant && simplifiedRight.evaluate(0) == 0) {
            System.out.println("Error: Division by zero!");
            return new Constant(0);
        }
        if (simplifiedLeft instanceof Constant && simplifiedLeft.evaluate(0) == 0) {
            return new Constant(0);
        }

        ArrayList<Expression> numeratorFactors = getFactors(simplifiedLeft);
        ArrayList<Expression> denominatorFactors = getFactors(simplifiedRight);

        Fraction totalConstant = new Fraction(1, 1);
        HashMap<Expression, Expression> map = new HashMap<>();

        for (Expression factor : numeratorFactors) {
            if (factor instanceof Fraction) {
                totalConstant = totalConstant.multiply((Fraction) factor);
                continue;
            } else if (factor instanceof Constant) {
                long val = (long) factor.evaluate(0);
                totalConstant = totalConstant.multiply(new Fraction(val, 1));
                continue;
            }
            PowerInfo info = ProCalculator.extractPower(factor);
            if (map.containsKey(info.base)) {
                Expression oldExp = map.get(info.base);
                Expression newExp = new Addition(oldExp, info.exponent).Simplify();
                map.put(info.base, newExp);
            } else {
                map.put(info.base, info.exponent);
            }
        }

        for (Expression factor : denominatorFactors) {
            if (factor instanceof Fraction) {
                Fraction f = (Fraction) factor;
                // در مخرج، کسر باید معکوس بشه و ضرب بشه
                Fraction inverse = new Fraction(f.getDenominator(), f.getNumerator());
                totalConstant = totalConstant.multiply(inverse);
                continue;
            } else if (factor instanceof Constant) {
                long val = (long) factor.evaluate(0);
                totalConstant = totalConstant.multiply(new Fraction(1, val));
                continue;
            }
            PowerInfo info = ProCalculator.extractPower(factor);
            if (map.containsKey(info.base)) {
                Expression oldExp = map.get(info.base);
                Expression newExp = new Subtraction(oldExp, info.exponent).Simplify();
                map.put(info.base, newExp);
            } else {
                Expression negativeExp = new Multiplication(new Constant(-1), info.exponent).Simplify();
                map.put(info.base, negativeExp);
            }
        }

        if (totalConstant.getNumerator() == 0) {
            return new Constant(0);
        }

        ArrayList<Expression> finalFactors = new ArrayList<>();
        if (totalConstant.getNumerator() != totalConstant.getDenominator() || map.isEmpty()) {
            finalFactors.add(totalConstant);
        }

        for (Map.Entry<Expression, Expression> entry : map.entrySet()) {
            Expression base = entry.getKey();
            Expression exp = entry.getValue();

            if (exp instanceof Constant && exp.evaluate(0) == 0) {
                continue;
            } else if (exp instanceof Constant && exp.evaluate(0) == 1) {
                finalFactors.add(base);
            } else {
                finalFactors.add(new Power(base, exp));
            }
        }

        if (finalFactors.isEmpty()) {
            return new Constant(1);
        }
        Expression result = finalFactors.get(0);
        for (int i = 1; i < finalFactors.size(); i++) {
            result = new Multiplication(result, finalFactors.get(i));
        }

        return result;
    }

    public Expression derive() {
        Expression first = new Multiplication(left.derive(), right);
        Expression second = new Multiplication(left, right.derive());
        Expression top = new Subtraction(first, second);
        Expression bottom = new Power(right, new Constant(2));
        return new Division(top, bottom);
    }

    public Expression integrate() {
        return null;
    }

    public String print() {
        String leftStr = left.print();
        String rightStr = right.print();
        if (rightStr.equalsIgnoreCase("0")) {
            return "Infinity (Division by Zero)";
        }
        if (ProCalculator.isNumber(leftStr) && ProCalculator.isNumber(rightStr)) {
            return ProCalculator.format(this.evaluate(0));
        }
        return "(" + leftStr + "/" + rightStr + ")";
    }
}

class Power implements Expression {
    private Expression base;
    private Expression power;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Power powerTerm = (Power) obj;
        return (this.base.equals(powerTerm.base) && this.power.equals(powerTerm.power));
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.base, this.power);
    }

    public Power(Expression base, Expression power) {
        this.base = base;
        this.power = power;
    }

    public double evaluate(double x) {
        return Math.pow(base.evaluate(x), power.evaluate(x));
    }

    public Expression getBase() {
        return this.base;
    }

    public Expression getPower() {
        return this.power;
    }

    public Expression Simplify() {
        Expression simplifiedBase = base.Simplify();
        Expression simplifiedPower = power.Simplify();
        if (simplifiedBase instanceof Constant && simplifiedPower instanceof Constant) {
            double result = Math.pow(simplifiedBase.evaluate(0), simplifiedPower.evaluate(0));
            return new Constant(result);
        }
        if (simplifiedPower instanceof Constant && simplifiedPower.evaluate(0) == 0) {
            return new Constant(1);
        }
        if (simplifiedBase instanceof Constant && simplifiedPower.evaluate(0) == 0) {
            return new Constant(1);
        }
        if (simplifiedPower instanceof Constant && simplifiedPower.evaluate(0) == 1) {
            return simplifiedBase;
        }
        if (simplifiedBase instanceof Constant && simplifiedBase.evaluate(0) == 1) {
            return new Constant(1);
        }
        return new Power(simplifiedBase, simplifiedPower);
    }

    public Expression derive() {
        Expression powerMinusOne = new Subtraction(power, new Constant(1));
        Expression newBase = new Power(base, powerMinusOne);
        Expression step1 = new Multiplication(power, newBase);
        return new Multiplication(step1, base.derive());
    }

    public Expression integrate() {
        if (base instanceof Variable && power instanceof Constant) {
            double n = power.evaluate(0);
            if (n == -1) {
                return new Ln(base);
            } else {
                Expression newPow = new Addition(power, new Constant(1));
                Expression pow = new Power(base, newPow);
                return new Division(pow, newPow);
            }
        } else if (base instanceof Constant && power instanceof Variable) {
            Expression denominator = new Ln(base);
            return new Division(this, denominator);
        } else {
            return null;
        }
    }

    public String print() {
        String baseStr = base.print();
        String powerStr = power.print();
        if (ProCalculator.isNumber(powerStr) && ProCalculator.isNumber(baseStr)) {
            return ProCalculator.format(this.evaluate(0));
        }
        if (powerStr.startsWith("-")) {
            String positivePower = powerStr.substring(1);
            if (positivePower.equalsIgnoreCase("1")) {
                return "(1 / " + baseStr + ")";
            } else {
                return "(1 / (" + baseStr + " ^ " + positivePower + "))";
            }
        } else {
            return "(" + baseStr + " ^ " + powerStr + ")";
        }
    }
}

class Sin implements Expression {

    private Expression argument;

    public Sin(Expression argument) {
        this.argument = argument;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Sin otherSin = (Sin) obj;

        return argument.equals(otherSin.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("sin" + argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (ProCalculator.isDegreeMode) {
            return Math.sin(Math.toRadians(val));
        } else {
            return Math.sin(val);
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        if (simplifiedArg instanceof Constant) {
            double result = this.evaluate(0);
            Constant finalAnswer = new Constant(result);
            return finalAnswer;
        }
        return new Sin(simplifiedArg);
    }

    public Expression derive() {
        return new Multiplication(new Cos(argument), argument.derive());
    }

    public Expression integrate() {
        Cos cosArg = new Cos(argument);
        return new Multiplication(new Constant(-1), cosArg);
    }

    public String print() {
        String argStr = argument.print();

        if (ProCalculator.isNumber(argStr)) {
            return ProCalculator.format(this.evaluate(0));
        } else {
            return ("sin(" + argStr + ")");
        }
    }
}

class Cos implements Expression {
    private Expression argument;

    public Cos(Expression argument) {
        this.argument = argument;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Cos otherCos = (Cos) obj;

        return argument.equals(otherCos.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("cos" + argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (ProCalculator.isDegreeMode) {
            return Math.cos(Math.toRadians(val));
        } else {
            return Math.cos(val);
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        if (simplifiedArg instanceof Constant) {
            double result = this.evaluate(0);
            Constant finalAnswer = new Constant(result);
            return finalAnswer;
        }
        return new Cos(simplifiedArg);
    }

    public Expression derive() {
        Expression negative = new Constant(-1);
        Expression sinNode = new Sin(argument);
        Expression step1 = new Multiplication(negative, sinNode);
        return new Multiplication(step1, argument.derive());
    }

    public Expression integrate() {
        return new Sin(argument);
    }

    public String print() {
        String argStr = argument.print();

        if (ProCalculator.isNumber(argStr)) {
            return ProCalculator.format(this.evaluate(0));
        } else {
            return ("cos(" + argStr + ")");
        }
    }
}

class Tan implements Expression {
    private Expression argument;

    public Tan(Expression argument) {
        this.argument = argument;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Tan otherTan = (Tan) obj;

        return argument.equals(otherTan.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("Tan" + argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (ProCalculator.isDegreeMode) {
            return Math.tan(Math.toRadians(val));
        } else {
            return Math.tan(val);
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        if (simplifiedArg instanceof Constant) {
            double result = this.evaluate(0);
            Constant finalAnswer = new Constant(result);
            return finalAnswer;
        }
        return new Tan(simplifiedArg);
    }

    public Expression derive() {
        Expression one = new Constant(1);
        Expression tanNode = new Tan(argument);
        Expression tan2 = new Power(tanNode, new Constant(2));
        Expression step1 = new Addition(tan2, one);
        return new Multiplication(step1, argument.derive());
    }

    public Expression integrate() {
        return null;
    }

    public String print() {
        String argStr = argument.print();

        if (ProCalculator.isNumber(argStr)) {
            return ProCalculator.format(this.evaluate(0));
        } else {
            return ("tan(" + argStr + ")");
        }
    }
}

class Cot implements Expression {
    private Expression argument;

    public Cot(Expression argument) {
        this.argument = argument;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Cot otherCot = (Cot) obj;

        return argument.equals(otherCot.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("cot" + argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (ProCalculator.isDegreeMode) {
            return (1 / (Math.tan(Math.toRadians(val))));
        } else {
            return (1 / (Math.tan(val)));
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        if (simplifiedArg instanceof Constant) {
            double result = this.evaluate(0);
            Constant finalAnswer = new Constant(result);
            return finalAnswer;
        }
        return new Cot(simplifiedArg);
    }

    public Expression derive() {
        Expression negative = new Constant(-1);
        Expression one = new Constant(1);
        Expression cotNode = new Cot(argument);
        Expression cot2 = new Power(cotNode, new Constant(2));
        Expression step1 = new Addition(cot2, one);
        Expression step2 = new Multiplication(negative, step1);
        return new Multiplication(step2, argument.derive());
    }

    public Expression integrate() {
        return null;
    }

    public String print() {
        String argStr = argument.print();

        if (ProCalculator.isNumber(argStr)) {
            return ProCalculator.format(this.evaluate(0));
        } else {
            return ("cot(" + argStr + ")");
        }
    }
}

class Sec implements Expression {
    private Expression argument;

    public Sec(Expression argument) {
        this.argument = argument;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Sec otherSec = (Sec) obj;

        return argument.equals(otherSec.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("sec" + argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (ProCalculator.isDegreeMode) {
            return (1 / (Math.cos(Math.toRadians(val))));
        } else {
            return (1 / (Math.cos(val)));
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        if (simplifiedArg instanceof Constant) {
            double result = this.evaluate(0);
            Constant finalAnswer = new Constant(result);
            return finalAnswer;
        }
        return new Sec(simplifiedArg);
    }

    public Expression derive() {
        Expression tanNode = new Tan(argument);
        Expression secNode = new Sec(argument);
        Expression step1 = new Multiplication(tanNode, secNode);
        return new Multiplication(step1, argument.derive());
    }

    public Expression integrate() {
        return null;
    }

    public String print() {
        String argStr = argument.print();

        if (ProCalculator.isNumber(argStr)) {
            return ProCalculator.format(this.evaluate(0));
        } else {
            return ("sec(" + argStr + ")");
        }
    }
}

class Csc implements Expression {
    private Expression argument;

    public Csc(Expression argument) {
        this.argument = argument;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Csc otherCsc = (Csc) obj;

        return argument.equals(otherCsc.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("csc" + argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (ProCalculator.isDegreeMode) {
            return (1 / (Math.sin(Math.toRadians(val))));
        } else {
            return (1 / (Math.sin(val)));
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        if (simplifiedArg instanceof Constant) {
            double result = this.evaluate(0);
            Constant finalAnswer = new Constant(result);
            return finalAnswer;
        }
        return new Csc(simplifiedArg);
    }

    public Expression derive() {
        Expression negative = new Constant(-1);
        Expression cotNode = new Cot(argument);
        Expression cscNode = new Csc(argument);
        Expression step1 = new Multiplication(cscNode, cotNode);
        Expression step2 = new Multiplication(negative, step1);
        return new Multiplication(step2, argument.derive());
    }

    public Expression integrate() {
        return null;
    }

    public String print() {
        String argStr = argument.print();

        if (ProCalculator.isNumber(argStr)) {
            return ProCalculator.format(this.evaluate(0));
        } else {
            return ("csc(" + argStr + ")");
        }
    }
}

class Ln implements Expression {
    private Expression argument;

    public Ln(Expression argument) {
        this.argument = argument;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Ln otherLn = (Ln) obj;

        return argument.equals(otherLn.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("ln" + argument);
    }

    public double evaluate(double x) {
        return Math.log(argument.evaluate(x));
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        if (simplifiedArg instanceof Constant) {
            double result = this.evaluate(0);
            Constant finalAnswer = new Constant(result);
            return finalAnswer;
        }
        return new Ln(simplifiedArg);
    }

    public Expression derive() {
        return new Division(argument.derive(), argument);
    }

    public Expression integrate() {
        return null;
    }

    public String print() {
        String argStr = argument.print();
        if (ProCalculator.isNumber(argStr)) {
            return ProCalculator.format(this.evaluate(0));
        } else {
            return ("ln(" + argument.print() + ")");
        }
    }
}

class Log10 implements Expression {
    private Expression argument;

    public Log10(Expression argument) {
        this.argument = argument;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Log10 otherLog10 = (Log10) obj;

        return argument.equals(otherLog10.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("log10" + argument);
    }

    public double evaluate(double x) {
        return Math.log10(argument.evaluate(x));
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        if (simplifiedArg instanceof Constant) {
            double result = this.evaluate(0);
            Constant finalAnswer = new Constant(result);
            return finalAnswer;
        }
        return new Log10(simplifiedArg);
    }

    public Expression derive() {
        Expression top = argument.derive();
        Expression bottom = new Multiplication(argument, new Constant(Math.log(10)));
        return new Division(top, bottom);
    }

    public Expression integrate() {
        return null;
    }

    public String print() {
        String argStr = argument.print();
        if (ProCalculator.isNumber(argStr)) {
            return ProCalculator.format(this.evaluate(0));
        } else {
            return ("log10(" + argument.print() + ")");
        }
    }
}

class TermInfo {
    public Fraction coefficient;
    public Expression core;

    public TermInfo(Fraction coefficient, Expression core) {
        this.coefficient = coefficient;
        this.core = core;
    }
}

class PowerInfo {
    public Expression base;
    public Expression exponent;

    public PowerInfo(Expression base, Expression exponent) {
        this.base = base;
        this.exponent = exponent;
    }
}
