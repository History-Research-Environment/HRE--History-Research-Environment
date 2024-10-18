package hre.bila;
/**************************************************************************
 * HBException - BusinessLayer exception report
 **************************************************************************
 * v0.00.0016 2019-12-13 - First version (N. Tolleshaug)
 * ************************************************************************
 */

/**
 * class HBException extends Exception
 * @author NTo
 * @since 2019-12-20
 */
public	class HBException extends Exception {
	private static final long serialVersionUID = 001L;
	String errorCode;
	int value;
	public HBException () {}
	public HBException (String s) {
		super(s);
	}
	public HBException (String errorCode, String message, int value) {
		super(message);
		this.errorCode = errorCode;
		this.value = value;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public int getValue( ) {
		return value;

	}
} // End class HBException


