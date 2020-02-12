package ch.elexis.core.services.eenv;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IMessageTransporter;
import ch.elexis.core.services.internal.Bundle;

@Component
public class RocketchatMessageTransporter implements IMessageTransporter {
	
	/**
	 * The webhook integration token to use to send messages as station to rocketchat.
	 */
	public static final String CFG_ROCKETCHAT_STATION_INTEGRATION_TOKEN =
		"rocketchat-station-integration-token";
	
	@Reference
	private IElexisEnvironmentService elexisEnvironmentService;
	
	@Reference
	private IConfigService configService;
	
	@Override
	public String getUriScheme(){
		return "rocketchat";
	}
	
	@Override
	public boolean isExternal(){
		return false;
	}
	
	@Override
	public IStatus send(TransientMessage message){
		return sendFromStationSender(message);
	}
	
	private IStatus sendFromStationSender(TransientMessage message){
		String integrationToken =
			configService.getLocal(CFG_ROCKETCHAT_STATION_INTEGRATION_TOKEN, null);
		if (integrationToken != null) {
			try {
				URL integrationUrl = new URL(
					elexisEnvironmentService.getRocketchatIntegrationBaseUrl() + integrationToken);
				
				String jsonMessage = prepareRocketchatMessage(message);
				return send(integrationUrl, jsonMessage.getBytes());
				
			} catch (IOException e) {
				return new Status(IStatus.ERROR, Bundle.ID, e.getMessage());
			}
		}
		
		return new Status(IStatus.ERROR, Bundle.ID,
			"No webhook integration token [" + CFG_ROCKETCHAT_STATION_INTEGRATION_TOKEN
				+ "] found in local config or malformed url.");
	}
	
	private String prepareRocketchatMessage(TransientMessage message){
		JSONObject json = new JSONObject();
		json.put("username", message.getSender());
		
		StringBuilder header = new StringBuilder();
		header
			.append("@" + message.getReceiver().substring(message.getReceiver().indexOf(':') + 1));
		
		Set<Entry<String, String>> entrySet = message.getMessageCodes().entrySet();
		if (!entrySet.isEmpty()) {
			header.append(" | ");
			message.getMessageCodes().entrySet()
				.forEach(c -> header.append(c.getKey() + ":" + c.getValue() + " "));
		}
		
		json.put("text", header.toString());
		
		Map<String, Object> params = new HashMap<>();
		params.put("color", "#0000FF");
		params.put("text", message.getMessageText());
		
		json.put("attachments", Collections.singletonList(params));
		
		return json.toString();
	}
	
	private IStatus send(URL url, byte[] postDataBytes) throws IOException{
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		con.getOutputStream().write(postDataBytes);
		
		int responseCode = con.getResponseCode();
		if (responseCode == 200) {
			return Status.OK_STATUS;
		}
		return new Status(IStatus.ERROR, Bundle.ID,
			"Error sending, with response code: " + responseCode);
	}
	
}
