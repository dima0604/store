
let originalTableHTML;
window.onload = function () {
    originalTableHTML = document.getElementById("tableOrder").innerHTML;
};

$(document).ready(function () {
    $('#orderModal').on('hidden.bs.modal', function () {
        document.getElementById('tableOrder').innerHTML = originalTableHTML;
    });
});

function showOrderModal(id, order) {
    document.getElementById('orderModalLabel').textContent = "Собрать отгрузку №" + id + "\tТТН: " + order.transport_no;

    const table = document.getElementById("tableOrder");

    for (let i = 0; i < order.items.length; i++) {

        let newRow = table.insertRow();
        newRow.insertCell(0);
        newRow.insertCell(1);
        newRow.insertCell(2);
        newRow.insertCell(3);


        let checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.className = "form-check-input mt-0";

        checkbox.onclick = function (event) {
            event.stopPropagation();
        }

        newRow.cells[0].appendChild(checkbox);
        newRow.cells[1].textContent = order.items[i].item_code;
        newRow.cells[2].textContent = order.items[i].item_description;
        newRow.cells[3].textContent = order.items[i].quantity;

        newRow.addEventListener("click", function () {
            var checkbox = newRow.cells[0].querySelector('input[type="checkbox"]');
            checkbox.checked = !checkbox.checked;
        });
    }
    let printButton = document.getElementById("downloadButton");
    printButton.value = order.transport_no;
    let acceptButton = document.getElementById("acceptButton");
    acceptButton.value = order.shipment_no;
    $('#orderModal').modal('show');
}

function redirectToAnotherPage(url) {
        window.location.href = url;
}

$('#acceptButton').click(function () {
    const shipment_no = $('#acceptButton').val();
    $.ajax({
        url: "/accept",
        type: "POST",
        data: "shipment_no=" + shipment_no,
        xhrFields: {
            responseType: 'blob'
        },
        success: function (data, status, xhr) {
            if (xhr.status == 200) {
                $('#newOrders').get(0).textContent = parseInt($('#newOrders').get(0).textContent,10)-1;
                $('#closedOrders').get(0).textContent = parseInt($('#closedOrders').get(0).textContent,10)+1;
                $('#'+shipment_no).remove();
                $('#orderModal').modal('hide');
            }
        },
        error: function (xhr, status, error) {
        }
    });
});
$('#downloadButton').click(function () {
    const transport_no = $(this).val();
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

                setTimeout(function() {
                    iframe.contentWindow.print();
                }, 1000);
            }
        },
        error: function (xhr, status, error) {
        }
    });
});