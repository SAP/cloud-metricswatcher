package com.sap.hana.cloud.samples.mail;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import com.sap.hana.cloud.samples.model.ApplicationIdentifier;
import com.sap.hana.cloud.samples.response.model.Application;
import com.sap.hana.cloud.samples.response.model.ApplicationProcess;
import com.sap.hana.cloud.samples.response.model.Metric;

public class MetricsWatcher {

	private static final Logger LOGGER = Logger.getLogger(MetricsWatcher.class);

	private static final int CHECK_INTERVAL = 60;

	public static final String CRITICAL_STATE = "Critical";
	public static final String ОК_STATE = "Ok";
	public static final String WARNING_STATE = "Warning";

	private static final String APPLICATION_STARTED = "STARTED";
	private static final String APPLICATION_STARTING = "STARTING";

	private static final String MAIL_SUBJECT_METRIC_CRITICAL = "A metric has reached a critical state.";
	private static final String MAIL_SUBJECT_THRESHOLD_REACHED = "A metric has reached the critical threshold.";

	private static final int CRTICAL_METRIC_TRESHOLD = 3;

	private final String mailTo;
	private final String mailToSms;
	private final String user;
	private final String password;

	private List<ApplicationIdentifier> applications;
	private ScheduledThreadPoolExecutor executor;

	public MetricsWatcher(List<ApplicationIdentifier> applications, String mailTo, String mailToSms, String user,
			String password) {
		this.applications = applications;
		this.executor = new ScheduledThreadPoolExecutor(2);
		this.mailTo = mailTo;
		this.mailToSms = mailToSms;
		this.user = user;
		this.password = password;
	}

	public void start() {
		for (ApplicationIdentifier applicaiton : applications) {
			createWorker(applicaiton);
		}
	}

	public void createWorker(ApplicationIdentifier applicaiton) {
		ApiClient apiClient = getApiClient(applicaiton);
		Worker worker = new Worker(applicaiton, apiClient);
		executor.scheduleAtFixedRate(worker, 0, CHECK_INTERVAL, TimeUnit.SECONDS);

		LOGGER.info(String.format("Application %s is now being watched", applicaiton.getApplication()));
	}

	public void shutdown() {
		executor.shutdown();
	}
	
	protected ApiClient getApiClient(ApplicationIdentifier application) {
		return new ApiClient(application, user, password);
	}

	class Worker implements Runnable {
		private final Map<String, Map<Metric, Integer>> processMetricsMap = new HashMap<>();
		private final ApiClient apiClient;
		private final String applicationName;
		private final String accountName;

		public Worker(ApplicationIdentifier applicationIdentifier, ApiClient apiClient) {
			this.applicationName = applicationIdentifier.getApplication();
			this.accountName = applicationIdentifier.getAccount();
			this.apiClient = apiClient;
		}

		public void run() {
			String applicationState = apiClient.getApplicationState();
			if (applicationState.equalsIgnoreCase(APPLICATION_STARTED)
					|| applicationState.equalsIgnoreCase(APPLICATION_STARTING)) {
				Application application;
				try {
					application = apiClient.getApplication();
					checkMetrics(application);
				} catch (IOException | InterruptedException e) {
					LOGGER.error(String.format("Error while getting metrics for application %s in account %s",
							applicationName, accountName), e);
				}
			} else {
				LOGGER.error(String.format("Application %s in account %s is not started. Attempting to start...",
						applicationName, accountName));
				startApplication();
			}
		}

		private void checkMetrics(Application application) {
			if (application == null) {
				return;
			}
			for (ApplicationProcess process : application.getProcesses()) {
				List<Metric> metrics = process.getMetrics();
				String processId = process.getProcessId();
				for (Metric metric : metrics) {
					checkMetricState(processId, metric);
				}
			}
		}

		private void checkMetricState(String processId, Metric metric) {
			if (isRecovery(processId, metric)) {
				removeCriticalMetric(processId, metric);
			} else if (isCritical(processId, metric)) {
				int timesCritical = updateCriticalMetric(processId, metric);
				sendMailCriticalMetric(metric);
				if (timesCritical == CRTICAL_METRIC_TRESHOLD) {
					sendEmailThreasholdReached(metric, timesCritical);
					restartProcess(processId);
					processMetricsMap.remove(processId);
				}
			}
		}

		private boolean isCritical(String processId, Metric metric) {
			return CRITICAL_STATE.equals(metric.getState());
		}

		private boolean isRecovery(String processId, Metric metric) {
			if (CRITICAL_STATE.equals(metric.getState())) {
				return false;
			}

			Map<Metric, Integer> metrics = processMetricsMap.get(processId);
			if (metrics == null) {
				// we are not tracking this process so there cannot be a
				// recovery
				return false;
			}

			return metrics.containsKey(metric);
		}

		private void sendEmailThreasholdReached(Metric metric, int criticalCount) {
			String emailBody = String.format(
					"Metric %s for application %s has reached critical state %d times. The application will be restarted.",
					metric.getName(), applicationName, criticalCount);
			try {
				Mailsender.sendEmail(mailToSms, MAIL_SUBJECT_THRESHOLD_REACHED, emailBody);
			} catch (MessagingException e) {
				LOGGER.error("Error while sending email", e);
			}
			LOGGER.info(String.format("Critical threshold reached application %s, metric %s alerts received so far %d.",
					applicationName, metric.getName(), criticalCount));
		}

		private void sendMailCriticalMetric(Metric metric) {
			try {
				String emailBody = String.format("Metric %s for application %s has reached a critical state.",
						applicationName, metric.getName());
				Mailsender.sendEmail(mailTo, MAIL_SUBJECT_METRIC_CRITICAL, emailBody);
			} catch (MessagingException e) {
				LOGGER.error("Error while sending email", e);
			}
		}

		private void restartProcess(String processId) {
			LOGGER.info(String.format("Attempting to restart account %s, application %s, process %s", accountName,
					applicationName, processId));
			stopProcess(processId);
			startApplication();
		}

		private void startApplication() {
			try {
				apiClient.startApplication();
				LOGGER.info(String.format("Application %s in account %s STARTED", accountName, applicationName));
			} catch (IOException e) {
				LOGGER.error(
						String.format("Error while starting application %s in accont %s", applicationName, accountName),
						e);
			}
		}

		private void stopProcess(String processId) {
			try {
				apiClient.stopApplicationProcess(processId);
				LOGGER.info(String.format("Process %s in account %s, application %s is stopped", processId, accountName,
						applicationName));
			} catch (IOException e) {
				LOGGER.error(String.format("Error while stopping process %s in account %s, application %s", processId,
						accountName, applicationName), e);
			}
		}

		private void removeCriticalMetric(String processId, Metric metric) {
			processMetricsMap.get(processId).remove(metric);
			LOGGER.info(String.format("Metric %s for application %s has recovered from critical state",
					metric.getName(), applicationName));
		}

		private int updateCriticalMetric(String processId, Metric metric) {
			Map<Metric, Integer> criticalMetrics = processMetricsMap.get(processId);
			if (criticalMetrics == null) {
				criticalMetrics = new HashMap<>();
				processMetricsMap.put(processId, criticalMetrics);
			}

			Integer timesCritical = criticalMetrics.get(metric);
			if (timesCritical == null) {
				// First time critical
				timesCritical = 0;
			}
			timesCritical++;
			criticalMetrics.put(metric, timesCritical);
			processMetricsMap.put(processId, criticalMetrics);

			LOGGER.info(String.format("Application %s in account %s received critical state for metric %s",
					applicationName, accountName, metric.getName()));
			return timesCritical;
		}
	}
}