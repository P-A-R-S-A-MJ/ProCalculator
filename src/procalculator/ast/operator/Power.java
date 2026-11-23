package procalculator.ast.operator;

import procalculator.ast.AstUtils;
import procalculator.ast.Expression;
import procalculator.ast.function.Ln;
import procalculator.ast.value.Constant;
import procalculator.ast.value.Fraction;
import procalculator.ast.value.Variable;

public class Power implements Expression {
    private Expression base;
    private Expression power;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Power powerTerm = (Power) obj;
        return (this.base.equals(powerTerm.base) && this.power.equals(powerTerm.power));
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.base, this.power);
    }

    public Power(Expression base, Expression power) {
        this.base = base;
        this.power = power;
    }

    public double evaluate(double x) {
        return Math.pow(base.evaluate(x), power.evaluate(x));
    }

    public Expression getBase() {
        return this.base;
    }

    public Expression getPower() {
        return this.power;
    }

    private boolean isZero(Expression expr) {
        if (expr instanceof Fraction)
            return ((Fraction) expr).getNumerator() == 0;
        if (expr instanceof Constant)
            return expr.evaluate(0) == 0;
        return false;
    }

    private boolean isOne(Expression expr) {
        if (expr instanceof Fraction) {
            Fraction f = (Fraction) expr;
            return f.getNumerator() == f.getDenominator();
        }
        if (expr instanceof Constant)
            return expr.evaluate(0) == 1;
        return false;
    }

    private long powLong(long base, long exp) {
        if (exp == 0) {
            return 1;
        }
        if (base == 0) {
            return 0;
        }
        if (base == 1) {
            return 1;
        }
        if (base == -1) {
            return (exp % 2 == 0) ? 1 : -1;
        }
        long res = 1;
        for (long i = 0; i < exp; i++) {
            res = Math.multiplyExact(res, base);
        }
        return res;
    }

    public Expression Simplify() {
        Expression simplifiedBase = base.Simplify();
        Expression simplifiedPower = power.Simplify();

        if (isZero(simplifiedPower)) {
            return new Fraction(1, 1);
        }
        if (isOne(simplifiedPower)) {
            return simplifiedBase;
        }
        if (isOne(simplifiedBase)) {
            return new Fraction(1, 1);
        }

        if (simplifiedBase instanceof Power) {
            Power inner = (Power) simplifiedBase;
            Expression combinedPower = new Multiplication(inner.getPower(), simplifiedPower).Simplify();
            return new Power(inner.getBase(), combinedPower).Simplify();
        }

        if (simplifiedBase instanceof Fraction && simplifiedPower instanceof Fraction) {
            Fraction bFrac = (Fraction) simplifiedBase;
            Fraction pFrac = (Fraction) simplifiedPower;

            long p = pFrac.getNumerator();
            long q = pFrac.getDenominator();

            if (bFrac.getNumerator() == 0) {
                if (p < 0) {
                    throw new ArithmeticException("Division by zero!");
                }
                return new Fraction(0, 1);
            }

            if (p < 0) {
                bFrac = new Fraction(bFrac.getDenominator(), bFrac.getNumerator());
                p = -p;
            }

            long a = bFrac.getNumerator();
            long b = bFrac.getDenominator();

            try {
                if (q == 1) {
                    return new Fraction(powLong(a, p), powLong(b, p));
                }

                if (a < 0 && q % 2 == 0) {
                    return new Power(simplifiedBase, simplifiedPower);
                }

                long sign = (a < 0) ? -1 : 1;
                long rootNum = Math.round(Math.pow(Math.abs(a), 1.0 / q));
                long rootDen = Math.round(Math.pow(b, 1.0 / q));

                if (powLong(rootNum, q) == Math.abs(a) && powLong(rootDen, q) == b) {
                    return new Fraction(powLong(sign * rootNum, p), powLong(rootDen, p));
                }
                return new Power(simplifiedBase, simplifiedPower);
            } catch (ArithmeticException e) {
                double approx = Math.pow(simplifiedBase.evaluate(0), simplifiedPower.evaluate(0));
                if (Double.isFinite(approx)) {
                    return new Constant(approx);
                }
                return new Power(simplifiedBase, simplifiedPower);
            }
        }

        if ((simplifiedBase instanceof Constant || simplifiedBase instanceof Fraction)
                && (simplifiedPower instanceof Constant || simplifiedPower instanceof Fraction)) {
            double baseVal = simplifiedBase.evaluate(0);
            double expVal = simplifiedPower.evaluate(0);
            if (baseVal == 0.0 && expVal < 0) {
                throw new ArithmeticException("Division by zero!");
            }
            double result = Math.pow(baseVal, expVal);
            if (Double.isFinite(result)) {
                return new Constant(result);
            }
        }
        return new Power(simplifiedBase, simplifiedPower);
    }

    public Expression derive() {
        Expression simpBase = base.Simplify();
        Expression simpPower = power.Simplify();
        if (simpPower instanceof Fraction || simpPower instanceof Constant) {
            Expression powerMinusOne = new Subtraction(simpPower, new Fraction(1, 1));
            Expression newBase = new Power(simpBase, powerMinusOne);
            Expression step1 = new Multiplication(simpPower, newBase);
            return new Multiplication(step1, simpBase.derive());
        } else if (simpBase instanceof Fraction || simpBase instanceof Constant) {
            Expression step1 = new Multiplication(new Power(simpBase, simpPower), new Ln(simpBase));
            return new Multiplication(step1, simpPower.derive());
        } else {
            Expression term1 = new Multiplication(simpPower.derive(), new Ln(simpBase));
            Expression term2 = new Division(new Multiplication(simpPower, simpBase.derive()), simpBase);
            return new Multiplication(new Power(simpBase, simpPower), new Addition(term1, term2));
        }
    }

    public Expression integrate() {
        if (base instanceof Variable && (power instanceof Constant || power instanceof Fraction)) {
            double n = power.evaluate(0);
            if (n == -1) {
                return new Ln(base);
            } else {
                Expression newPow = new Addition(power, new Fraction(1, 1)).Simplify();
                Expression pow = new Power(base, newPow);
                return new Division(pow, newPow);
            }
        } else if ((base instanceof Constant || base instanceof Fraction) && power instanceof Variable) {
            Expression denominator = new Ln(base);
            return new Division(this, denominator);
        } else {
            return null;
        }
    }

    public String print() {
        String baseStr = base.print();
        String powerStr = power.print();
        if (AstUtils.isNumber(powerStr) && AstUtils.isNumber(baseStr)) {
            return AstUtils.format(this.evaluate(0));
        }
        if (powerStr.startsWith("-")) {
            String positivePower = powerStr.substring(1);
            if (positivePower.equalsIgnoreCase("1")) {
                return "(1 / " + baseStr + ")";
            } else {
                return "(1 / (" + baseStr + " ^ " + positivePower + "))";
            }
        } else {
            return "(" + baseStr + " ^ " + powerStr + ")";
        }
    }
}
