/**
 * Copyright 2010 The University of Nottingham
 * 
 * This file is part of locationbasedgame.
 *
 *  locationbasedgame is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  locationbasedgame is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with locationbasedgame.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package uk.ac.horizon.ug.mrcreator.user;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.appengine.api.datastore.Key;

import uk.ac.horizon.ug.mrcreator.http.CRUDServlet;
import uk.ac.horizon.ug.mrcreator.http.JsonConstants;
import uk.ac.horizon.ug.mrcreator.http.RequestException;
import uk.ac.horizon.ug.mrcreator.model.DeviceProfile;
import uk.ac.horizon.ug.mrcreator.model.GUIDFactory;
import uk.ac.horizon.ug.mrcreator.model.Item;

/**
 * @author cmg
 *
 */
public class DeviceProfileCRUDServlet extends CRUDServlet implements JsonConstants {

	/** cons */
	public DeviceProfileCRUDServlet () {		
		// filter by creator
		super(null, null, 0, true);
	}
	
	/**
	 * @param listFilterPropertyName
	 * @param listFilterPropertyValue
	 * @param discardPathParts
	 * @param filterByCreator
	 */
	public DeviceProfileCRUDServlet(String listFilterPropertyName,
			Object listFilterPropertyValue, int discardPathParts,
			boolean filterByCreator) {
		super(listFilterPropertyName, listFilterPropertyValue, discardPathParts,
				filterByCreator);
	}

	@Override
	protected Class getObjectClass() {
		return DeviceProfile.class;
	}

	@Override
	protected void listObject(JSONWriter jw, Object o) throws JSONException {
		DeviceProfile g = (DeviceProfile)o;
		jw.object();
		// ID first
		jw.key(ID);
		jw.value(g.getId());
		jw.key(CREATED);
		jw.value(g.getCreated());
		if (g.getCreator()!=null) {
			jw.key(CREATOR);
			jw.value(g.getCreator());
		}
		if (g.getItemKey()!=null) {
			jw.key(ITEM_ID);
			jw.value(g.getItemKey().getName());
		}
		if (g.getItemType()!=null) {
			jw.key(ITEM_TYPE);
			jw.value(g.getItemType());
		}
		if (g.getMetadata()!=null) {
			jw.key(METADATA);
			jw.value(g.getMetadata());
		}
		if (g.getName()!=null) {
			jw.key(NAME);
			jw.value(g.getName());
		}
		if (g.getRequirements()!=null) {
			jw.key(REQUIREMENTS);
			jw.value(g.getRequirements());
		}
		jw.endObject();
	}

	@Override
	protected Object parseObject(JSONObject json) throws RequestException,
			IOException, JSONException {
		DeviceProfile gc = new DeviceProfile();
		Iterator keys = json.keys();
		while(keys.hasNext()) {
			String key = (String)keys.next();
			if (METADATA.equals(key))
				gc.setMetadata(json.getString(key));
			else if (NAME.equals(key))
				gc.setName(json.getString(key));
			else if (ITEM_TYPE.equals(key))
				gc.setItemType(json.getString(key));
			else if (ITEM_ID.equals(key))
				gc.setItemKey(Item.idToKey(json.getString(key)));
			else if (REQUIREMENTS.equals(key))
				gc.setRequirements(json.getString(key));
			else
				throw new JSONException("Unsupported key '"+key+"' in Item: "+json);

		}
		return gc;
	}

	/** common checks
	 * 
	 * @param i
	 * @throws RequestException
	 */
	private void validateUpdateOrCreate(DeviceProfile i) throws RequestException {
		if (i.getName()==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"No name specified for DeviceProfile");
		if (i.getMetadata()==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"No metadata specified for DeviceProfile");
		if (i.getCreator()==null)
			throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED,"No creator specified for DeviceProfile");		
	}
	@Override
	protected Key validateCreate(Object o) throws RequestException {
		DeviceProfile i = (DeviceProfile)o;
		validateUpdateOrCreate(i);
		i.setCreated(System.currentTimeMillis());
		String id = GUIDFactory.newGUID();
		i.setKey(DeviceProfile.idToKey(id));
		return i.getKey();
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#validateUpdate(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void validateUpdate(Object newobj, Object oldobj)
			throws RequestException {
		DeviceProfile i = (DeviceProfile)newobj;
		validateUpdateOrCreate(i);
		DeviceProfile oi = (DeviceProfile)oldobj;
		// these can't be changed...
		i.setCreated(oi.getCreated());
		if (!i.getCreator().equals(oi.getCreator()))
			throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED, "Update cannot change creator: "+i);
		if (i.getId()!=null && !i.getId().equals(oi.getId()))
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "Update ID does not match path ID ("+i.getId()+" / "+oi.getId()+")");
		// make sure you clone the key!
		i.setKey(oi.getKey());
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#getCreator(java.lang.Object)
	 */
	@Override
	protected String getCreator(Object o) {
		DeviceProfile i = (DeviceProfile)o;
		return i.getCreator();
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#setCreator(java.lang.Object, java.lang.String)
	 */
	@Override
	protected void setCreator(Object o, String creator) {
		DeviceProfile i = (DeviceProfile)o;
		i.setCreator(creator);
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#validateDelete(java.lang.Object)
	 */
	@Override
	protected Key validateDelete(Object o) throws RequestException {
		// ok
		DeviceProfile i = (DeviceProfile)o;
		return i.getKey();
	}
	
	
}
