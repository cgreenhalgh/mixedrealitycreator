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
package uk.ac.horizon.ug.mrcreator.test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import uk.ac.horizon.ug.mrcreator.http.CRUDServlet;
import uk.ac.horizon.ug.mrcreator.http.JsonConstants;
import uk.ac.horizon.ug.mrcreator.http.RequestException;
import uk.ac.horizon.ug.mrcreator.model.DeviceProfile;
import uk.ac.horizon.ug.mrcreator.model.EMF;
import uk.ac.horizon.ug.mrcreator.model.Item;
import uk.ac.horizon.ug.mrcreator.model.Membership;

import com.google.appengine.api.datastore.Key;

/**
 * @author cmg
 *
 */
public class ExportDeviceProfileServlet extends HttpServlet implements JsonConstants {
	/** logger */
	static Logger logger = Logger.getLogger(ExportDeviceProfileServlet.class.getName());

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			logger.log(Level.INFO, "doGet("+req.getPathInfo()+")");
			String pathParts[] = CRUDServlet.getPathParts(req);
			if (pathParts.length==0)
				throw new RequestException(HttpServletResponse.SC_NOT_FOUND);
			String id = pathParts[0];
			if (id.length()==0)
				throw new RequestException(HttpServletResponse.SC_NOT_FOUND, "No ID specified");
			
			Key key = DeviceProfile.idToKey(id);
			if (key==null) {
				throw new RequestException(HttpServletResponse.SC_NOT_FOUND, "DeviceProfile "+id+" could not map to key");
			}
			
			EntityManager em = EMF.get().createEntityManager();
			try {
				DeviceProfile dp = em.find(DeviceProfile.class, key);
				if (dp==null)
					throw new RequestException(HttpServletResponse.SC_NOT_FOUND, "DeviceProfile "+id+" not found");
				if (dp.getItemKey()==null)
					throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "DeviceProfile "+id+" is abstract");
					
				Item item = em.find(Item.class, dp.getItemKey());
				if (item==null)
					throw new RequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DeviceProfile Item "+dp.getItemKey().getName()+" not found");

				Map<Key,Item> items = new HashMap<Key,Item>();
				Map<Key,List<Membership>> memberships = new HashMap<Key,List<Membership>>();

				items.put(item.getKey(), item);
				getMemberships(item, em, items, memberships);
				
				// write response
				resp.setCharacterEncoding(CRUDServlet.ENCODING);
				resp.setContentType(CRUDServlet.JSON_MIME_TYPE);		
				Writer w = new OutputStreamWriter(resp.getOutputStream(), CRUDServlet.ENCODING);
				JSONWriter jw = new JSONWriter(w);
				jw.object();
				// device
				jw.key(DEVICE_PROFILE);
				exportDeviceProfile(jw, dp);
				jw.key(ITEM);
				exportItem(jw, item, items, memberships);
				jw.endObject();
				w.close();
			}
			finally {
				em.close();
			}
		}
		catch (RequestException re) {
			resp.sendError(re.getErrorCode(), re.getMessage());
			return;
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Doing export", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}
	}

	/**
	 * @param jw
	 * @param item
	 * @param items
	 * @param memberships
	 * @throws JSONException 
	 */
	private void exportItem(JSONWriter jw, Item item, Map<Key, Item> items,
			Map<Key, List<Membership>> memberships) throws JSONException {
		if (item==null)
			return;
		jw.object();
		if (item.getName()!=null) {
			jw.key(NAME);
			jw.value(item.getName());
		}
		if (item.getType()!=null) {
			jw.key(TYPE);
			jw.value(item.getType());
		}
		if (item.getMetadata()!=null) {
			jw.key(METADATA);
			jw.value(item.getMetadata());
		}
		if (item.getBlobUrl()!=null) {
			jw.key(BLOB_URL);
			jw.value(item.getBlobUrl());
		}
		List<Membership> ms = memberships.get(item.getKey());
		if (ms!=null && ms.size()>0) {
			jw.key(MEMBERS);
			jw.array();
			for (Membership m : ms) {
				exportMembership(jw, m, items, memberships);
			}
			jw.endArray();
		}
		// TODO Auto-generated method stub
		jw.endObject();
	}

	/**
	 * @param jw
	 * @param m
	 * @param items
	 * @param memberships
	 * @throws JSONException 
	 */
	private void exportMembership(JSONWriter jw, Membership m,
			Map<Key, Item> items, Map<Key, List<Membership>> memberships) throws JSONException {
		if (m==null)
			return;
		jw.object();
		if (m.getMetadata()!=null) {
			jw.key(METADATA);
			jw.value(m.getMetadata());
		}
		if (m.getSortValue()!=null) {
			jw.key(SORT_VALUE);
			jw.value(m.getSortValue());
		}
		if (m.getItemKey()!=null) {
			Item i = items.get(m.getItemKey());
			if (i!=null) {
				jw.key(ITEM);
				exportItem(jw, i, items, memberships);
			}
		}		
		jw.endObject();
	}

	/**
	 * @param jw
	 * @param dp
	 * @throws JSONException 
	 */
	private void exportDeviceProfile(JSONWriter jw, DeviceProfile dp) throws JSONException {
		jw.object();
		if (dp.getName()!=null) {
			jw.key(NAME);
			jw.value(dp.getName());
		}
		if (dp.getRequirements()!=null) {
			jw.key(REQUIREMENTS);
			jw.value(dp.getRequirements());
		}
		if (dp.getMetadata()!=null) {
			jw.key(METADATA);
			jw.value(dp.getMetadata());
		}
		jw.endObject();
	}

	/**
	 * @param item
	 * @param items
	 * @param memberships
	 * @throws RequestException 
	 */
	private void getMemberships(Item item, EntityManager em, Map<Key, Item> items,
			Map<Key, List<Membership>> memberships) throws RequestException {
		if (memberships.containsKey(item.getKey()))
			// already done - bad!
			throw new RequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Sorry, the requested information cannot be returned (there is a loop)");
		Query q = em.createQuery("SELECT x FROM "+Membership.class.getSimpleName()+" x WHERE x.contextKey = :contextKey");
		q.setParameter("contextKey", item.getKey());
		List<Membership> ms = (List<Membership>)q.getResultList();
		// add early in case of loop(s)
		memberships.put(item.getKey(), ms);
		// get items and recurse
		for (Membership m : ms) {
			if (m.getItemKey()!=null) {
				Item i = em.find(Item.class, m.getItemKey());
				if (i==null)
					logger.warning("Item "+m.getItemKey().getName()+" not found (Membership "+m.getId()+")");
				else {
					items.put(m.getItemKey(), i);
					// recurse
					getMemberships(i, em, items, memberships);
				}
			}
		}
	}

}
