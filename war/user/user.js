// Javascript for locationbasedgame author/index.hmtl

// start here...
$.ajaxSetup({ cache: false, async: true, timeout: 30000 });

function log(msg) {
    //if (get_lobbyclient() != undefined)
    //        get_lobbyclient().log(msg);
    // debug
    //	else
    alert(msg);
}

// jquery.escape 1.0 - escape strings for use in jQuery selectors
// http://ianloic.com/tag/jquery.escape
// Copyright 2009 Ian McKellar <http://ian.mckellar.org/>
// Just like jQuery you can use it under either the MIT license or the GPL
// (see: http://docs.jquery.com/License)
(function() {
    escape_re = /[#;&,\.\+\*~':"!\^\$\[\]\(\)=>|\/\\]/;
    jQuery.escape = function jQuery$escape(s) {
        var left = s.split(escape_re, 1)[0];
        if (left == s) return s;
        return left + '\\' +
    s.substr(left.length, 1) +
    jQuery.escape(s.substr(left.length + 1));
    }
})();
//END jquery.escape 1.0

function show_div(id) {
    $('body > div').hide('fast');
    $('#' + id).show('fast');
    return false;
}

// initialise table for start of loading
function prepare_table(table) {
    $('tr', table).remove();
    table.append('<tr><td>Loading...</td></tr>');
}

// update table with data array
function update_table_list(table, properties, data, detail_function_name) {
    $('tr', table).remove();
    var header = '<tr>';
    for (var i = 0; i < properties.length; i++) {
        header += '<td class="list_item">' + properties[i] + '</td>';
    }
    header += '</tr>';
    table.append(header);
    for (var di = 0; di < data.length; di++) {
        var item = data[di];
        var row = '<tr>';
        for (var i = 0; i < properties.length; i++) {
            if (properties[i] == 'id' && detail_function_name!=null && detail_function_name!=undefined)
                row += '<td class="list_item"><a href="#" onclick="'+detail_function_name+'(\''+item['id']+'\')">' + item[properties[i]] + '</a></td>';
            else            
                row += '<td class="list_item">' + item[properties[i]] + '</td>';
        }
        row += '</tr>';
        table.append(row);
    }
}

// update table with data array
function update_table_item(table, properties, data, editable) {
    $('tr', table).remove();
    for (var i = 0; i < properties.length; i++) {
        var item = data[properties[i]];
        var row = '<tr><td class="item_item">' + properties[i] + '</td><td class="item_item">';
        if (editable && !(properties[i]=='id'))
            row += '<input type="text" value="'+(item!=undefined ? item : '')+'" name="'+properties[i]+'" cols="40"/>';
        else if (item!=undefined)
            row += item;
        row += '</td></tr>';
        table.append(row);
    }
}

// update table with error
function error_table(table, status) {
    $('tr', table).remove();
    table.append('<tr><td>Sorry - ' + status + '</td></tr>');
}

function refresh_table_list(table, properties, url, detail_function_name) {
    prepare_table(table);
    try {
        $.ajax({ url: url,
            type: 'GET',
            contentType: 'application/json',
            processData: false,
            data: null,
            dataType: 'json',
            success: function success(data, status) {
                //  debug
                //alert('got ' + $.toJSON(data));
                update_table_list(table, properties, data, detail_function_name);
            },
            error: function error(req, status) {
                error_table(table, status + ' (' + req.status + ': ' + req.statusText + ')');
            }
        });
    } catch (err) {
        error_table(table, err.name + ': ' + err.message); //$.toJSON(err));
    }
}

function refresh_table_item(table, properties, url, editable) {
    prepare_table(table);
    try {
        $.ajax({ url: url,
            type: 'GET',
            contentType: 'application/json',
            processData: false,
            data: null,
            dataType: 'json',
            success: function success(data, status) {
                update_table_item(table, properties, data, editable);
            },
            error: function error(req, status) {
                error_table(table, status+' ('+req.status+': '+req.statusText+')');
            }
        });
    } catch (err) {
        error_table(table, err.name + ': ' + err.message); //$.toJSON(err));
    }
}

function get_input_value(item, table, name) {
    var value = String($('input[name=' + name + ']', table).attr('value'))
//	alert('get_input_value('+item+','+table+','+name+') value='+value);
	if (value!=undefined && value.length>0)
		item[name] = value;
}

var item_id = null;

function refresh_item(id) {
    // check/update id
    if (id == undefined)
        id = item_id;
    else
        item_id = id;
    var table = $('#item_table');
    if (id == null) {
        // empty
        var properties = ['name', 'type', 'metadata', 'blobUrl'];//'creator', 'created', 'topLevel'
        update_table_item(table, properties, { metadata: '{}' }, true);
        var member_table = $('#item_member_table');
        var context_table = $('#item_context_table');
        $('tr', member_table).remove();
        $('tr', context_table).remove();

        refresh_member_list();

        return false;
    }
    //alert('refresh game ' + id);
    var properties = ['id', 'name', 'type', 'metadata', 'topLevel', 'blobUrl'];//'creator', 'created', 
    refresh_table_item(table, properties, 'item/' + id, true);
    refresh_item_member_list();
    refresh_item_context_list();
    refresh_item_deviceprofile_list();

    refresh_member_list();
    
    show_div('item');

    return false;
}

function add_update_item() {
	//alert('add_update: item_id='+item_id);
	
    var table = $('#item_table');
    var item = {};
    get_input_value(item, table, 'name');
    get_input_value(item, table,  'type');
    get_input_value(item, table, 'metadata');
    get_input_value(item, table, 'blobUrl');
    
    if (item_id == null) {
        //alert('add configuration...');
        var data = $.toJSON(item);
        $('tr', table).remove();
        table.append('<tr><td>Saving...</td></tr>');
        try {
            $.ajax({ url: 'item/',
                type: 'POST',
                contentType: 'application/json',
                processData: false,
                data: data,
                dataType: 'json',
                success: function success(data, status) {
                    refresh_item();
                    refresh_item_list();
                    if (item_id == null)
                        show_div('item_list');
                },
                error: function error(req, status) {
                error_table(table, status + ' (' + req.status + ': ' + req.statusText + ')');
                }
            });
        } catch (err) {
            error_table(table, err.name + ': ' + err.message); //$.toJSON(err));
        }
    }
    else {
    	// don't include id!
    	//item.id = item_id;
    	var data = $.toJSON(item);
    	//alert('update: '+data);
        $('tr', table).remove();
        table.append('<tr><td>Saving...</td></tr>');
        try {
            $.ajax({ url: 'item/'+item_id,
                type: 'PUT',
                contentType: 'application/json',
                processData: false,
                data: data,
                dataType: 'json',
                success: function success(data, status) {
                    refresh_item();
                    refresh_item_list();
                },
                error: function error(req, status) {
                	error_table(table, status + ' (' + req.status + ': ' + req.statusText + ')');
                }
            });
        } catch (err) {
            error_table(table, err.name + ': ' + err.message); //$.toJSON(err));
        }
    }
}

function reset_item() {
    item_id = null;
    refresh_item(null);
}

function refresh_item_list() {
    var table = $('#item_list_table');
    var properties = ['id', 'name', 'type', 'creator', 'created', 'metadata', 'topLevel', 'blobUrl'];
    refresh_table_list(table, properties, 'item/', 'refresh_item');
    return false;
}

function refresh_item_member_list() {
	if (item_id==null) {
		return false;
	}
    var table = $('#item_member_list_table');
    var properties = ['id', 'itemId', 'creator', 'created', 'metadata', 'sortValue'];
    refresh_table_list(table, properties, 'item/'+item_id+'/member/');
    return false;
}

function refresh_item_context_list() {
	if (item_id==null) {
		return false;
	}
    var table = $('#item_context_list_table');
    var properties = ['id', 'contextId', 'creator', 'created', 'metadata', 'sortValue'];
    refresh_table_list(table, properties, 'item/'+item_id+'/context/');
    return false;
}

function refresh_item_deviceprofile_list() {
    if (item_id == null) {
        return false;
    }
    var table = $('#item_deviceprofile_list_table');
    var properties = ['id', 'name', 'itemType', 'metadata', 'requirements'];
    refresh_table_list(table, properties, 'item/' + item_id + '/deviceprofile/', 'refresh_deviceprofile');
    return false;
}

function refresh_member_list() {
	var table = $('#member_list_table');
	prepare_table(table);
	if (item_id == null)
	    return;
    try {
    	var items = null;
    	var members = null;
        $.ajax({ url: 'item/',
            type: 'GET',
            contentType: 'application/json',
            processData: false,
            data: null,
            dataType: 'json',
            success: function success(data, status) {
                //  debug
        		items = data;
        		if (members!=null)
        			update_member_list(items, members);
        	},
            error: function error(req, status) {
                error_table(table, status + ' (' + req.status + ': ' + req.statusText + ')');
            }
        });
        $.ajax({ url: 'item/'+item_id+'/member/',
            type: 'GET',
            contentType: 'application/json',
            processData: false,
            data: null,
            dataType: 'json',
            success: function success(data, status) {
        		members = data;
        		if (items!=null)
        			update_member_list(items, members);

        	},
            error: function error(req, status) {
                error_table(table, status + ' (' + req.status + ': ' + req.statusText + ')');
            }
        });
    } catch (err) {
        error_table(table, err.name + ': ' + err.message); //$.toJSON(err));
    }
}
var member_data = null;
function update_member_list(items, members) {

	var table = $('#member_list_table');
	var member_map = {};
	for (var i = 0; i < members.length; i++) {
	    var member = members[i];
	    member_map[member.itemId] = member;
	}
	//alert('member_map: '+$.toJSON(member_map)+'\nmembers: ' + $.toJSON(members));

	var properties = ['id', 'name', 'type', 'metadata', 'blobUrl']; //,'isMember','memberMetadata','memberSortValue'];
	var data = [];
	for (var i=0; i<items.length; i++) {
		var item = Object(items[i]);
		var member = member_map[item.id];
		if (member==undefined) {
			item.isMember = false;
			item.memberMetadata = '{}';
			item.memberSortValue = '';        } else {
			item.isMember = true;
			item.memberMetadata = member.metadata;
			item.memberSortValue = member.sortValue;
			item.memberId = member.id;
		}		
		data[data.length] = item;
	}
    member_data = data;
    
	//update table
	$('tr', table).remove();
    var header = '<tr>';
    for (var i = 0; i < properties.length; i++) {
        header += '<td class="list_item">' + properties[i] + '</td>';
    }
    //header += '<td>memberId</td>';
    header += '<td>memberMetadata</td><td>memberSortValue</td><td>Member actions</td>';
    header += '</tr>';
    table.append(header);
    for (var di = 0; di < data.length; di++) {
        var item = data[di];
        var row = '<tr>';
        for (var i = 0; i < properties.length; i++) {
            row += '<td class="list_item">' + item[properties[i]] + '</td>';
        }
        //row += '<td><input type="text"  name="metadata" value="' + item.memberId + '"/></td>';
        row += '<td><input type="text"  name="metadata'+di+'"/></td><td><input type="text" name="sortValue'+di+'" value="' + item.memberSortValue + '"/></td>';
        if (item.isMember)
            row += '<td><input type="button" value="Update" onclick="update_member(' + di + ');"/><input type="button" value="Remove" onclick="delete_member(' + di + ');"/></td>';
        else
        	row += '<td><input type="button" value="Add" onclick="add_member('+di+');"/></td>';
        row += '</tr>';
        table.append(row);
        $('input[name=metadata'+di+']', table).attr('value', item.memberMetadata);
    }
}
function add_member(di) {
    var item = member_data[di];
    var membership = {};
    membership.itemId = member_data[di].id;
    membership.contextId = item_id;
    var table = $('#member_list_table');
    membership.metadata = String($('input[name=metadata'+di+']',table).attr('value'));
    membership.sortValue = String($('input[name=sortValue'+di+']',table).attr('value'));
    var data = $.toJSON(membership);
    try {
        var url = 'item/'+item_id+'/member/';
        //alert('add_member(' + di + ') to ' + url + ' : ' + data);
        $.ajax({ url: url,
            type: 'POST',
            contentType: 'application/json',
            processData: false,
            data: data,
            dataType: 'json',
            success: function success(data, status) {
                refresh_member_list();
            },
            error: function error(req, status) {
            alert(status + ' (' + req.status + ': ' + req.statusText + ')');
            }
        });
    } catch (err) {
        alert(err.name + ': ' + err.message); //$.toJSON(err));
    }
    return false;
}
function update_member(di) {
    var item = member_data[di];
    var membership = {};
    membership.itemId = member_data[di].id;
    membership.contextId = item_id;
    var table = $('#member_list_table');
    membership.metadata = String($('input[name=metadata' + di + ']', table).attr('value'));
    membership.sortValue = String($('input[name=sortValue' + di + ']', table).attr('value'));
    var data = $.toJSON(membership);
    try {
        var url = 'item/' + item_id + '/member/' + item.memberId;
        //alert('update_member(' + di + ') to ' + url + ' : ' + data);
        $.ajax({ url: url,
            type: 'PUT',
            contentType: 'application/json',
            processData: false,
            data: data,
            dataType: 'json',
            success: function success(data, status) {
                refresh_member_list();
            },
            error: function error(req, status) {
                alert(status + ' (' + req.status + ': ' + req.statusText + ')');
            }
        });
    } catch (err) {
        alert(err.name + ': ' + err.message); //$.toJSON(err));
    }
    return false;
}
function delete_member(di) {
    var item = member_data[di];
    try {
        var url = 'item/' + item_id + '/member/' + item.memberId;
        //alert('delete_member(' + di + ') to ' + url);
        $.ajax({ url: url,
            type: 'DELETE',
            contentType: null,
            processData: false,
            data: null,
            dataType: 'json',
            success: function success(data, status) {
                refresh_member_list();
            },
            error: function error(req, status) {
                alert(status + ' (' + req.status + ': ' + req.statusText + ')');
            }
        });
    } catch (err) {
        alert(err.name + ': ' + err.message); //$.toJSON(err));
    }
    return false;
}

// map...
// open layers map and map imagery layer
var map, layer;
// map marker layer
var markers;
// map marker icon
var player_icon;
var other_icon;
// player marker
var player_marker = null;
// other player's markers
var other_markers_map = {};
var other_names_map = {};

// default/standard click handler as class
OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
    defaultHandlerOptions: {
        'single': true,
        'double': false,
        'pixelTolerance': 0,
        'stopSingle': false,
        'stopDouble': false
    },

    initialize: function(options) {
        this.handlerOptions = OpenLayers.Util.extend(
                        {}, this.defaultHandlerOptions
                    );
        OpenLayers.Control.prototype.initialize.apply(
                        this, arguments
                    );
        this.handler = new OpenLayers.Handler.Click(
                        this, {
                            'click': this.trigger
                        }, this.handlerOptions
                    );
    },

    trigger: function(e) {
        var lonlat = map.getLonLatFromViewPortPx(e.xy);
        //alert("You clicked near " + lonlat.lat + " N, " +
        //                              +lonlat.lon + " E");
        //alert('lonlat = ' + lonlat);
        lonlat = lonlat.transform(
                    map.getProjectionObject(),
                    new OpenLayers.Projection("EPSG:4326")
                    );
        alert('trigger: ' + e);
        //        alert('lonlat = ' + lonlat);
        //        $('#latlon_out').attr('value', new Number(lonlat.lon).toFixed(6) + ',' + new Number(lonlat.lat).toFixed(6));
    }
});

function onFeatureSelect(feature) {
    alert('select ' + feature);
}
function onFeatureUnselect(feature) {
    alert('unselect ' + feature);
}

function map_init() {
    map = new OpenLayers.Map('map');
    layer = new OpenLayers.Layer.OSM("Simple OSM Map");
    map.addLayer(layer);

    markers = new OpenLayers.Layer.Markers("Markers");
    map.addLayer(markers);

    var size = new OpenLayers.Size(21, 25);
    var offset = new OpenLayers.Pixel(-(size.w / 2), -size.h);
    player_icon = new OpenLayers.Icon('../lib/img/marker.png', size, offset);
    // bigger for debug
    size = new OpenLayers.Size(21, 25);
    other_icon = new OpenLayers.Icon('../lib/img/marker-blue.png', size, offset);

    // doesn't work with Marker layer?
    //var selectControl = new OpenLayers.Control.SelectFeature(markers,
    //            { onSelect: onFeatureSelect, onUnselect: onFeatureUnselect });
    //map.addControl(selectControl);
    //selectControl.activate();

    map.setCenter(
                new OpenLayers.LonLat(-1.188, 52.953).transform(
                    new OpenLayers.Projection("EPSG:4326"),
                    map.getProjectionObject()
                ), 12
            );

    // add click handler
    //var click = new OpenLayers.Control.Click();
    //map.addControl(click);
    //click.activate();
}
function show_map() {
    show_div('map_view');

    refresh_map();
}
function refresh_map() {
    // remove old markers
    for (var id in other_markers_map) {
        var marker = other_markers_map[id];
        markers.removeMarker(marker);
    }
    markers.redraw();
    other_markers_map = {};
    if (item_id == null)
        return false;
    // get new markers
    try {
        $.ajax({ url: 'item/' + item_id + '/member/',
            type: 'GET',
            contentType: 'application/json',
            processData: false,
            data: null,
            dataType: 'json',
            success: function success(data, status) {
                update_markers(data);

            },
            error: function error(req, status) {
                alert(status + ' (' + req.status + ': ' + req.statusText + ')');
            }
        });
    } catch (err) {
        alert(err.name + ': ' + err.message); //$.toJSON(err));
    }
    return false;
}
function update_markers(members) {
    for (var i=0; i<members.length; i++) {
        var member = members[i];
        try {
            var meta = $.parseJSON(String(member.metadata));
            var latitudeE6 = meta.latitudeE6;
            var longitudeE6 = meta.longitudeE6;
            //alert('marker at ' + longitudeE6 + ',' + latitudeE6);
            var marker = new OpenLayers.Marker(
                    new OpenLayers.LonLat(longitudeE6 * 0.000001, latitudeE6 * 0.000001).transform(
                        new OpenLayers.Projection("EPSG:4326"),
                        map.getProjectionObject()
                    ), other_icon.clone());
            markers.addMarker(marker);
            other_markers_map[member.id] = marker;
        }
        catch (err) {
            alert('Error in metadata for ' + member.id + ': ' + member.metadata + ' (' + err.message + ': ' + err.description + ')');
        }
    }
    markers.redraw();
}

// DeviceProfiles...
var deviceprofile_id = null;

function refresh_deviceprofile(id) {
    // check/update id
    if (id == undefined)
        id = deviceprofile_id;
    else
        deviceprofile_id = id;
    var table = $('#deviceprofile_table');
    if (id == null) {
        // empty
        var properties = ['name', 'itemType', 'itemId', 'metadata', 'requirements']; //'creator', 'created', 'topLevel'
        var itemId = (item_id!=null ? item_id : '');
        update_table_item(table, properties, { metadata: '{}', itemId : itemId }, true);

        return false;
    }
    //alert('refresh game ' + id);
    var properties = ['id', 'name', 'itemType', 'itemId', 'metadata', 'requirements']; //'creator', 'created',
    refresh_table_item(table, properties, 'deviceprofile/' + id, true);

    show_div('deviceprofile');

    return false;
}

function add_update_deviceprofile() {
    //alert('add_update: item_id='+item_id);

    var table = $('#deviceprofile_table');
    var item = {};
    get_input_value(item, table, 'name');
    get_input_value(item, table, 'itemType');
    get_input_value(item, table, 'itemId');
    get_input_value(item, table, 'metadata');
    get_input_value(item, table, 'requirements');

    if (deviceprofile_id == null) {
        //alert('add configuration...');
        var data = $.toJSON(item);
        $('tr', table).remove();
        table.append('<tr><td>Saving...</td></tr>');
        try {
            $.ajax({ url: 'deviceprofile/',
                type: 'POST',
                contentType: 'application/json',
                processData: false,
                data: data,
                dataType: 'json',
                success: function success(data, status) {
                    refresh_deviceprofile();
                    refresh_deviceprofile_list();
                    if (deviceprofile_id == null)
                        show_div('deviceprofile_list');
                },
                error: function error(req, status) {
                    error_table(table, status + ' (' + req.status + ': ' + req.statusText + ')');
                }
            });
        } catch (err) {
            error_table(table, err.name + ': ' + err.message); //$.toJSON(err));
        }
    }
    else {
        // don't include id!
        //item.id = item_id;
        var data = $.toJSON(item);
        //alert('update: '+data);
        $('tr', table).remove();
        table.append('<tr><td>Saving...</td></tr>');
        try {
            $.ajax({ url: 'deviceprofile/' + deviceprofile_id,
                type: 'PUT',
                contentType: 'application/json',
                processData: false,
                data: data,
                dataType: 'json',
                success: function success(data, status) {
                    refresh_deviceprofile();
                    refresh_deviceprofile_list();
                },
                error: function error(req, status) {
                    error_table(table, status + ' (' + req.status + ': ' + req.statusText + ')');
                }
            });
        } catch (err) {
            error_table(table, err.name + ': ' + err.message); //$.toJSON(err));
        }
    }
}

function reset_deviceprofile() {
    deviceprofile_id = null;
    refresh_deviceprofile(null);
}

function refresh_deviceprofile_list() {
    var table = $('#deviceprofile_list_table');
    var properties = ['id', 'name', 'itemType', 'itemId', 'metadata', 'requirements']; //'creator', 'created',
//    var properties = ['id', 'name', 'type', 'creator', 'created', 'metadata', 'topLevel', 'blobUrl'];
    refresh_table_list(table, properties, 'deviceprofile/', 'refresh_deviceprofile');
    return false;
}


// loaded...
$(document).ready(function() {
    show_div('item_list');

    map_init();

    refresh_item_list();
    refresh_deviceprofile_list();

});
