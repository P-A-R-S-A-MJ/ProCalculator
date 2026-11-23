package procalculator.ast.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.PowerInfo;
import procalculator.ast.value.Constant;
import procalculator.ast.value.Fraction;

public class Division implements Expression {
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

        if ((simplifiedRight instanceof Constant && simplifiedRight.evaluate(0) == 0) ||
                (simplifiedRight instanceof Fraction && ((Fraction) simplifiedRight).getNumerator() == 0)) {
            throw new ArithmeticException("Division by zero!");
        }
        if ((simplifiedLeft instanceof Constant && simplifiedLeft.evaluate(0) == 0) ||
                (simplifiedLeft instanceof Fraction && ((Fraction) simplifiedLeft).getNumerator() == 0)) {
            return new Fraction(0, 1);
        }

        ArrayList<Expression> numeratorFactors = getFactors(simplifiedLeft);
        ArrayList<Expression> denominatorFactors = getFactors(simplifiedRight);

        Fraction totalConstant = new Fraction(1, 1);
        double floatConstant = 1.0;
        boolean hasFloat = false;
        HashMap<Expression, Expression> map = new HashMap<>();

        for (Expression factor : numeratorFactors) {
            if (factor instanceof Fraction) {
                totalConstant = totalConstant.multiply((Fraction) factor);
                continue;
            } else if (factor instanceof Constant) {
                floatConstant *= factor.evaluate(0);
                hasFloat = true;
                continue;
            }
            PowerInfo info = AstUtils.extractPower(factor);
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
                Fraction inverse = new Fraction(f.getDenominator(), f.getNumerator());
                totalConstant = totalConstant.multiply(inverse);
                continue;
            } else if (factor instanceof Constant) {
                double val = factor.evaluate(0);
                if (val == 0.0) {
                    throw new ArithmeticException("Division by zero!");
                }
                floatConstant /= val;
                hasFloat = true;
                continue;
            }
            PowerInfo info = AstUtils.extractPower(factor);
            if (map.containsKey(info.base)) {
                Expression oldExp = map.get(info.base);
                Expression newExp = new Subtraction(oldExp, info.exponent).Simplify();
                map.put(info.base, newExp);
            } else {
                Expression negativeExp = new Multiplication(new Fraction(-1, 1), info.exponent).Simplify();
                map.put(info.base, negativeExp);
            }
        }

        if (totalConstant.getNumerator() == 0 || (hasFloat && floatConstant == 0.0)) {
            return new Fraction(0, 1);
        }

        ArrayList<Expression> finalFactors = new ArrayList<>();
        if (hasFloat) {
            double combined = totalConstant.evaluate(0) * floatConstant;
            if (combined != 1.0 || map.isEmpty()) {
                finalFactors.add(new Constant(combined));
            }
        } else if (totalConstant.getNumerator() != totalConstant.getDenominator() || map.isEmpty()) {
            finalFactors.add(totalConstant);
        }

        for (Map.Entry<Expression, Expression> entry : map.entrySet()) {
            Expression base = entry.getKey();
            Expression exp = entry.getValue();

            if ((exp instanceof Constant && exp.evaluate(0) == 0) ||
                    (exp instanceof Fraction && ((Fraction) exp).getNumerator() == 0)) {
                continue;
            } else if ((exp instanceof Constant && exp.evaluate(0) == 1) ||
                    (exp instanceof Fraction && ((Fraction) exp).getNumerator() == ((Fraction) exp).getDenominator())) {
                finalFactors.add(base);
            } else {
                finalFactors.add(new Power(base, exp));
            }
        }

        if (finalFactors.isEmpty()) {
            return new Fraction(1, 1);
        }
        Expression result = finalFactors.get(finalFactors.size() - 1);
        for (int i = finalFactors.size() - 2; i >= 0; i--) {
            result = new Multiplication(finalFactors.get(i), result);
        }

        return result.Simplify();
    }

    public Expression derive() {
        Expression first = new Multiplication(left.derive(), right);
        Expression second = new Multiplication(left, right.derive());
        Expression top = new Subtraction(first, second);
        Expression bottom = new Power(right, new Fraction(2, 1));
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
        if (AstUtils.isNumber(leftStr) && AstUtils.isNumber(rightStr)) {
            return AstUtils.format(this.evaluate(0));
        }
        return "(" + leftStr + "/" + rightStr + ")";
    }
}