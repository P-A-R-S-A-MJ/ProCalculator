package procalculator.ast.value;

import procalculator.ast.Expression;
import procalculator.ast.operator.Multiplication;
import procalculator.ast.operator.Power;

public class Variable implements Expression {
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
        return new Fraction(1, 1);
    }

    public Expression integrate() {
        Fraction half = new Fraction(1, 2);
        Power squared = new Power(this, new Fraction(2, 1));
        return new Multiplication(half, squared);
    }

    public String print() {
        return this.name;
    }
}
