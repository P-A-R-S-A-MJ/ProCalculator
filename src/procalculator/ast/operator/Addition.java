package procalculator.ast.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.TermInfo;
import procalculator.ast.value.Constant;
import procalculator.ast.value.Fraction;

public class Addition implements Expression {
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
        Addition tempAdd = new Addition(simplifiedLeft, simplifiedRight);
        ArrayList<Expression> allTerms = getSummands(tempAdd);
        HashMap<Expression, Fraction> map = new HashMap<>();
        double floatSum = 0.0;
        boolean hasFloatSum = false;

        for (Expression term : allTerms) {
            if (term instanceof Constant) {
                floatSum += term.evaluate(0);
                hasFloatSum = true;
                continue;
            }
            TermInfo info = AstUtils.extractTerm(term);
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
                if (hasFloatSum) {
                    floatSum += coeff.evaluate(0);
                } else {
                    finalTerms.add(coeff);
                }
            } else if (coeff.getNumerator() == coeff.getDenominator()) {
                finalTerms.add(core);
            } else {
                finalTerms.add(new Multiplication(coeff, core));
            }
        }
        if (hasFloatSum && floatSum != 0.0) {
            finalTerms.add(new Constant(floatSum));
        }
        finalTerms.sort((expr1, expr2) -> {
            double degree1 = AstUtils.getDegree(expr1);
            double degree2 = AstUtils.getDegree(expr2);
            return Double.compare(degree2, degree1);
        });
        if (finalTerms.isEmpty()) {
            return new Fraction(0, 1);
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

        if (AstUtils.isNumber(leftStr) && AstUtils.isNumber(rightStr)) {
            return AstUtils.format(this.evaluate(0));
        }

        if (rightStr.startsWith("-")) {
            return "(" + leftStr + rightStr + ")";
        }

        return "(" + leftStr + "+" + rightStr + ")";
    }
}