/** index.js **/

var server;

$(function () 
{

	//Obtenemos el valor de la variable server
	var loc = window.location;
	var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
	server = loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));
		
	$('#txtPassword').keypress(function (event) {
            if (event.which == 13) {
                autenticar();
                //return false; only if needed
            }
        });

});

function autenticar()
{
	var usuario =  $('#txtUsuario').val();
	var password =  $('#txtPassword').val();
	// 'GetIngresarAplicacion?txtUsuario=' + usuario + "&txtPassword=" + password
	$.ajax({ 
	    				url: server + 'GetIngresarAplicacion', 
	    				dataType: 'text',
	    				type: 'post', 
	    				data: {'txtUsuario' : usuario , 'txtPassword' : password }, 
	    				async: false, 
	    				success: function(data){ 
	    						if(data == 'OK')
	    						{
	    							location.href = server + "CalcularInventario.html";
	    						}
	    						else
	    						{
	    							alert(data);
	    							$('#txtPassword').val('');
	    						}
							} 
						});
}

