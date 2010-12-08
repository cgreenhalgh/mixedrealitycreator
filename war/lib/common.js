// common js stuff

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


//http://www.netlobo.com/url_query_string_javascript.html
function gup(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.href);
    if (results == null)
        return "";
    else
        return results[1];
}

