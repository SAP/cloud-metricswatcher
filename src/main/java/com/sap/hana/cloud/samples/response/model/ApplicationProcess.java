package com.sap.hana.cloud.samples.response.model;

import java.util.List;

public class ApplicationProcess {
	private final String process;

	private List<Metric> metrics;
	private String state;

	public ApplicationProcess(String processId) {
		this.process = processId;
	}

	public List<Metric> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getProcessId() {
		return process;
	}

}
