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
        var context_table = $('#item_member_table');
        $('tr', member_table).remove();
        $('tr', context_table).remove();
        return false;
    }
    //alert('refresh game ' + id);
    var properties = ['id', 'name', 'type', 'metadata', 'topLevel', 'blobUrl'];//'creator', 'created', 
    refresh_table_item(table, properties, 'item/' + id, true);
    refresh_item_member_list();
    refresh_item_context_list();
    
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
    var properties = ['id', 'item_id', 'creator', 'created', 'metadata', 'sortValue'];
    refresh_table_list(table, properties, 'item/'+item_id+'/member/', 'refresh_item');
    return false;
}

function refresh_item_context_list() {
	if (item_id==null) {
		return false;
	}
    var table = $('#item_context_list_table');
    var properties = ['id', 'context_id', 'creator', 'created', 'metadata', 'sortValue'];
    refresh_table_list(table, properties, 'item/'+item_id+'/context/', 'refresh_item');
    return false;
}


// loaded...
$(document).ready(function() {
    show_div('item_list');
    refresh_item_list();
});
