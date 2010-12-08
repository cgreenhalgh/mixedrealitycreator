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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.horizon.ug.mrcreator.http.RequestException;
import uk.ac.horizon.ug.mrcreator.model.DeviceProfile;

import com.google.appengine.api.datastore.Key;

/**
 * @author cmg
 *
 */
public class ExportDeviceProfileServlet extends HttpServlet {
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
			// TODO move to static method in CRUDServlet or similar
			String pathInfo = req.getPathInfo();
			if (pathInfo==null)
				pathInfo = "";
			if (pathInfo.startsWith("/"))
				pathInfo = pathInfo.substring(1);
			String pathParts[] = pathInfo.split("/");
			if (pathParts.length==0)
				throw new RequestException(HttpServletResponse.SC_NOT_FOUND);
			String id = pathParts[0];
			if (id.length()==0)
				throw new RequestException(HttpServletResponse.SC_NOT_FOUND, "No ID specified");
			
			Key key = DeviceProfile.idToKey(id);
			if (key==null) {
				throw new RequestException(HttpServletResponse.SC_NOT_FOUND, "DeviceProfile "+id+" could not map to key");
			}
			// TODO Auto-generated method stub
			super.doGet(req, resp);
			// TODO...
		}
		catch (RequestException re) {
			resp.sendError(re.getErrorCode(), re.getMessage());
			return;
		}
	}

}
