import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Tickets");
    }

    async getHtml() {
        this.setAppProgress(20);
        let myTickets = await getRemoteTicketsByUser(this.params.token);
        let tickets = await getRemoteTickets(this.params.token);
        let ticketsInclude = await getRemoteTicketsIncluded(this.params.token);
        let returnHTML = `<h1>Tickets</h1>`;

        this.setAppProgress(50);
        if (myTickets.length > 0) {
            returnHTML += '<h3>My Tickets</h3>';
            for (let n in myTickets) {
                let mt = myTickets[n];
                returnHTML += htmlTicketLine(mt);
            }
        }

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

async function getRemoteTicketsByUser(token) {
    const response = await fetch('/api/v1/app/ticket/listbyuser', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;
    return tickets;
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

function formatDate(dte) {
    let strDate = dte.split("T")[0];
    let strTime = dte.split("T")[1];
    let partsDate = strDate.split("-");
    let partTime = strTime.split(":");
    return strDate + " " + partTime[0] + ":" + partTime[1];
}