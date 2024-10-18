package hre.dbla;
/*****************************************************************
 * HDException - DatabaseLayer exception report
 * ***************************************************************
 * v0.00.0016 2019-12-13 - First version (N. Tolleshaug)
 *****************************************************************
 */


/**
 * class HDException
 * @author NTo
 */
public	class HDException extends Exception {
	private static final long serialVersionUID = 001L;
	public HDException () {}
	public HDException (String s) { super(s); }
} // End class HDException


