package hre.bila;
/**
 * 
 * @author NTo
 *
 */
public class HBLibraryBusiness {
/**
 * Pointer to HBHdateSort
 */
	public HdateSort sorter;
	
/**
 * int[] sort(String[] array) access HdateSort method
 * @param array
 * @return
 */
	public int[] sort(String[] array) {
		return sorter.sort(array);
	}

/**
 * Format the place String based on place style array data	
 * @param eventPlace event place array
 * @param placeStyle the index of selected place elements
 * @return String with place data
 */
	public String setUpStyleString(String[] eventPlace, int[] placeStyle) {
		return null;
	}	

}
/**
 * Sort class for HDATE
 * @author NTo
 *
 */

class HdateSort {	
	
/**
 * Sort according to HDATEs in sortStrings
 * @param sortStrings input array with HDATE sort strings
 * @return int[] with the sorted sequence
 */
	public int[] sort(String[] sortStrings) {
		return null;
	}	
}

