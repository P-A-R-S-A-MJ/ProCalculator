package procalculator.ast.function;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.operator.Division;
import procalculator.ast.value.Constant;
import procalculator.ast.value.Fraction;

public class Ln implements Expression {
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
        return java.util.Objects.hash("ln", argument);
    }

    public double evaluate(double x) {
        return Math.log(argument.evaluate(x));
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        Ln simplifiedNode = new Ln(simplifiedArg);
        if (simplifiedArg instanceof Constant || simplifiedArg instanceof Fraction) {
            double result = simplifiedNode.evaluate(0);
            if (Double.isFinite(result) && Math.abs(result) < 1e12 && Math.abs(result - Math.round(result)) < 1e-9) {
                return new Fraction(Math.round(result), 1);
            }
        }
        return simplifiedNode;
    }

    public Expression derive() {
        return new Division(argument.derive(), argument);
    }

    public Expression integrate() {
        return null;
    }

    public String print() {
        String argStr = argument.print();
        if (AstUtils.isNumber(argStr)) {
            return AstUtils.format(this.evaluate(0));
        } else {
            return ("ln(" + argument.print() + ")");
        }
    }
}
