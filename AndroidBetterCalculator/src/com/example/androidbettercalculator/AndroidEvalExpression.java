package com.example.androidbettercalculator;

/*
 * Course: CS 2302
 * Section: 02
 * Name: Barry Oliver
 * Professor: Alan Shaw
 * Assignment #: Homework 08
 */
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.EmptyStackException;
import java.util.Stack;

import android.content.Context;

public class AndroidEvalExpression {
	public final int SCALE = 10; // default number of places after decimal
	private String answer = ""; // stores the computed answer as a string

	/** Evaluate a given expression */
	public AndroidEvalExpression(Context context, String expression) {
		// Create operandStack to store operands
		Stack<BigDecimal> operandStack = new Stack<BigDecimal>();

		// Create operatorStack to store operators and (
		Stack<Character> operatorStack = new Stack<Character>();

		// Flags when the last token was an operator
		boolean lastTokWasOp = false;

		// if the expression is empty, then answer = 0 and return
		if (expression.trim().equals("")) {
			answer = context.getString(R.string.strNoValue);
			return;
		}

		// Insert blanks around (, ), +, -, /, and *
		expression = insertBlanks(expression);

		// Extract operands and operators
		String[] tokens = expression.trim().split("" + ' ' + '+');

		try {
			// Phase 1: Scan tokens
			for (String token : tokens) {
				token = token.trim();

				if (token.length() == 0) // Blank space
					continue; // Back to extract the next token

				else if (token.charAt(0) == '-' && !lastTokWasOp) {
					operatorStack.push('*');
					operandStack.push(new BigDecimal(-1));

				} else if (isOperator(token.charAt(0))) {
					// Process higher precedence (power) operators on top of operator stack
					while (!operatorStack.isEmpty()
							&& PowerPrec(operatorStack.peek(),
									token.charAt(0)))
						processAnOperator(operandStack, operatorStack);
					// Push operators into the operator stack
					operatorStack.push(token.charAt(0));
					lastTokWasOp = false;

				} else if (token.charAt(0) == '(') {
					operatorStack.push('('); // Push '(' to stack
					lastTokWasOp = false;

				} else if (token.charAt(0) == ')') {
					// Process all the operators in the stack until seeing '('
					while (operatorStack.peek() != '(')
						processAnOperator(operandStack, operatorStack);
					operatorStack.pop(); // Pop the '(' symbol from the stack
					lastTokWasOp = true;

				} else if (isOperator(token.charAt(0))) {
					// Process higher precedence (square root) operators on top of operator stack
					while (!operatorStack.isEmpty()	&& SquareRootPrec(token.charAt(0)))									
						
						processAnOperator(operandStack, operatorStack);
					// Push operators into the operator stack
					operatorStack.push(token.charAt(0));					
					lastTokWasOp = false;

				} else if (token.charAt(0) == '('){
					operatorStack.push('('); // Push '(' to stack
					lastTokWasOp = false;

				} else if (token.charAt(0) == ')') {
					// Process all the operators in the stack until seeing '('
					while (operatorStack.peek() != '(')
						processAnOperator(operandStack, operatorStack);
					operatorStack.pop(); // Pop the '(' symbol from the stack
					lastTokWasOp = true;

				} else if (isOperator(token.charAt(0))) {
					// Process high precedence operators on top of operator stack
					while (!operatorStack.isEmpty()&& higherPrec(operatorStack.peek(), token.charAt(0)))
						processAnOperator(operandStack, operatorStack);

					// Push operators operator into the operator stack
					operatorStack.push(token.charAt(0));
					lastTokWasOp = false;

				} else if (token.charAt(0) == '(') {
					operatorStack.push('('); // Push '(' to stack
					lastTokWasOp = false;

				} else if (token.charAt(0) == ')') {
					// Process all the operators in the stack until seeing '('
					while (operatorStack.peek() != '(')
						processAnOperator(operandStack, operatorStack);
					operatorStack.pop(); // Pop the '(' symbol from the stack
					lastTokWasOp = true;

				} else { // An operand scanned Push an operand to the stack
					operandStack.push(new BigDecimal(token));
					lastTokWasOp = true;
				}
			}

			// Phase 2: process all the remaining operators in the stack
			while (!operatorStack.isEmpty())
				processAnOperator(operandStack, operatorStack);

			// Phase 3: pop off the result
			answer = operandStack.pop().stripTrailingZeros().toPlainString();

		} catch (EmptyStackException ex) {
			answer = context.getString(R.string.strImproperEq);
		} catch (ArithmeticException ex) {
			answer = context.getString(R.string.strNoDivByZero);
		} catch (Exception ex) {
			answer = context.getString(R.string.strInvalidEq);
		}
	}

	/** Return the answer */
	public String getAnswer() {
		return answer;
	}

	/**
	 * Process one operator: Take an operator from operatorStack and apply it
	 * the operands on the operandStack
	 */
	private void processAnOperator(Stack<BigDecimal> operandStack,
			Stack<Character> operatorStack) {
		char op = operatorStack.pop();		

		if (op == '+'){
			BigDecimal op1 = operandStack.pop();
			BigDecimal op2 = operandStack.pop();
			operandStack.push(op2.add(op1));
		}else if (op == '-'){
			BigDecimal op1 = operandStack.pop();
			BigDecimal op2 = operandStack.pop();		
			operandStack.push(op2.subtract(op1));
		}else if (op == '*'){
			BigDecimal op1 = operandStack.pop();
			BigDecimal op2 = operandStack.pop();
			operandStack.push(op2.multiply(op1).setScale(SCALE,
					BigDecimal.ROUND_HALF_UP));
		}else if (op == '/'){
			BigDecimal op1 = operandStack.pop();
		BigDecimal op2 = operandStack.pop();
			operandStack.push(op2.divide(op1, SCALE, BigDecimal.ROUND_HALF_UP));
		}else if (op == '\u221a') {
			BigDecimal op1 = operandStack.pop();			
			operandStack.push(bigSqrt(op1));			
		} else if (op == '^'){
			BigDecimal op1 = operandStack.pop();
			BigDecimal op2 = operandStack.pop();		
			operandStack.push(bigPow(op2, op1));
	}
	}

	/** Determines if an character is an operator */
	private boolean isOperator(char val) {
		return (val == '+' || val == '-' || val == '*' || val == '/'
				|| val == '\u221a' || val == '^');
	}

	/**
	 * Determines if the first operator has higher precedence than the second
	 * operator or the same precedence but on left, so higher
	 */
	private boolean higherPrec(char op1, char op2) {
		return ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-'
				|| op2 == '*' || op2 == '/'));
	}

	/**
	 * Determines if the first operator has the highest precedence(power) 
	 * over the second operator or the same precedence but on left,
	 * so higher
	 */
	private boolean PowerPrec(char op1, char op2) {
		return ((op1 == '^') && (op2 == '+' || op2 == '-' || op2 == '*'
				|| op2 == '/' || op2 == '\u221a' || op2 == '^'));
	}
	/**
	 * Determines if the first operator has the highest precedence(square root)
	 * over the second operator or the same precedence but on left,
	 * so higher
	 */
	private boolean SquareRootPrec(char op1) {
		return (op1 == '\u221a');
	}

	/** Inserts spaces into an expression string between tokens */
	private String insertBlanks(String s) {
		String result = "";

		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '(' || s.charAt(i) == ')'
					|| isOperator(s.charAt(i)))
				result += " " + s.charAt(i) + " ";
			else
				result += s.charAt(i);
		}
		return result;
	}

	/** Returns the square root of a BigDecimal */
	private BigDecimal bigSqrt(BigDecimal num) {
		return bigPow(num, new BigDecimal(0.5), SCALE);
	}

	/**
	 * Returns the value of the first BigDecimal argument raised to the power of
	 * the second BigDecimal argument
	 */
	private BigDecimal bigPow(BigDecimal n1, BigDecimal n2) {
		return bigPow(n1, n2, SCALE);
	}

	/**
	 * Returns the value of the first BigDecimal argument raised to the power of
	 * the second BigDecimal argument to the given number of places after the
	 * decimal (the scale)
	 */
	private BigDecimal bigPow(BigDecimal n1, BigDecimal n2, int scale) {
		int signOf2 = n2.signum();
		double dn1 = n1.doubleValue(); // Perform X^(A+B)=X^A*X^B (B=fraction)

		n2 = n2.multiply(new BigDecimal(signOf2)); // n2 is now positive
		BigDecimal remOf2 = n2.remainder(BigDecimal.ONE);
		BigDecimal n2IntPart = n2.subtract(remOf2);
		BigDecimal intPow = n1.pow(n2IntPart.intValueExact(), new MathContext(
				1000, RoundingMode.HALF_UP));
		BigDecimal doublePow = new BigDecimal(Math.pow(dn1,
				remOf2.doubleValue()));
		BigDecimal result = intPow.multiply(doublePow);
		if (signOf2 == -1) // Negative power
			result = BigDecimal.ONE.divide(result, scale, RoundingMode.HALF_UP);
		String str = result.toPlainString();
		if (str.indexOf('.') > -1) // Fix scale
			scale += str.indexOf('.');
		return new BigDecimal(str, new MathContext(scale, RoundingMode.HALF_UP));
	}
}