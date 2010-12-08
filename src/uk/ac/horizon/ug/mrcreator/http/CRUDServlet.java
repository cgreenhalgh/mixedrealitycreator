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
package uk.ac.horizon.ug.mrcreator.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import uk.ac.horizon.ug.mrcreator.http.RequestException;
import uk.ac.horizon.ug.mrcreator.model.EMF;

import java.util.List;
import java.util.logging.*;

/**
 * @author cmg
 *
 */
public abstract class CRUDServlet extends HttpServlet {
	/** logger */
	static Logger logger = Logger.getLogger(CRUDServlet.class.getName());
	
	public static final String ENCODING = "UTF-8";
	public static final String JSON_MIME_TYPE = "application/json";

	/** optional list filter property */
	private String listFilterPropertyName;
	/** optional list filter property */
	private Object listFilterPropertyValue;
	/** path parts to discard (for filter) */
	private int discardPathParts = 0;
	/** filter by creator = authenticated user? */
	private boolean filterByCreator = false;
	
	/** default cons */
	public CRUDServlet() {
	}
	/**
	 * @return the listFilterPropertyName
	 */
	public String getListFilterPropertyName() {
		return listFilterPropertyName;
	}


	/**
	 * @return the listFilterPropertyValue
	 */
	public Object getListFilterPropertyValue() {
		return listFilterPropertyValue;
	}


	/**
	 * @return the discardPathParts
	 */
	public int getDiscardPathParts() {
		return discardPathParts;
	}
	/** filter cons */
	/** the persisent class to be managed */
	protected abstract Class getObjectClass();

    /**
	 * @param listFilterPropertyName
	 * @param listFilterPropertyValue
	 */
	public CRUDServlet(String listFilterPropertyName,
			Object listFilterPropertyValue, int discardPathParts,
			boolean filterByCreator) {
		super();
		this.listFilterPropertyName = listFilterPropertyName;
		this.listFilterPropertyValue = listFilterPropertyValue;
		this.discardPathParts = discardPathParts;
		this.filterByCreator = filterByCreator;
	}
	
	/** get "creator", i.e. current authenticated user.
	 * Null of not required. RequestException if required and not authenticated. */
	private String getRequestCreator(HttpServletRequest req) throws RequestException {
		if (!filterByCreator)
			return null;		
		// TODO
        UserService userService = UserServiceFactory.getUserService(); 
//        if (req!=null && req.getUserPrincipal() == null) { 
//        	logger.warning("getUserPrinciple failed");
//        	throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED, "User is not authenticated");
//        }
        // NOTE: GAE-specific API
        User user = userService.getCurrentUser();
        if (user==null) {
        	logger.warning("getCurrentUser failed");
        	throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED, "User is not authenticated");
        }
		// on testing userId = null; fall back to email?!
		//String userId = user.getUserId();
        String creator = user.getEmail();
        if (creator==null || creator.length()==0)
        {
        	logger.warning("User email unknown/invalid ("+creator+")");
        	throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED, "User email unknown/invalid ("+creator+")");
		}

		return creator;
	}
	
	/** get creator property name, default "creator" */
	protected String getCreatorPropertyName() {
		return "creator";
	}
	
	/** get creator property value from object. Default undefined. */
	protected String getCreator(Object o) {
		throw new RuntimeException("getCreator() is not defined for "+this.getClass().getName());
	}
	
	/** get creator property value from object. Default undefined. */
	protected void setCreator(Object o, String creator) {
		throw new RuntimeException("setCreator() is not defined for "+this.getClass().getName());
	}
	
	/** generate key - default */
    protected Key idToKey(String id) {
    	return KeyFactory.createKey(getObjectClass().getSimpleName(), id);
    }

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			// TODO move to static method and reuse
			logger.log(Level.INFO, "doGet("+req.getPathInfo()+")");
			String pathInfo = req.getPathInfo();
			if (pathInfo==null)
				pathInfo = "";
			String pathParts[] = pathInfo.split("/");
			// ignore first "part" '' if there is a leading '/' in pathInfo (there should be)
			int discardPathParts = this.discardPathParts+(pathParts.length>0 && pathParts[0].length()==0 ? 1 : 0);
			if (pathParts.length<discardPathParts) {
				throw new RequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not enough part in path ("+pathParts.length+" vs "+discardPathParts+") for "+pathInfo);
			}
			if (pathParts.length==discardPathParts) {
				// get all
				doList(req, resp);
				return;
			}
			// possible filtered query...
			String id = pathParts[discardPathParts];
			Key key = idToKey(id);
			if (key==null) {
				throw new RequestException(HttpServletResponse.SC_NOT_FOUND, getObjectClass().getSimpleName()+" "+id+" could not map to key");
			}
			if (pathParts.length>discardPathParts+1) {
				String childScope = pathParts[discardPathParts+1];
				CRUDServlet childScopeServlet = getChildScopeServlet(id, childScope);
				childScopeServlet.doGet(req, resp);
				return;
			}
			EntityManager em = EMF.get().createEntityManager();
			try {
				Class clazz = getObjectClass();
				Object obj = em.find(clazz, key);
				if (obj==null) {
					throw new RequestException(HttpServletResponse.SC_NOT_FOUND, getObjectClass().getSimpleName()+" "+id+" not found");
				}
				if (filterByCreator) {
					String creator = getCreator(obj);
					String requestCreator = getRequestCreator(req);
					if (!requestCreator.equals(creator)) 
						throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED, "Requestor is not creator of "+getObjectClass().getSimpleName()+" "+id);
				}
				resp.setCharacterEncoding(ENCODING);
				resp.setContentType(JSON_MIME_TYPE);		
				Writer w = new OutputStreamWriter(resp.getOutputStream(), ENCODING);
				JSONWriter jw = new JSONWriter(w);
				writeObject(jw, obj);
				w.close();
			}
			catch (RequestException re) {
				throw re;
			}
			catch (Exception e) {
				logger.log(Level.WARNING, "Getting object "+id+" of type "+getObjectClass(), e);
				throw new RequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			}
			finally {
				em.close();
			}
		}
		catch (RequestException re) {
			resp.sendError(re.getErrorCode(), re.getMessage());
			return;
		}
	}
	
	/** default - throws not found; override */
	protected CRUDServlet getChildScopeServlet(String id, String childScope) throws RequestException {
		throw new RequestException(HttpServletResponse.SC_NOT_FOUND, "child scope "+childScope+" not defined on this class");
	}
	
	private void doList(HttpServletRequest req, HttpServletResponse resp) 
		throws ServletException, IOException {
		List results = null;
		EntityManager em = EMF.get().createEntityManager();
		try {
			Class clazz = getObjectClass();
			Query q = null;
			if (!filterByCreator) {
				if (listFilterPropertyName==null) {
					q = em.createQuery("SELECT x FROM "+clazz.getSimpleName()+" x");
				}
				else {
					q = em.createQuery("SELECT x FROM "+clazz.getSimpleName()+" x WHERE x."+listFilterPropertyName+" = :"+listFilterPropertyName);				
					q.setParameter(listFilterPropertyName, listFilterPropertyValue);
				}
			}
			else {
				// filter
				String creator = getRequestCreator(req);
				String prop = getCreatorPropertyName();
				if (listFilterPropertyName==null) {					
					q = em.createQuery("SELECT x FROM "+clazz.getSimpleName()+" x WHERE x."+prop+" = :creator");
					q.setParameter("creator", creator);
				}
				else {
					q = em.createQuery("SELECT x FROM "+clazz.getSimpleName()+" x WHERE x."+listFilterPropertyName+" = :"+listFilterPropertyName+" AND x."+prop+" = :creator");				
					q.setParameter(listFilterPropertyName, listFilterPropertyValue);
					q.setParameter("creator", creator);
				}
			}
			// TODO order?
			results = q.getResultList();
			// warning: lazy
			resp.setCharacterEncoding(ENCODING);
			resp.setContentType(JSON_MIME_TYPE);		
			Writer w = new OutputStreamWriter(resp.getOutputStream(), ENCODING);
			JSONWriter jw = new JSONWriter(w);
			jw.array();
			for (Object o : results) {
				listObject(jw, o);
			}
			jw.endArray();
			w.close();
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Getting object of type "+getObjectClass(), e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}
		finally {
			em.close();
		}
	}

	/** marshall 
	 * @throws JSONException */
	protected abstract void listObject(JSONWriter jw, Object o) throws JSONException;

	/** marshall 
	 * @throws JSONException */
	protected void writeObject(JSONWriter jw, Object o) throws JSONException {
		// default
		listObject(jw, o);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			logger.log(Level.INFO, "doPost("+req.getPathInfo()+")");
			String pathInfo = req.getPathInfo();
			if (pathInfo==null)
				pathInfo = "";
			String pathParts[] = pathInfo.split("/");
			// ignore first "part" '' if there is a leading '/' in pathInfo (there should be)
			int discardPathParts = this.discardPathParts+(pathParts.length>0 && pathParts[0].length()==0 ? 1 : 0);
			if (pathParts.length<discardPathParts) {
				throw new RequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not enough part in path ("+pathParts.length+" vs "+discardPathParts+") for "+pathInfo);
			}
			if (pathParts.length==discardPathParts) {
				doCreate(req, resp);
				return;
			}
			// possible filtered query...
			String id = pathParts[discardPathParts];
			Key key = idToKey(id);
			if (key==null) {
				throw new RequestException(HttpServletResponse.SC_NOT_FOUND, getObjectClass().getSimpleName()+" "+id+" could not map to key");
			}
			if (pathParts.length>discardPathParts+1) {
				String childScope = pathParts[discardPathParts+1];
				CRUDServlet childScopeServlet = getChildScopeServlet(id, childScope);
				childScopeServlet.doPost(req, resp);
				return;
			}
			// ?!
			super.doPost(req, resp);
		}
		catch (RequestException re) {
			resp.sendError(re.getErrorCode(), re.getMessage());
			return;
		}
	}

	/** Create on POST.
	 * E.g. curl -d '{...}' http://localhost:8888/author/configuration/
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doCreate(HttpServletRequest req, HttpServletResponse resp) 
		throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			Object o = parseObject(req);
			if (filterByCreator) {
				String creator = getRequestCreator(req);
				setCreator(o, creator);
			}
			Key key = validateCreate(o);
			// try adding
			EntityManager em = EMF.get().createEntityManager();
			EntityTransaction et = em.getTransaction();
			try {
				et.begin();
				if (em.find(getObjectClass(), key)!=null)
					throw new RequestException(HttpServletResponse.SC_CONFLICT, "object already exists ("+key+")");
				em.persist(o);
				et.commit();
				logger.info("Added "+o);
			}
			finally {
				if (et.isActive())
					et.rollback();
				em.close();
			}
			resp.setCharacterEncoding(ENCODING);
			resp.setContentType(JSON_MIME_TYPE);		
			Writer w = new OutputStreamWriter(resp.getOutputStream(), ENCODING);
			JSONWriter jw = new JSONWriter(w);
			listObject(jw, o);
			w.close();			
		}
		catch (RequestException e) {
			resp.sendError(e.getErrorCode(), e.getMessage());
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Getting object of type "+getObjectClass(), e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}
	}

	protected Key validateCreate(Object o) throws RequestException {
		throw new RequestException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Create not support for "+getObjectClass());
	}
	protected void validateUpdate(Object newobj, Object oldobj) throws RequestException {
		throw new RequestException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Update not support for "+getObjectClass());
	}
	protected Key validateDelete(Object o) throws RequestException {
		throw new RequestException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Delete not support for "+getObjectClass());
	}

	private Object parseObject(HttpServletRequest req) throws RequestException {
		try {
			//tx.begin();
			BufferedReader r = req.getReader();
			String line = r.readLine();
			//logger.info("UpdateAccount(1): "+line);
			// why does this seem to read {} ??
			//JSONObject json = new JSONObject(req.getReader());
			JSONObject json = new JSONObject(line);
			r.close();
			return parseObject(json);
		}
		catch (RequestException re)
		{
			throw re;
		}
		catch (Exception e) {
			throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "Error parsing request (JSON): "+e);
		}
	}

	protected abstract Object parseObject(JSONObject json) throws RequestException, IOException, JSONException;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		//logger.log(Level.INFO, "doPost("+req.getContextPath()+")");
		try {
			// get ID
			logger.log(Level.INFO, "doPut("+req.getPathInfo()+")");
			String pathInfo = req.getPathInfo();
			if (pathInfo==null)
				pathInfo = "";
			String pathParts[] = pathInfo.split("/");
			// ignore first "part" '' if there is a leading '/' in pathInfo (there should be)
			int discardPathParts = this.discardPathParts+(pathParts.length>0 && pathParts[0].length()==0 ? 1 : 0);
			if (pathParts.length<discardPathParts) {
				throw new RequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not enough part in path ("+pathParts.length+" vs "+discardPathParts+") for "+pathInfo);
			}
			if (pathParts.length==discardPathParts) {
				throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "Cannot PUT to collection "+req.getPathInfo());
			}
			// possible filtered query...
			String id = pathParts[discardPathParts];
			Key key = idToKey(id);
			if (key==null) {
				throw new RequestException(HttpServletResponse.SC_NOT_FOUND, getObjectClass().getSimpleName()+" "+id+" could not map to key");
			}
			if (pathParts.length>discardPathParts+1) {
				String childScope = pathParts[discardPathParts+1];
				CRUDServlet childScopeServlet = getChildScopeServlet(id, childScope);
				childScopeServlet.doPut(req, resp);
				return;
			}
			// parse new value
			Object newobj = parseObject(req);

			EntityManager em = EMF.get().createEntityManager();
			try {
				// check that object exists
				Class clazz = getObjectClass();
				Object obj = em.find(clazz, key);
				if (obj==null) {
					throw new RequestException(HttpServletResponse.SC_NOT_FOUND, getObjectClass().getSimpleName()+" "+id+" not found");
				}
				// check (if required) that object is owned by requestor
				if (filterByCreator) {
					String creator = getCreator(obj);
					String requestCreator = getRequestCreator(req);
					if (!requestCreator.equals(creator)) 
						throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED, "Requestor is not creator of "+getObjectClass().getSimpleName()+" "+id);
					// set creator
					setCreator(newobj, creator);
				}
				// validate update
				validateUpdate(newobj, obj);
				// perform update
				em.merge(newobj);
				
				resp.setCharacterEncoding(ENCODING);
				resp.setContentType(JSON_MIME_TYPE);		
				Writer w = new OutputStreamWriter(resp.getOutputStream(), ENCODING);
				JSONWriter jw = new JSONWriter(w);
				writeObject(jw, newobj);
				w.close();
			}
			catch (RequestException re) {
				throw re;
			}
			catch (Exception e) {
				logger.log(Level.WARNING, "Updating object "+id+" of type "+getObjectClass(), e);
				throw new RequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			}
			finally {
				em.close();
			}
		}
		catch (RequestException re) {
			resp.sendError(re.getErrorCode(), re.getMessage());
			return;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			// get ID
			logger.log(Level.INFO, "doDelete("+req.getPathInfo()+")");
			String pathInfo = req.getPathInfo();
			if (pathInfo==null)
				pathInfo = "";
			String pathParts[] = pathInfo.split("/");
			// ignore first "part" '' if there is a leading '/' in pathInfo (there should be)
			int discardPathParts = this.discardPathParts+(pathParts.length>0 && pathParts[0].length()==0 ? 1 : 0);
			if (pathParts.length<discardPathParts) {
				throw new RequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not enough part in path ("+pathParts.length+" vs "+discardPathParts+") for "+pathInfo);
			}
			if (pathParts.length==discardPathParts) {
				throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "Cannot PUT to collection "+req.getPathInfo());
			}
			// possible filtered query...
			String id = pathParts[discardPathParts];
			Key key = idToKey(id);
			if (key==null) {
				throw new RequestException(HttpServletResponse.SC_NOT_FOUND, getObjectClass().getSimpleName()+" "+id+" could not map to key");
			}
			if (pathParts.length>discardPathParts+1) {
				String childScope = pathParts[discardPathParts+1];
				CRUDServlet childScopeServlet = getChildScopeServlet(id, childScope);
				childScopeServlet.doDelete(req, resp);
				return;
			}

			EntityManager em = EMF.get().createEntityManager();
			try {
				// check that object exists
				Class clazz = getObjectClass();
				Object obj = em.find(clazz, key);
				if (obj==null) {
					throw new RequestException(HttpServletResponse.SC_NOT_FOUND, getObjectClass().getSimpleName()+" "+id+" not found");
				}
				// check (if required) that object is owned by requestor
				if (filterByCreator) {
					String creator = getCreator(obj);
					String requestCreator = getRequestCreator(req);
					if (!requestCreator.equals(creator)) 
						throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED, "Requestor is not creator of "+getObjectClass().getSimpleName()+" "+id);
				}
				// validate delete
				validateDelete(obj);
				// perform update
				em.remove(obj);
				
				// write back old value?
				resp.setCharacterEncoding(ENCODING);
				resp.setContentType(JSON_MIME_TYPE);		
				Writer w = new OutputStreamWriter(resp.getOutputStream(), ENCODING);
				JSONWriter jw = new JSONWriter(w);
				writeObject(jw, obj);
				w.close();
			}
			catch (RequestException re) {
				throw re;
			}
			catch (Exception e) {
				logger.log(Level.WARNING, "Deleting object "+id+" of type "+getObjectClass(), e);
				throw new RequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			}
			finally {
				em.close();
			}
		}
		catch (RequestException re) {
			resp.sendError(re.getErrorCode(), re.getMessage());
			return;
		}
	}
	
}
