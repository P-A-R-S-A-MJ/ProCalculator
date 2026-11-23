package procalculator.ast.value;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.operator.Multiplication;

public class Constant implements Expression {
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
        return new Fraction(0, 1);
    }

    public Expression integrate() {
        return new Multiplication(this, new Variable());
    }

    public String print() {
        return AstUtils.format(this.value);
    }

}