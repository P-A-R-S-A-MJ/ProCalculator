package procalculator.ast.function;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.operator.Addition;
import procalculator.ast.operator.Multiplication;
import procalculator.ast.operator.Power;
import procalculator.ast.value.Constant;
import procalculator.ast.value.Fraction;

public class Tan implements Expression {
    private Expression argument;

    public Tan(Expression argument) {
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
        Tan otherTan = (Tan) obj;

        return argument.equals(otherTan.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("Tan", argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (AstUtils.isDegreeMode) {
            return Math.tan(Math.toRadians(val));
        } else {
            return Math.tan(val);
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        Tan simplifiedNode = new Tan(simplifiedArg);
        if (simplifiedArg instanceof Constant || simplifiedArg instanceof Fraction) {
            double result = simplifiedNode.evaluate(0);
            if (Double.isFinite(result) && Math.abs(result) < 1e12 && Math.abs(result - Math.round(result)) < 1e-9) {
                return new Fraction(Math.round(result), 1);
            }
        }
        return simplifiedNode;
    }

    public Expression derive() {
        Expression one = new Fraction(1, 1);
        Expression tanNode = new Tan(argument);
        Expression tan2 = new Power(tanNode, new Fraction(2, 1));
        Expression step1 = new Addition(tan2, one);
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
            return ("tan(" + argStr + ")");
        }
    }
}
