package com.sap.hana.cloud.samples.model;

public class ApplicationIdentifier {
	private final String landscapeFqdn;
	private final String account;
	private final String application;

	public ApplicationIdentifier(String landscapeFqdn, String account, String application) {
		this.landscapeFqdn = landscapeFqdn;
		this.account = account;
		this.application = application;
	}

	public String getAccount() {
		return account;
	}

	public String getApplication() {
		return application;
	}

	public String getLandscapeFqdn() {
		return landscapeFqdn;
	}
}
