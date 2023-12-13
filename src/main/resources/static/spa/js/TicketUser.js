import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Tickets");
    }

    async getHtml() {
        this.setAppProgress(20);
        let user = await getRemoteUser(this.params.id, this.params.token);
        let ticketsOpen = await getRemoteTicketsOpen(this.params.id, this.params.token);
        let ticketsClosed = await getRemoteTicketsClosed(this.params.id, this.params.token);
        let returnHTML = `<div class="list__group">
                        <div class="list__group-item-grow">Tickets by `+user.fullName+ `</div></div>`;

        this.setAppProgress(60);
        returnHTML += `<div id="ticketList">`;

        if (ticketsOpen.length > 0) {
            returnHTML += `<h3>Open Tickets</h3>`;
            for (let n in ticketsOpen) {
                let mt = ticketsOpen[n];
                returnHTML += htmlTicketLine(mt);
            }
        }
        if (ticketsClosed.length > 0) {
            returnHTML += `<h3>Closed Tickets</h3>`;
            for (let r in ticketsClosed) {
                let ct = ticketsClosed[r];
                returnHTML += htmlTicketLine(ct);
            }
        }
        if (ticketsOpen.length === 0 && ticketsClosed.length === 0) {
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

async function getRemoteUser(id, token) {
    const response = await fetch('/api/v1/app/users/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const user = await response.json();
    const status = response.status;
    return user;
}

async function getRemoteTicketsOpen(id, token) {
    const response = await fetch('/api/v1/app/ticket/listbyuser/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;
    return tickets;
}

async function getRemoteTicketsClosed(id, token) {
    const response = await fetch('/api/v1/app/ticket/listbyuser/'+id+"?status=closed", {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;
    return tickets;
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

function formatDate(dte) {
    let strDate = dte.split("T")[0];
    let strTime = dte.split("T")[1];
    let partsDate = strDate.split("-");
    let partTime = strTime.split(":");
    return partsDate[1] + " / " + partsDate[2] + " / " + partsDate[0];
}