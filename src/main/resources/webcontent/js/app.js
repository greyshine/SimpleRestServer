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
/**
 * Returns a form value safely
 * @param inCss
 * @returns
 */
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
	files:{},
	imageGreetings:{},
};

app.status.click = function($a) {
	
	alert();
	
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
app.post.handleFileSelected = function(inEvent) {
	
	if ( inEvent == null || inEvent.target == null || inEvent.target.files == null ) { return; }
	if ( inEvent.target.files.length < 1 ) { return; }
	
	app.post.loadFile(inEvent.target.files[0]);
};
app.post.loadFile = function(inFile) {
	if ( inFile == null ) { return; }
	console.log( inFile.name );
	
	var theReader = new FileReader();
	
	theReader.onloadend = function(inEvt) {
		
		/** try to parse as json */
		try {
		
			JSON.parse(inEvt.target.result);
			
			$('#restPostJson').html( inEvt.target.result );
			
		} catch (e) {
			
			alert('cannot json parse file: '+ inFile.name);
			// unable to pa
		}
	};
	
	theReader.readAsText(inFile);
}

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
app.files.click = function($a) {
	alert();
	$('nav.navbar li.active').removeClass('active');
	$a = $($a).parent().addClass('active');
	$('#content').empty().append( app.load( 'files-list.html' ) );
	
	$.ajax( {
		method:'GET',
		url:'/file',
		async:false,
		success: function(inData) {
			
			for(var idx in inData) {
				
				var binary = inData[idx];
				
				binary.mime = binary.mime || '&lt;unknown&gt;'
				
				var tr$ = $('<tr>');
				
				tr$.append( $('<td/>').append( binary.id ).append('<br/>')
						.append( $('<a/>').attr('href', '/file/'+binary.id +"?_download" ).append( 'download' ) )
						.append('&nbsp;')
						.append( $('<a target="_blank" download/>').attr('href', '/file/'+binary.id ).append( 'open' ) ) );
				
				tr$.append( $('<td/>').append( binary.mime ) );
				tr$.append( $('<td/>').append( binary.size ).append('<br/>')
									  .append('updated: '+ binary.updated ).append('<br/>')
									  .append('created: '+ binary.created ).append('<br/>')
									  .append('SHA-256: '+ binary.sha256 ) );
				
				//li$.find('a').attr('href','/file/'+ binary.id );
				
				$('table#files-list tbody').append( tr$ );
			}
		},
		error: function(xhr, ajaxOptions, thrownError ) {
			alert( xhr.status +' '+ thrownError );
		}
		
	} );
	
};

app.get.submitForm = function() {
	alert(null);
	var theUrl = "/"+$( '#restGetCollection' ).val();
	if ($( '#restGetId' ).val() != ''  ) {
		theUrl = theUrl +'/'+$( '#restGetId' ).val();
	}
	
	var isParam = false;
	
	if ( $('#restGetEnvelope').is(':checked') ) {
		
		theUrl += isParam ? '&' : '?';
		isParam = true;
		theUrl += '_envelope=true';
	}

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

/*
app.imageGreetings.click = function() {
	
};
*/


