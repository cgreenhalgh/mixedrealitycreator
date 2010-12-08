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
import uk.ac.horizon.ug.mrcreator.model.GUIDFactory;
import uk.ac.horizon.ug.mrcreator.model.Item;
import uk.ac.horizon.ug.mrcreator.model.Membership;

/**
 * @author cmg
 *
 */
public class ItemMembershipCRUDServlet extends CRUDServlet implements JsonConstants {

	/**
	 * 
	 */
	public ItemMembershipCRUDServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param listFilterPropertyName
	 * @param listFilterPropertyValue
	 * @param discardPathParts
	 * @param filterByCreator
	 */
	public ItemMembershipCRUDServlet(String listFilterPropertyName,
			Object listFilterPropertyValue, int discardPathParts,
			boolean filterByCreator) {
		super(listFilterPropertyName, listFilterPropertyValue, discardPathParts,
				filterByCreator);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#getObjectClass()
	 */
	@Override
	protected Class getObjectClass() {
		return Membership.class;
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#listObject(org.json.JSONWriter, java.lang.Object)
	 */
	@Override
	protected void listObject(JSONWriter jw, Object o) throws JSONException {
		Membership m = (Membership)o;
		jw.object();
		if (m.getContextKey()!=null) {
			jw.key(CONTEXT_ID);
			jw.value(m.getContextKey().getName());
		}
		jw.key(CREATED);
		jw.value(m.getCreated());
		if (m.getCreator()!=null) {
			jw.key(CREATOR);
			jw.value(m.getCreator());
		}
		if (m.getId()!=null) {
			jw.key(ID);
			jw.value(m.getId());
		}
		if (m.getItemKey()!=null) {
			jw.key(ITEM_ID);
			jw.value(m.getItemKey().getName());
		}
		if (m.getMetadata()!=null) {
			jw.key(METADATA);
			jw.value(m.getMetadata());
		}
		if (m.getSortValue()!=null) {
			jw.key(SORT_VALUE);
			jw.value(m.getSortValue());
		}
		jw.endObject();
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#parseObject(org.json.JSONObject)
	 */
	@Override
	protected Object parseObject(JSONObject json) throws RequestException,
			IOException, JSONException {
		Membership gc = new Membership();
		// implicit value(s) from servlet context
		if (getListFilterPropertyName()!=null && getListFilterPropertyValue() instanceof Key) {
			Key key = (Key)getListFilterPropertyValue();
			if ("contextKey".equals(getListFilterPropertyName()))
				gc.setContextKey(key);
			else if ("itemKey".equals(getListFilterPropertyName()))
				gc.setItemKey(key);
		}
		// passed values
		Iterator keys = json.keys();
		while(keys.hasNext()) {
			String key = (String)keys.next();
			if (CONTEXT_ID.equals(key))
				gc.setContextKey(Item.idToKey(json.getString(key)));
			else if (ITEM_ID.equals(key))
				gc.setItemKey(Item.idToKey(json.getString(key)));
			else if (METADATA.equals(key))
				gc.setMetadata(json.getString(key));
			else if (SORT_VALUE.equals(key))
				gc.setSortValue(json.getString(key));
			else
				throw new JSONException("Unsupported key '"+key+"' in Membership: "+json);
		}			
		return gc;
	}
	/** common checks
	 * 
	 * @param i
	 * @throws RequestException
	 */
	private void validateUpdateOrCreate(Membership i) throws RequestException {
		if (i.getItemKey()==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"No item specified for Membership");
		if (i.getContextKey()==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"No context specified for Membership");
		//if (i.getMetadata()==null)
		//throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"No metadata specified for Item");
		if (i.getCreator()==null)
			throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED,"No creator specified for Membeship");		
	}
	@Override
	protected Key validateCreate(Object o) throws RequestException {
		Membership i = (Membership)o;
		validateUpdateOrCreate(i);
		i.setCreated(System.currentTimeMillis());
		String id = GUIDFactory.newGUID();
		i.setKey(Membership.idToKey(id));
		return i.getKey();
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#validateUpdate(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void validateUpdate(Object newobj, Object oldobj)
			throws RequestException {
		Membership i = (Membership)newobj;
		validateUpdateOrCreate(i);
		Membership oi = (Membership)oldobj;
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
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#validateDelete(java.lang.Object)
	 */
	@Override
	protected Key validateDelete(Object o) throws RequestException {
		// ok
		Membership m = (Membership)o;
		return m.getKey();
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#getCreator(java.lang.Object)
	 */
	@Override
	protected String getCreator(Object o) {
		Membership i = (Membership)o;
		return i.getCreator();
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#setCreator(java.lang.Object, java.lang.String)
	 */
	@Override
	protected void setCreator(Object o, String creator) {
		Membership i = (Membership)o;
		i.setCreator(creator);
	}

}
