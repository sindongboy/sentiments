package com.skplanet.nlp.sentiment.util;

import java.util.ArrayList;
import java.util.Set;


public final class ArrayUtils {
	public static String array2string(String [] array) {
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < array.length; i++) {
			if(i == array.length - 1) {
				result.append(array[i]);
				break;
			}
			result.append(array[i] + " ");
		}
		return result.toString();
	}

	public static String[] subArray(String [] array, int sIdx, int eIdx) {
        String [] result = new String[eIdx - sIdx];
        int index = 0;
        for(int i = sIdx; i < eIdx; i++) {
            result[index++] = array[i];
        }
        return result;
	}

	public static String[] subArray(String [] array, int sIdx, int eIdx, Set<Integer> skipPosition) {
        //String [] result = new String[eIdx - sIdx];
		ArrayList<String> result = new ArrayList<String>();
        for(int i = sIdx; i < eIdx; i++) {
			if(skipPosition.contains(i)) {
				continue;
			}
            result.add(array[i]);
        }
        return result.toArray(new String[result.size()]);
	}

    private ArrayUtils() {

    }
}
