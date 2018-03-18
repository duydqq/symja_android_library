package org.matheclipse.core.builtin;

import java.util.HashMap;
import java.util.Map;

import org.matheclipse.core.basic.Config;
import org.matheclipse.core.basic.ToggleFeature;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.exception.RecursionLimitExceeded;
import org.matheclipse.core.eval.exception.Validate;
import org.matheclipse.core.eval.exception.WrongArgumentType;
import org.matheclipse.core.eval.interfaces.AbstractFunctionEvaluator;
import org.matheclipse.core.eval.util.Assumptions;
import org.matheclipse.core.eval.util.IAssumptions;
import org.matheclipse.core.eval.util.Options;
import org.matheclipse.core.expression.ASTSeriesData;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IASTAppendable;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.interfaces.IInteger;
import org.matheclipse.core.interfaces.INumber;
import org.matheclipse.core.interfaces.ISignedNumber;
import org.matheclipse.core.interfaces.ISymbol;
import org.matheclipse.core.polynomials.ExprPolynomial;
import org.matheclipse.core.polynomials.ExprPolynomialRing;
import org.matheclipse.core.polynomials.PartialFractionGenerator;
import org.matheclipse.core.reflection.system.rules.LimitRules;
import org.matheclipse.core.reflection.system.rules.SeriesCoefficientRules;

public class SeriesFunctions {
	static {
		F.Limit.setEvaluator(new Limit());
		if (ToggleFeature.SERIES) {
			F.ComposeSeries.setEvaluator(new ComposeSeries());
			F.InverseSeries.setEvaluator(new InverseSeries());
			F.Series.setEvaluator(new Series());
			F.SeriesCoefficient.setEvaluator(new SeriesCoefficient());
			F.SeriesData.setEvaluator(new SeriesData());
		}
	}

	/**
	 * <pre>
	 * Limit(expr, x -&gt; x0)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * gives the limit of <code>expr</code> as <code>x</code> approaches <code>x0</code>
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Limit(7+Sin(x)/x, x-&gt;Infinity)
	 * 7
	 * </pre>
	 */
	private final static class Limit extends AbstractFunctionEvaluator implements LimitRules {

		static class LimitData implements Cloneable {
			final ISymbol symbol;

			final IExpr limitValue;

			final IAST rule;

			int direction;

			public LimitData(ISymbol symbol, IExpr limitValue, IAST rule) {
				this(symbol, limitValue, rule, DIRECTION_TWO_SIDED);
			}

			public LimitData(ISymbol symbol, IExpr limitValue, IAST rule, int direction) {
				this.symbol = symbol;
				this.limitValue = limitValue;
				this.rule = rule;
				this.direction = direction;
			}

			@Override
			protected LimitData clone() throws CloneNotSupportedException {
				return (LimitData) super.clone();
			}

			public int getDirection() {
				return direction;
			}

			public IExpr getLimitValue() {
				return limitValue;
			}

			public IAST getRule() {
				return rule;
			}

			public ISymbol getSymbol() {
				return symbol;
			}

			public void setDirection(int direction) {
				this.direction = direction;
			}

			/**
			 * Create a new <code>F.Limit( arg1, ... )</code> expression from his <code>LimitData</code> object
			 * 
			 * @param arg1
			 *            the first argument of the Limit expression
			 * @return a new <code>F.Limit( arg1, ... )</code> expression
			 */
			public IAST limit(IExpr arg1) {
				// if (direction == DIRECTION_FROM_LARGER_VALUES) {
				// return F.Limit(arg1, rule, F.Rule(F.Direction, F.CN1));
				// }

				if (direction == DIRECTION_FROM_BELOW) {
					return F.Limit(arg1, rule, F.Rule(F.Direction, F.C1));
				}
				return F.Limit(arg1, rule);
			}

			/**
			 * Map a <code>F.Limit( arg1, ... )</code> expression at each argument of the given <code>ast</code>.
			 * 
			 * @param ast
			 * @return
			 */
			public IExpr mapLimit(final IAST ast) {
				return ast.mapThread(limit(null), 1);
			}
		}

		private static IExpr evalLimitQuiet(final IExpr expr, LimitData data) {
			EvalEngine engine = EvalEngine.get();
			boolean quiet = engine.isQuietMode();
			try {
				engine.setQuietMode(true);
				return evalLimit(expr, data, true);
			} finally {
				engine.setQuietMode(quiet);
			}

		}

		/**
		 * 
		 * @param expr
		 * @param data
		 *            the limits data definition
		 * @param evalExpr
		 * @return
		 */
		private static IExpr evalLimit(final IExpr expr, LimitData data, boolean evalExpr) {
			IExpr expression = expr;
			if (evalExpr) {
				IExpr result = F.evalQuiet(expression);
				if (result.isNumericFunction()) {
					return result;
				}
				if (!result.equals(F.Indeterminate)) {
					expression = result;
				}
				if (result.isFree(data.getSymbol(), true)) {
					// Limit[a_,sym->lim] -> a
					return expression;
				}
				if (result.equals(data.getSymbol())) {
					// Limit[x_,x_->lim] -> lim
					return data.getLimitValue();
				}

				if (data.getLimitValue().isNumericFunction()) {
					result = expression.replaceAll(data.getRule());
					if (result.isPresent()) {
						result = F.evalQuiet(result);
						if (result.isNumericFunction()) {
							if (result.isZero()) {
								IExpr temp = F
										.evalQuiet(F.N(F.Greater(F.Subtract(expression, data.getLimitValue()), F.C0)));
								if (temp != null) {
									IAssumptions assumptions = Assumptions.getInstance(temp);
									if (assumptions != null) {
										int direction = data.getDirection();
										if (assumptions.isNegative(data.getSymbol())) {
											if (direction == DIRECTION_TWO_SIDED || direction == DIRECTION_FROM_BELOW) {
												data.setDirection(DIRECTION_FROM_BELOW);
											} else {
												return F.NIL;
											}
										} else if (assumptions.isNonNegative(data.getSymbol())) {
											if (direction == DIRECTION_TWO_SIDED || direction == DIRECTION_FROM_ABOVE) {
												data.setDirection(DIRECTION_FROM_ABOVE);
											} else {
												return F.NIL;
											}
										}
									}
								}
							}
							return result;
						}
					}
				}
			}

			if (expression.isAST()) {
				final IAST arg1 = (IAST) expression;
				if (arg1.isSin() || arg1.isCos()) {
					return F.unaryAST1(arg1.head(), data.limit(arg1.arg1()));
				} else if (arg1.isPlus()) {
					return plusLimit(arg1, data);
				} else if (arg1.isTimes()) {
					return timesLimit(arg1, data);
				} else if (arg1.isLog()) {
					return logLimit(arg1, data);
				} else if (arg1.isPower()) {
					return powerLimit(arg1, data);
				}

			}

			return F.NIL;
		}

		/**
		 * Try L'hospitales rule. See <a href="http://en.wikipedia.org/wiki/L%27H%C3%B4pital%27s_rule"> Wikipedia
		 * L'Hôpital's rule</a>
		 * 
		 * @param numerator
		 * @param denominator
		 * @param data
		 *            the limits data definition
		 * @return
		 */
		private static IExpr lHospitalesRule(IExpr numerator, IExpr denominator, LimitData data) {
			EvalEngine engine = EvalEngine.get();
			ISymbol x = data.getSymbol();
			try {
				int recursionCounter = engine.incRecursionCounter();
				int recursionLimit = engine.getRecursionLimit();
				if (recursionLimit > 0) {
					if (recursionCounter > recursionLimit) {
						return F.NIL;
					}
					IExpr expr = F.evalQuiet(F.Times(F.D(numerator, x), F.Power(F.D(denominator, x), F.CN1)));
					// System.out.println(expr.toString());
					return evalLimit(expr, data, false);
				}
				try {
					if (recursionLimit <= 0) {
						// set recursion limit for using l'Hospitales rule
						engine.setRecursionLimit(128);
					}
					IExpr expr = F.evalQuiet(F.Times(F.D(numerator, x), F.Power(F.D(denominator, x), F.CN1)));
					return evalLimit(expr, data, false);
				} catch (RecursionLimitExceeded rle) {
					engine.setRecursionLimit(recursionLimit);
				} finally {
					engine.setRecursionLimit(recursionLimit);
				}
			} finally {
				engine.decRecursionCounter();
			}
			return F.NIL;
		}

		/**
		 * See: <a href="http://en.wikibooks.org/wiki/Calculus/Infinite_Limits">Limits at Infinity of Rational
		 * Functions</a>
		 * 
		 * @param numeratorPoly
		 * @param denominatorPoly
		 * @param symbol
		 *            the variable for which to approach to the limit
		 * @param limit
		 *            the limit value which the variable should approach to
		 * @param rule
		 * @return
		 */
		// private static IExpr limitsInfinityOfRationalFunctions(GenPolynomial<IExpr>
		// numeratorPoly,
		// GenPolynomial<IExpr> denominatorPoly, ISymbol symbol, IExpr limit, IAST rule)
		// {
		// long numDegree = numeratorPoly.degree();
		// long denomDegree = denominatorPoly.degree();
		// if (numDegree > denomDegree) {
		// // If the numerator has the highest term, then the fraction is
		// // called "top-heavy". If, when you divide the numerator
		// // by the denominator the resulting exponent on the variable is
		// // even, then the limit (at both \infty and -\infty) is
		// // \infty. If it is odd, then the limit at \infty is \infty, and the
		// // limit at -\infty is -\infty.
		// long oddDegree = (numDegree + denomDegree) % 2;
		// if (oddDegree == 1) {
		// return F.Limit(F.Times(
		// F.Divide(numeratorPoly.leadingBaseCoefficient(),
		// denominatorPoly.leadingBaseCoefficient()),
		// limit), rule);
		// } else {
		// return F.Limit(F.Times(
		// F.Divide(numeratorPoly.leadingBaseCoefficient(),
		// denominatorPoly.leadingBaseCoefficient()),
		// F.CInfinity), rule);
		// }
		// } else if (numDegree < denomDegree) {
		// // If the denominator has the highest term, then the fraction is
		// // called "bottom-heavy" and the limit (at both \infty
		// // and -\infty) is zero.
		// return F.C0;
		// }
		// // If the exponent of the highest term in the numerator matches the
		// // exponent of the highest term in the denominator,
		// // the limit (at both \infty and -\infty) is the ratio of the
		// // coefficients of the highest terms.
		// return F.Divide(numeratorPoly.leadingBaseCoefficient(),
		// denominatorPoly.leadingBaseCoefficient());
		// }

		/**
		 * See: <a href="http://en.wikibooks.org/wiki/Calculus/Infinite_Limits">Limits at Infinity of Rational
		 * Functions</a>
		 * 
		 * @param numeratorPoly
		 * @param denominatorPoly
		 * @param symbol
		 *            the variable for which to approach to the limit
		 * @param data
		 *            the limit expression which the variable should approach to
		 * @param rule
		 * @return
		 */
		private static IExpr limitsInfinityOfRationalFunctions(ExprPolynomial numeratorPoly,
				ExprPolynomial denominatorPoly, ISymbol symbol, IExpr limit, LimitData data) {
			long numDegree = numeratorPoly.degree();
			long denomDegree = denominatorPoly.degree();
			if (numDegree > denomDegree) {
				// If the numerator has the highest term, then the fraction is
				// called "top-heavy". If, when you divide the numerator
				// by the denominator the resulting exponent on the variable is
				// even, then the limit (at both \infty and -\infty) is
				// \infty. If it is odd, then the limit at \infty is \infty, and the
				// limit at -\infty is -\infty.
				long oddDegree = (numDegree + denomDegree) % 2;
				if (oddDegree == 1) {
					return data.limit(F.Times(
							F.Divide(numeratorPoly.leadingBaseCoefficient(), denominatorPoly.leadingBaseCoefficient()),
							limit));
				} else {
					return data.limit(F.Times(
							F.Divide(numeratorPoly.leadingBaseCoefficient(), denominatorPoly.leadingBaseCoefficient()),
							F.CInfinity));
				}
			} else if (numDegree < denomDegree) {
				// If the denominator has the highest term, then the fraction is
				// called "bottom-heavy" and the limit (at both \infty
				// and -\infty) is zero.
				return F.C0;
			}
			// If the exponent of the highest term in the numerator matches the
			// exponent of the highest term in the denominator,
			// the limit (at both \infty and -\infty) is the ratio of the
			// coefficients of the highest terms.
			return F.Divide(numeratorPoly.leadingBaseCoefficient(), denominatorPoly.leadingBaseCoefficient());
		}

		// private static IExpr mapLimit(final IAST ast, LimitData data) {
		// return ast.mapThread(data.limit(null), 1);
		// }

		/**
		 * Try l'Hospitales rule for numerator and denominator expression.
		 * 
		 * @param numerator
		 * @param denominator
		 * @param data
		 *            the limit data definition
		 * @return <code>F.NIL</code> if no limit found
		 */
		private static IExpr numeratorDenominatorLimit(final IExpr numerator, final IExpr denominator, LimitData data) {
			IExpr numValue;
			IExpr denValue;
			IExpr limit = data.getLimitValue();
			IAST rule = data.getRule();
			EvalEngine engine = EvalEngine.get();
			if (denominator.isOne() && numerator.isTimes()) {
				// Limit[a_*b_*c_,sym->lim] ->
				// Limit[a,sym->lim]*Limit[b,sym->lim]*Limit[c,sym->lim]
				return data.mapLimit((IAST) numerator);
			}
			if (!denominator.isNumber() || denominator.isZero()) {
				ISymbol x = data.getSymbol();
				denValue = engine.evalBlock(denominator, x, limit);
				if (denValue.equals(F.Indeterminate)) {
					return F.NIL;
				} else if (denValue.isZero()) {
					numValue = engine.evalBlock(numerator, x, limit);
					if (numValue.isZero()) {
						return lHospitalesRule(numerator, denominator, data);
					}
					return F.NIL;
				} else if (F.CInfinity.equals(denValue)) {
					numValue = engine.evalBlock(numerator, x, limit);
					if (F.CInfinity.equals(numValue)) {
						return lHospitalesRule(numerator, denominator, data);
					}
					return F.NIL;
				} else if (denValue.isNegativeInfinity()) {
					numValue = engine.evalBlock(numerator, x, limit);
					if (numValue.isNegativeInfinity()) {
						return lHospitalesRule(numerator, denominator, data);
					}
					return F.NIL;
				}
			}

			return F.Times(data.limit(numerator), F.Power(data.limit(denominator), F.CN1));
		}

		private static IExpr plusLimit(final IAST arg1, LimitData data) {
			// Limit[a_+b_+c_,sym->lim] ->
			// Limit[a,sym->lim]+Limit[b,sym->lim]+Limit[c,sym->lim]
			// IAST rule = data.getRule();
			IExpr limit = data.getLimitValue();
			if (limit.isInfinity() || limit.isNegativeInfinity()) {
				ISymbol symbol = data.getSymbol();
				try {
					ExprPolynomialRing ring = new ExprPolynomialRing(symbol);
					ExprPolynomial poly = ring.create(arg1);
					IExpr coeff = poly.leadingBaseCoefficient();
					long oddDegree = poly.degree() % 2;
					if (oddDegree == 1) {
						// odd degree
						return data.limit(F.Times(coeff, limit));
					}
					// even degree
					return data.limit(F.Times(coeff, F.CInfinity));
				} catch (RuntimeException e) {
					if (Config.DEBUG) {
						e.printStackTrace();
					}
				}

			}
			return data.mapLimit(arg1);
		}

		private static IExpr powerLimit(final IAST powerAST, LimitData data) {
			// IAST rule = data.getRule();
			IExpr base = powerAST.arg1();
			IExpr exponent = powerAST.arg1();
			if (powerAST.arg2().equals(data.getSymbol()) && data.getLimitValue().isZero()) {
				return F.C1;
			}
			if (base.isTimes() && exponent.isFree(data.getSymbol())) {
				IAST isFreeResult = ((IAST) base).partitionTimes(x -> x.isFree(data.getSymbol(), true), F.C1, F.C1,
						F.List);
				if (!isFreeResult.get(2).isOne()) {
					return F.Times(F.Power(isFreeResult.get(1), exponent),
							data.limit(F.Power(isFreeResult.get(2), exponent)));
				}
			}
			if (powerAST.arg2().isNumericFunction()) {
				// Limit[a_^exp_,sym->lim] -> Limit[a,sym->lim]^exp
				IExpr exp = powerAST.arg2();
				// IExpr temp = F.evalQuiet(F.Limit(arg1.arg1(), rule));?
				IExpr temp = evalLimitQuiet(powerAST.arg1(), data);
				if (temp.isNumericFunction()) {
					if (temp.isZero()) {
						if (exp.isPositive()) {
							// 0 ^ (positve exponent)
							return F.C0;
						}
						if (exp.isNegative()) {
							// 0 ^ (negative exponent)
							if (exp.isInteger()) {
								IInteger n = (IInteger) exp;
								if (n.isEven()) {
									return F.CInfinity;
								}
								if (data.getDirection() == DIRECTION_FROM_BELOW) {
									return F.CNInfinity;
								} else {
									data.setDirection(DIRECTION_FROM_ABOVE);
									return F.CInfinity;
								}
							} else if (exp.isFraction()) {
								if (data.getDirection() != DIRECTION_FROM_BELOW) {
									data.setDirection(DIRECTION_FROM_ABOVE);
									return F.CInfinity;
								}
							}

						}

						return F.NIL;
					}
					return F.Power(temp, exp);
				}
				if (exp.isInteger()) {
					IInteger n = (IInteger) exp;
					if (temp.isInfinity()) {
						if (n.isPositive()) {
							return temp;
						} else if (n.isNegative()) {
							return F.C0;
						}
						return F.NIL;
					} else if (temp.isNegativeInfinity()) {
						if (n.isPositive()) {
							if (n.isEven()) {
								return F.CInfinity;
							} else {
								return F.CNInfinity;
							}
						} else if (n.isNegative()) {
							return F.C0;
						}
						return F.NIL;
					} else if (temp.equals(F.Indeterminate) || temp.isAST(F.Limit)) {
						return F.NIL;
					}
					if (n.isPositive()) {
						return F.Power(temp, n);
					} else if (n.isNegative() && n.isEven()) {
						return F.Power(temp, n);
					}
				}
			}
			return F.NIL;
		}

		/**
		 * Try a substitution. <code>y = 1/x</code>. As <code>|x|</code> approaches <code>Infinity</code> or
		 * <code>-Infinity</code>, <code>y</code> approaches <code>0</code>.
		 * 
		 * @param arg1
		 * @param data
		 *            (the datas limit must be Infinity or -Infinity)
		 * @return <code>F.NIL</code> if the substitution didn't succeed.
		 */
		private static IExpr substituteInfinity(final IAST arg1, LimitData data) {
			ISymbol x = data.getSymbol();
			IExpr y = F.Power(x, F.CN1); // substituting by 1/x
			IExpr temp = F.evalQuiet(F.subst(arg1, x, y));
			if (temp.isTimes()) {
				IExpr[] parts = Algebra.fractionalPartsTimesPower((IAST) temp, false, false, true, true);
				if (parts != null) {
					if (!parts[1].isOne()) { // denominator != 1
						LimitData ndData = new LimitData(x, F.C0, F.Rule(x, F.C0), data.getDirection());
						temp = numeratorDenominatorLimit(parts[0], parts[1], ndData);
						if (temp.isPresent()) {
							return temp;
						}
					}
				}
			}
			return F.NIL;
		}

		private static IExpr timesLimit(final IAST timesAST, LimitData data) {
			IAST isFreeResult = timesAST.partitionTimes(x -> x.isFree(data.getSymbol(), true), F.C1, F.C1, F.List);
			if (!isFreeResult.get(1).isOne()) {
				return F.Times(isFreeResult.get(1), data.limit(isFreeResult.get(2)));
			}
			IExpr[] parts = Algebra.fractionalPartsTimesPower(timesAST, false, false, true, true);
			if (parts != null) {

				IExpr numerator = parts[0];
				IExpr denominator = parts[1];
				IExpr limit = data.getLimitValue();
				ISymbol symbol = data.getSymbol();
				if (limit.isInfinity() || limit.isNegativeInfinity()) {
					try {
						ExprPolynomialRing ring = new ExprPolynomialRing(symbol);
						ExprPolynomial denominatorPoly = ring.create(denominator);
						ExprPolynomial numeratorPoly = ring.create(numerator);
						return limitsInfinityOfRationalFunctions(numeratorPoly, denominatorPoly, symbol, limit, data);
					} catch (RuntimeException e) {
						if (Config.DEBUG) {
							e.printStackTrace();
						}
					}
				}

				IExpr plusResult = Algebra.partialFractionDecompositionRational(new PartialFractionGenerator(), parts,
						symbol);
				if (plusResult.isPlus()) {
					// OneIdentity if plusResult.isAST1()
					// if (plusResult.size() > 2) {
					return data.mapLimit((IAST) plusResult);
					// }
				}

				if (denominator.isOne()) {
					if (limit.isInfinity() || limit.isNegativeInfinity()) {
						IExpr temp = substituteInfinity(timesAST, data);
						if (temp.isPresent()) {
							return temp;
						}
					}
				}
				IExpr temp = numeratorDenominatorLimit(numerator, denominator, data);
				if (temp.isPresent()) {
					return temp;
				}

			}
			return data.mapLimit(timesAST);
		}

		private static IExpr logLimit(final IAST logAST, LimitData data) {
			if (logAST.isAST2() && !logAST.isFree(data.getSymbol())) {
				return F.NIL;
			}
			IExpr firstArg = logAST.arg1();
			if (firstArg.isPower() && firstArg.exponent().isFree(data.getSymbol())) {
				IAST arg1 = logAST.setAtClone(1, firstArg.base());
				return F.Times(firstArg.exponent(), data.limit(arg1));
			} else if (firstArg.isTimes()) {
				IAST isFreeResult = firstArg.partitionTimes(x -> x.isFree(data.getSymbol(), true), F.C1, F.C1, F.List);
				if (!isFreeResult.get(1).isOne()) {
					IAST arg1 = logAST.setAtClone(1, isFreeResult.get(1));
					IAST arg2 = logAST.setAtClone(1, isFreeResult.get(2));
					return F.Plus(arg1, data.limit(arg2));
				}
			}
			return F.NIL;
		}

		/**
		 * Compute the limit approaching from larger real values.
		 */
		public final static int DIRECTION_FROM_ABOVE = -1;

		/**
		 * Compute the limit approaching from larger or smaller real values automatically.
		 */
		public final static int DIRECTION_TWO_SIDED = 0;

		/**
		 * Compute the limit approaching from smaller real values.
		 */
		public final static int DIRECTION_FROM_BELOW = 1;

		/**
		 * Limit of a function. See <a href="http://en.wikipedia.org/wiki/List_of_limits">List of Limits</a>
		 */
		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkRange(ast, 3, 4);

			if (!ast.arg2().isRuleAST()) {
				throw new WrongArgumentType(ast, ast.arg2(), 2, "Limit: rule definition expected!");
			}
			IAST rule = (IAST) ast.arg2();
			if (!(rule.arg1().isSymbol())) {
				throw new WrongArgumentType(ast, ast.arg1(), 2, "Limit: variable symbol for rule definition expected!");
			}
			boolean numericMode = engine.isNumericMode();
			try {
				engine.setNumericMode(false);
				int direction = DIRECTION_TWO_SIDED; // no direction as default
				if (ast.isAST3()) {
					final Options options = new Options(ast.topHead(), ast, 2, engine);
					IExpr option = options.getOption("Direction");
					if (option.isPresent()) {
						if (option.isOne()) {
							direction = DIRECTION_FROM_BELOW;
						} else if (option.isMinusOne()) {
							direction = DIRECTION_FROM_ABOVE;
						} else if (option.equals(F.Automatic)) {
							direction = DIRECTION_TWO_SIDED;
						} else {
							throw new WrongArgumentType(ast, ast.arg2(), 2, "Limit: direction option expected!");
						}
					} else {
						throw new WrongArgumentType(ast, ast.arg2(), 2, "Limit: direction option expected!");
					}
					if (direction == DIRECTION_TWO_SIDED) {
						IExpr temp = F.Limit.evalDownRule(engine, F.Limit(ast.arg1(), ast.arg2()));
						if (temp.isPresent()) {
							return temp;
						}
					}
				}
				ISymbol symbol = (ISymbol) rule.arg1();
				IExpr limit = null;
				if (rule.isFreeAt(2, symbol)) {
					limit = rule.arg2();
				} else {
					throw new WrongArgumentType(ast, ast.arg2(), 2,
							"Limit: limit value contains variable symbol for rule definition!");
				}
				LimitData data = new LimitData(symbol, limit, rule, direction);
				return evalLimit(ast.arg1(), data, true);
			} finally {
				engine.setNumericMode(numericMode);
			}
		}

		@Override
		public IAST getRuleAST() {
			return RULES;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
			newSymbol.setAttributes(ISymbol.NHOLDALL);
			super.setUp(newSymbol);
		}

	}

	private final static class ComposeSeries extends AbstractFunctionEvaluator {
		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			if (ast.size() > 2) {
				for (int i = 1; i < ast.size(); i++) {

				}
			}
			return F.NIL;
		}

	}

	/**
	 * <pre>
	 * InverseSeries(series)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * return the inverse series.
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; InverseSeries(Series(Sin(x), {x, 0, 7}))
	 * x+x^3/6+3/40*x^5+5/112*x^7+O(x)^8
	 * </pre>
	 */
	private final static class InverseSeries extends AbstractFunctionEvaluator {
		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			if (ast.isAST1() && (ast.arg1() instanceof ASTSeriesData)) {

				ASTSeriesData ps = (ASTSeriesData) ast.arg1();
				ps = ps.inverse();
				if (ps != null) {
					return ps;
				}
			}
			return F.NIL;
		}

	}

	/**
	 * <pre>
	 * Series(expr, {x, x0, n})
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * create a power series of <code>expr</code> up to order <code>(x- x0)^n</code> at the point <code>x = x0</code>
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Series(f(x),{x,a,3})  
	 * f(a)+f'(a)*(-a+x)+1/2*f''(a)*(-a+x)^2+1/6*Derivative(3)[f][a]*(-a+x)^3+O(-a+x)^4
	 * </pre>
	 */
	private final static class Series extends AbstractFunctionEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			if (ast.isAST2() && (ast.arg2().isVector() == 3)) {

				IExpr function = ast.arg1();

				IAST list = (IAST) ast.arg2();
				IExpr x = list.arg1();
				IExpr x0 = list.arg2();

				try {
					final int lowerLimit = ((ISignedNumber) x0).toInt();
					if (lowerLimit != 0) {
						// TODO support other cases than 0
						return F.NIL;
					}
					x0 = F.integer(lowerLimit);
				} catch (ClassCastException cce) {
				} catch (ArithmeticException ae) {
				}

				final int n = Validate.checkIntType(list, 3, Integer.MIN_VALUE);
				if (n < 0) {
					return F.NIL;
				}
				if (function.isFree(x)) {
					return function;
				}
				return createSeriesData(function, x, x0, n, 1, engine);

			}
			return F.NIL;
		}

		/**
		 * 
		 * @param function
		 *            the function which should be generated as a power series
		 * @param x
		 *            the variable
		 * @param x0
		 *            the point to do the power expansion for
		 * @param n
		 *            the order of the expansion
		 * @param denominator
		 * @return
		 */
		private IExpr createSeriesData(final IExpr function, IExpr x, IExpr x0, final int n, int denominator,
				EvalEngine engine) {
			ISymbol order = F.Dummy("$$$n");
			IExpr temp = engine.evaluate(F.SeriesCoefficient(function, F.List(x, x0, order)));
			if (temp.isFree(F.SeriesCoefficient)) {
				ASTSeriesData ps = new ASTSeriesData(x, x0, 0, n + denominator, denominator);
				for (int i = 0; i <= n; i++) {
					ps.append(engine.evaluate(temp.replaceAll(F.Rule(order, F.ZZ(i)))));
				}
				return ps;
			}
			ASTSeriesData ps = new ASTSeriesData(x, x0, 0, n + denominator, denominator);
			IExpr derivedFunction = function;
			for (int i = 0; i <= n; i++) {
				ps.append(F.Times.of(F.Power(F.Factorial(F.integer(i)), F.CN1),
						F.ReplaceAll(derivedFunction, F.Rule(x, x0))));
				derivedFunction = F.D(derivedFunction, x);
			}
			return ps;
		}
	}

	/**
	 * <pre>
	 * SeriesCoefficient(expr, {x, x0, n})
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * get the coefficient of <code>(x- x0)^n</code> at the point <code>x = x0</code>
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; SeriesCoefficient(Sin(x),{x,f+g,n}) 
	 * Piecewise({{Sin(f+g+1/2*n*Pi)/n!,n&gt;=0}},0)
	 * </pre>
	 */
	private final static class SeriesCoefficient extends AbstractFunctionEvaluator implements SeriesCoefficientRules {

		@Override
		public IAST getRuleAST() {
			return RULES;
		}

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			if (ast.isAST2() && (ast.arg2().isVector() == 3)) {

				IExpr function = ast.arg1();

				IAST list = (IAST) ast.arg2();
				IExpr x = list.arg1();
				IExpr x0 = list.arg2();

				IExpr n = list.arg3();
				if (function.isFree(x)) {
					if (n.isZero()) {
						return function;
					}
					return F.Piecewise(F.List(F.List(function, F.Equal(n, F.C0))), F.C0);
				}
				IExpr temp = polynomialSeriesCoefficient(function, x, x0, n, ast, engine);
				if (temp.isPresent()) {
					return temp;
				}
				if (function.isPower()) {
					if (function.base().equals(x)) {
						if (function.exponent().isNumber()) {
							// x^exp
							INumber exp = (INumber) function.exponent();
							if (exp.isInteger()) {
								return F.Piecewise(
										F.List(F.List(F.Times(F.Power(x0, F.Plus(exp, n.negate())), F.Binomial(exp, n)),
												F.LessEqual(F.C0, n, exp))),
										F.C0);
							}
						}
						IExpr exp = function.exponent();
						return F.Piecewise(
								F.List(F.List(F.Times(F.Power(x0, F.Plus(exp, n.negate())), F.Binomial(exp, n)),
										F.GreaterEqual(n, F.C0))),
								F.C0);
					} else if (function.exponent().equals(x) && function.base().isFree(x)) {
						// b^x with b is free of x
						IExpr b = function.base();
						return F.Piecewise(F.List(
								F.List(F.Times(F.Power(b, x0), F.Power(F.Factorial(n), F.CN1), F.Power(F.Log(b), n)),
										F.GreaterEqual(n, F.C0))),
								F.C0);
					}
				}

				if (x0.isSignedNumber()) {
					try {
						final int lowerLimit = ((ISignedNumber) x0).toInt();
						if (lowerLimit != 0) {
							// TODO support other cases than 0
							return F.NIL;
						}
						x0 = F.integer(lowerLimit);
					} catch (ClassCastException cce) {
					} catch (ArithmeticException ae) {
					}
				}

				final int degree = n.toIntDefault(Integer.MIN_VALUE);
				if (degree < 0) {
					return F.NIL;
				}

				if (degree == 0) {
					return F.ReplaceAll(function, F.Rule(x, x0));
				}
				IExpr derivedFunction = F.D.of(function, F.List(x, n));
				return F.Times(F.Power(F.Factorial(n), F.CN1), F.ReplaceAll(derivedFunction, F.Rule(x, x0)));

			}

			return F.NIL;
		}

		/**
		 * 
		 * @param univariatePolynomial
		 * @param x
		 * @param x0
		 * @param n
		 * @param seriesTemplate
		 * @param engine
		 * @return
		 */
		public IExpr polynomialSeriesCoefficient(IExpr univariatePolynomial, IExpr x, IExpr x0, IExpr n,
				final IAST seriesTemplate, EvalEngine engine) {
			try {
				Map<IExpr, IExpr> coefficientMap = new HashMap<IExpr, IExpr>();
				IASTAppendable rest = F.ListAlloc(4);
				ExprPolynomialRing.create(univariatePolynomial, x, coefficientMap, rest);
				IASTAppendable coefficientPlus = F.PlusAlloc(2);
				if (coefficientMap.size() > 0) {
					IASTAppendable piecewiseAST = F.ast(F.Piecewise);
					IASTAppendable rules = F.ListAlloc(2);
					IASTAppendable plus = F.PlusAlloc(coefficientMap.size());
					for (Map.Entry<IExpr, IExpr> entry : coefficientMap.entrySet()) {
						IExpr exp = entry.getKey();
						if (exp.isZero()) {
							continue;
						}
						IExpr coefficient = entry.getValue();
						if (coefficient.isZero()) {
							continue;
						}
						if (coefficient.isOne()) {
							plus.append(F.Times(F.Power(x0, exp), F.Binomial(exp, n)));
						} else {
							plus.append(F.Times(coefficient, F.Power(x0, exp), F.Binomial(exp, n)));
						}
					}
					rules.append(F.List(engine.evaluate(F.Times(F.Power(x0, n.negate()), plus)), F.Greater(n, F.C0)));

					plus = F.PlusAlloc(coefficientMap.size());
					for (Map.Entry<IExpr, IExpr> entry : coefficientMap.entrySet()) {
						IExpr exp = entry.getKey();
						IExpr coefficient = entry.getValue();
						if (coefficient.isZero()) {
							continue;
						}
						if (coefficient.isOne()) {
							plus.append(F.Times(F.Power(x0, exp)));
						} else {
							plus.append(F.Times(coefficient, F.Power(x0, exp)));
						}
					}
					rules.append(F.List(engine.evaluate(plus), F.Equal(n, F.C0)));
					piecewiseAST.append(rules);

					piecewiseAST.append(F.C0);
					coefficientPlus.append(piecewiseAST);
				} else {
					if (!univariatePolynomial.isPlus()) {
						return F.NIL;
					}
				}
				for (int i = 1; i < rest.size(); i++) {
					IASTAppendable term = seriesTemplate.copyAppendable();
					term.set(1, rest.get(i));
					coefficientPlus.append(term);
				}
				return coefficientPlus.getOneIdentity(F.C0);
			} catch (RuntimeException re) {
				if (Config.SHOW_STACKTRACE) {
					re.printStackTrace();
				}
			}
			return F.NIL;
		}
	}

	/**
	 * <pre>
	 * SeriesData(x, x0, {coeff0, coeff1, coeff2,...}, nMin, nMax, denominator})
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * internal structure of a power series at the point <code>x = x0</code> the <code>coeff</code>-i are coefficients
	 * of the power series.
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; SeriesData(x, 0,{1,0,-1/6,0,1/120,0,-1/5040,0,1/362880}, 1, 11, 2) 
	 * Sqrt(x)-x^(3/2)/6+x^(5/2)/120-x^(7/2)/5040+x^(9/2)/362880+O(x)^(11/2)
	 * </pre>
	 */
	private final static class SeriesData extends AbstractFunctionEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			if (ast.size() == 7) {
				if (ast.arg1().isNumber()) {
					return F.Indeterminate;
				}
				IExpr x = ast.arg1();
				IExpr x0 = ast.arg2();
				IExpr coefficients = ast.arg3();
				if (coefficients.isVector() < 0) {
					return F.NIL;
				}
				final int nMin = ast.arg4().toIntDefault(Integer.MIN_VALUE);
				if (nMin == Integer.MIN_VALUE) {
					return F.NIL;
				}
				final int nMax = ast.arg5().toIntDefault(Integer.MIN_VALUE);
				if (nMax == Integer.MIN_VALUE) {
					return F.NIL;
				}
				final int denominator = ast.get(6).toIntDefault(Integer.MIN_VALUE);
				if (denominator == Integer.MIN_VALUE) {
					return F.NIL;
				}
				ASTSeriesData ps = new ASTSeriesData(x, x0, nMin, nMax, denominator);
				ps.appendArgs((IAST) coefficients);
				return ps;
			}
			return F.NIL;
		}
	}

	private final static SeriesFunctions CONST = new SeriesFunctions();

	public static SeriesFunctions initialize() {
		return CONST;
	}

	private SeriesFunctions() {

	}

}