package procalculator.ast.function;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.operator.Addition;
import procalculator.ast.operator.Multiplication;
import procalculator.ast.operator.Power;
import procalculator.ast.value.Fraction;

public class Cot implements Expression {
    private Expression argument;

    public Cot(Expression argument) {
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
        Cot otherCot = (Cot) obj;

        return argument.equals(otherCot.argument);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash("cot", argument);
    }

    public double evaluate(double x) {
        double val = argument.evaluate(x);
        if (AstUtils.isDegreeMode) {
            return (1 / (Math.tan(Math.toRadians(val))));
        } else {
            return (1 / (Math.tan(val)));
        }
    }

    public Expression Simplify() {
        Expression simplifiedArg = argument.Simplify();
        Cot simplifiedNode = new Cot(simplifiedArg);
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
        Expression one = new Fraction(1, 1);
        Expression cotNode = new Cot(argument);
        Expression cot2 = new Power(cotNode, new Fraction(2, 1));
        Expression step1 = new Addition(cot2, one);
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
            return ("cot(" + argStr + ")");
        }
    }
}
