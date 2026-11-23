package procalculator.ast;

import procalculator.ast.value.Fraction;

public class TermInfo {
    public Fraction coefficient;
    public Expression core;

    public TermInfo(Fraction coefficient, Expression core) {
        this.coefficient = coefficient;
        this.core = core;
    }
}