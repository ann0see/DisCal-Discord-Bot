package com.cloudcraftgaming.discal.api.database;

import com.cloudcraftgaming.discal.api.crypto.KeyGenerator;
import com.cloudcraftgaming.discal.api.enums.announcement.AnnouncementType;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.announcement.Announcement;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.object.event.EventData;
import com.cloudcraftgaming.discal.api.object.event.RsvpData;
import com.cloudcraftgaming.discal.api.object.web.UserAPIAccount;
import com.cloudcraftgaming.discal.logger.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"SqlResolve", "UnusedReturnValue", "SqlNoDataSourceInspection"})
public class DatabaseManager {
	private static DatabaseManager instance;
	private DatabaseInfo databaseInfo;

	private DatabaseManager() {
	} //Prevent initialization.

	/**
	 * Gets the instance of the {@link DatabaseManager}.
	 *
	 * @return The instance of the {@link DatabaseManager}
	 */
	public static DatabaseManager getManager() {
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

	/**
	 * Connects to the MySQL server specified.
	 *
	 */
	public void connectToMySQL() {
		try {
			MySQL mySQL = new MySQL(BotSettings.SQL_HOST.get(), BotSettings.SQL_PORT.get(), BotSettings.SQL_DB.get(), BotSettings.SQL_PREFIX.get(), BotSettings.SQL_USER.get(), BotSettings.SQL_PASSWORD.get());

			Connection mySQLConnection = mySQL.openConnection();
			databaseInfo = new DatabaseInfo(mySQL, mySQLConnection, mySQL.getPrefix());
			System.out.println("Connected to MySQL database!");
		} catch (Exception e) {
			System.out.println("Failed to connect to MySQL database! Is it properly configured?");
			e.printStackTrace();
			Logger.getLogger().exception(null, "Connecting to MySQL server failed.", e, this.getClass(), true);
		}
	}

	/**
	 * Disconnects from the MySQL server if still connected.
	 */
	public void disconnectFromMySQL() {
		if (databaseInfo != null) {
			try {
				databaseInfo.getMySQL().closeConnection();
				System.out.println("Successfully disconnected from MySQL Database!");
			} catch (SQLException e) {
				Logger.getLogger().exception(null, "Disconnecting from MySQL failed.", e, this.getClass(), true);
				System.out.println("MySQL Connection may not have closed properly! Data may be invalidated!");
			}
		}
	}

	/**
	 * Creates all required tables in the database if they do not exist.
	 */
	public void createTables() {
		try {
			Statement statement = databaseInfo.getConnection().createStatement();

			String announcementTableName = String.format("%sannouncements", databaseInfo.getPrefix());
			String calendarTableName = String.format("%scalendars", databaseInfo.getPrefix());
			String settingsTableName = String.format("%sguild_settings", databaseInfo.getPrefix());
			String eventTableName = String.format("%sevents", databaseInfo.getPrefix());
			String rsvpTableName = String.format("%srsvp", databaseInfo.getPrefix());
			String apiTableName = String.format("%sapi", databaseInfo.getPrefix());
			String createSettingsTable = "CREATE TABLE IF NOT EXISTS " + settingsTableName +
					"(GUILD_ID VARCHAR(255) not NULL, " +
					" EXTERNAL_CALENDAR BOOLEAN not NULL, " +
					" PRIVATE_KEY VARCHAR(16) not NULL, " +
					" ACCESS_TOKEN LONGTEXT not NULL, " +
					" REFRESH_TOKEN LONGTEXT not NULL, " +
					" CONTROL_ROLE LONGTEXT not NULL, " +
					" DISCAL_CHANNEL LONGTEXT not NULL, " +
					" SIMPLE_ANNOUNCEMENT BOOLEAN not NULL, " +
					" LANG VARCHAR(255) not NULL, " +
					" PREFIX VARCHAR(255) not NULL, " +
					" PATRON_GUILD BOOLEAN not NULL, " +
					" DEV_GUILD BOOLEAN not NULL, " +
					" MAX_CALENDARS INTEGER not NULL, " +
					" DM_ANNOUNCEMENTS LONGTEXT not NULL, " +
					" 12_HOUR BOOLEAN not NULL, " +
					" BRANDED BOOLEAN not NULL, " +
					" PRIMARY KEY (GUILD_ID))";
			String createAnnouncementTable = "CREATE TABLE IF NOT EXISTS " + announcementTableName +
					" (ANNOUNCEMENT_ID VARCHAR(255) not NULL, " +
					" GUILD_ID VARCHAR(255) not NULL, " +
					" SUBSCRIBERS_ROLE LONGTEXT not NULL, " +
					" SUBSCRIBERS_USER LONGTEXT not NULL, " +
					" CHANNEL_ID VARCHAR(255) not NULL, " +
					" ANNOUNCEMENT_TYPE VARCHAR(255) not NULL, " +
					" EVENT_ID LONGTEXT not NULL, " +
					" EVENT_COLOR VARCHAR(255) not NULL, " +
					" HOURS_BEFORE INTEGER not NULL, " +
					" MINUTES_BEFORE INTEGER not NULL, " +
					" INFO LONGTEXT not NULL, " +
					" ENABLED BOOLEAN not NULL, " +
					" INFO_ONLY BOOLEAN not NULL, " +
					" PRIMARY KEY (ANNOUNCEMENT_ID))";
			String createCalendarTable = "CREATE TABLE IF NOT EXISTS " + calendarTableName +
					" (GUILD_ID VARCHAR(255) not NULL, " +
					" CALENDAR_NUMBER INTEGER not NULL, " +
					" CALENDAR_ID VARCHAR(255) not NULL, " +
					" CALENDAR_ADDRESS LONGTEXT not NULL, " +
					" EXTERNAL BOOLEAN not NULL , " +
					" PRIMARY KEY (GUILD_ID, CALENDAR_NUMBER))";
			String createEventTable = "CREATE TABLE IF NOT EXISTS " + eventTableName +
					" (GUILD_ID VARCHAR(255) not NULL, " +
					" EVENT_ID VARCHAR(255) not NULL, " +
					" EVENT_END LONG not NULL, " +
					" IMAGE_LINK LONGTEXT, " +
					" PRIMARY KEY (GUILD_ID, EVENT_ID))";
			String createRsvpTable = "CREATE TABLE IF NOT EXISTS " + rsvpTableName +
					" (GUILD_ID VARCHAR(255) not NULL, " +
					" EVENT_ID VARCHAR(255) not NULL, " +
					" EVENT_END LONG not NULL, " +
					" GOING_ON_TIME LONGTEXT, " +
					" GOING_LATE LONGTEXT, " +
					" NOT_GOING LONGTEXT, " +
					" UNDECIDED LONGTEXT, " +
					" PRIMARY KEY (GUILD_ID, EVENT_ID))";
			String createAPITable = "CREATE TABLE IF NOT EXISTS " + apiTableName +
					" (USER_ID varchar(255) not NULL, " +
					" API_KEY varchar(64) not NULL, " +
					" BLOCKED BOOLEAN not NULL, " +
					" TIME_ISSUED LONG not NULL, " +
					" USES INT not NULL, " +
					" PRIMARY KEY (USER_ID, API_KEY))";
			statement.executeUpdate(createAnnouncementTable);
			statement.executeUpdate(createSettingsTable);
			statement.executeUpdate(createCalendarTable);
			statement.executeUpdate(createEventTable);
			statement.executeUpdate(createRsvpTable);
			statement.executeUpdate(createAPITable);
			statement.close();
			System.out.println("Successfully created needed tables in MySQL database!");
		} catch (SQLException e) {
			System.out.println("Failed to created database tables! Something must be wrong.");
			Logger.getLogger().exception(null, "Creating MySQL tables failed", e, this.getClass(), true);
			e.printStackTrace();
		}
	}

	public boolean updateAPIAccount(UserAPIAccount acc) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String tableName = String.format("%sapi", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + tableName + " WHERE API_KEY = '" + String.valueOf(acc.getAPIKey()) + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				Boolean hasStuff = res.next();

				if (!hasStuff || res.getString("API_KEY") == null) {
					//Data not present, add to DB.
					String insertCommand = "INSERT INTO " + tableName +
							"(USER_ID, API_KEY, BLOCKED, TIME_ISSUED, USES)" +
							" VALUES (?, ?, ?, ?, ?);";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
					ps.setString(1, acc.getUserId());
					ps.setString(2, acc.getAPIKey());
					ps.setBoolean(3, acc.isBlocked());
					ps.setLong(4, acc.getTimeIssued());
					ps.setInt(5, acc.getUses());

					ps.executeUpdate();
					ps.close();
					statement.close();
				} else {
					//Data present, update.
					String update = "UPDATE " + tableName
							+ " SET USER_ID = ?, BLOCKED = ?,"
							+ " USES = ? WHERE API_KEY = ?";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(update);

					ps.setString(1, acc.getUserId());
					ps.setBoolean(2, acc.isBlocked());
					ps.setInt(3, acc.getUses());
					ps.setString(4, acc.getAPIKey());

					ps.executeUpdate();

					ps.close();
					statement.close();
				}
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.getLogger().exception(null, "Failed to update API account", e, this.getClass(), true);
		}
		return false;
	}

	public boolean updateSettings(GuildSettings settings) {
		if (settings.getPrivateKey().equalsIgnoreCase("N/a")) {
			settings.setPrivateKey(KeyGenerator.csRandomAlphaNumericString(16));
		}
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String dataTableName = String.format("%sguild_settings", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + dataTableName + " WHERE GUILD_ID = '" + String.valueOf(settings.getGuildID()) + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				Boolean hasStuff = res.next();

				if (!hasStuff || res.getString("GUILD_ID") == null) {
					//Data not present, add to DB.
					String insertCommand = "INSERT INTO " + dataTableName +
							"(GUILD_ID, EXTERNAL_CALENDAR, PRIVATE_KEY, ACCESS_TOKEN, REFRESH_TOKEN, CONTROL_ROLE, DISCAL_CHANNEL, SIMPLE_ANNOUNCEMENT, LANG, PREFIX, PATRON_GUILD, DEV_GUILD, MAX_CALENDARS, DM_ANNOUNCEMENTS, 12_HOUR, BRANDED)" +
							" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
					ps.setString(1, String.valueOf(settings.getGuildID()));
					ps.setBoolean(2, settings.useExternalCalendar());
					ps.setString(3, settings.getPrivateKey());
					ps.setString(4, settings.getEncryptedAccessToken());
					ps.setString(5, settings.getEncryptedRefreshToken());
					ps.setString(6, settings.getControlRole());
					ps.setString(7, settings.getDiscalChannel());
					ps.setBoolean(8, settings.usingSimpleAnnouncements());
					ps.setString(9, settings.getLang());
					ps.setString(10, settings.getPrefix());
					ps.setBoolean(11, settings.isPatronGuild());
					ps.setBoolean(12, settings.isDevGuild());
					ps.setInt(13, settings.getMaxCalendars());
					ps.setString(14, settings.getDmAnnouncementsString());
					ps.setBoolean(15, settings.useTwelveHour());
					ps.setBoolean(16, settings.isBranded());


					ps.executeUpdate();
					ps.close();
					statement.close();
				} else {
					//Data present, update.
					String update = "UPDATE " + dataTableName
							+ " SET EXTERNAL_CALENDAR = ?, PRIVATE_KEY = ?,"
							+ " ACCESS_TOKEN = ?, REFRESH_TOKEN = ?,"
							+ " CONTROL_ROLE = ?, DISCAL_CHANNEL = ?, SIMPLE_ANNOUNCEMENT = ?,"
							+ " LANG = ?, PREFIX = ?, PATRON_GUILD = ?, DEV_GUILD = ?,"
							+ " MAX_CALENDARS = ?, DM_ANNOUNCEMENTS = ?, 12_HOUR = ?,"
							+ " BRANDED = ? WHERE GUILD_ID = ?";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(update);

					ps.setBoolean(1, settings.useExternalCalendar());
					ps.setString(2, settings.getPrivateKey());
					ps.setString(3, settings.getEncryptedAccessToken());
					ps.setString(4, settings.getEncryptedRefreshToken());
					ps.setString(5, settings.getControlRole());
					ps.setString(6, settings.getDiscalChannel());
					ps.setBoolean(7, settings.usingSimpleAnnouncements());
					ps.setString(8, settings.getLang());
					ps.setString(9, settings.getPrefix());
					ps.setBoolean(10, settings.isPatronGuild());
					ps.setBoolean(11, settings.isDevGuild());
					ps.setInt(12, settings.getMaxCalendars());
					ps.setString(13, settings.getDmAnnouncementsString());
					ps.setBoolean(14, settings.useTwelveHour());
					ps.setBoolean(15, settings.isBranded());
					ps.setString(16, String.valueOf(settings.getGuildID()));

					ps.executeUpdate();

					ps.close();
					statement.close();
				}
				return true;
			}
		} catch (SQLException e) {
			System.out.println("Failed to input data into database! Error Code: 00101");
			Logger.getLogger().exception(null, "Failed to update/insert guild settings.", e, this.getClass(), true);
			e.printStackTrace();
		}
		return false;
	}

	public boolean updateCalendar(CalendarData calData) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String calendarTableName = String.format("%scalendars", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = '" + String.valueOf(calData.getGuildId()) + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				Boolean hasStuff = res.next();

				if (!hasStuff || res.getString("GUILD_ID") == null) {
					//Data not present, add to DB.
					String insertCommand = "INSERT INTO " + calendarTableName +
							"(GUILD_ID, CALENDAR_NUMBER, CALENDAR_ID, CALENDAR_ADDRESS, EXTERNAL)" +
							" VALUES (?, ?, ?, ?, ?);";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
					ps.setString(1, String.valueOf(calData.getGuildId()));
					ps.setInt(2, calData.getCalendarNumber());
					ps.setString(3, calData.getCalendarId());
					ps.setString(4, calData.getCalendarAddress());
					ps.setBoolean(5, calData.isExternal());

					ps.executeUpdate();
					ps.close();
					statement.close();
				} else {
					//Data present, update.
					String update = "UPDATE " + calendarTableName
							+ " SET CALENDAR_NUMBER = ?, CALENDAR_ID = ?,"
							+ " CALENDAR_ADDRESS = ?, EXTERNAL = ?"
							+ " WHERE GUILD_ID = ?";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(update);
					ps.setInt(1, calData.getCalendarNumber());
					ps.setString(2, calData.getCalendarId());
					ps.setString(3, calData.getCalendarAddress());
					ps.setBoolean(4, calData.isExternal());
					ps.setString(5, String.valueOf(calData.getGuildId()));

					ps.executeUpdate();

					ps.close();
					statement.close();
				}
				return true;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to update/insert calendar data.", e, this.getClass(), true);
		}
		return false;
	}

	/**
	 * Updates or Adds the specified {@link Announcement} Object to the database.
	 *
	 * @param announcement The announcement object to add to the database.
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	public Boolean updateAnnouncement(Announcement announcement) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String announcementTableName = String.format("%sannouncements", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = '" + announcement.getAnnouncementId() + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				Boolean hasStuff = res.next();

				if (!hasStuff || res.getString("ANNOUNCEMENT_ID") == null) {
					//Data not present, add to db.
					String insertCommand = "INSERT INTO " + announcementTableName +
							"(ANNOUNCEMENT_ID, GUILD_ID, SUBSCRIBERS_ROLE, SUBSCRIBERS_USER, CHANNEL_ID, ANNOUNCEMENT_TYPE, EVENT_ID, EVENT_COLOR, HOURS_BEFORE, MINUTES_BEFORE, INFO, ENABLED, INFO_ONLY)" +
							" VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
					ps.setString(1, announcement.getAnnouncementId().toString());
					ps.setString(2, String.valueOf(announcement.getGuildId()));
					ps.setString(3, announcement.getSubscriberRoleIdString());
					ps.setString(4, announcement.getSubscriberUserIdString());
					ps.setString(5, announcement.getAnnouncementChannelId());
					ps.setString(6, announcement.getAnnouncementType().name());
					ps.setString(7, announcement.getEventId());
					ps.setString(8, announcement.getEventColor().name());
					ps.setInt(9, announcement.getHoursBefore());
					ps.setInt(10, announcement.getMinutesBefore());
					ps.setString(11, announcement.getInfo());
					ps.setBoolean(12, announcement.isEnabled());
					ps.setBoolean(13, announcement.isInfoOnly());

					ps.executeUpdate();
					ps.close();
					statement.close();
				} else {
					//Data present, update.

					String update = "UPDATE " + announcementTableName
							+ " SET SUBSCRIBERS_ROLE = ?, SUBSCRIBERS_USER = ?, CHANNEL_ID = ?,"
							+ " ANNOUNCEMENT_TYPE = ?, EVENT_ID = ?, EVENT_COLOR = ?, "
							+ " HOURS_BEFORE = ?, MINUTES_BEFORE = ?,"
							+ " INFO = ?, ENABLED = ?, INFO_ONLY = ?"
							+ " WHERE ANNOUNCEMENT_ID = ?";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(update);

					ps.setString(1, announcement.getSubscriberRoleIdString());
					ps.setString(2, announcement.getSubscriberUserIdString());
					ps.setString(3, announcement.getAnnouncementChannelId());
					ps.setString(4, announcement.getAnnouncementType().name());
					ps.setString(5, announcement.getEventId());
					ps.setString(6, announcement.getEventColor().name());
					ps.setInt(7, announcement.getHoursBefore());
					ps.setInt(8, announcement.getMinutesBefore());
					ps.setString(9, announcement.getInfo());
					ps.setBoolean(10, announcement.isEnabled());
					ps.setBoolean(11, announcement.isInfoOnly());

					ps.setString(12, announcement.getAnnouncementId().toString());

					ps.executeUpdate();

					ps.close();
					statement.close();
				}
				return true;
			}
		} catch (SQLException e) {
			System.out.print("Failed to input announcement data! Error Code: 00201");
			Logger.getLogger().exception(null, "Failed to update/insert announcement.", e, this.getClass(), true);
			e.printStackTrace();
		}
		return false;
	}

	public Boolean updateEventData(EventData data) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String eventTableName = String.format("%sevents", databaseInfo.getPrefix());

				if (data.getEventId().contains("_")) {
					data.setEventId(data.getEventId().split("_")[0]);
				}

				String query = "SELECT * FROM " + eventTableName + " WHERE EVENT_ID = '" + data.getEventId() + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				Boolean hasStuff = res.next();

				if (!hasStuff || res.getString("EVENT_ID") == null) {
					//Data not present, add to DB.
					String insertCommand = "INSERT INTO " + eventTableName +
							"(GUILD_ID, EVENT_ID, EVENT_END, IMAGE_LINK)" +
							" VALUES (?, ?, ?, ?)";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
					ps.setString(1, String.valueOf(data.getGuildId()));
					ps.setString(2, data.getEventId());
					ps.setLong(3, data.getEventEnd());
					ps.setString(4, data.getImageLink());

					ps.executeUpdate();
					ps.close();
					statement.close();
				} else {
					//Data present, update.
					String update = "UPDATE " + eventTableName
							+ " SET IMAGE_LINK = ?, EVENT_END = ?"
							+ " WHERE EVENT_ID = ?";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(update);

					ps.setString(1, data.getImageLink());
					ps.setLong(2, data.getEventEnd());
					ps.setString(3, data.getEventId());

					ps.executeUpdate();

					ps.close();
					statement.close();
				}
				return true;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to update/insert event data.", e, this.getClass(), true);
		}
		return false;
	}

	public Boolean updateRsvpData(RsvpData data) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String rsvpTableName = String.format("%srsvp", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + rsvpTableName + " WHERE EVENT_ID = '" + data.getEventId() + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				Boolean hasStuff = res.next();

				if (!hasStuff || res.getString("EVENT_ID") == null) {
					//Data not present, add to DB.
					String insertCommand = "INSERT INTO " + rsvpTableName +
							"(GUILD_ID, EVENT_ID, EVENT_END, GOING_ON_TIME, GOING_LATE, NOT_GOING, UNDECIDED)" +
							" VALUES (?, ?, ?, ?, ?, ?, ?)";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
					ps.setString(1, String.valueOf(data.getGuildId()));
					ps.setString(2, data.getEventId());
					ps.setLong(3, data.getEventEnd());
					ps.setString(4, data.getGoingOnTimeString());
					ps.setString(5, data.getGoingLateString());
					ps.setString(6, data.getNotGoingString());
					ps.setString(7, data.getUndecidedString());

					ps.executeUpdate();
					ps.close();
					statement.close();
				} else {
					//Data present, update.
					String update = "UPDATE " + rsvpTableName
							+ " SET EVENT_END = ?,"
							+ " GOING_ON_TIME = ?,"
							+ " GOING_LATE = ?,"
							+ " NOT_GOING = ?,"
							+ " UNDECIDED = ?"
							+ " WHERE EVENT_ID = ?";
					PreparedStatement ps = databaseInfo.getConnection().prepareStatement(update);

					ps.setLong(1, data.getEventEnd());
					ps.setString(2, data.getGoingOnTimeString());
					ps.setString(3, data.getGoingLateString());
					ps.setString(4, data.getNotGoingString());
					ps.setString(5, data.getUndecidedString());
					ps.setString(6, data.getEventId());

					ps.executeUpdate();

					ps.close();
					statement.close();
				}
				return true;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to update/insert event data.", e, this.getClass(), true);
		}
		return false;
	}

	public UserAPIAccount getAPIAccount(String APIKey) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String dataTableName = String.format("%sapi", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + dataTableName + " WHERE API_KEY = '" + APIKey + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				Boolean hasStuff = res.next();

				if (hasStuff && res.getString("API_KEY") != null) {
					UserAPIAccount account = new UserAPIAccount();
					account.setAPIKey(APIKey);
					account.setUserId(res.getString("USER_ID"));
					account.setBlocked(res.getBoolean("BLOCKED"));
					account.setTimeIssued(res.getLong("TIME_ISSUED"));
					account.setUses(res.getInt("USES"));

					statement.close();

					return account;
				} else {
					//Data not present.
					statement.close();
					return null;
				}
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get API Account.", e, this.getClass(), true);
		}
		return null;
	}

	public GuildSettings getSettings(long guildId) {
		GuildSettings settings = new GuildSettings(guildId);
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String dataTableName = String.format("%sguild_settings", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + dataTableName + " WHERE GUILD_ID = '" + String.valueOf(guildId) + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				Boolean hasStuff = res.next();

				if (hasStuff && res.getString("GUILD_ID") != null) {
					settings.setUseExternalCalendar(res.getBoolean("EXTERNAL_CALENDAR"));
					settings.setPrivateKey(res.getString("PRIVATE_KEY"));
					settings.setEncryptedAccessToken(res.getString("ACCESS_TOKEN"));
					settings.setEncryptedRefreshToken(res.getString("REFRESH_TOKEN"));
					settings.setControlRole(res.getString("CONTROL_ROLE"));
					settings.setDiscalChannel(res.getString("DISCAL_CHANNEL"));
					settings.setSimpleAnnouncements(res.getBoolean("SIMPLE_ANNOUNCEMENT"));
					settings.setLang(res.getString("LANG"));
					settings.setPrefix(res.getString("PREFIX"));
					settings.setPatronGuild(res.getBoolean("PATRON_GUILD"));
					settings.setDevGuild(res.getBoolean("DEV_GUILD"));
					settings.setMaxCalendars(res.getInt("MAX_CALENDARS"));
					settings.setDmAnnouncementsFromString(res.getString("DM_ANNOUNCEMENTS"));
					settings.setTwelveHour(res.getBoolean("12_HOUR"));
					settings.setBranded(res.getBoolean("BRANDED"));

					statement.close();
				} else {
					//Data not present.
					statement.close();
					return settings;
				}
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get Guild Settings.", e, this.getClass(), true);
		}
		return settings;
	}

	public ArrayList<GuildSettings> getAllSettings() {
		ArrayList<GuildSettings> allSettings = new ArrayList<>();
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String dataTableName = String.format("%sguild_settings", databaseInfo.getPrefix());

				PreparedStatement stmt = databaseInfo.getConnection().prepareStatement("SELECT * FROM " + dataTableName);
				ResultSet res = stmt.executeQuery();

				while (res.next()) {
					if (res.getString("GUILD_ID") != null) {
						GuildSettings settings = new GuildSettings(Long.valueOf(res.getString("GUILD_ID")));
						settings.setUseExternalCalendar(res.getBoolean("EXTERNAL_CALENDAR"));
						settings.setPrivateKey(res.getString("PRIVATE_KEY"));
						settings.setEncryptedAccessToken(res.getString("ACCESS_TOKEN"));
						settings.setEncryptedRefreshToken(res.getString("REFRESH_TOKEN"));
						settings.setControlRole(res.getString("CONTROL_ROLE"));
						settings.setDiscalChannel(res.getString("DISCAL_CHANNEL"));
						settings.setSimpleAnnouncements(res.getBoolean("SIMPLE_ANNOUNCEMENT"));
						settings.setLang(res.getString("LANG"));
						settings.setPrefix(res.getString("PREFIX"));
						settings.setPatronGuild(res.getBoolean("PATRON_GUILD"));
						settings.setDevGuild(res.getBoolean("DEV_GUILD"));
						settings.setMaxCalendars(res.getInt("MAX_CALENDARS"));
						settings.setDmAnnouncementsFromString(res.getString("DM_ANNOUNCEMENTS"));
						settings.setBranded(res.getBoolean("BRANDED"));

						allSettings.add(settings);
					}
				}
				stmt.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all guild settings", e, this.getClass(), true);
		}
		return allSettings;
	}

	public CalendarData getMainCalendar(long guildId) {
		CalendarData calData = new CalendarData(guildId, 1);
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String calendarTableName = String.format("%scalendars", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = '" + String.valueOf(guildId) + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				while (res.next()) {
					if (res.getInt("CALENDAR_NUMBER") == 1) {
						calData.setCalendarId(res.getString("CALENDAR_ID"));
						calData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));
						calData.setExternal(res.getBoolean("EXTERNAL"));
						break;
					}
				}
				statement.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get calendar settings.", e, this.getClass(), true);
		}
		return calData;
	}

	public CalendarData getCalendar(long guildId, Integer calendarNumber) {
		CalendarData calData = new CalendarData(guildId, calendarNumber);
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String calendarTableName = String.format("%scalendars", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = '" + String.valueOf(guildId) + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				while (res.next()) {
					if (res.getInt("CALENDAR_NUMBER") == calendarNumber) {
						calData.setCalendarId(res.getString("CALENDAR_ID"));
						calData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));
						calData.setExternal(res.getBoolean("EXTERNAL"));
						break;
					}
				}
				statement.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get calendar data", e, this.getClass(), true);
		}
		return calData;
	}

	public ArrayList<CalendarData> getAllCalendars(long guildId) {
		ArrayList<CalendarData> calendars = new ArrayList<>();
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String calendarTableName = String.format("%scalendars", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = '" + String.valueOf(guildId) + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				while (res.next()) {
					CalendarData calData = new CalendarData(guildId, res.getInt("CALENDAR_NUMBER"));
					calData.setCalendarId(res.getString("CALENDAR_ID"));
					calData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));
					calData.setExternal(res.getBoolean("EXTERNAL"));
					calendars.add(calData);
				}
				statement.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all guild calendars.", e, this.getClass(), true);
		}
		return calendars;
	}

	public ArrayList<CalendarData> getAllCalendars() {
		ArrayList<CalendarData> calendars = new ArrayList<>();
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String calendarTableName = String.format("%scalendars", databaseInfo.getPrefix());

				PreparedStatement stmt = databaseInfo.getConnection().prepareStatement("SELECT * FROM " + calendarTableName);
				ResultSet res = stmt.executeQuery();

				while (res.next()) {
					CalendarData calData = new CalendarData(Long.valueOf(res.getString("GUILD_ID")), res.getInt("CALENDAR_NUMBER"));
					calData.setCalendarId(res.getString("CALENDAR_ID"));
					calData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));
					calData.setExternal(res.getBoolean("EXTERNAL"));
					calendars.add(calData);
				}
				stmt.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all calendars!", e, this.getClass(), true);
		}
		return calendars;
	}

	public Integer getCalendarCount() {
		Integer amount = -1;
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String calendarTableName = String.format("%scalendars", databaseInfo.getPrefix());

				String query = "SELECT COUNT(*) FROM " + calendarTableName + ";";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				if (res.next()) {
					amount = res.getInt(1);
				} else {
					amount = 0;
				}

				res.close();
				statement.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get calendar count", e, this.getClass(), true);
		}
		return amount;
	}

	public EventData getEventData(long guildId, String eventId) {
		EventData data = new EventData(guildId);

		if (eventId.contains("_")) {
			eventId = eventId.split("_")[0];
		}

		data.setEventId(eventId);

		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String eventTableName = String.format("%sevents", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + eventTableName + " WHERE GUILD_ID= '" + String.valueOf(guildId) + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				while (res.next()) {
					if (res.getString("EVENT_ID").equals(eventId)) {
						data.setEventEnd(res.getLong("EVENT_END"));
						data.setImageLink(res.getString("IMAGE_LINK"));
						break;
					}
				}
				statement.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get event data", e, this.getClass(), true);
		}
		return data;
	}

	public ArrayList<EventData> getAllEventData() {
		ArrayList<EventData> allData = new ArrayList<>();
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String eventTableName = String.format("%sevents", databaseInfo.getPrefix());

				PreparedStatement stmt = databaseInfo.getConnection().prepareStatement("SELECT * FROM " + eventTableName);
				ResultSet res = stmt.executeQuery();

				while (res.next()) {
					if (res.getString("EVENT_ID") != null) {
						EventData data = new EventData(Long.valueOf(res.getString("GUILD_ID")));

						data.setEventId(res.getString("EVENT_ID"));
						data.setEventEnd(res.getLong("EVENT_END"));
						data.setImageLink(res.getString("IMAGE_LINK"));

						allData.add(data);
					}
				}
				stmt.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all event data", e, this.getClass(), true);
		}
		return allData;
	}

	public RsvpData getRsvpData(long guildId, String eventId) {
		RsvpData data = new RsvpData(guildId);
		data.setEventId(eventId);
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String rsvpTableName = String.format("%srsvp", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + rsvpTableName + " WHERE GUILD_ID= '" + String.valueOf(guildId) + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				while (res.next()) {
					if (res.getString("EVENT_ID").equals(eventId)) {
						data.setEventEnd(res.getLong("EVENT_END"));
						data.setGoingOnTimeFromString(res.getString("GOING_ON_TIME"));
						data.setGoingLateFromString(res.getString("GOING_LATE"));
						data.setNotGoingFromString(res.getString("NOT_GOING"));
						data.setUndecidedFromString(res.getString("UNDECIDED"));
						break;
					}
				}
				statement.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get RSVP data for event", e, this.getClass(), true);
		}
		return data;
	}

	public ArrayList<RsvpData> getAllRsvpData() {
		ArrayList<RsvpData> dataList = new ArrayList<>();
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String rsvpTableName = String.format("%srsvp", databaseInfo.getPrefix());
				PreparedStatement stmt = databaseInfo.getConnection().prepareStatement("SELECT * FROM " + rsvpTableName);
				ResultSet res = stmt.executeQuery();

				while (res.next()) {
					if (res.getString("GUILD_ID") != null) {
						RsvpData data = new RsvpData(Long.valueOf(res.getString("GUILD_ID")));
						data.setEventId(res.getString("EVENT_ID"));
						data.setEventEnd(res.getLong("EVENT_END"));
						data.setGoingOnTimeFromString(res.getString("GOING_ON_TIME"));
						data.setGoingLateFromString(res.getString("GOING_LATE"));
						data.setNotGoingFromString(res.getString("NOT_GOING"));
						data.setUndecidedFromString(res.getString("UNDECIDED"));

						dataList.add(data);
					}
				}
				stmt.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all RSVP Data!", e, this.getClass(), true);
		}
		return dataList;
	}

	/**
	 * Gets the {@link Announcement} Object with the corresponding ID for the specified Guild.
	 *
	 * @param announcementId The ID of the announcement.
	 * @param guildId        The ID of the guild the Announcement belongs to.
	 * @return The {@link Announcement} with the specified ID if it exists, otherwise <c>null</c>.
	 */
	public Announcement getAnnouncement(UUID announcementId, long guildId) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String announcementTableName = String.format("%sannouncements", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = '" + announcementId.toString() + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				Boolean hasStuff = res.next();

				if (hasStuff && res.getString("ANNOUNCEMENT_ID") != null) {
					Announcement announcement = new Announcement(announcementId, guildId);
					announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
					announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
					announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
					announcement.setAnnouncementType(AnnouncementType.valueOf(res.getString("ANNOUNCEMENT_TYPE")));
					announcement.setEventId(res.getString("EVENT_ID"));
					announcement.setEventColor(EventColor.fromNameOrHexOrID(res.getString("EVENT_COLOR")));
					announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
					announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
					announcement.setInfo(res.getString("INFO"));
					announcement.setEnabled(res.getBoolean("ENABLED"));
					announcement.setInfoOnly(res.getBoolean("INFO_ONLY"));

					statement.close();
					return announcement;
				}
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get announcement data.", e, this.getClass(), true);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets an {@link ArrayList} of {@link Announcement}s belonging to the specific Guild.
	 *
	 * @param guildId The ID of the guild whose data is to be retrieved.
	 * @return An ArrayList of Announcements that belong to the specified Guild.
	 */
	public ArrayList<Announcement> getAnnouncements(long guildId) {
		ArrayList<Announcement> announcements = new ArrayList<>();
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String announcementTableName = String.format("%sannouncements", databaseInfo.getPrefix());

				String query = "SELECT * FROM " + announcementTableName + " WHERE GUILD_ID = '" + String.valueOf(guildId) + "';";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				while (res.next()) {
					if (res.getString("ANNOUNCEMENT_ID") != null) {
						Announcement announcement = new Announcement(UUID.fromString(res.getString("ANNOUNCEMENT_ID")), guildId);
						announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
						announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
						announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
						announcement.setAnnouncementType(AnnouncementType.valueOf(res.getString("ANNOUNCEMENT_TYPE")));
						announcement.setEventId(res.getString("EVENT_ID"));
						announcement.setEventColor(EventColor.fromNameOrHexOrID(res.getString("EVENT_COLOR")));
						announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
						announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
						announcement.setInfo(res.getString("INFO"));
						announcement.setEnabled(res.getBoolean("ENABLED"));
						announcement.setInfoOnly(res.getBoolean("INFO_ONLY"));

						announcements.add(announcement);
					}
				}

				statement.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all guild announcements.", e, this.getClass(), true);
			e.printStackTrace();
		}
		return announcements;
	}

	public ArrayList<Announcement> getAnnouncements() {
		ArrayList<Announcement> announcements = new ArrayList<>();
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String announcementTableName = String.format("%sannouncements", databaseInfo.getPrefix());

				PreparedStatement stmt = databaseInfo.getConnection().prepareStatement("SELECT * FROM " + announcementTableName);
				ResultSet res = stmt.executeQuery();

				while (res.next()) {
					if (res.getString("ANNOUNCEMENT_ID") != null) {
						Announcement announcement = new Announcement(UUID.fromString(res.getString("ANNOUNCEMENT_ID")), Long.valueOf(res.getString("GUILD_ID")));
						announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
						announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
						announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
						announcement.setAnnouncementType(AnnouncementType.valueOf(res.getString("ANNOUNCEMENT_TYPE")));
						announcement.setEventId(res.getString("EVENT_ID"));
						announcement.setEventColor(EventColor.fromNameOrHexOrID(res.getString("EVENT_COLOR")));
						announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
						announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
						announcement.setInfo(res.getString("INFO"));
						announcement.setEnabled(res.getBoolean("ENABLED"));
						announcement.setInfoOnly(res.getBoolean("INFO_ONLY"));

						announcements.add(announcement);
					}
				}

				stmt.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all announcements.", e, this.getClass(), true);
		}

		return announcements;
	}

	public ArrayList<Announcement> getEnabledAnnouncements() {
		ArrayList<Announcement> announcements = new ArrayList<>();
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String announcementTableName = String.format("%sannouncements", databaseInfo.getPrefix());

				PreparedStatement stmt = databaseInfo.getConnection().prepareStatement("SELECT * FROM " + announcementTableName + " WHERE ENABLED = 1");
				ResultSet res = stmt.executeQuery();

				while (res.next()) {
					if (res.getString("ANNOUNCEMENT_ID") != null) {
						Announcement announcement = new Announcement(UUID.fromString(res.getString("ANNOUNCEMENT_ID")), Long.valueOf(res.getString("GUILD_ID")));
						announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
						announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
						announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
						announcement.setAnnouncementType(AnnouncementType.valueOf(res.getString("ANNOUNCEMENT_TYPE")));
						announcement.setEventId(res.getString("EVENT_ID"));
						announcement.setEventColor(EventColor.fromNameOrHexOrID(res.getString("EVENT_COLOR")));
						announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
						announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
						announcement.setInfo(res.getString("INFO"));
						announcement.setInfoOnly(res.getBoolean("INFO_ONLY"));

						//The announcement is obviously enabled if we have gotten here lol
						announcement.setEnabled(true);

						announcements.add(announcement);
					}
				}

				stmt.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all announcements.", e, this.getClass(), true);
		}

		return announcements;
	}

	public Integer getAnnouncementCount() {
		int amount = -1;
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String announcementTableName = String.format("%sannouncements", databaseInfo.getPrefix());

				String query = "SELECT COUNT(*) FROM " + announcementTableName + ";";
				PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
				ResultSet res = statement.executeQuery();

				if (res.next()) {
					amount = res.getInt(1);
				} else {
					amount = 0;
				}

				res.close();
				statement.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get announcement count", e, this.getClass(), true);
		}
		return amount;
	}

	/**
	 * Deletes the specified announcement from the Database.
	 *
	 * @param announcementId The ID of the announcement to delete.
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	public Boolean deleteAnnouncement(String announcementId) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String announcementTableName = String.format("%sannouncements", databaseInfo.getPrefix());

				String query = "DELETE FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = ?";
				PreparedStatement preparedStmt = databaseInfo.getConnection().prepareStatement(query);
				preparedStmt.setString(1, announcementId);

				preparedStmt.execute();
				preparedStmt.close();
				return true;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete announcement.", e, this.getClass(), true);
		}
		return false;
	}

	public Boolean deleteAnnouncementsForEvent(long guildId, String eventId) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String announcementTableName = String.format("%sannouncements", databaseInfo.getPrefix());

				String query = "DELETE FROM " + announcementTableName + " WHERE EVENT_ID = ? AND GUILD_ID = ? AND ANNOUNCEMENT_TYPE = ?";
				PreparedStatement preparedStmt = databaseInfo.getConnection().prepareStatement(query);
				preparedStmt.setString(1, eventId);
				preparedStmt.setString(2, String.valueOf(guildId));
				preparedStmt.setString(3, AnnouncementType.SPECIFIC.name());

				preparedStmt.execute();
				preparedStmt.close();
				return true;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete announcements for specific event.", e, this.getClass(), true);
		}
		return false;
	}

	public Boolean deleteEventData(String eventId) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String eventTable = String.format("%sevents", databaseInfo.getPrefix());

				//Check if recurring...
				if (eventId.contains("_")) {
					//Don't delete if child event of recurring event.
					return false;
				}

				String query = "DELETE FROM " + eventTable + " WHERE EVENT_ID = ?";
				PreparedStatement preparedStmt = databaseInfo.getConnection().prepareStatement(query);
				preparedStmt.setString(1, eventId);

				preparedStmt.execute();
				preparedStmt.close();
				return true;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete event data.", e, this.getClass(), true);
		}
		return false;
	}

	public boolean deleteAllEventData(long guildId) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String eventTable = String.format("%sevents", databaseInfo.getPrefix());

				String query = "DELETE FROM " + eventTable + " WHERE GUILD_ID = ?";
				PreparedStatement preparedStmt = databaseInfo.getConnection().prepareStatement(query);
				preparedStmt.setString(1, String.valueOf(guildId));

				preparedStmt.execute();
				preparedStmt.close();
				return true;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete all event data for guild.", e, this.getClass(), true);
		}
		return false;
	}

	public boolean deleteAllAnnouncementData(long guildId) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String announcementTable = String.format("%sannouncements", databaseInfo.getPrefix());

				String query = "DELETE FROM " + announcementTable + " WHERE GUILD_ID = ?";
				PreparedStatement preparedStmt = databaseInfo.getConnection().prepareStatement(query);
				preparedStmt.setString(1, String.valueOf(guildId));

				preparedStmt.execute();
				preparedStmt.close();
				return true;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete all announcements for guild.", e, this.getClass(), true);
		}
		return false;
	}

	public boolean deleteAllRSVPData(long guildId) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String rsvpTable = String.format("%srsvp", databaseInfo.getPrefix());

				String query = "DELETE FROM " + rsvpTable + " WHERE GUILD_ID = ?";
				PreparedStatement preparedStmt = databaseInfo.getConnection().prepareStatement(query);
				preparedStmt.setString(1, String.valueOf(guildId));

				preparedStmt.execute();
				preparedStmt.close();
				return true;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete all RSVP data for guild.", e, this.getClass(), true);
		}
		return false;
	}

	public boolean deleteCalendar(CalendarData data) {
		try {
			if (databaseInfo.getMySQL().checkConnection()) {
				String calendarTable = String.format("%scalendars", databaseInfo.getPrefix());

				String query = "DELETE FROM " + calendarTable + " WHERE GUILD_ID = ? AND CALENDAR_ADDRESS = ?";
				PreparedStatement preparedStmt = databaseInfo.getConnection().prepareStatement(query);
				preparedStmt.setString(1, String.valueOf(data.getGuildId()));
				preparedStmt.setString(2, data.getCalendarAddress());

				preparedStmt.execute();
				preparedStmt.close();
				return true;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete calendar from database for guild.", e, this.getClass(), true);
		}
		return false;
	}
}
