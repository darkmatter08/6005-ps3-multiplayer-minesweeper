package minesweeper.server;

/*
 * Represents an Integer Pair.
 * Borrowed from my abcplayer project implementation.
 */
public class IntPair implements Cloneable {
	public final int numerator;
    public final int denominator; 
    
    public IntPair(int num, int denom){
    	this.denominator = denom;
    	this.numerator = num;
    }
    
    /**
     * @return double a representation of the quotient of the
     * 	numerator and denominator
     */
    // @cr where ever this is used to compare two values, we should use compareTo()
    public double getValue() {
    	return ((double) this.numerator) / this.denominator;
    }
    
    /**
     * @return int an int representation of the hashCode
     */
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + denominator;
		result = prime * result + numerator;
		return result;
	}
    
    /**
     * Two IntPairs are equal if they have the same numerator and denominator
     * @return boolean true if the objects are equal, false otherwise
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntPair other = (IntPair) obj;
		if (denominator != other.denominator)
			return false;
		if (numerator != other.numerator)
			return false;
		return true;
	}

//	@Override
//	public int compareTo(Object arg0) {
//		if(arg0.getClass() != this.getClass())
//			throw new RuntimeException("Invalid comparision. Must be of type IntPair");
//		IntPair other = (IntPair) arg0;
//		double thisValue = this.getValue();
//		double otherValue = other.getValue();
//		if (thisValue > otherValue)
//			return 1;
//		else if (thisValue == otherValue)
//			return 0;
//		else
//			return -1;
//	}
	
	@Override
	public IntPair clone(){
		return new IntPair(this.numerator, this.denominator);
	}
	
	/**
	 * Fuzzy comparison for doubles. Will return true if the absolute difference
	 * between d1 and d2 is less than 0.00001. Used for comparing values of IntPairs
	 * @param d1 double first to compare
	 * @param d2 double second to compare
	 * @return boolean true if the difference is within the tolerance, false otherwise. 
	 */
	public static boolean fuzzyDoubleEquals(double d1, double d2){
		// Tolerance value used to avoid problems in double equals. 
		final double TOLERANCE = 0.00001;
		return Math.abs(d1 - d2) < TOLERANCE;
	}
	
	public String toString(){
	    return "(" + numerator + ", " + denominator + ")"; 
	}

}
