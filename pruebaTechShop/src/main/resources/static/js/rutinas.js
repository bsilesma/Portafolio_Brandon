/**
 * Lec12: agrega un producto al carrito asumiendo cantidad = 1.
 * Envía la petición por AJAX y pinta el fragmento devuelto en #resultBlock.
 * @param {HTMLFormElement} formulario - El form que contiene el ID del producto.
 */
function addCart(formulario) {
    // 1. Obtención de datos y ruta (solo el ID del producto)
    var idProducto = $(formulario).find('input[name="idProducto"]').val();
    var ruta = $(formulario).attr('action') || '/carrito/agregar';

    // 2. Seguridad (CSRF Token)
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    // 3. Petición AJAX (solo se envía el ID del producto)
    $.ajax({
        url: ruta,
        type: 'POST',
        data: {
            idProducto: idProducto
        },
        beforeSend: function (xhr) {
            if (csrfHeader && csrfToken) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        },
        success: function (response) {
            // Actualiza el fragmento HTML del carrito
            $("#resultBlock").html(response);
        },
        error: function (xhr) {
            var mensaje = xhr.responseText || 'Error en la conexión.';
            alert("Error al agregar producto: " + mensaje);
        }
    });
}

// funcion para hacer un preview de una imagen
function mostrarImagen(input) {
    if (input.files && input.files[0]) {
        const imagen = input.files[0];
        const maximo = 512 * 1024; // Se limita el tamaño a 512 Kb las imágenes.
        if (imagen.size <= maximo) {
            var lector = new FileReader();
            lector.onload = function (e) {
                $('#blah').attr('src', e.target.result).height(200);
            };
            lector.readAsDataURL(input.files[0]);
        } else {
            alert("La imagen seleccionada es muy grande... no debe superar los 512 Kb!");
        }
    }
}

// Para insertar información en el modal de confirmación según el registro.
// Este archivo se carga en todas las páginas, así que se valida que el modal
// exista antes de registrar el evento.
document.addEventListener('DOMContentLoaded', function () {
    const confirmModal = document.getElementById('confirmModal');
    if (!confirmModal) {
        return;
    }
    confirmModal.addEventListener('show.bs.modal', function (event) {
        const button = event.relatedTarget;
        document.getElementById('modalId').value = button.getAttribute('data-bs-id');
        document.getElementById('modalDescripcion').textContent = button.getAttribute('data-bs-descripcion');
    });
});

// Para quitar el toast después de unos segundos
setTimeout(() => {
    document.querySelectorAll('.toast').forEach(t => t.classList.remove('show'));
}, 4000);
