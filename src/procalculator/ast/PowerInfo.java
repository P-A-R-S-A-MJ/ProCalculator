package procalculator.ast;

public class PowerInfo {
    public Expression base;
    public Expression exponent;

    public PowerInfo(Expression base, Expression exponent) {
        this.base = base;
        this.exponent = exponent;
    }
}