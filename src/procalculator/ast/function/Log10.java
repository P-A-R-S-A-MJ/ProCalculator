package procalculator.ast.function;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.operator.Division;
import procalculator.ast.operator.Multiplication;
import procalculator.ast.value.Constant;
import procalculator.ast.value.Fraction;

public class Log10 implements Expression {
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
        return java.util.Objects.hash("log10", argument);
    }

    public double evaluate(double x) {
        return Math.log10(argument.evaluate(x));
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        Log10 simplifiedNode = new Log10(simplifiedArg);
        if (simplifiedArg instanceof Constant || simplifiedArg instanceof Fraction) {
            double result = simplifiedNode.evaluate(0);
            if (Double.isFinite(result) && Math.abs(result) < 1e12 && Math.abs(result - Math.round(result)) < 1e-9) {
                return new Fraction(Math.round(result), 1);
            }
        }
        return simplifiedNode;
    }

    public Expression derive() {
        Expression top = argument.derive();
        Expression bottom = new Multiplication(argument, new Ln(new Fraction(10, 1)));
        return new Division(top, bottom);
    }

    public Expression integrate() {
        return null;
    }

    public String print() {
        String argStr = argument.print();
        if (AstUtils.isNumber(argStr)) {
            return AstUtils.format(this.evaluate(0));
        } else {
            return ("log10(" + argument.print() + ")");
        }
    }
}
