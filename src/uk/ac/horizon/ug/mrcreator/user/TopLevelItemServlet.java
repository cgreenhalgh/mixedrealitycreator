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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.appengine.api.datastore.Key;

import uk.ac.horizon.ug.mrcreator.http.CRUDServlet;
import uk.ac.horizon.ug.mrcreator.http.RequestException;
import uk.ac.horizon.ug.mrcreator.model.Item;

/** Return only top-level items (for requesting user).
 * 
 * @author cmg
 *
 */
public class TopLevelItemServlet extends ItemCRUDServlet {

	/** cons */
	public TopLevelItemServlet() {
		super(TOP_LEVEL, true, 0, true);
	}
}
