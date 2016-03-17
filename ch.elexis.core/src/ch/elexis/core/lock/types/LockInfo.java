package ch.elexis.core.lock.types;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.elexis.core.constants.StringConstants;

@XmlRootElement
public class LockInfo {

	@XmlElement
	private String elementId;
	@XmlElement
	private String elementType;
	@XmlElement
	private String user;
	@XmlElement
	private Date creationDate;
	@XmlElement
	private String systemUuid;

	public LockInfo() {
	}

	public LockInfo(String storeToString, String userId, String systemUuid) {
		String[] split = storeToString.split(StringConstants.DOUBLECOLON);
		if (split.length == 2) {
			this.elementId = split[1];
			this.elementType = split[0];
			this.user = userId;
			this.creationDate = new Date();
			this.systemUuid = systemUuid;
		} else {
			throw new IllegalArgumentException(storeToString);
		}
	}

	/**
	 * Get the class name of the element, equals storeToString without the id.
	 * 
	 * @return
	 */
	public String getElementType() {
		return elementType;
	}

	/**
	 * Get the local id of the element, equals storeToString without the class name.
	 * 
	 * @return
	 */
	public String getElementId() {
		return elementId;
	}

	public String getUser() {
		return user;
	}

	public Date getCreationDate() {
		return creationDate;
	}
	
	public String getSystemUuid() {
		return systemUuid;
	}

	public static String getElementId(String storeToString) {
		String[] split = storeToString.split(StringConstants.DOUBLECOLON);
		if (split.length == 2) {
			return split[1];
		} else {
			throw new IllegalArgumentException(storeToString);
		}
	}

	public String getElementStoreToString() {
		return elementType+StringConstants.DOUBLECOLON+elementId;
	}
}
