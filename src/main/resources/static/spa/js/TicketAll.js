import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Tickets");
    }

    async getHtml() {
        this.setAppProgress(20);
        let tickets = await getRemoteTicketsAll(this.params.token);
        let returnHTML = `<div class="list__group">
                        <div class="list__group-item-grow title__module">Tickets</div>
                        <div class="list__group-item-right">
                        <button class="btn btn-sm btn-generic" data-nav-my-tickets>View Mine</button></div></div>`;

        this.setAppProgress(60);
        returnHTML += `<div class="list__group">
                        <div class="list__group-item-grow"><h3>Open Tickets</h3></div>
                        <div class="list__group-item-grow">
                        <select class="form-control" id="allTicketsSorting" style="float: right"
                                onchange="document.getElementById('allTicketsSortingBtn').click()">
                            <option value="ticketDue,ASC">Date Due | ASC</option>
                            <option value="ticketDue,DESC">Date Due | DESC</option>
                        </select>
                        <button type="button" class="btn btn-generic btn-sm" style="display: none"
                         id="allTicketsSortingBtn" data-nav-all-tickets-sort></button>
                        </div></div>
                        <div class="list__group-short">
                            <div id="allTicketSLALabel" class="list__group-item-grow">All SLAs</div>
                            <input type="hidden" id="allTicketSLAInput" value="0">
                            <div class="list__group-item-right">
                            <button type="button" class="btn btn-generic btn-sm"
                             id="allTicketsSLABtn" data-nav-all-tickets-sla>SLA</button>
                        </div></div>`;
        returnHTML += `<div class="item__hr"><hr></div>`;
        returnHTML += `<div id="ticketList">`;

        if (tickets.length > 0) {
            for (let n in tickets) {
                let mt = tickets[n];
                returnHTML += htmlTicketLine(mt);
            }
        }
        returnHTML += `</div>`;

        this.setAppProgress(90);
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }

    async getNotification() {
        return null;
    }
}

function setAppProgress(prg) {
    try {
        if (prg < 0) {
            document.getElementById("appProgress").value = 1;
            document.getElementById("appProgress").style.display = "none";
        } else if (prg > 100) {
            document.getElementById("appProgress").value = 100;
            document.getElementById("appProgress").style.display = "none";
        } else if (prg === 0) {
            document.getElementById("appProgress").style.display = "block";
            document.getElementById("appProgress").removeAttribute("value");
        } else {
            document.getElementById("appProgress").style.display = "block";
            document.getElementById("appProgress").value = prg;
        }
    } catch (e) {
        document.getElementById("appProgress").style.display = "none";
    }
}

async function sortAllTickets (srt,ordr,sla,token) {
    const response = await fetch('/api/v1/app/ticket/allopen?sortCol='+srt+'&sortOrder='+ordr+'&sortSLA='+sla, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;

    document.getElementById('ticketList').innerHTML = "";
    let newHTML = "";
    if (tickets.length > 0) {
        for (let n in tickets) {
            let mt = tickets[n];
            newHTML += htmlTicketLine(mt);
        }
    }
    document.getElementById('ticketList').innerHTML = newHTML;
}

async function getRemoteTicketsAll(token) {
    const response = await fetch('/api/v1/app/ticket/allopen', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;
    return tickets;
}

async function getTicketSLAOptions(token) {
    const response = await fetch('/api/v1/app/ticket/slaoptions', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();

}

async function showAllTicketSLADialog(token) {
    let slas = await getTicketSLAOptions(token);
    let returnHTML = htmlDialogSLA(slas);
    let dialogHTML =  document.createElement("div");
    dialogHTML.id = "formSLAChooser";
    dialogHTML.classList.add("form__popup");
    dialogHTML.innerHTML = returnHTML;

    document.body.appendChild(dialogHTML);

    document.getElementById('btnSLASortCancel').addEventListener("click", (event) => {
        event.preventDefault();
        document.getElementById("formSLAChooser").remove();
    });
}

function htmlTicketLine(tkt) {
    let r = "";
    r+=`<div class="list__item" data-link-ticket="`+tkt.id+`">`;
    r+=`<div class="list__Item-line" data-link-ticket="`+tkt.id+`">`;
    r+=`<div class="item-id" data-link-ticket="`+tkt.id+`">`+tkt.id+`</div>`;
    r+=`<div class="item-user" data-link-ticket="`+tkt.id+`">`+tkt.user+`</div>`;
    r+=`<div class="item-date" data-link-ticket="`+tkt.id+`">`+formatDate(tkt.date)+`</div>`;
    r+=`</div><div class="list__Item-line" data-link-ticket="`+tkt.id+`">`;
    r+=`<div class="item-detail" data-link-ticket="`+tkt.id+`">`+tkt.detail+`</div>`;
    r+=`</div></div>`;

    return r;
}

function htmlDialogSLA(slas) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="request__item-detail">`;
    r+=`<h4>Select Service Level Agreement</h4>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<select id="slaSelect" class="form-control">`;
    r+=`<option value="0">All SLAs</option>`;
    for (let s in slas) {
        let opt = slas[s];
        r+=`<option value="`+opt.id+`">`+opt.name+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-support" data-all-tickets-sla>Select</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button id="btnSLASortCancel" type="button" class="btn btn-sm btn-outline-cancel">Cancel</button>`;
    r+=`</div>`;
    r+=`</div>`;

    r+=`</div></form></div>`;
    return r;
}

function formatDate(dte) {
    let strDate = dte.split("T")[0];
    let strTime = dte.split("T")[1];
    let partsDate = strDate.split("-");
    let partTime = strTime.split(":");
    return partsDate[1] + " / " + partsDate[2] + " / " + partsDate[0];
}

export { sortAllTickets, showAllTicketSLADialog }