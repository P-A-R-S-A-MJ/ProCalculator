package procalculator.ast.value;

import java.util.Objects;
import procalculator.ast.Expression;
import procalculator.ast.operator.Multiplication;

public class SymbolicConstant implements Expression {
    public static final SymbolicConstant PI = new SymbolicConstant("pi", Math.PI);
    public static final SymbolicConstant E = new SymbolicConstant("e", Math.E);
    private final String symbol;
    private final double value;

    public SymbolicConstant(String symbol, double value) {
        this.symbol = symbol;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        SymbolicConstant other = (SymbolicConstant) obj;
        return this.symbol.equals(other.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.symbol);
    }

    public String getSymbol() {
        return this.symbol;
    }

    @Override
    public double evaluate(double x) {
        return this.value;
    }

    @Override
    public Expression Simplify() {
        return this;
    }

    @Override
    public Expression derive() {
        return new Fraction(0, 1);
    }

    @Override
    public Expression integrate() {
        return new Multiplication(this, new Variable());
    }

    @Override
    public String print() {
        return this.symbol;
    }
}