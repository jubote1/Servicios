var server = 'http://localhost:8080/ProyectoPizzaAmericana/';

$(document).ready(function() {

		table = $('#grid-especialidades').DataTable( {
    		"aoColumns": [
            { "mData": "idespecialidad" },
            { "mData": "abreviatura" },
            { "mData": "nombre" }
        ]
    	} );

		$.getJSON(server + 'GetEspecialidades' , function(data1){
			table.clear().draw();
			for(var i = 0; i < data1.length;i++){
				var cadaEspecialidad  = data1[i];
				table.row.add(data1[i]).draw();
			}
		});

});

function guardarEspecialidad()
{
	var nombre = $('#nombre').val();
	var abreviatura = $('#abreviatura').val();
	var idespecialidad = 
	$.getJSON(server + 'InsertarEspecialidad?nombre=' + nombre + "&abreviatura=" + abreviatura, function(data){
		var respuesta = data[0];
		idespecialidad = respuesta.idespecialidad;
				
	});
}