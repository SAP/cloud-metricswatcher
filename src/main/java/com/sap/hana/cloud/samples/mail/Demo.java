package com.sap.hana.cloud.samples.mail;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

import com.sap.hana.cloud.samples.model.ApplicationIdentifier;

public class Demo {
	public static void main(String[] args) {
		BasicConfigurator.configure();

		String mailTo = "my_email@gmail.com";
		String mailToSms = "my_email@sms-service.com";

		String user = "my_username";
		String password = "my_password";

		List<ApplicationIdentifier> appList = new ArrayList<>();

		String landscapeFqdn1 = "api.hana.ondemand.com";
		String account1 = "a1";
		String application1 = "app1";
		ApplicationIdentifier appId1 = new ApplicationIdentifier(landscapeFqdn1, account1, application1);
		appList.add(appId1);

		String landscapeFqdn2 = "api.hana.ondemand.com";
		String account2 = "a2 ";
		String application2 = "app2";
		ApplicationIdentifier appId2 = new ApplicationIdentifier(landscapeFqdn2, account2, application2);
		appList.add(appId2);

		String landscapeFqdn3 = "api.us1.hana.ondemand.com";
		String account3 = "b1";
		String application3 = "app3";
		ApplicationIdentifier appId3 = new ApplicationIdentifier(landscapeFqdn3, account3, application3);
		appList.add(appId3);

		String landscapeFqdn4 = "api.us1.hana.ondemand.com";
		String account4 = "b2";
		String application4 = "app4";
		ApplicationIdentifier appId4 = new ApplicationIdentifier(landscapeFqdn4, account4, application4);
		appList.add(appId4);

		MetricsWatcher metricsWatcher = new MetricsWatcher(appList, mailTo, mailToSms, user, password);
		metricsWatcher.start();
	}
}
