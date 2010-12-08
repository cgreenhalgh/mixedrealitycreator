// Javascript for mixedrealitycreator test/
// requires common.js


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

//    refresh_map();
}

var id = null;

// loaded...
$(document).ready(function() {
    show_div('debug');

    id = decodeURIComponent(gup('id'));
    $('#id').html(id);

    map_init();

});
