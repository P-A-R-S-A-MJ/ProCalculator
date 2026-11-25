package procalculator.ast;

import procalculator.ast.operator.Multiplication;
import procalculator.ast.operator.Power;
import procalculator.ast.value.Constant;
import procalculator.ast.value.Fraction;
import procalculator.ast.value.SymbolicConstant;
import procalculator.ast.value.Variable;

public class AstUtils {
    public static boolean isDegreeMode = false;

    public static String format(double num) {
        if (!Double.isFinite(num) || Math.abs(num) >= 1e11) {
            return String.valueOf(num);
        }
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

    public static TermInfo extractTerm(Expression expr) {
        if (expr instanceof Fraction) {
            return new TermInfo((Fraction) expr, new Fraction(1, 1));
        } else if (expr instanceof Multiplication) {
            Multiplication multi = (Multiplication) expr;
            if (multi.getLeft() instanceof Fraction) {
                return new TermInfo((Fraction) multi.getLeft(), multi.getRight());
            }
            if (multi.getRight() instanceof Fraction) {
                return new TermInfo((Fraction) multi.getRight(), multi.getLeft());
            }
        }
        return new TermInfo(new Fraction(1, 1), expr);
    }

    public static PowerInfo extractPower(Expression expr) {
        if (expr instanceof Power) {
            Power pow = (Power) expr;

            return new PowerInfo(pow.getBase(), pow.getPower());
        }

        return new PowerInfo(expr, new Fraction(1, 1));
    }

    public static double getDegree(Expression expr) {
        if (isConstantNode(expr)) {
            return 0;
        } else if (expr instanceof Variable) {
            return 1;
        } else if (expr instanceof Power) {
            Power pow = (Power) expr;
            if (isConstantNode(pow.getBase()) && isConstantNode(pow.getPower())) {
                return 0;
            }
            if (isConstantNode(pow.getPower())) {
                return pow.getPower().evaluate(0);
            }
        } else if (expr instanceof Multiplication) {
            Multiplication mul = (Multiplication) expr;
            return Math.max(getDegree(mul.getLeft()), getDegree(mul.getRight()));
        }
        return -100;
    }

    public static boolean isConstantNode(Expression expr) {
        return (expr instanceof Constant || expr instanceof Fraction || expr instanceof SymbolicConstant);
    }

    public static boolean isRationalMultipleOfPi(Expression expr) {
        TermInfo info = extractTerm(expr);
        return SymbolicConstant.PI.equals(info.core);
    }

    public static Fraction toExactHalfInteger(double result) {
        if (Double.isFinite(result) && Math.abs(result) < 1e12) {
            double doubled = result * 2.0;
            long roundedDoubled = Math.round(doubled);
            if (Math.abs(doubled - roundedDoubled) < 1e-9) {
                return new Fraction(roundedDoubled, 2);
            }
        }
        return null;
    }
}