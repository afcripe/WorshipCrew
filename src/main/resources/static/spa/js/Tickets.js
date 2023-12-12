import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Tickets");
    }

    async getHtml() {
        this.setAppProgress(20);
        let tickets = await getRemoteTickets(this.params.token);
        let ticketsInclude = await getRemoteTicketsIncluded(this.params.token);
        let returnHTML = `<div class="list__group">
                        <div class="list__group-item-grow title__module">Tickets</div>
                        <div class="list__group-item-right">
                        <button class="btn btn-sm btn-generic" data-nav-all-tickets>View All</button></div></div>`;

        this.setAppProgress(50);
        returnHTML += `<div class="list__group">
                        <div class="list__group-item-grow"><h3>My Tickets</h3></div>
                        <div class="list__group-item-grow">
                        <select class="form-control" id="myTicketsSorting" style="float: right"
                                onchange="document.getElementById('myTicketsSortingBtn').click()">
                            <option value="ticketDue,ASC">Date Due | ASC</option>
                            <option value="ticketDue,DESC">Date Due | DESC</option>
                        </select>
                        <button type="button" class="btn btn-generic btn-sm" style="display: none"
                         id="myTicketsSortingBtn" data-nav-my-tickets-sort></button>
                        </div></div>
                        <div class="list__group-short">
                            <div id="myTicketSLALabel" class="list__group-item-grow">All SLAs</div>
                            <input type="hidden" id="myTicketSLAInput" value="0">
                            <div class="list__group-item-right">
                            <button type="button" class="btn btn-generic btn-sm"
                             id="myTicketsSLABtn" data-nav-my-tickets-sla>SLA</button>
                        </div></div>`;
        returnHTML += `<div class="item__hr"><hr></div>`;
        returnHTML += `<div id="ticketList">`;

        this.setAppProgress(60);
        if (tickets.length > 0) {
            returnHTML += '<h3>Tickets Assigned to Me</h3>';
            for (let i in tickets) {
                let ticket = tickets[i];
                returnHTML += htmlTicketLine(ticket);
            }
        }

        this.setAppProgress(80);
        if (ticketsInclude.length > 0) {
            returnHTML += '<h3>Tickets to Monitor</h3>';
            for (let t in ticketsInclude) {
                let tkt = ticketsInclude[t];
                returnHTML += htmlTicketLine(tkt);
            }
        }

        if (tickets.length === 0 && ticketsInclude.length === 0) {
            returnHTML += '<h4>No Tickets Found</h4>';
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

async function sortMyTickets (srt,ordr,sla,token) {
    const response = await fetch('/api/v1/app/ticket/listbyagent?sortCol='+srt+'&sortOrder='+ordr+'&sortSLA='+sla, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;

    const responseIncl = await fetch('/api/v1/app/ticket/listbyincluded?sortCol='+srt+'&sortOrder='+ordr+'&sortSLA='+sla, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const ticketsIncl = await responseIncl.json();
    const statusIncl = responseIncl.status;

    document.getElementById('ticketList').innerHTML = "";
    let newHTML = "";
    if (tickets.length > 0) {
        newHTML += '<h3>Tickets Assigned to Me</h3>';
        for (let n in tickets) {
            let mt = tickets[n];
            newHTML += htmlTicketLine(mt);
        }
    }
    if (ticketsIncl.length > 0) {
        newHTML += '<h3>Tickets to Monitor</h3>';
        for (let t in ticketsIncl) {
            let tkt = ticketsIncl[t];
            newHTML += htmlTicketLine(tkt);
        }
    }
    document.getElementById('ticketList').innerHTML = newHTML;
}

async function getRemoteTickets(token) {
    const response = await fetch('/api/v1/app/ticket/listbyagent', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;
    return tickets;
}

async function getRemoteTicketsIncluded(token) {
    const response = await fetch('/api/v1/app/ticket/listbyincluded', {
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

async function showMyTicketSLADialog(token) {
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
    r+=`<div class="appList__item-detail" data-link-ticket="`+tkt.id+`">`+tkt.detail+`</div>`;
    r+=`<div class="appList__item-right appList__item-outline" data-link-ticket="`+tkt.id+`" style="white-space: nowrap">`+tkt.name+`</div>`;
    r+=`</div></div>`;

    return r;
}

function htmlDialogSLA(slas) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="list__Item-line">
            <div class="request__item-field-grow">&nbsp;</div>
            <div class="request__item-field-right">
                <button id="btnSLASortSelect" type="button" class="btn btn-sm btn-support" style="display: none" data-my-tickets-sla>Select</button>
                <button id="btnSLASortCancel" type="button" class="btn btn-sm btn-outline-cancel">Cancel</button>
            </div>
        </div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<select id="slaSelect" class="form-control" onchange="document.getElementById('btnSLASortSelect').click()">`;
    r+=`<option value="0">Select Service Level Agreement</option>`;
    r+=`<option value="0">All SLAs</option>`;
    for (let s in slas) {
        let opt = slas[s];
        r+=`<option value="`+opt.id+`">`+opt.name+`</option>`;
    }
    r+=`</select>`;
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

export { sortMyTickets, showMyTicketSLADialog }