package orion.sdk.monitoring.incidents;

import java.util.List;

public interface IIncidentListener
{
	public void processIncidents(List<Incident> incidents) throws Exception;
}
