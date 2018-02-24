package com.suppresswarnings.osgi.corpus;

public class RequireMinMax extends RequireChain {
	int min;
	int max;
	public RequireMinMax(){}
	public RequireMinMax(int min, int max) {
		this.min = min;
		this.max = max;
	}
	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	@Override
	public String desc() {
		String minV = (min == Integer.MIN_VALUE) ? "(-∞": "[" + min;
		String maxV = (max == Integer.MAX_VALUE) ? "+∞)": max + "]";
		return "数字类型，取值范围"+minV+", "+maxV;
	}

	@Override
	public boolean agree(String value) {
		int var = Integer.valueOf(value);
		if(var > max || var < min) {
			return false;
		}
		return true;
	}
}
