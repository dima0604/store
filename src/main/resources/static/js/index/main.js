let originalTableHTML;
window.onload = function () {
    originalTableHTML = document.getElementById("tableOrder").innerHTML;
};

$(document).ready(function () {
    $('#orderModal').on('hidden.bs.modal', function () {
        document.getElementById('tableOrder').innerHTML = originalTableHTML;
        $('#printAlert').hide();
        $('#confirmAlert').hide();
    });
    setInterval(updatePage, 600000);
    
    function updatePage(){
    	if (!$("#orderModal").hasClass("show")){
    		window.location.href = "/";
    	}
    }
});

function showOrderModal(id, order) {
    // document.getElementById('orderModalLabel').textContent = "Собрать отгрузку №" + id + "\tТТН: " + order.transport_no;
    document.getElementById('orderModalLabel').innerHTML = "Собрать отгрузку №" + id + "; &nbsp;&nbsp;&nbsp;&nbsp;ТТН: "
        + "<span style='font-weight: normal;'>" + order.transport_no.substring(0, order.transport_no.length - 4)+ "</span>"
        + "<span style='font-weight: bold;'>" + order.transport_no.substring(order.transport_no.length - 4, order.transport_no.length) + "</span>";
    const table = document.getElementById("tableOrder");
    const ss = "ddd";
    ss.length
    for (let i = 0; i < order.items.length; i++) {

        let newRow = table.insertRow();
        newRow.insertCell(0);
        newRow.insertCell(1);
        newRow.insertCell(2);
        newRow.insertCell(3);
        newRow.insertCell(4);

        let checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.className = "form-check-input mt-0";

        checkbox.onclick = function (event) {
            event.stopPropagation();
        }

        newRow.cells[0].appendChild(checkbox);
        newRow.cells[1].textContent = order.items[i].item_code;
        newRow.cells[2].textContent = order.items[i].item_description;
        newRow.cells[3].textContent = order.items[i].item_full_description;
        newRow.cells[4].textContent = order.items[i].quantity;

        newRow.addEventListener("click", function () {
            var checkbox = newRow.cells[0].querySelector('input[type="checkbox"]');
            checkbox.checked = !checkbox.checked;
        });
    }
    let printButton = document.getElementById("downloadButton");
    printButton.value = order.transport_no;
    let confirmButton = document.getElementById("confirmButton");
    confirmButton.value = order.shipment_no;
    $('#orderModal').modal('show');
}

function redirectToAnotherPage(url) {
    window.location.href = url;
}

function hideToast() {

}


$('#confirmButton').click(function () {
    checkSession(function (isSessionValid) {
        if (isSessionValid) {
            const shipment_no = $('#confirmButton').val();
            $.ajax({
                url: "/confirm",
                type: "POST",
                data: "shipment_no=" + shipment_no,
                xhrFields: {
                    responseType: 'blob'
                },
                success: function (data, status, xhr) {
                    console.log(xhr.status)
                    if (xhr.status == 200) {
                        $('#newOrders').get(0).textContent = parseInt($('#newOrders').get(0).textContent, 10) - 1;
                        $('#closedOrders').get(0).textContent = parseInt($('#closedOrders').get(0).textContent, 10) + 1;
                        $('#' + shipment_no).remove();
                        $('#orderModal').modal('hide');
                    }
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 400) {
                        $('#confirmAlert').show();
                    }
                }
            });
        } else {
            window.location.href = '/login';
        }
    });

});
$('#downloadButton').click(function () {
    const transport_no = $(this).val();
    checkSession(function (isSessionValid) {
        if (isSessionValid) {
            $.ajax({
                url: "/download",
                type: "POST",
                data: "transport_no=" + transport_no,
                xhrFields: {
                    responseType: 'blob'
                },
                success: function (data, status, xhr) {
                    if (xhr.status == 200) {
                        let iframe = document.createElement('iframe');
                        iframe.style.display = 'none';
                        document.body.appendChild(iframe);

                        iframe.src = window.URL.createObjectURL(data);

                        setTimeout(function () {
                            iframe.contentWindow.print();
                        }, 1000);
                    }
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 404) {
                        $('#printAlert').show();
                    }
                }
            });
        } else {
            window.location.href = '/login';
        }
    });
});

$('#hideToastButton').click(function () {
    $('#printAlert').hide();
});
$('#hideConfirmButton').click(function () {
    $('#confirmAlert').hide();
});

function checkSession(callback) {
    $.ajax({
        url: '/check-session',
        type: 'GET',
        success: function (response) {
            if (response === 'OK') {
                callback(true);
            } else {
                console.log('Session expired');
                callback(false);
            }
        },
        error: function () {
            console.log('Error checking session');
            callback(false);
        }
    });
}
