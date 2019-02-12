	

var server;
var tiendas;
var urlTienda ="";
var inventariosReq;
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
	getDiasSemana();
	
});



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

function getDiasSemana()
{
	var str = "";
	str +="<option value='Domingo' id ='1'>" + "Domingo" +"</option>";
	str +="<option value='Lunes' id ='2'>" + "Lunes" +"</option>";
	str +="<option value='Martes' id ='3'>" + "Martes" +"</option>";
	str +="<option value='Miercoles' id ='4'>" + "Miercoles" +"</option>";
	str +="<option value='Jueves' id ='5'>" + "Jueves" +"</option>";
	str +="<option value='Viernes' id ='6'>" + "Viernes" +"</option>";
	str +="<option value='Sabado' id ='7'>" + "Sabado" +"</option>";
	$('#selectDiaSemana').html(str);
}

//Método principal invocado para validar los datos de los parámetros y adicionalmente ejecutar los servicios para 
//recuperar la informacion y pintarla en pantalla.
function traerInventarioRequerido() 
{

	var diaSemana = $("#selectDiaSemana option:selected").attr('id');
	var strDiaSemana = $("#selectDiaSemana").val();
	var tienda = $("#selectTiendas").val();
	var idtienda = $("#selectTiendas option:selected").attr('id');
	
	if(diaSemana == '' || diaSemana == null)
	{
		alert ('Debe seleccionar un día en semana para traer la información');
		return;
	}

	if (tienda == '' || tienda == null)
	{

		alert ('La tienda no puede estar vacía');
		return;
	}
	// Si pasa a este punto es porque paso las validaciones
	
	//Llamamos servicio que se va a encargar de traer los inventarios requeridos para modificarlos si es el caso
	$.ajax({ 
	    		url: server + 'ConsultarInventarioRequerido?diasemana=' + diaSemana + "&idtienda=" + idtienda, 
	    		dataType: 'json', 
	    		async: false, 
	    		success: function(data1){ 
	    				console.log(data1);
	    				inventariosReq = data1;
	    				bandera = true;
	    				var inventario;
						
						var strInv='';
						
						strInv += '<table id="inventariorequerido" class="table table-bordered">';
						strInv += '<thead><tr><th COLSPAN="2"><img src="LogoPizzaAmericanapeque.png" class="img-circle" /></th>';
						strInv += '<th COLSPAN="4"> <h2>'+ "INSUMOS REQUERIDOS PARA LA TIENDA  " + tienda + " " + strDiaSemana +'</h2></th></tr>'
						strInv += '<tr><th>Nom/Insumo</th><th>Cant/Req</th><th>Cant/Minima</th><th>Cantidad x Canasta</th><th>Nombre Contenedor</th></tr></thead>';
				        strInv += '<tbody>';
						for (var i = 0; i < inventariosReq.length; i++)
						{
							invenReq = inventariosReq[i];
							strInv +='<tr> ';
							strInv +='<td> ';
							strInv +='<label>' + invenReq.nombreinsumo + '</label> </td>';
							strInv +='<td> ';
							strInv +='<input type="text" name="' + "cant" + invenReq.idinsumo + '"" value="' + invenReq.cantidad +'" id="'+ "cant" + invenReq.idinsumo +'" maxlength="10" size="10"> </td>';
							strInv +='<td> ';
							if(invenReq.cantidadminima > 0)
							{
								strInv +='<input type="text" name="' + "cantmin" + invenReq.idinsumo + '"" value="' + invenReq.cantidadminima +'" id="'+ "cantmin" + invenReq.idinsumo +'" maxlength="10" size="10"> </td>';
							}
							else
							{
								strInv +='<input type="text" name="' + "cantmin" + invenReq.idinsumo + '"" value="' + '' +'" id="'+ "cantmin" + invenReq.idinsumo +'" maxlength="10" size="10"> </td>';
							}
							strInv +='<td> ';
							strInv +='<td><label>' + invenReq.cantidadxcanasta + '</label> </td>';
							if(invenReq.nombrecontenedor == null || invenReq.nombrecontenedor == '')
							{
								strInv +='<td><label>' + '' + '</label> </td>';
							}else
							{
								strInv +='<td><label>' + invenReq.nombrecontenedor + '</label> </td>';
							}
							strInv +='</tr> ';
						}
						strInv +='<tr><td COLSPAN="2"><input type="button" class="btn btn-primary btn-sd" value="Confirmar Inventario Requerido" onclick="confirmarInventarioRequerido()"> </td>';
						strInv +='<td COLSPAN="2"> <input type="button" class="btn btn-danger btn-sd" value="Reiniciar Inventario Requerido" onclick="reiniciarInventario()"> </td><td COLSPAN="2"></td></tr>';
						strInv +='</tbody> ';
						$('#forminventarioRequerido').html(strInv);
					
				} 
			});
	 $('#selectDiaSemana').attr('disabled', true);
	 $('#selectTiendas').attr('disabled', true);
	
}


//Opción que implementará la lógica para la confirmación de un inventario y de guardar la información despachada para la tienda
function confirmarInventarioRequerido()
{
	if(!bandera)
	{
		alert("No se han cargado inventarios para confirmar");
		return;
	}
	
	// Ser realiza la generación del excel con la información de lo que se surtira en la tienda -- comentamos para revisar  LA GENERACION
	//$("#inventariosurtir").table2excel({
	//    filename: "InventarioSurtir"
	//  }); 
	
	//Ingresamos primero el encabezado del despacho del pedido
	var diaSemana = $("#selectDiaSemana option:selected").attr('id');
	var strDiaSemana = $("#selectDiaSemana").val();
	var idtienda = $("#selectTiendas option:selected").attr('id');
	var tienda = $("#selectTiendas").val();
	$.confirm({
				'title'		: 'Confirmacion para Guardar inventario Requerido',
				'content'	: 'Desea el confirmar los datos de inventario requerido para la tienda ' + tienda + '<br> Dia de la semana '+ strDiaSemana,
				'type': 'dark',
   				'typeAnimated': true,
				'buttons'	: {
					'Si'	: {
						'class'	: 'blue',
						'action': function(){
								for (var i = 0; i < inventariosReq.length; i++)
								{
									var cadaInventarioReq = inventariosReq[i];
									var cantidad = $("#cant"+cadaInventarioReq.idinsumo).val();
									var cantidadMin = $("#cantmin"+cadaInventarioReq.idinsumo).val();
									if((cantidad == null) || (cantidad == '') || (cantidad == 'null'))
									{
										cantidad = 0
									}
									if((cantidadMin == null) || (cantidadMin == '') || (cantidadMin == 'null'))
									{
										cantidadMin = 0
									}
									var idinsumo = cadaInventarioReq.idinsumo;
										$.ajax({ 
									    		url: server + 'InsertarActualizarInsumReqTienda?idtienda=' + idtienda + "&idinsumo=" + idinsumo + "&cantidad=" + cantidad + "&cantidadminima=" + cantidadMin + "&diasemana=" + diaSemana, 
									    		dataType: 'json', 
									    		async: false, 
									    		success: function(data2){
										   			var resultado = data2[0];
										   			if(resultado.respuesta == 'exitoso')
										   			{
										   				
										   			}
										   		}
										});
								}
								$.alert('SE ACTUALIZÓ/INGRESÓ CORRECTAMENTE LA INFORMACIÓN');
						}
					},
					'No'	: {
						'class'	: 'gray',
						'action': function(){}	// Nothing to do in this case. You can as well omit the action property.
					}
				}
			});
	
}

function reiniciarInventario()
{
	$('#selectDiaSemana').attr('disabled', false);
	$('#selectTiendas').attr('disabled', false);
	bandera = false;
	inventarios = "";
	var str = '';
	$('#forminventarioRequerido').html(str);
}


