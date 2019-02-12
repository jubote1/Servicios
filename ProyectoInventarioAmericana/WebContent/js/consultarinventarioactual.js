	

var server;
var tiendas;
var urlTienda ="";
var inventarios;
var bandera = false;


// Se arma el valor de la variable global server, con base en la cual se realiza llamado a los servicios.
$(document).ready(function() {

	//Obtenemos el valor de la variable server
	var loc = window.location;
	var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
	server = loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));
	

    

	} );

//Se obtiene el listado de tiendas con el fin de seleccionar la tienda a surtir.

$(function(){
	
	getListaTiendas();
	
	
});

function imprimirDivInventario(nombreDiv)
{
	var contenido= document.getElementById(nombreDiv).innerHTML;
    var contenidoOriginal= document.body.innerHTML;
    document.body.innerHTML = contenido;
    window.print();
    document.body.innerHTML = contenidoOriginal;
}


//Método que invoca el servicio para obet
function getListaTiendas(){
	$.getJSON(server + 'GetTiendas', function(data){
		tiendas = data;
		var str = '';
		for(var i = 0; i < data.length;i++){
			var cadaTienda  = data[i];
			str +='<option value="'+ cadaTienda.nombre +'" id ="'+ cadaTienda.id +'">' + cadaTienda.nombre +'</option>';
		}
		$('#selectTiendas').html(str);
	});
}



//Método principal invocado para validar los datos de los parámetros y adicionalmente ejecutar los servicios para 
//recuperar la informacion y pintarla en pantalla.
function consultarInventarioActual() 
{

	var tienda = $("#selectTiendas").val();
	var idtienda = $("#selectTiendas option:selected").attr('id');
	
		
	if (tienda == '' || tienda == null)
	{

		alert ('La tienda no puede estar vacía');
		return;
	}
	// Si pasa a este punto es porque paso las validaciones
	
	$.ajax({ 
	    		url: server + 'ConsultarInventarioTienda?idtienda=' + idtienda, 
	    		dataType: 'json', 
	    		async: false, 
	    		success: function(data1){ 
	    				
	    				inventarios = data1;
	    				bandera = true;
	    				var inventario;
						
						var strInv='';
						
						strInv += '<table id="inventarioTienda" class="table table-bordered table-striped">';
						strInv += '<thead><tr><th COLSPAN="1"><img src="LogoPizzaAmericanapeque.png" class="img-circle" /></th>';
						strInv += '<th COLSPAN="2"> <h2>'+ "INVENTARIO ACTUAL TIENDA " + tienda + " " + inventarios[0].fechainsercion +'</h2></th></tr>'
						strInv += '<tr><th>Id/Insumo</th><th>Nombre Insumo</th><th>Cantidad</th></tr></thead>';
				        strInv += '<tbody>';
				        $("#fechaactualizacion").val(inventarios[0].fechainsercion);
						for (var i = 0; i < inventarios.length; i++)
						{
							inventario = inventarios[i];
							strInv +='<tr> ';
							strInv +='<td> ';
							strInv +='<label>' + inventario.idinsumo + '</label> </td>';
							strInv +='<td> ';
							strInv += '<label>' + inventario.nombreinsumo + '</label> </td>';
							strInv +='<td> ';
							strInv +='<label>' + inventario.cantidad + '</label> </td>';
							strInv +='</tr> ';
						}
						strInv +='</tbody> ';
						$('#inventarioActual').html(strInv);
						
				} 
			});
	 
	
}
