package nl.tudelft.bw4t.server;

import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.bw4t.scenariogui.BW4TClientConfig;
import nl.tudelft.bw4t.scenariogui.BotConfig;
import nl.tudelft.bw4t.scenariogui.EPartnerConfig;

/**
 * This stores the entities and their type that a client requests when
 * connecting to the server
 *
 */
class ClientInfo {

	private BW4TClientConfig clientConfig;
	private List<BotConfig> requestedBots = new ArrayList<>();
	private List<EPartnerConfig> requestedEPartners = new ArrayList<>();
	private final String name;

	public ClientInfo(String uniqueName, int reqAgent, int reqHuman) {
		assert reqHuman >= 0;
		assert reqAgent >= 0;
		name = uniqueName;
		this.clientConfig = new BW4TClientConfig();
		if (reqAgent > 0) {
			BotConfig bot = BotConfig.createDefaultRobot();
			bot.setBotAmount(reqAgent);
			clientConfig.addBot(bot);
			requestedBots.add(bot);
		}
		if (reqHuman > 0) {
			BotConfig bot = BotConfig.createDefaultHumans();
			bot.setBotAmount(reqHuman);
			clientConfig.addBot(bot);
			requestedBots.add(bot);
		}
	}

	/**
	 * 
	 * @param clientConfig
	 * @param uniqueName
	 *            the {@link RemoteServer#getClientHost() value}
	 */
	public ClientInfo(BW4TClientConfig clientConfig, String uniqueName) {
		assert clientConfig != null;

		name = uniqueName;
		if (clientConfig.getBots() != null) {
			this.requestedBots = clientConfig.getBots();
		}
		if (clientConfig.getEpartners() != null) {
			this.requestedEPartners = clientConfig.getEpartners();
		}
		this.clientConfig = clientConfig;
	}

	public List<BotConfig> getRequestedBots() {
		return requestedBots;
	}

	public List<EPartnerConfig> getRequestedEPartners() {
		return requestedEPartners;
	}

	public String getMapFile() {
		return clientConfig == null ? "" : clientConfig.getMapFile();
	}

	public boolean isCollisionEnabled() {
		return clientConfig.isCollisionEnabled();
	}

	public boolean isVisualizePaths() {
		return clientConfig.isVisualizePaths();
	}

	public Object getName() {
		return name;
	}
}
