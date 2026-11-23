package procalculator.ast.operator;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.value.Constant;
import procalculator.ast.value.Fraction;
public class Subtraction implements Expression {
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
            return new Multiplication(new Fraction(-1, 1), simplifiedRight).Simplify();
        }
        Fraction minus = new Fraction(-1, 1);
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

        return new Subtraction(leftInt, rightInt);
    }

    public String print() {
        String leftStr = left.print();
        String rightStr = right.print();
        if (AstUtils.isNumber(leftStr) && AstUtils.isNumber(rightStr)) {
            return AstUtils.format(this.evaluate(0));
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