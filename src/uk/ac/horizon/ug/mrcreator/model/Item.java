/**
 * Copyright 2010 The University of Nottingham
 * 
 * This file is part of mixedrealitycreator.
 *
 *  mixedrealitycreator is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  mixedrealitycreator is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with mixedrealitycreator.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package uk.ac.horizon.ug.mrcreator.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/** a digital Artefact or Collection (including a Space).
 * 
 * @author cmg
 *
 */
@Entity
public class Item {
	/** key - autogenerated */
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Key key; 
    
    /** name */
    private String name;
    /** creator (ID) */
    private String creator;
    /** created date/time (Java time) */
    private long created;
    /** blob URL (not for collection) */
    private String blobUrl;
    /** top-level flag (i.e. typically not listed under any collection) */
    private boolean topLevel;
    /** Item type, e.g. collection */
    private String type;
    /** (other) json-encoded metadata */
    private String metadata;
    /* hidden? */
    /* status/issues? */
    
    /** cons */
    public Item() {
    	
    }
    /** generate key */
    public static final Key idToKey(String id) {
    	return KeyFactory.createKey(Item.class.getSimpleName(), id);
    }

	/**
	 * @return the key
	 */
	public Key getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(Key key) {
		this.key = key;
	}
	/**
	 * @return the id (string of key)
	 */
	public String getId() {
		if (key!=null)
			return key.getName();
		else
			return null;
		//return id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}
	/**
	 * @param creator the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}
	/**
	 * @return the created
	 */
	public long getCreated() {
		return created;
	}
	/**
	 * @param created the created to set
	 */
	public void setCreated(long created) {
		this.created = created;
	}
	/**
	 * @return the blobUrl
	 */
	public String getBlobUrl() {
		return blobUrl;
	}
	/**
	 * @param blobUrl the blobUrl to set
	 */
	public void setBlobUrl(String blobUrl) {
		this.blobUrl = blobUrl;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the metadata
	 */
	public String getMetadata() {
		return metadata;
	}
	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
	/**
	 * @return the topLevel
	 */
	public boolean isTopLevel() {
		return topLevel;
	}
	/**
	 * @param topLevel the topLevel to set
	 */
	public void setTopLevel(boolean topLevel) {
		this.topLevel = topLevel;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Item [blobUrl=" + blobUrl + ", created=" + created
				+ ", creator=" + creator + ", key=" + key + ", metadata="
				+ metadata + ", name=" + name + ", type=" + type + "]";
	}

}
