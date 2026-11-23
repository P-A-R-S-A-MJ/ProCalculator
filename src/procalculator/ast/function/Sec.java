package procalculator.ast.function;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.operator.Multiplication;
import procalculator.ast.value.Constant;
import procalculator.ast.value.Fraction;

public class Sec implements Expression {
    private Expression argument;

    public Sec(Expression argument) {
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
        Sec otherSec = (Sec) obj;

        return argument.equals(otherSec.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("sec", argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (AstUtils.isDegreeMode) {
            return (1 / (Math.cos(Math.toRadians(val))));
        } else {
            return (1 / (Math.cos(val)));
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        Sec simplifiedNode = new Sec(simplifiedArg);
        if (simplifiedArg instanceof Constant || simplifiedArg instanceof Fraction) {
            double result = simplifiedNode.evaluate(0);
            if (Double.isFinite(result) && Math.abs(result) < 1e12 && Math.abs(result - Math.round(result)) < 1e-9) {
                return new Fraction(Math.round(result), 1);
            }
        }
        return simplifiedNode;
    }

    public Expression derive() {
        Expression tanNode = new Tan(argument);
        Expression secNode = new Sec(argument);
        Expression step1 = new Multiplication(tanNode, secNode);
        return new Multiplication(step1, argument.derive());
    }

    public Expression integrate() {
        return null;
    }

    public String print() {
        String argStr = argument.print();

        if (AstUtils.isNumber(argStr)) {
            return AstUtils.format(this.evaluate(0));
        } else {
            return ("sec(" + argStr + ")");
        }
    }
}
