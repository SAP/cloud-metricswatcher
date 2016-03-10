package com.sap.hana.cloud.samples.response.model;

import java.util.ArrayList;
import java.util.List;

public class Application {

	private final List<ApplicationProcess> processes = new ArrayList<ApplicationProcess>();

	private final String account;
	private final String application;

	private String state;

	public Application(String account, String application) {
		this.account = account;
		this.application = application;
	}

	public List<ApplicationProcess> getProcesses() {
		return processes;
	}
	
	public void addProcess(ApplicationProcess process) {
		processes.add(process);
	}

	public String getState() {
		return state;
	}

	public String getAccount() {
		return account;
	}

	public String getApplicationName() {
		return application;
	}
	
}
