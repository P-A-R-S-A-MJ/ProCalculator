# ProCalculator

**ProCalculator** is a command-line symbolic mathematics and calculus engine built from scratch in **pure Java** with zero external dependencies. Powered by a custom tokenizer, expression parser, and modular Abstract Syntax Tree (AST), it performs exact algebraic simplification, symbolic differentiation and integration, numerical definite integration, and single-variable equation solving.

## Features

**Symbolic Simplification & Exact Arithmetic:** Simplifies expressions by combining like terms, applying polynomial factor ordering, and preserving exact rational fractions without floating-point precision loss.
- **Symbolic Constants (`pi` & `e`):** Maintains exact symbolic representations for `π` and `e` across algebra and calculus—supporting exact trigonometric evaluation at rational multiples of `π` (e.g., `sin(pi/2) = 1`, `sin(pi/6) = 1/2`) and logarithmic/exponential identities (`ln(e) = 1`, `ln(e^x) = x`, `e^ln(x) = x`).
- **Calculus Engine:**
  - **Symbolic Differentiation:** Supports power, exponential, logarithmic, trigonometric, product, quotient, and chain rules.
  - **Indefinite Integration:** Computes antiderivatives for polynomials, exponentials, trigonometric functions with linear substitution, integration by parts (e.g., `integral x*e^x`), and constant-factor products (e.g., `integral pi*e*x`).
  - **Definite Integration:** Approximates the area under curves numerically across user-defined intervals `[a, b]`.
- **Equation Solver & Verification:** Numerically solves single-variable equations within default or custom bounds and evaluates numerical equalities (`True` / `False`).
- **Smart Parsing & CLI Experience:** Automatically detects implicit multiplication (e.g., `2pi`, `e(x+1)`, `3sin(2x)`), supports both Radian (`R`) and Degree (`D`) modes, works with arbitrary single-letter variables beyond `x`, and validates syntax such as unbalanced parentheses.

## Examples
`ln(e^x) + e^ln(x)` -> 2x

`sin(pi/2) + cos(pi) + sin(pi/6)` -> `(1/2)`

`(e^(2x) + sin(3x))'` -> `2(e ^ 2x)+3cos(3x)`

`integral x*e^x` -> `(x*(e ^ x))-(e ^ x) + C`

`integral pi*e*x` -> `(1/2)*(e*(pi*(x ^ 2))) + C`

`x^2 - 4 = 0` -> `x1 = -2, x2 = 2`

## Project Architecture

```text
src/procalculator/
├── ProCalculator.java            # CLI entry point & interactive session controller
├── ast/
│   ├── Expression.java           # Core AST node interface (evaluate, Simplify, derive, integrate, print)
│   ├── AstUtils.java             # Shared AST helpers, degree analysis, and exact fraction utilities
│   ├── PowerInfo.java            # Base/exponent extraction helper
│   ├── TermInfo.java             # Coefficient/core term extraction helper
│   ├── value/
│   │   ├── Constant.java         # Floating-point constant node
│   │   ├── Fraction.java         # Exact rational fraction node with automatic GCD reduction
│   │   ├── SymbolicConstant.java # Symbolic constants (pi, e) with exact double evaluation
│   │   └── Variable.java         # Variable node
│   ├── operator/
│   │   ├── Addition.java
│   │   ├── Subtraction.java
│   │   ├── Multiplication.java
│   │   ├── Division.java
│   │   └── Power.java
│   └── function/
│       ├── Sin.java, Cos.java, Tan.java, Cot.java, Sec.java, Csc.java
│       ├── Ln.java, Log10.java
│       └── RootN.java
├── parser/
│   ├── Tokenizer.java            # Lexer & implicit multiplication handler
│   └── ExpressionParser.java     # Recursive-descent parser producing AST nodes
└── engine/
    └── NumericalEngine.java      # Root-finding solver & numerical definite integration
```

- **`procalculator.ast`:** Defines the `Expression` contract and all concrete AST nodes (`value`, `operator`, `function`) responsible for symbolic simplification, differentiation, and indefinite integration.
- **`procalculator.parser`:** Converts raw user input into normalized tokens, inserts implicit multiplication operators, validates syntax, and constructs the AST.
- **`procalculator.engine`:** Handles numerical methods, including single-variable root-finding and definite integral approximation.
- **`procalculator.ProCalculator`:** Manages the CLI loop, arbitrary variable detection, angle mode selection (`R`/`D`), and routing to the calculation, calculus, or solver engines.

## Getting Started

### Prerequisites

- **Java Development Kit (JDK) 8 or higher**

### Compile & Run

Clone the repository, compile all source files into `bin/`, and launch the CLI:

```bash
# Clone the repository
git clone [https://github.com/](https://github.com/)<your-username>/ProCalculator.git
cd ProCalculator

# Compile (Linux / macOS / Git Bash)
javac -d bin $(find src -name "*.java")

# Run
java -cp bin procalculator.ProCalculator
```

### Quick Usage Guide

- **Simplify an expression:** Enter any algebraic or trigonometric expression directly (e.g., `2pi + 3pi`, `ln(e^x) + e^ln(x)`).
- **Differentiate:** Append `'` to an expression (e.g., `(e^(2x) + sin(3x))'`).
- **Integrate:** Prefix an expression with `integral` (e.g., `integral x*e^x`), then select **`I`** for indefinite or **`D`** for definite integration.
- **Solve equations / verify equalities:** Use `=` (e.g., `x^2 - 4 = 0` or `pi = 3.1415926536`).
- **Exit:** Type `end` to close the program.

## Roadmap

- [ ] Upgrade numerical definite integration from Riemann sums to **Simpson's Rule** for higher precision
- [ ] Add automated unit tests with **JUnit 5** covering simplification and calculus edge cases
- [ ] Improve `print()` operator-precedence formatting to eliminate redundant parentheses
- [ ] Expand symbolic integration with non-linear substitution and partial fractions
- [ ] Add radical simplification and combining of like radical terms