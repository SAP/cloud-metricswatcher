package com.sap.hana.cloud.samples.mail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sap.hana.cloud.samples.model.ApplicationIdentifier;
import com.sap.hana.cloud.samples.response.model.Application;

public class ApiClient {
	private static final Logger LOGGER = Logger.getLogger(ApiClient.class);

	private final String user;
	private final String password;
	private static final Gson gson = new Gson();

	private static final String STOPPED_STATE = "STOPPED";
	private static final String STARTED_STATE = "STARTED";

	private final String landscapeFqdn;
	private final String applicationName;
	private final String accountName;

	private String sessionId;
	private String csrfToken;

	public ApiClient(ApplicationIdentifier applicationIdentifier, String user, String password) {
		Logger.getRootLogger().setLevel(Level.INFO);

		this.user = user;
		this.password = password;
		this.applicationName = applicationIdentifier.getApplication();
		this.accountName = applicationIdentifier.getAccount();
		this.landscapeFqdn = applicationIdentifier.getLandscapeFqdn();
		this.csrfToken = getCSRFToken();
	}

	public Application getApplication() throws IOException, ClientProtocolException, InterruptedException {
		String responseString = null;
		try {
			LOGGER.info(String.format("Attempting to retrieve logs for application %s", applicationName));

			HttpGet apiRequest = new HttpGet(getApplicationMetricsUrl());
			DefaultHttpClient httpClient = createHttpClient();
			HttpResponse apiResponse = httpClient.execute(apiRequest);
			responseString = EntityUtils.toString(apiResponse.getEntity());
			List<Application> apps = gson.fromJson(responseString, new TypeToken<List<Application>>() {
			}.getType());
			if (apps != null && apps.size() > 0) {
				LOGGER.info(String.format("Received metrics for application %s", applicationName));
				EntityUtils.consume(apiResponse.getEntity());
				return apps.get(0);
			}
			LOGGER.info(String.format("No metrics have been retrieved for applicaiton %s", applicationName));
			return null;
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format("Invalid JSON response for application %s", applicationName, responseString), e);
		}
		return null;
	}

	public void startApplication() throws ClientProtocolException, IOException {
		String body = "{\"applicationState\":\"STARTED\"}";

		HttpPut put = new HttpPut(getStateUrl());
		put = setRequestHeaders(put, body);

		DefaultHttpClient httpClient = createHttpClient();
		HttpResponse apiResponse = httpClient.execute(put);
		LOGGER.info(String.format("Application %s in account %s is starting", applicationName, accountName));
		EntityUtils.consume(apiResponse.getEntity());

		awaitOperationCompletion(STARTED_STATE);

		try {
			LOGGER.info("Application started, waiting 60 seconds for metrics to be generated");
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void stopApplicationProcess(String processId) throws ClientProtocolException, IOException {
		String body = "{\"processState\":\"STOPPED\"}";
		HttpPut put = new HttpPut(getStopUrl(processId));
		put = setRequestHeaders(put, body);

		DefaultHttpClient httpClient = createHttpClient();
		HttpResponse apiResponse = httpClient.execute(put);
		LOGGER.info(String.format("Application %s in account %s is stopping", applicationName, accountName));
		EntityUtils.consume(apiResponse.getEntity());

		awaitOperationCompletion(STOPPED_STATE);
	}

	private DefaultHttpClient createHttpClient() {
		DefaultHttpClient apiClient = new DefaultHttpClient();
		apiClient.getCredentialsProvider().setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(user, password));
		return apiClient;
	}

	private HttpPut setRequestHeaders(HttpPut put, String body) throws UnsupportedEncodingException {
		put.setHeader("Content-type", "application/json");
		put.setHeader("Accept", "application/json");
		put.setHeader("X-CSRF-Token", csrfToken);
		put.addHeader("Cookie", sessionId);
		put.setEntity(new StringEntity(body));
		return put;
	}

	private void awaitOperationCompletion(String expectedState) {
		while (!expectedState.equalsIgnoreCase(getApplicationState())) {
			try {
				LOGGER.info(String.format(
						"Application %s in account %s - state change is still ongoing. Will check again in 10 seconds",
						applicationName, accountName));
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public String getApplicationState() {
		String responseString = null;
		String applicationState = null;
		try {
			HttpGet apiRequest = new HttpGet(getStateUrl() + "?appStateOnly=true");
			DefaultHttpClient httpClient = createHttpClient();
			HttpResponse apiResponse = httpClient.execute(apiRequest);
			HttpEntity entity = apiResponse.getEntity();
			responseString = EntityUtils.toString(entity);
			JsonObject appStateJson = new Gson().fromJson(responseString, JsonObject.class);
			applicationState = appStateJson.getAsJsonObject("entity").getAsJsonPrimitive("applicationState")
					.getAsString();
			LOGGER.info(String.format("Application %s in account %s - received applciation state: %s", applicationName,
					accountName, applicationState));
			EntityUtils.consume(entity);
		} catch (Exception e) {
			LOGGER.error(String.format("Could not obtain state for application %s", applicationName), e);
		}
		return applicationState;
	}

	private String getCSRFToken() {
		String url = getCSRFTokenUrl();
		String responseString = null;
		try {
			HttpGet apiRequest = new HttpGet(url);
			apiRequest.setHeader("X-CSRF-Token", "Fetch");

			DefaultHttpClient httpClient = createHttpClient();
			HttpResponse apiResponse = httpClient.execute(apiRequest);
			responseString = apiResponse.getFirstHeader("X-CSRF-Token").getValue();

			Header sessionHeader = apiResponse.getFirstHeader("Set-Cookie");
			if (sessionHeader != null) {
				sessionId = sessionHeader.getValue();
			}

			EntityUtils.consume(apiResponse.getEntity());
		} catch (Exception e) {
			LOGGER.error(String.format("Error while creating CSRF token for application %s in accont %s",
					applicationName, accountName), e);
		}
		return responseString;
	}

	private String getCSRFTokenUrl() {
		return String.format("https://%s/lifecycle/v1/csrf", landscapeFqdn);
	}

	private String getStateUrl() {
		return String.format("https://%s/lifecycle/v1/accounts/%s/apps/%s/state", landscapeFqdn, accountName,
				applicationName);
	}

	private String getStopUrl(String processId) {
		return String.format("https://%s/lifecycle/v1/shortcuts/processes/%s/state", landscapeFqdn, processId);
	}

	private String getApplicationMetricsUrl() {
		return String.format("https://%s/monitoring/v2/accounts/%s/apps/%s/metrics", landscapeFqdn, accountName,
				applicationName);
	}

}
