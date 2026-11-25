package procalculator.ast.function;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.operator.Division;
import procalculator.ast.operator.Multiplication;
import procalculator.ast.value.Fraction;
import procalculator.ast.value.Variable;

public class Cos implements Expression {
    private Expression argument;

    public Cos(Expression argument) {
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
        Cos otherCos = (Cos) obj;

        return argument.equals(otherCos.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("cos", argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (AstUtils.isDegreeMode) {
            return Math.cos(Math.toRadians(val));
        } else {
            return Math.cos(val);
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        Cos simplifiedNode = new Cos(simplifiedArg);
        if (AstUtils.isConstantNode(simplifiedArg) || AstUtils.isRationalMultipleOfPi(simplifiedArg)) {
            Fraction exact = AstUtils.toExactHalfInteger(simplifiedNode.evaluate(0));
            if (exact != null) {
                return exact;
            }
        }
        return simplifiedNode;
    }

    public Expression derive() {
        Expression negative = new Fraction(-1, 1);
        Expression sinNode = new Sin(argument);
        Expression step1 = new Multiplication(negative, sinNode);
        return new Multiplication(step1, argument.derive());
    }

    public Expression integrate() {
        Expression du = argument.derive().Simplify();
        if (AstUtils.isConstantNode(du)) {
            if (du.evaluate(0) == 0)
                return new Multiplication(this, new Variable());
            return new Division(new Sin(argument), du);
        }
        return null;
    }

    public String print() {
        String argStr = argument.print();

        if (AstUtils.isNumber(argStr)) {
            return AstUtils.format(this.evaluate(0));
        } else {
            return ("cos(" + argStr + ")");
        }
    }
}
