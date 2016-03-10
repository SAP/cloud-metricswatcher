package com.sap.hana.cloud.samples.response.model;

import com.google.common.base.Objects;

public class Metric {
	private String name;
	private String state;
	private double value;
	private String unit;
	private double warningThreshold;
	private double errorThreshold;
	private long timestamp;
	private String output;
	private String metricType;
	private double min;
	private double max;

	public String getName() {
		return name;
	}

	public void setName(String metricName) {
		this.name = metricName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double d) {
		this.value = d;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public double getWarningThreshold() {
		return warningThreshold;
	}

	public void setWarningThreshold(double warningThreshold) {
		this.warningThreshold = warningThreshold;
	}

	public double getErrorThreshold() {
		return errorThreshold;
	}

	public void setErrorThreshold(double errorThreshold) {
		this.errorThreshold = errorThreshold;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getMetricType() {
		return metricType;
	}

	public void setMetricType(String metricType) {
		this.metricType = metricType;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double d) {
		this.min = d;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double d) {
		this.max = d;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.name, this.unit, this.metricType);

	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Metric other = (Metric) obj;
		return Objects.equal(this.name, other.name) && Objects.equal(this.metricType, other.metricType)
				&& Objects.equal(this.unit, other.unit);
	}
}
