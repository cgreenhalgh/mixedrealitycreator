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
import uk.ac.horizon.ug.mrcreator.model.GUIDFactory;
import uk.ac.horizon.ug.mrcreator.model.Item;

/**
 * @author cmg
 *
 */
public class ItemCRUDServlet extends CRUDServlet implements JsonConstants {

	/** cons */
	public ItemCRUDServlet () {		
		// filter by creator
		super(null, null, 0, true);
	}
	
	/**
	 * @param listFilterPropertyName
	 * @param listFilterPropertyValue
	 * @param discardPathParts
	 * @param filterByCreator
	 */
	public ItemCRUDServlet(String listFilterPropertyName,
			Object listFilterPropertyValue, int discardPathParts,
			boolean filterByCreator) {
		super(listFilterPropertyName, listFilterPropertyValue, discardPathParts,
				filterByCreator);
	}

	@Override
	protected Class getObjectClass() {
		return Item.class;
	}

	@Override
	protected void listObject(JSONWriter jw, Object o) throws JSONException {
		Item g = (Item)o;
		jw.object();
		// ID first
		jw.key(ID);
		jw.value(g.getId());
		if (g.getBlobUrl()!=null) {
			jw.key(BLOB_URL);
			jw.value(g.getBlobUrl());
		}
		jw.key(CREATED);
		jw.value(g.getCreated());
		if (g.getCreator()!=null) {
			jw.key(CREATOR);
			jw.value(g.getCreator());
		}
		if (g.getMetadata()!=null) {
			jw.key(METADATA);
			jw.value(g.getMetadata());
		}
		if (g.getName()!=null) {
			jw.key(NAME);
			jw.value(g.getName());
		}
		jw.key(TOP_LEVEL);
		jw.value(g.isTopLevel());
		if (g.getType()!=null) {
			jw.key(TYPE);
			jw.value(g.getType());
		}
		jw.endObject();
	}

	@Override
	protected Object parseObject(JSONObject json) throws RequestException,
			IOException, JSONException {
		Item gc = new Item();
		Iterator keys = json.keys();
		// default!
		gc.setTopLevel(true);
		while(keys.hasNext()) {
			String key = (String)keys.next();
			if (BLOB_URL.equals(key)) 
				gc.setBlobUrl(json.getString(key));
			else if (METADATA.equals(key))
				gc.setMetadata(json.getString(key));
			else if (NAME.equals(key))
				gc.setName(json.getString(key));
			else if (TOP_LEVEL.equals(key))
				gc.setTopLevel(json.getBoolean(key));
			else if (TYPE.equals(key))
				gc.setType(json.getString(key));
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
	private void validateUpdateOrCreate(Item i) throws RequestException {
		if (i.getType()==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"No type specified for Item");
		if (i.getName()==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"No name specified for Item");
		if (i.getMetadata()==null)
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST,"No metadata specified for Item");
		if (i.getCreator()==null)
			throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED,"No creator specified for Item");		
	}
	@Override
	protected Key validateCreate(Object o) throws RequestException {
		Item i = (Item)o;
		validateUpdateOrCreate(i);
		i.setCreated(System.currentTimeMillis());
		String id = GUIDFactory.newGUID();
		i.setKey(Item.idToKey(id));
		return i.getKey();
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#validateUpdate(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void validateUpdate(Object newobj, Object oldobj)
			throws RequestException {
		Item i = (Item)newobj;
		validateUpdateOrCreate(i);
		Item oi = (Item)oldobj;
		// these can't be changed...
		i.setCreated(oi.getCreated());
		i.setTopLevel(oi.isTopLevel());
		if (!i.getCreator().equals(oi.getCreator()))
			throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED, "Update cannot change creator: "+i);
		if (i.getId()!=null && !i.getId().equals(oi.getId()))
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "Update ID does not match path ID ("+i.getId()+" / "+oi.getId()+")");
		// make sure you clone the key!
		i.setKey(oi.getKey());
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.locationbasedgame.author.CRUDServlet#getChildScopeServlet(java.lang.String, java.lang.String)
	 */
	@Override
	protected CRUDServlet getChildScopeServlet(String id, String childScope)
			throws RequestException {
		if (childScope.equals("member")) {
			return new ItemMembershipCRUDServlet("contextKey", Item.idToKey(id), 2, true);
		}
		if (childScope.equals("context")) {
			return new ItemMembershipCRUDServlet("itemKey", Item.idToKey(id), 2, true);
		}
		// TODO Auto-generated method stub
		return super.getChildScopeServlet(id, childScope);
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#getCreator(java.lang.Object)
	 */
	@Override
	protected String getCreator(Object o) {
		Item i = (Item)o;
		return i.getCreator();
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.mrcreator.http.CRUDServlet#setCreator(java.lang.Object, java.lang.String)
	 */
	@Override
	protected void setCreator(Object o, String creator) {
		Item i = (Item)o;
		i.setCreator(creator);
	}
	
	
}
