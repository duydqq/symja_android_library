package org.matheclipse.core.expression;

import static org.matheclipse.core.expression.F.List;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math4.fraction.BigFraction;
import org.matheclipse.core.form.output.OutputFormFactory;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.interfaces.IInteger;
import org.matheclipse.core.interfaces.INumber;
import org.matheclipse.core.interfaces.IRational;
import org.matheclipse.core.interfaces.ISignedNumber;
import org.matheclipse.core.reflection.system.Subsets;
import org.matheclipse.core.reflection.system.Subsets.KSubsetsList;

import com.google.common.math.BigIntegerMath;

/**
 * IInteger implementation which delegates most of the methods to the BigInteger
 * methods.
 * 
 * @see AbstractIntegerSym
 * @see IntegerSym
 */
public class BigIntegerSym extends AbstractIntegerSym {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6389228668633533063L;

	/* package private */BigInteger fBigIntValue;

	private transient int fHashValue = 0;

	/**
	 * do not use directly, needed for serialization/deserialization
	 * 
	 */
	public BigIntegerSym() {
		fBigIntValue = null;
	}

	public BigIntegerSym(BigInteger value) {
		fBigIntValue = value;
	}

	/**
	 * @param that
	 * @return
	 */
	@Override
	public AbstractIntegerSym add(final AbstractIntegerSym that) {
		return valueOf(fBigIntValue.add(that.getBigNumerator()));
	}

	/**
	 * @param val
	 * @return
	 */
	@Override
	public IInteger add(final IInteger val) {
		return valueOf(fBigIntValue.add(val.getBigNumerator()));
	}

	/**
	 * @return
	 */
	public int bitLength() {
		return fBigIntValue.bitLength();
	}

	/** {@inheritDoc} */
	@Override
	public int compareAbsValueToOne() {
		BigInteger temp = fBigIntValue;
		if (fBigIntValue.compareTo(BigInteger.ZERO) < 0) {
			temp = temp.negate();
		}
		return temp.compareTo(BigInteger.ONE);
	}

	public int compareInt(final int value) {
		if (fBigIntValue.bitLength() <= 31) {
			int temp = fBigIntValue.intValue();
			return temp > value ? 1 : temp == value ? 0 : -1;
		}
		return fBigIntValue.signum();
	}

	/**
	 * Compares this expression with the specified expression for order. Returns
	 * a negative integer, zero, or a positive integer as this expression is
	 * canonical less than, equal to, or greater than the specified expression.
	 */
	@Override
	public int compareTo(final IExpr expr) {
		if (expr instanceof IntegerSym) {
			return compareInt(((IntegerSym) expr).fIntValue);
		}
		if (expr instanceof BigIntegerSym) {
			return fBigIntValue.compareTo(((BigIntegerSym) expr).fBigIntValue);
		}
		if (expr instanceof AbstractFractionSym) {
			return -((AbstractFractionSym) expr).compareTo(AbstractFractionSym.valueOf(fBigIntValue, BigInteger.ONE));
		}
		if (expr instanceof Num) {
			double d = fBigIntValue.doubleValue() - ((Num) expr).getRealPart();
			if (d < 0.0) {
				return -1;
			}
			if (d > 0.0) {
				return 1;
			}
		}
		return super.compareTo(expr);
	}

	@Override
	public ComplexNum complexNumValue() {
		// double precision complex number
		return ComplexNum.valueOf(doubleValue());
	}

	@Override
	public int complexSign() {
		return sign();
	}

	/**
	 * @param that
	 * @return
	 */
	@Override
	public AbstractIntegerSym div(final AbstractIntegerSym that) {
		return valueOf(fBigIntValue.divide(that.getBigNumerator()));
	}

	/** {@inheritDoc} */
	@Override
	public IInteger[] divideAndRemainder(final IInteger that) {
		final AbstractIntegerSym[] res = new BigIntegerSym[2];
		BigInteger[] largeRes = fBigIntValue.divideAndRemainder(that.getBigNumerator());
		res[0] = valueOf(largeRes[0]);
		res[1] = valueOf(largeRes[1]);

		return res;
	}

	@Override
	public ISignedNumber divideBy(ISignedNumber that) {
		if (that instanceof BigIntegerSym) {
			return AbstractFractionSym.valueOf(fBigIntValue).divideBy(that);
		}
		if (that instanceof AbstractFractionSym) {
			return AbstractFractionSym.valueOf(fBigIntValue).divideBy(that);
		}
		return Num.valueOf(fBigIntValue.doubleValue() / that.doubleValue());
	}

	/**
	 * Return the divisors of this integer number.
	 * 
	 * <pre>
	 * divisors(24) ==> {1,2,3,4,6,8,12,24}
	 * </pre>
	 */
	@Override
	public IAST divisors() {
		Set<IInteger> set = new TreeSet<IInteger>();
		final IAST primeFactorsList = factorize(F.List());
		int len = primeFactorsList.size() - 1;

		// build the k-subsets from the primeFactorsList
		for (int k = 1; k < len; k++) {
			final KSubsetsList iter = Subsets.createKSubsets(primeFactorsList, k, F.List(), 1);
			for (IAST subset : iter) {
				if (subset == null) {
					break;
				}
				// create the product of all integers in the k-subset
				IInteger factor = F.C1;
				for (int j = 1; j < subset.size(); j++) {
					factor = factor.multiply((IInteger) subset.get(j));
				}
				// add this divisor to the set collection
				set.add(factor);
			}
		}

		// build the final divisors list from the tree set
		final IAST resultList = List(F.C1);
		for (IInteger entry : set) {
			resultList.add(entry);
		}
		resultList.add(this);
		return resultList;
	}

	/**
	 * @return
	 */
	@Override
	public double doubleValue() {
		return fBigIntValue.doubleValue();
	}

	/** {@inheritDoc} */
	@Override
	public AbstractIntegerSym eabs() {
		return valueOf(fBigIntValue.abs());
	}

	/**
	 * IntegerSym extended greatest common divisor.
	 * 
	 * @param that
	 *            if that is of type IntegerSym calculate the extended GCD
	 *            otherwise call {@link super#egcd(IExpr)};
	 * 
	 * @return [ gcd(this,S), a, b ] with a*this + b*S = gcd(this,S).
	 */
	@Override
	public IExpr[] egcd(IExpr that) {
		if (that instanceof BigIntegerSym) {
			BigInteger S = ((BigIntegerSym) that).fBigIntValue;
			IInteger[] result = new IInteger[3];
			result[0] = null;
			result[1] = F.C1;
			result[2] = F.C1;
			if (that == null || that.isZero()) {
				result[0] = (this);
				return result;
			}
			if (this.isZero()) {
				result[0] = ((BigIntegerSym) that);
				return result;
			}
			BigInteger[] qr;
			BigInteger q = fBigIntValue;
			BigInteger r = S;
			BigInteger c1 = BigInteger.ONE;
			BigInteger d1 = BigInteger.ZERO;
			BigInteger c2 = BigInteger.ZERO;
			BigInteger d2 = BigInteger.ONE;
			BigInteger x1;
			BigInteger x2;
			while (!r.equals(BigInteger.ZERO)) {
				qr = q.divideAndRemainder(r);
				q = qr[0];
				x1 = c1.subtract(q.multiply(d1));
				x2 = c2.subtract(q.multiply(d2));
				c1 = d1;
				c2 = d2;
				d1 = x1;
				d2 = x2;
				q = r;
				r = qr[1];
			}
			if (q.signum() < 0) {
				q = q.negate();
				c1 = c1.negate();
				c2 = c2.negate();
			}
			result[0] = valueOf(q);
			result[1] = valueOf(c1);
			result[2] = valueOf(c2);
			return result;
		}
		return super.egcd(that);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof IntegerSym) {
			return equalsInt(((IntegerSym) obj).fIntValue);
		}
		if (obj instanceof BigIntegerSym) {
			if (hashCode() != obj.hashCode()) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			return fBigIntValue.equals(((BigIntegerSym) obj).fBigIntValue);
		}
		return false;
	}

	@Override
	public final boolean equalsFraction(final int numerator, final int denominator) {
		if (denominator != 1) {
			return false;
		}
		return fBigIntValue.intValue() == numerator && fBigIntValue.bitLength() <= 31;
	}

	@Override
	public final boolean equalsInt(int value) {
		return fBigIntValue.intValue() == value && fBigIntValue.bitLength() <= 31;
	}

	/**
	 * Get the highest exponent of <code>base</code> that divides
	 * <code>this</code>
	 * 
	 * @return the exponent
	 */
	@Override
	public IExpr exponent(IInteger base) {
		AbstractIntegerSym b = this;
		if (sign() < 0) {
			b = b.negate();
		} else if (b.isZero()) {
			return F.CInfinity;
		} else if (b.isOne()) {
			return F.C0;
		}
		if (b.equals(base)) {
			return F.C1;
		}
		BigInteger rest = Primality.countExponent(b.getBigNumerator(), base.getBigNumerator());
		return valueOf(rest);
	}

	/** {@inheritDoc} */
	@Override
	public IAST factorInteger() {
		IInteger factor;
		IInteger last = AbstractIntegerSym.valueOf(-2);
		int count = 0;
		final IAST iFactors = factorize(F.List());
		final IAST list = List();
		IAST subList = null;
		for (int i = 1; i < iFactors.size(); i++) {
			factor = (IInteger) iFactors.get(i);
			if (!last.equals(factor)) {
				if (subList != null) {
					subList.add(AbstractIntegerSym.valueOf(count));
					list.add(subList);
				}
				count = 0;
				subList = List(factor);
			}
			count++;
			last = factor;
		}
		if (subList != null) {
			subList.add(AbstractIntegerSym.valueOf(count));
			list.add(subList);
		}
		return list;
	}

	/**
	 * Get all prime factors of this integer
	 * 
	 * @return
	 */
	public IAST factorize(IAST result) {
		// final ArrayList<IInteger> result = new ArrayList<IInteger>();
		AbstractIntegerSym b = this;
		if (sign() < 0) {
			b = b.multiply(AbstractIntegerSym.valueOf(-1));
			result.add(AbstractIntegerSym.valueOf(-1));
		} else if (b.getBigNumerator().equals(BigInteger.ZERO)) {
			result.add(AbstractIntegerSym.valueOf(0));
			return result;
		} else if (b.getBigNumerator().equals(BigInteger.ONE)) {
			result.add(AbstractIntegerSym.valueOf(1));
			return result;
		}
		Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
		BigInteger rest = Primality.countPrimes32749(b.getBigNumerator(), map);

		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			int key = entry.getKey();
			AbstractIntegerSym is = valueOf(key);
			for (int i = 0; i < entry.getValue(); i++) {
				result.add(is);
			}
		}
		if (rest.equals(BigInteger.ONE)) {
			return result;
		}
		b = valueOf(rest);

		Map<BigInteger, Integer> bigMap = new TreeMap<BigInteger, Integer>();
		Primality.pollardRhoFactors(b.getBigNumerator(), bigMap);

		for (Map.Entry<BigInteger, Integer> entry : bigMap.entrySet()) {
			BigInteger key = entry.getKey();
			AbstractIntegerSym is = valueOf(key);
			for (int i = 0; i < entry.getValue(); i++) {
				result.add(is);
			}
		}
		//
		// if (b.fInteger.isProbablePrime(32)) {
		// result.add(b);
		// return result;
		// }

		// TODO improve performance
		// IntegerSym p = IntegerSym.valueOf(1023);
		// while (true) {
		// final IntegerSym q[] = b.divideAndRemainder(p);
		// if (q[0].compareTo(p) < 0) {
		// result.add(b);
		// break;
		// }
		// if (q[1].sign() == 0) {
		// result.add(p);
		// b = q[0];
		// } else {
		// // test only odd integers
		// p = p.add(IntegerSym.valueOf(2));
		// }
		// }
		return result;
	}

	/**
	 * Returns the greatest common divisor of this large integer and the one
	 * specified.
	 * 
	 */
	@Override
	public AbstractIntegerSym gcd(final AbstractIntegerSym that) {
		return valueOf(fBigIntValue.gcd(that.getBigNumerator()));
	}

	/** {@inheritDoc} */
	@Override
	public BigInteger getBigDenominator() {
		return BigInteger.ONE;
	}

	/** {@inheritDoc} */
	@Override
	public BigInteger getBigNumerator() {
		return fBigIntValue;
	}

	/** {@inheritDoc} */
	@Override
	public IInteger getDenominator() {
		return F.C1;
	}

	/** {@inheritDoc} */
	@Override
	public BigFraction getFraction() {
		return new BigFraction(fBigIntValue);
	}

	/** {@inheritDoc} */
	@Override
	public ISignedNumber getIm() {
		return F.C0;
	}

	/** {@inheritDoc} */
	@Override
	public IInteger getNumerator() {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ISignedNumber getRe() {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public final int hashCode() {
		if (fHashValue == 0) {
			fHashValue = fBigIntValue.hashCode();
		}
		return fHashValue;
	}

	@Override
	public String internalJavaString(boolean symbolsAsFactoryMethod, int depth, boolean useOperators) {
		int value = NumberUtil.toInt(fBigIntValue);
		switch (value) {
		case -1:
			return "CN1";
		case -2:
			return "CN2";
		case -3:
			return "CN3";
		case -4:
			return "CN4";
		case -5:
			return "CN5";
		case -6:
			return "CN6";
		case -7:
			return "CN7";
		case -8:
			return "CN8";
		case -9:
			return "CN9";
		case -10:
			return "CN10";
		case 0:
			return "C0";
		case 1:
			return "C1";
		case 2:
			return "C2";
		case 3:
			return "C3";
		case 4:
			return "C4";
		case 5:
			return "C5";
		case 6:
			return "C6";
		case 7:
			return "C7";
		case 8:
			return "C8";
		case 9:
			return "C9";
		case 10:
			return "C10";
		}
		return "ZZ(" + value + "L)";
	}

	@Override
	public String internalScalaString(boolean symbolsAsFactoryMethod, int depth) {
		return internalJavaString(symbolsAsFactoryMethod, depth, true);
	}

	@Override
	public int intValue() {
		return fBigIntValue.intValue();
	}

	/**
	 * @return
	 */
	@Override
	public ISignedNumber inverse() {
		if (isOne()) {
			return this;
		}
		if (NumberUtil.isNegative(fBigIntValue)) {
			return AbstractFractionSym.valueOf(BigInteger.valueOf(-1), fBigIntValue.negate());
		}
		return AbstractFractionSym.valueOf(BigInteger.ONE, fBigIntValue);
	}

	@Override
	public boolean isEven() {
		return NumberUtil.isEven(fBigIntValue);
	}

	@Override
	public boolean isGreaterThan(ISignedNumber obj) {
		if (obj instanceof BigIntegerSym) {
			return fBigIntValue.compareTo(((BigIntegerSym) obj).fBigIntValue) > 0;
		}
		if (obj instanceof AbstractFractionSym) {
			return -((AbstractFractionSym) obj)
					.compareTo(AbstractFractionSym.valueOf(fBigIntValue, BigInteger.ONE)) > 0;
		}
		return fBigIntValue.doubleValue() > obj.doubleValue();
	}

	/**
	 * @param that
	 * @return
	 */
	public boolean isLargerThan(final BigInteger that) {
		return fBigIntValue.compareTo(that) > 0;
	}

	@Override
	public boolean isLessThan(ISignedNumber obj) {
		if (obj instanceof BigIntegerSym) {
			return fBigIntValue.compareTo(((BigIntegerSym) obj).fBigIntValue) < 0;
		}
		if (obj instanceof AbstractFractionSym) {
			return -((AbstractFractionSym) obj)
					.compareTo(AbstractFractionSym.valueOf(fBigIntValue, BigInteger.ONE)) < 0;
		}
		return fBigIntValue.doubleValue() < obj.doubleValue();
	}

	@Override
	public boolean isMinusOne() {
		return fBigIntValue.equals(BI_MINUS_ONE);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isNegative() {
		return fBigIntValue.compareTo(BigInteger.ZERO) < 0;
	}

	@Override
	public boolean isOdd() {
		return NumberUtil.isOdd(fBigIntValue);
	}

	@Override
	public boolean isOne() {
		return fBigIntValue.equals(BigInteger.ONE);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isPositive() {
		return fBigIntValue.compareTo(BigInteger.ZERO) > 0;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isProbablePrime() {
		return isProbablePrime(PRIME_CERTAINTY);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isProbablePrime(int certainty) {
		return fBigIntValue.isProbablePrime(certainty);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isRationalValue(IRational value) {
		return equals(value);
	}

	@Override
	public boolean isZero() {
		return fBigIntValue.equals(BigInteger.ZERO);
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public long longValue() {
		return fBigIntValue.longValue();
	}

	@Override
	public AbstractIntegerSym mod(final AbstractIntegerSym that) {
		return valueOf(fBigIntValue.mod(that.getBigNumerator()));
	}

	public AbstractIntegerSym modInverse(final AbstractIntegerSym m) {
		return valueOf(fBigIntValue.modInverse(m.getBigNumerator()));
	}

	@Override
	public AbstractIntegerSym modPow(final AbstractIntegerSym exp, final AbstractIntegerSym m) {
		return valueOf(fBigIntValue.modPow(exp.getBigNumerator(), m.getBigNumerator()));
	}

	@Override
	public AbstractIntegerSym moebiusMu() {
		if (this.compareTo(AbstractIntegerSym.valueOf(1)) == 0) {
			return AbstractIntegerSym.valueOf(1);
		}
		IAST ast = factorInteger();
		AbstractIntegerSym max = AbstractIntegerSym.valueOf(1);
		for (int i = 1; i < ast.size(); i++) {
			IAST element = (IAST) ast.get(i);
			AbstractIntegerSym c = (AbstractIntegerSym) element.arg2();
			if (c.compareTo(max) > 0) {
				max = c;
			}
		}
		if (max.compareTo(AbstractIntegerSym.valueOf(1)) > 0) {
			return AbstractIntegerSym.valueOf(0);
		}
		if (((ast.size() - 1) & 0x00000001) == 0x00000001) {
			// odd number
			return AbstractIntegerSym.valueOf(-1);
		}
		return AbstractIntegerSym.valueOf(1);
	}

	/**
	 * @param that
	 * @return
	 */
	@Override
	public AbstractIntegerSym multiply(final AbstractIntegerSym that) {
		if (that instanceof IntegerSym) {
			switch (((IntegerSym) that).fIntValue) {
			case 0:
				return F.C0;
			case 1:
				return this;
			case -1:
				return negate();
			}
		}
		return valueOf(fBigIntValue.multiply(that.getBigNumerator()));
	}

	/**
	 * @param val
	 * @return
	 */
	@Override
	public IInteger multiply(final IInteger val) {
		return valueOf(fBigIntValue.multiply(val.getBigNumerator()));
	}

	@Override
	public AbstractIntegerSym negate() {
		return valueOf(fBigIntValue.negate());
	}

	@Override
	public INumber normalize() {
		return this;
	}

	/**
	 * Returns the nth-root of this integer.
	 * 
	 * @return <code>k<code> such as <code>k^n <= this < (k + 1)^n</code>
	 * @throws IllegalArgumentException
	 *             if {@code this < 0}
	 * @throws ArithmeticException
	 *             if this integer is negative and n is even.
	 */
	@Override
	public IInteger nthRoot(int n) throws ArithmeticException {
		if (n < 0) {
			throw new IllegalArgumentException("nthRoot(" + n + ") n must be >= 0");
		}
		if (n == 2) {
			return sqrt();
		}
		if (sign() == 0) {
			return AbstractIntegerSym.valueOf(0);
		} else if (sign() < 0) {
			if (n % 2 == 0) {
				// even exponent n
				throw new ArithmeticException();
			} else {
				// odd exponent n
				return negate().nthRoot(n).negate();
			}
		} else {
			IInteger result;
			IInteger temp = this;
			do {
				result = temp;
				temp = divideAndRemainder(temp.pow(n - 1))[0].add(temp.multiply(AbstractIntegerSym.valueOf(n - 1)))
						.divideAndRemainder(AbstractIntegerSym.valueOf(n))[0];
			} while (temp.compareTo(result) < 0);
			return result;
		}
	}

	/**
	 * Split this integer into the nth-root (with prime factors less equal 1021)
	 * and the &quot;rest-factor&quot;, so that
	 * <code>this== (nth-root)^n + rest</code>
	 * 
	 * @return <code>{nth-root, rest}</code>
	 */
	@Override
	public IInteger[] nthRootSplit(int n) throws ArithmeticException {
		IInteger[] result = new IInteger[2];
		if (sign() == 0) {
			result[0] = AbstractIntegerSym.valueOf(0);
			result[1] = AbstractIntegerSym.valueOf(1);
			return result;
		} else if (sign() < 0) {
			if (n % 2 == 0) {
				// even exponent n
				throw new ArithmeticException();
			} else {
				// odd exponent n
				result = negate().nthRootSplit(n);
				result[1] = result[1].negate();
				return result;
			}
		}

		BigIntegerSym b = this;
		Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
		BigInteger rest = Primality.countPrimes1021(b.fBigIntValue, map);
		AbstractIntegerSym nthRoot = F.C1;
		AbstractIntegerSym restFactors = AbstractIntegerSym.valueOf(rest);
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			AbstractIntegerSym primeLE1021 = valueOf(entry.getKey());
			int primeCounter = entry.getValue();
			int div = primeCounter / n;
			if (div > 0) {
				// build nth-root
				nthRoot = nthRoot.multiply(primeLE1021.pow(div));
			}
			int mod = primeCounter % n;
			if (mod > 0) {
				// build rest factor
				restFactors = restFactors.multiply(primeLE1021.pow(mod));
			}
		}
		result[0] = nthRoot;
		result[1] = restFactors;
		return result;

	}

	@Override
	public final INumber numericNumber() {
		return F.num(this);
	}

	@Override
	public Num numValue() {
		return Num.valueOf(doubleValue());
	}

	@Override
	public IExpr plus(final IExpr that) {
		if (isZero()) {
			return that;
		}
		if (that instanceof AbstractIntegerSym) {
			return this.add((AbstractIntegerSym) that);
		}
		if (that instanceof AbstractFractionSym) {
			return AbstractFractionSym.valueOf(fBigIntValue).add((AbstractFractionSym) that);
		}
		if (that instanceof ComplexSym) {
			return ((ComplexSym) that).add(ComplexSym.valueOf(this));
		}
		return super.plus(that);
	}

	@Override
	public AbstractIntegerSym quotient(final AbstractIntegerSym that) {
		return valueOf(fBigIntValue.divide(that.getBigNumerator()));
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
		byte attributeFlags = objectInput.readByte();
		int value;
		switch (attributeFlags) {
		case 1:
			value = objectInput.readByte();
			fBigIntValue = BigInteger.valueOf(value);
			return;
		case 2:
			value = objectInput.readShort();
			fBigIntValue = BigInteger.valueOf(value);
			return;
		case 4:
			value = objectInput.readInt();
			fBigIntValue = BigInteger.valueOf(value);
			return;
		}
		fBigIntValue = (BigInteger) objectInput.readObject();
	}

	public AbstractIntegerSym remainder(final AbstractIntegerSym that) {
		return valueOf(fBigIntValue.remainder(that.getBigNumerator()));
	}

	@Override
	public IExpr remainder(final IExpr that) {
		if (that instanceof BigIntegerSym) {
			return valueOf(fBigIntValue.remainder(((BigIntegerSym) that).fBigIntValue));
		}
		return this;
	}

	@Override
	public IInteger round() {
		return this;
	}

	@Override
	public int sign() {
		return fBigIntValue.signum();
	}

	/**
	 * Returns the integer square root of this integer.
	 * 
	 * @return <code>k<code> such as <code>k^2 <= this < (k + 1)^2</code>
	 * @throws ArithmeticException
	 *             if this integer is negative.
	 */
	public IInteger sqrt() throws ArithmeticException {
		return valueOf(BigIntegerMath.sqrt(fBigIntValue, RoundingMode.UNNECESSARY));
	}

	@Override
	public IInteger subtract(final IInteger that) {
		return valueOf(fBigIntValue.subtract(that.getBigNumerator()));
	}

	@Override
	public ISignedNumber subtractFrom(ISignedNumber that) {
		if (that instanceof BigIntegerSym) {
			return this.add((BigIntegerSym) that.negate());
		}
		if (isZero()) {
			return that.negate();
		}
		if (that instanceof AbstractFractionSym) {
			return AbstractFractionSym.valueOf(fBigIntValue).subtractFrom(that);
		}
		return Num.valueOf(fBigIntValue.doubleValue() - that.doubleValue());
	}

	/**
	 * @param that
	 * @return
	 */
	@Override
	public IExpr times(final IExpr that) {
		if (that instanceof BigIntegerSym) {
			return this.multiply((BigIntegerSym) that);
		}
		if (isZero()) {
			return F.C0;
		}
		if (isOne()) {
			return that;
		}
		if (that instanceof AbstractFractionSym) {
			return AbstractFractionSym.valueOf(fBigIntValue).multiply((AbstractFractionSym) that).normalize();
		}
		if (that instanceof ComplexSym) {
			return ((ComplexSym) that).multiply(ComplexSym.valueOf(this));
		}
		return super.times(that);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int toInt() throws ArithmeticException {
		return NumberUtil.toInt(fBigIntValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long toLong() throws ArithmeticException {
		return NumberUtil.toLong(fBigIntValue);
	}

	@Override
	public String toString() {
		try {
			StringBuilder sb = new StringBuilder();
			OutputFormFactory.get().convertInteger(sb, this, Integer.MIN_VALUE, OutputFormFactory.NO_PLUS_CALL);
			return sb.toString();
		} catch (Exception e1) {
		}
		// fall back to simple output format
		return fBigIntValue.toString();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		if ((fBigIntValue.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0)
				&& (fBigIntValue.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0)) {
			int value = fBigIntValue.intValue();
			if (value <= Byte.MAX_VALUE && value >= Byte.MIN_VALUE) {
				objectOutput.writeByte(1);
				objectOutput.writeByte((byte) value);
				return;
			}
			if (value <= Short.MAX_VALUE && value >= Short.MIN_VALUE) {
				objectOutput.writeByte(2);
				objectOutput.writeShort((short) value);
				return;
			}
			objectOutput.writeByte(4);
			objectOutput.writeInt(value);
			return;
		}

		objectOutput.writeByte(0);
		objectOutput.writeObject(fBigIntValue);
	}

	private Object writeReplace() throws ObjectStreamException {
		return optional(F.GLOBAL_IDS_MAP.get(this));
	}
}