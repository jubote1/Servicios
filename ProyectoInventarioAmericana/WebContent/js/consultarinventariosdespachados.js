	

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
function consultarInventarioDespachado() 
{

	var fechasurtir = $("#fechasurtir").val();
	var tienda = $("#selectTiendas").val();
	var idtienda = $("#selectTiendas option:selected").attr('id');
	
	if(fechasurtir == '' || fechasurtir == null)
	{
		alert ('La fecha de Surtir debe ser diferente a vacía');
		return;
	}

	
	if(existeFecha(fechasurtir))
	{
	}
	else
	{
		alert ('La fecha a surtir no es correcta');
		return;
	}

	
	if (tienda == '' || tienda == null)
	{

		alert ('La tienda no puede estar vacía');
		return;
	}
	// Si pasa a este punto es porque paso las validaciones
	
	$.ajax({ 
	    		url: server + 'ConsultarInventariosDespachados?fechasurtir=' + fechasurtir + "&idtienda=" + idtienda, 
	    		dataType: 'json', 
	    		async: false, 
	    		success: function(data1){ 
	    				
	    				inventarios = data1;
	    				bandera = true;
	    				var inventario;
						
						var strInv='';
						
						strInv += '<table id="inventariosurtir" class="table table-bordered table-striped">';
						strInv += '<thead><tr><th COLSPAN="3"><img src="LogoPizzaAmericanapeque.png" class="img-circle" /></th>';
						strInv += '<th COLSPAN="3"> <h3>'+ "PRODUCTOS A LLEVAR TIENDA " + tienda + " " + fechasurtir +'</h3></th></tr>'
						strInv += '<tr><th>Nom/Insumo</th><th>Cantidad a Surtir</th><th>Empaque</th><th>Nom/Insumo</th><th>Cantidad a Surtir</th><th>Empaque</th></tr></thead>';
				        strInv += '<tbody>';
				        var contador = 1;
						for (var i = 0; i < inventarios.length; i++)
						{
							inventario = inventarios[i];
							if (contador == 1)
							{
								strInv +='<tr> ';
								strInv +='<td> <font size=1>';
								strInv +='<label>' + inventario.nombreinsumo + '</label> </font> </td>';
								strInv +='<td> <font size=1>';
								strInv += '<label>' + inventario.cantidadsurtir + '</label> </font> </td>';
								strInv +='<td> <font size=1>';
								strInv +='<label>' + inventario.contenedor + '</label> </font> </td>';
							}else
							if (contador == 2)
							{
								strInv +='<td> <font size=1> ';
								strInv +='<label>' + inventario.nombreinsumo + '</label> </font> </td>';
								strInv +='<td> <font size=1>';
								strInv += '<label>' + inventario.cantidadsurtir + '</label> </font> </td>';
								strInv +='<td> <font size=1>';
								strInv +='<label>' + inventario.contenedor + '</label> </font> </td>';
								strInv +='</tr> ';
							}
							contador++;
							if(contador == 3)
							{
								contador = 1;
							}
						}
						strInv +='</tbody> ';
						$('#inventario').html(strInv);
					
				} 
			});
	 
	
}

function generarExcel()
{
	$("#inventariosurtir").table2excel({
	    filename: "InventarioSurtir"
	  });

}

function reiniciarInventario()
{
	$('#fechasurtir').attr('disabled', false);
	$('#selectTiendas').attr('disabled', false);
	bandera = false;
	var str = '';
	$('#inventario').html(str);
}

// Método creado para confirmar que una fecha exista
function existeFecha(fecha){
      var fechaf = fecha.split("/");
      var day = fechaf[0];
      var month = fechaf[1];
      var year = fechaf[2];
      var date = new Date(year,month,'0');
      if((day-0)>(date.getDate()-0)){
            return false;
      }
      return true;
}


function validarFechaMenorActual(date1, date2){
      var fechaini = new Date();
      var fechafin = new Date();
      var fecha1 = date1.split("/");
      var fecha2 = date2.split("/");
      fechaini.setFullYear(fecha1[2],fecha1[1]-1,fecha1[0]);
      fechafin.setFullYear(fecha2[2],fecha2[1]-1,fecha2[0]);
      
      if (fechaini > fechafin)
        return false;
      else
        return true;
}

