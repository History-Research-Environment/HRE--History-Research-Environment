package hre.tmgjava;
/**
 * HDException - DatabseLayer exception report
 * @author NTo
 * v0.00.0016 2019-12-13 - First version (N. Tolleshaug)
 */
public	class HCException extends Exception {
	private static final long serialVersionUID = 001L;
	public HCException () {}
	public HCException (String s) { super(s); }
} // End class HDException