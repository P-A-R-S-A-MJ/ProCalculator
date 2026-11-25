package procalculator.ast.function;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.operator.Multiplication;
import procalculator.ast.value.Fraction;

public class Csc implements Expression {
    private Expression argument;

    public Csc(Expression argument) {
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
        Csc otherCsc = (Csc) obj;

        return argument.equals(otherCsc.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("csc", argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (AstUtils.isDegreeMode) {
            return (1 / (Math.sin(Math.toRadians(val))));
        } else {
            return (1 / (Math.sin(val)));
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        Csc simplifiedNode = new Csc(simplifiedArg);
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
        Expression cotNode = new Cot(argument);
        Expression cscNode = new Csc(argument);
        Expression step1 = new Multiplication(cscNode, cotNode);
        Expression step2 = new Multiplication(negative, step1);
        return new Multiplication(step2, argument.derive());
    }

    public Expression integrate() {
        return null;
    }

    public String print() {
        String argStr = argument.print();

        if (AstUtils.isNumber(argStr)) {
            return AstUtils.format(this.evaluate(0));
        } else {
            return ("csc(" + argStr + ")");
        }
    }
}
