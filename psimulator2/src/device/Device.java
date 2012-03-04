/*
 * Erstellt am 27.10.2011.
 */
package device;

import commands.AbstractCommandParser;
import networkModule.NetMod;
import physicalModule.PhysicMod;
import shell.apps.CommandShell.CommandShell;
import telnetd.pridaneTridy.TelnetProperties;

/**
 *
 * @author neiss
 */
public class Device {

	public final int id;	// id z konfiguraku
	String name;
	public final DeviceType type;
	public final PhysicMod physicalModule;
	private NetMod networkModule;
	ApplicationsList applications;
	
	private boolean networkModuleSet = false;
	
	/**
	 * telnet port is configured by TelnetProperties.addListerner method, which is called in constructor
	 * telnetPort is allocated when simulator is started. There is no need to store it in file.
	 */
	private transient int telnetPort = -1;

	/**
	 * Konstruktor. Nastavi zadany promenny, vytvori si fysickej modul.
	 *
	 * @param id
	 * @param name
	 * @param type 	 *
	 *
	 */
	public Device(int id, String name, DeviceType type) {
		this.id = id;
		this.name = name;
		this.type = type;
		physicalModule = new PhysicMod(this);
		TelnetProperties.addListener(this);  // telnetPort is configured in this method
	}

	public ApplicationsList getApplications() {
		return applications;
	}

	public int getTelnetPort() {
		return telnetPort;
	}

	public void setTelnetPort(int telnetPort) {
		this.telnetPort = telnetPort;
	}

	
	
	public String getName() {
		return name;
	}

	public NetMod getNetworkModule() {
		return networkModule;
	}

	public void setNetworkModule(NetMod networkModule) {
		if(!networkModuleSet){
			this.networkModule = networkModule;
			networkModuleSet=true;
		} else throw new RuntimeException("Tohle by nemelo nastat, kontaktujte tvurce softwaru.");
		
	}
	
	/**
	 * 
	 * @param cmd
	 * @return
	 */
	public AbstractCommandParser createParser(CommandShell cmd){
	
		throw new UnsupportedOperationException("Not supported yet."); // @TODO STANDA, TOMÁŠ .. implementovat prosím :)
		
	}

	public enum DeviceType {

		cisco_router,
		linux_computer,
		simple_switch
	}
}
