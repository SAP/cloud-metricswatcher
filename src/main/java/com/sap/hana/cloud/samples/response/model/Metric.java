package com.sap.hana.cloud.samples.response.model;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((metricType == null) ? 0 : metricType.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Metric other = (Metric) obj;
        if (metricType == null) {
            if (other.metricType != null)
                return false;
        } else if (!metricType.equals(other.metricType))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (unit == null) {
            if (other.unit != null)
                return false;
        } else if (!unit.equals(other.unit))
            return false;
        return true;
    }
}
