package procalculator.ast.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.PowerInfo;
import procalculator.ast.value.Constant;
import procalculator.ast.value.Fraction;
import procalculator.ast.value.Variable;
import procalculator.ast.value.SymbolicConstant;

public class Multiplication implements Expression {
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

    private boolean isPolynomialPart(Expression expr) {
        if (expr instanceof Variable) {
            return true;
        }
        if (expr instanceof Power) {
            Power p = (Power) expr;
            if (p.getBase() instanceof Variable && p.getPower() instanceof Fraction) {
                Fraction f = (Fraction) p.getPower();
                return f.getDenominator() == 1 && f.getNumerator() > 0;
            }
        }
        return false;
    }

    private int getFactorRank(Expression expr) {
        if (expr instanceof SymbolicConstant) {
            return 0;
        } else if (isPolynomialPart(expr)) {
            return 1;
        } else {
            return 2;
        }
    }

    private boolean isConstantFactor(Expression expr) {
        if (AstUtils.isConstantNode(expr)) {
            return true;
        }
        Expression d = expr.derive().Simplify();
        return ((d instanceof Fraction && ((Fraction) d).getNumerator() == 0)
                || (d instanceof Constant && d.evaluate(0) == 0));
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
        double floatConstant = 1.0;
        boolean hasFloat = false;
        HashMap<Expression, Expression> map = new HashMap<>();

        for (Expression factor : allFactors) {
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

        if (totalConstant.getNumerator() == 0 || (hasFloat && floatConstant == 0.0)) {
            return new Fraction(0, 1);
        }

        ArrayList<Expression> variableFactors = new ArrayList<>();
        for (Map.Entry<Expression, Expression> entry : map.entrySet()) {
            Expression base = entry.getKey();
            Expression exp = entry.getValue();
            if ((exp instanceof Constant && exp.evaluate(0) == 0) ||
                    (exp instanceof Fraction && ((Fraction) exp).getNumerator() == 0)) {
                continue;
            } else if ((exp instanceof Constant && exp.evaluate(0) == 1) ||
                    (exp instanceof Fraction && ((Fraction) exp).getNumerator() == ((Fraction) exp).getDenominator())) {
                variableFactors.add(base);
            } else {
                variableFactors.add(new Power(base, exp));
            }
        }

        variableFactors.sort((e1, e2) -> {
            int rank1 = getFactorRank(e1);
            int rank2 = getFactorRank(e2);
            if (rank1 != rank2) {
                return Integer.compare(rank1, rank2);
            }
            return e1.print().compareTo(e2.print());
        });

        ArrayList<Expression> finalFactors = new ArrayList<>();
        if (hasFloat) {
            double combined = totalConstant.evaluate(0) * floatConstant;
            if (combined != 1.0 || variableFactors.isEmpty()) {
                finalFactors.add(new Constant(combined));
            }
        } else if (totalConstant.getNumerator() != totalConstant.getDenominator() || variableFactors.isEmpty()) {
            finalFactors.add(totalConstant);
        }
        finalFactors.addAll(variableFactors);

        if (finalFactors.isEmpty()) {
            return new Fraction(1, 1);
        }
        Expression result = finalFactors.get(finalFactors.size() - 1);
        for (int i = finalFactors.size() - 2; i >= 0; i--) {
            result = new Multiplication(finalFactors.get(i), result);
        }

        return result;
    }

    public Expression derive() {
        Expression derive1 = new Multiplication(left.derive(), right);
        Expression derive2 = new Multiplication(left, right.derive());
        return new Addition(derive1, derive2);
    }

    public Expression integrate() {
        if (isConstantFactor(left)) {
            Expression rightInt = right.integrate();
            if (rightInt != null)
                return new Multiplication(left, rightInt);
        } else if (isConstantFactor(right)) {
            Expression leftInt = left.integrate();
            if (leftInt != null)
                return new Multiplication(right, leftInt);
        }
        Expression u = null;
        Expression dv = null;
        if (isPolynomialPart(left) && !isPolynomialPart(right)) {
            u = left;
            dv = right;
        } else if (isPolynomialPart(right) && !isPolynomialPart(left)) {
            u = right;
            dv = left;
        }
        if (u != null && dv != null) {
            Expression v = dv.integrate();
            if (v != null) {
                Expression du = u.derive();

                Expression uv = new Multiplication(u, v);
                Expression v_du = new Multiplication(v, du).Simplify();

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
        if (AstUtils.isNumber(leftStr) && AstUtils.isNumber(rightStr)) {
            return AstUtils.format(this.evaluate(0));
        }
        if (leftStr.equals("1")) {
            return rightStr;
        } else if (rightStr.equals("1")) {
            return leftStr;
        } else if (leftStr.equals("-1")) {
            return "-" + rightStr;
        } else if (rightStr.equals("-1")) {
            return "-" + leftStr;
        } else if (AstUtils.isNumber(leftStr) && !AstUtils.isNumber(rightStr)) {
            return leftStr + rightStr;
        } else if (!AstUtils.isNumber(leftStr) && AstUtils.isNumber(rightStr)) {
            return rightStr + leftStr;
        } else {
            return "(" + leftStr + "*" + rightStr + ")";
        }
    }
}
