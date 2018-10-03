package com.hcpt.taxinear.utility;

import java.text.DecimalFormat;

public class NumberUtil {

	public static String getNumberFomatDistance(float distance) {
		DecimalFormat df = new DecimalFormat("0.###");
		return df.format(distance);
	}
}
