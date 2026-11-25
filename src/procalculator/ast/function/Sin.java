package procalculator.ast.function;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.operator.Division;
import procalculator.ast.operator.Multiplication;
import procalculator.ast.value.Fraction;
import procalculator.ast.value.Variable;

public class Sin implements Expression {

    private Expression argument;

    public Sin(Expression argument) {
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
        Sin otherSin = (Sin) obj;

        return argument.equals(otherSin.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("sin", argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (AstUtils.isDegreeMode) {
            return Math.sin(Math.toRadians(val));
        } else {
            return Math.sin(val);
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        Sin simplifiedNode = new Sin(simplifiedArg);
        if (AstUtils.isConstantNode(simplifiedArg) || AstUtils.isRationalMultipleOfPi(simplifiedArg)) {
            Fraction exact = AstUtils.toExactHalfInteger(simplifiedNode.evaluate(0));
            if (exact != null) {
                return exact;
            }
        }
        return simplifiedNode;
    }

    public Expression derive() {
        return new Multiplication(new Cos(argument), argument.derive());
    }

    public Expression integrate() {
        Expression du = argument.derive().Simplify();
        if (AstUtils.isConstantNode(du)) {
            if (du.evaluate(0) == 0)
                return new Multiplication(this, new Variable());
            return new Division(new Multiplication(new Fraction(-1, 1), new Cos(argument)), du);
        }
        return null;
    }

    public String print() {
        String argStr = argument.print();

        if (AstUtils.isNumber(argStr)) {
            return AstUtils.format(this.evaluate(0));
        } else {
            return ("sin(" + argStr + ")");
        }
    }
}
