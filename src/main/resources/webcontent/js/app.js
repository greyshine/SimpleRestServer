alert = function(inMsg) {
	if ( inMsg == null ) {
		$('#infos').hide();
		$('#alerts').hide();
	} else {
		$('#alerts').empty().append(inMsg).show();	
	}
};
info = function(inMsg) {
	if ( inMsg == null ) {
		$('#infos').hide();
		$('#alerts').hide();
	} else {
		$('#infos').empty().append(inMsg).show();	
	}
};

function getFv(inCss) {
	var v = '';
	try {
		
		v = $( inCss ).val();
		if (typeof( v ) == 'undefined') { return null; }
		v =  (''+v).trim();
		
	} catch (e) {
		
	}
	return v == '' ? null : v;
}

app = {
	load:function(inUrl) {
		
		var theResult = '';
		
		$.ajax( { 
			url: inUrl,
			async:false,
			success: function(inData) {
				theResult = inData; 
			},
			error: function() {
				theResult = '';
			}
		} )
		
		return theResult;
		
	},
	
	status:{},
	get:{},
	post:{},
	put:{},
	patch:{},
	delete:{},
	imageGreetings:{},
};

app.status.click = function($a) {
	
	$('nav.navbar li.active').removeClass('active');
	$a = $($a).parent().addClass('active');
	
	$('#content').empty().append('<div id="status" />');
	
	$.ajax( {
		method:'META',
		url:'/status',
		success:function( inData ) {
			$('div#status').append( '<textarea>'+ JSON.stringify(inData, null, 4) +'</textarea>' );		
			
		}
	} );
};

app.get.click = function($a) {
	alert();
	$('nav.navbar li.active').removeClass('active');
	$a = $($a).parent().addClass('active');
	$('#content').empty().append( app.load( 'rest-get.html' ) );
};
app.post.click = function($a) {
	alert();
	$('nav.navbar li.active').removeClass('active');
	$a = $($a).parent().addClass('active');
	$('#content').empty().append( app.load( 'rest-post.html' ) );
};
app.put.click = function($a) {
	alert();
	$('nav.navbar li.active').removeClass('active');
	$a = $($a).parent().addClass('active');
	$('#content').empty().append( app.load( 'rest-put.html' ) );
};
app.patch.click = function($a) {
	alert();
	$('nav.navbar li.active').removeClass('active');
	$a = $($a).parent().addClass('active');
	$('#content').empty().append( app.load( 'rest-patch.html' ) );
};
app.delete.click = function($a) {
	alert();
	$('nav.navbar li.active').removeClass('active');
	$a = $($a).parent().addClass('active');
	$('#content').empty().append( app.load( 'rest-delete.html' ) );
};
app.imageGreetings.click = function($a) {
	alert();
	$('nav.navbar li.active').removeClass('active');
	$a = $($a).parent().addClass('active');
	$('#content').empty().append( app.load( 'imageGreetings.html' ) );
};

app.get.submitForm = function() {
	
	alert(null);
	
	var theUrl = "/"+$( '#restGetCollection' ).val();
	if ($( '#restGetId' ).val() != ''  ) {
		theUrl = theUrl +'/'+$( '#restGetId' ).val();
	}
	
	var isParam = false;
	
	if ( $('#restGetEmbed').is(':checked') ) {
		
		theUrl += isParam ? '&' : '?';
		isParam = true;
		theUrl += '_embed=true';
	}
	
	if ( $('#restGetOffset').val() != '' ) {
		
		theUrl += isParam ? '&' : '?';
		isParam = true;
		theUrl += '_offset='+$('#restGetOffset').val();
	}
	
	if ( $('#restGetLength').val() != '' ) {
		
		theUrl += isParam ? '&' : '?';
		isParam = true;
		theUrl += '_length='+$('#restGetLength').val();
	}
	
	$.ajax( {
		url: theUrl,
		async: false,
		success: function( inData ) {
			$('#restGetResult').val( JSON.stringify(inData, null, 4) );
		},
		error( xhr, ajaxOptions, thrownError ) {
			alert( xhr.status +' '+ thrownError );
		}
	} );
};

app.post.submitForm = function() {
	
	alert(null);
	info(null)
	
	var theUrl = "/"+$( '#restPostCollection' ).val();
	
	$.ajax( {
		url: theUrl,
		method: 'POST',
		headers: { 'Content-Type': 'application/json; charset=UTF-8' },
		async: false,
		data: $('#restPostJson').val(),
		success: function( inData ) {
			info('created: '+ inData.id);
		},
		error( xhr, ajaxOptions, thrownError ) {
			alert( xhr.status +' '+ thrownError );
		}
	});
};

app.put.submitForm = function() {
	
	alert(null);
	info(null)
	
	var theUrl = "/"+$( '#restPutCollection' ).val()+"/"+$( '#restPutId' ).val();
	
	$.ajax( {
		url: theUrl,
		method: 'PUT',
		headers: { 'Content-Type': 'application/json; charset=UTF-8' },
		async: false,
		data: $('#restPutJson').val(),
		success: function( inData ) {
			info('updated: '+ inData.id);
		},
		error( xhr, ajaxOptions, thrownError ) {
			alert( xhr.status +' '+ thrownError );
		}
	});
};

app.delete.submitForm = function() {
	
	alert(null);
	info(null)
	
	var c = getFv('#restDeleteCollection');
	var i = getFv('#restDeleteId');
	if ( c== null || i == null) {
		alert('missing collection or id');
		return;
	}
	
	var theUrl = encodeURIComponent(c)+"/"+encodeURIComponent(i);
	
	var isOk = confirm('Really delete?\n'+ theUrl);
	
	if ( !isOk ) {
		return;
	}
	
	theUrl = '/'+theUrl;
	
	$.ajax( {
		url: theUrl,
		method: 'DELETE',
		async: false,
		success: function( inData ) {
			info('deleted: '+ inData.id);
		},
		error( xhr, ajaxOptions, thrownError ) {
			alert( xhr.status +' '+ thrownError );
		}
	});
};

app.imageGreetings.click = function() {
	
};


