package procalculator.ast.value;

import procalculator.ast.Expression;
import procalculator.ast.operator.Multiplication;

public class Fraction implements Expression {
    private long numerator;
    private long denominator;

    public Fraction(long numerator, long denominator) {
        if (denominator == 0) {
            throw (new ArithmeticException("Division by zero"));
        } else if (denominator < 0) {
            this.numerator = -numerator;
            this.denominator = -denominator;
        } else {
            this.numerator = numerator;
            this.denominator = denominator;
        }
        long gcdNum = gcd(Math.abs(numerator), Math.abs(denominator));
        this.numerator = this.numerator / gcdNum;
        this.denominator = this.denominator / gcdNum;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Fraction otherFrac = (Fraction) obj;

        return this.numerator == otherFrac.numerator && this.denominator == otherFrac.denominator;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.numerator, this.denominator);
    }

    public long gcd(long a, long b) {
        if (b == 0) {
            return a;
        } else {
            return gcd(b, a % b);
        }
    }

    public Fraction add(Fraction other) {
        long newNum = (this.numerator * other.denominator) + (other.numerator * this.denominator);
        long newDen = this.denominator * other.denominator;
        return new Fraction(newNum, newDen);
    }

    public Fraction multiply(Fraction other) {
        long newNum = this.numerator * other.numerator;
        long newDen = this.denominator * other.denominator;
        return new Fraction(newNum, newDen);
    }

    public long getNumerator() {
        return this.numerator;
    }

    public long getDenominator() {
        return this.denominator;
    }

    public double evaluate(double x) {
        double doubleNumerator = (double) numerator;
        return doubleNumerator / denominator;
    }

    public Expression Simplify() {
        return this;
    }

    public Expression derive() {
        return new Fraction(0, 1);
    }

    public Expression integrate() {
        return new Multiplication(this, new Variable());
    }

    public String print() {
        if (denominator == 1) {
            String strNumerator = Long.toString(numerator);
            return strNumerator;
        } else {
            return "(" + numerator + "/" + denominator + ")";
        }

    }
}
